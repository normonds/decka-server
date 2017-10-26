package decka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import nooni.Constants.DataTarget;
import nooni.ReadXMLFile;
import nooni.Util;
import nooni.events.Eve;
import nooni.events.EveListener;
import nooni.events.EventDispatcher;
import nooni.sockets.Client;
import nooni.sockets.ClientEve;
import nooni.sockets.JSONserverEvent;

public class Decka {
public static String gameUrl = "https://decka-server.herokuapp/";
//public static String gameUrl = "http://goodshop.lv/_decka/";
public static Boolean isLocalServer = true;
public static DeckaRoom lobby;
public static ReadXMLFile xmlFile;
public static Random random = new Random();
public static EventDispatcher dispatcher = new EventDispatcher();

public static boolean useBot = true;
public static long startTime;
public static int frameDelay = 33;
public static Timer timer;
public static TimerTask timerTask;

class EnterFrameTask extends TimerTask {
	public long milis = System.currentTimeMillis();
	public void run () {	
		try {
			enterFrame();
		} catch (Exception e) {}
	}
}
public Decka () {
	System.out.println("Decka constructor");
	DeckaRoom.plusMinus.add("+");
	DeckaRoom.plusMinus.add("-");
}
public void init () {
	timer = new Timer();
	startTime = System.currentTimeMillis();
	timerTask = new EnterFrameTask();
	timer.scheduleAtFixedRate(timerTask, 0, frameDelay);
	
	lobby = new DeckaRoom("lobby");
	xmlFile = new ReadXMLFile("serverDeckaData.xml");
	Cards.cards = xmlFile.getCardsData("cards", DataTarget.CLIENTSIDE);
	System.out.println(Cards.printAll());
	Client.dispatcher.addEventListener(ClientEve.CONNECT, clientConnect);
	Client.dispatcher.addEventListener(ClientEve.DISCONNECT, clientDisconn);
	Client.dispatcher.addEventListener(ClientEve.RECONNECT, clientReconn);
	Client.dispatcher.addEventListener(ClientEve.REMOVE, clientRemove);
	Client.dispatcher.addEventListener(ClientEve.LOGOUT, clientLogOut);
	Client.dispatcher.addEventListener(ClientEve.MESSAGE, receiveMessage);
	
	dispatcher.addEventListener(DeckaEve.CHAT_MSG, chatMsg);
	//dispatcher.addEventListener(DeckaEve.REQUEST_AUTH_INIT, reqAuthInit);
	
	dispatcher.addEventListener(DeckaEve.REQUEST_CONCEDE, reqConcede);
	dispatcher.addEventListener(DeckaEve.REQUEST_CLEAR_DECKLIST, reqClearDecklist);
	dispatcher.addEventListener(DeckaEve.REQUEST_CHANGE_NICK, reqChangeNick);
	dispatcher.addEventListener(DeckaEve.REQUEST_CANCEL_QUEUE, reqCancelQueue);
	dispatcher.addEventListener(DeckaEve.REQUEST_QUEUE, queueGame);
	dispatcher.addEventListener(DeckaEve.REQUEST_CHOICE_CARDS, requestChoiceCards);
	dispatcher.addEventListener(DeckaEve.REQUEST_PICK_CARD, requestPickCard);
	dispatcher.addEventListener(DeckaEve.REQUEST_CARD, drawCard);
	dispatcher.addEventListener(DeckaEve.REQUEST_END_TURN, reqEndTurn);
	dispatcher.addEventListener(DeckaEve.REQUEST_CONFIRM_MINION, reqConfirmMinion);
	dispatcher.addEventListener(DeckaEve.REQUEST_ATTACK, reqAttack);
	dispatcher.addEventListener(DeckaEve.REQUEST_SPELL_CAST, reqSpellCast);
	dispatcher.addEventListener(DeckaEve.REQUEST_DECKLIST, reqDecklist);
	dispatcher.addEventListener(DeckaEve.REQUEST_REFRESH_STAGE, reqStageRefresh);
	dispatcher.addEventListener(DeckaEve.REQUEST_MULLIGAN_CONFIRM, reqMulliganConfirm);
}
public void enterFrame () {
	DeckaRoom.enterStaticFrame();
}
public static EveListener reqConcede = new EveListener () { 		@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(clientEve.client);
	
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	System.out.println("room:"+room);
	System.out.println(room.otherPlayer(player));
	room.endGame(
			room.otherPlayer(player));
	
}};
public static EveListener reqAuthInit = new EveListener () { 		@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	DeckaPlayer player = new DeckaPlayer(clientEve.client);

	//String ret = Util.sendGet(gameUrl+"facebook.authorize.php");
	//System.out.println(ret);
	//DeckaRoom.movePlayer(player, lobby);
	//System.out.println("clientConnect:"+clientEve.client);
	//lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" joins");
}};
public static EveListener reqChangeNick = new EveListener () { 		@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	//DeckaPlayer player = new DeckaPlayer(clientEve.client);
	//ArrayList<Object> datas = (ArrayList<Object>) clientEve.json.data;
	if (clientEve.json.data.getClass().getName().equals("java.util.ArrayList")) {
		@SuppressWarnings("unchecked")
		ArrayList<Object> datas = (ArrayList<Object>) clientEve.json.data;
		System.out.println(datas.get(1));
		//player.client.token = (String) datas.get(0);
		String ret = Util.urlGET(Decka.gameUrl+"facebook.authorize.php?token="+datas.get(0)+"&nick="+datas.get(1));
		System.out.println(ret);
		if (ret.substring(0, 5).equals("nick:")) {
			clientEve.client.sendJSON(JSONserverEvent.NICK, ret.substring(5));
		}
	} else {
		clientEve.client.sendJSON(JSONserverEvent.ERROR, "NETWRAP_CONNECT received data must be array ("+
				clientEve.json.data.getClass().getName()+")");
	}
	
	//DeckaRoom.movePlayer(player, lobby);
	//System.out.println("clientConnect:"+clientEve.client);
	//lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" joins");
}};//ju
public static EveListener clientConnect = new EveListener () { 			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	DeckaPlayer player = new DeckaPlayer(clientEve.client);
	DeckaRoom.movePlayer(player, lobby);
	System.out.println("clientConnect:"+clientEve.client+", token:"+clientEve.client.token);
	lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" joins");
	player.client.sendJSON(DeckaEve.DECKLIST, Cards.cards);
	if (player.client.token != null) {
		String curDeck = Util.urlGET(Decka.gameUrl+"facebook.authorize.php?getdeck=1&token="+player.client.token);
		if (curDeck.length()>0) {
			player.deserializeDeck(curDeck);
			for (Card card : player.deck) {
				player.client.sendJSON(DeckaEve.PICK_CARD, card.stats());
			}
		}
	}
	processChoiceCardReq(player, null);
}};
public static EveListener clientReconn = new EveListener () { 			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(clientEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (!room.name.equals("lobby")) {
		System.out.println("room client reconnect");
		room.broadcastJSON(DeckaEve.PLAYER_RECONNECT, clientEve.client.nick);
	} //else lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" reconnects");
	lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" reconnects");
}};
public static EveListener clientRemove = new EveListener () { 			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	//lobby.remove(DeckaPlayer.getPlayerFromClient(clientEve.client));
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(clientEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	DeckaPlayer.removePlayer(clientEve.client);
	if (room!=null && !room.name.equals("lobby")) {
		System.out.println("room client reconnect");
		room.endGameOnRemove(player);
	} //else lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" reconnects");
	lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" is removed");
}};
public static EveListener clientLogOut = new EveListener () { 			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	//System.out.println(clientEve);
}};
public static EveListener clientDisconn = new EveListener () { 			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(clientEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (room!=null && !room.name.equals("lobby")) {
		room.broadcastJSON(DeckaEve.PLAYER_DISCONNECT, clientEve.client.nick);
	}
	lobby.broadcast("chat.msg", ":status", clientEve.client.nick+" is disconnected");
}};
public static EveListener receiveMessage = new EveListener () {			@Override public void onEvent (Eve e) {ClientEve clientEve=(ClientEve) e;
	try {
		dispatcher.dispatchEvent(new DeckaEve(clientEve.json.event, clientEve.client, clientEve.json));
	} catch (Error err) {
		System.out.println("Decka dispatcher error");
	}
	/*
		if (clientEve.json.data==null) {System.out.println("json.data is null");return;}
		lobby.broadcast("chat.msg", clientEve.client.nick, clientEve.json.data);
	}*/
}};

/*
 * DECKA EVENTS 
 */
public static EveListener reqClearDecklist = new EveListener () { 	@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	System.out.println("reqClearDecklist");
	player.deck.clear();
	player.client.sendJSON(DeckaEve.DRAW_CHOICE_CARDS, Cards.choiceCards(player));
	//processChoiceCardReq(player, null);
	//DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	//player.client.sendJSON(DeckaEve.DECKLIST, Cards.cards);
}};
public static EveListener reqDecklist = new EveListener () { 			@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	//DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	player.client.sendJSON(DeckaEve.DECKLIST, Cards.cards);
}};
public static EveListener chatMsg = new EveListener () { 				@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	room.broadcast(DeckaEve.CHAT_MSG, player.client.nick, deckaEve.json.data);
}};
public static EveListener queueGame = new EveListener () { 			@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom.queuePlayer(player);
}};
public static EveListener reqCancelQueue = new EveListener () { 	@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	//DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	//Object obj = room.refreshPlayer(player);
	DeckaRoom.cancelQueue(player);
	//System.out.println(obj);
	//player.client.sendJSON(DeckaEve.REQUEST_REFRESH_STAGE, obj);
}};
public static EveListener requestChoiceCards = new EveListener () { 	@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	processChoiceCardReq(player, null);
}};
public static EveListener requestPickCard = new EveListener () { 		@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	//Card crd = Cards.randomCard(player);
	
	processChoiceCardReq(player, deckaEve.json.data);
	//System.out.println(crd.serialize());
	//player.client.sendJSON(DeckaEve.DRAW_CHOICE_CARDS, Cards.choiceCards(player));
}};
public static EveListener reqMulliganConfirm = new EveListener () { 	@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (notInMatch(player, room, "You're not in match")) return;
	player.mulliganConfirm(deckaEve.json.data);
	//lobby.broadcastJSON(DeckaEve.DRAW_CARD, Cards.randomCard());
}};

public static EveListener reqEndTurn = new EveListener () { 				@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (notInMatch(player, room, "You're not in match")) return;
	if (room.playable) room.endTurn(player);
	//lobby.broadcastJSON(DeckaEve.DRAW_CARD, Cards.randomCard());
}};
public static EveListener reqAttack = new EveListener () { 					@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (notInMatch(player, room, "You're not in match")) return;
	room.attack(deckaEve.json.data, player);
}};
public static EveListener reqSpellCast = new EveListener () { 			@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (notInMatch(player, room, "You're not in match")) return;
	room.clientSpellCast(deckaEve.json.data, player);
}};

public static EveListener reqConfirmMinion = new EveListener () { @Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (notInMatch(player, room, "You're not in match")) return;
	room.clientSpellCast(deckaEve.json.data, player);
}};
public static EveListener reqStageRefresh = new EveListener () { @Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	Object obj = room.refreshPlayer(player);
	//System.out.println(obj);
	player.client.sendJSON(DeckaEve.REFRESH_ROOM, obj);
}};
public static Boolean notInMatch (DeckaPlayer playa, DeckaRoom room, String errorText) {
	if (room==null || room.state.equals("ended")) {
		playa.client.sendJSON(DeckaEve.DECKA_ERROR, errorText);
		return true;
	} else return false;
}

public static void processChoiceCardReq (DeckaPlayer player, Object pickCard) {
	//long milis = System.currentTimeMillis();
	if (pickCard!=null) {
		Card card = player.pickCard(pickCard);
		if (card!=null) player.client.sendJSON(DeckaEve.PICK_CARD, card.stats());
	}
	if (player.deck.size()==0 && isLocalServer) {
		player.deserializeDeck(Util.urlGET(gameUrl+"facebook.authorize.php?getdeck&token="+player.client.token));
		System.out.println("CARDS IN PLAYERS DECK: "+player.deck.size());
	}
	if (player.deck.size()>=DeckaPlayer.deckCardsMax) {
		player.client.sendJSON(DeckaEve.CHOICE_CARDS_COMPLETE, player.deckToClient());
		//if (player.client.token!=null) 
		System.out.println(Util.urlPOST(gameUrl+"facebook.authorize.php?token="+player.client.token, "deck="+player.serializeDeck()));
		//Util.serialize(player.deck)
	} else {
		player.client.sendJSON(DeckaEve.DRAW_CHOICE_CARDS, Cards.choiceCards(player));
	}
}

/** ADMIN FUNCS */
public static EveListener drawCard = new EveListener () { 				@Override public void onEvent (Eve e) {DeckaEve deckaEve=(DeckaEve) e;
	DeckaPlayer player = DeckaPlayer.getPlayerFromClient(deckaEve.client);
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	//room.endTurn(player);
	//System.out.println("drawCard "+room);
	if (room!=null && room.state.equals("started")) player.drawCard((String)deckaEve.json.data);
	//player.client.sendJSON(DeckaEve.DRAW_CARD, Cards.randomCard());
	//room.passivePlayer();
}};



}