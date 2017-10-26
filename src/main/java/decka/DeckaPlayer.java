package decka;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nooni.Util;
import nooni.sockets.Client;

public class DeckaPlayer {
public static ArrayList<DeckaPlayer> players = new ArrayList<DeckaPlayer>();
public static int deckCardsMax = 5;
public int crystalsMAX=0, crystalsActive = 0, cardNum=0, maxMinions = 7, maxCards = 10, minionCount = 0, drawnCards = 0;
public Client client;
public boolean isBot = false, turn = false;
public DeckaRoom room;
public String prefix;
public Hero hero;
public List<Ability> abilities;
public List<Card> mulligan;
public List<Minion> minions = new ArrayList<Minion>();
public List<Card> hand = new ArrayList<Card>();
public List<Card> choiceCards = new ArrayList<Card>();
public ArrayList<Card> deck = new ArrayList<Card>();
public Map<String, Object> prevUpdatedPlaysPlayer;
public Map<String, Object> prevUpdatedPlaysOppon;
public ArrayList<Card> deckPlayable = new ArrayList<Card>();

public DeckaPlayer (Client clientRef) {
	client = clientRef;
	hero = new Hero(this);
	players.add(this);
}
public void error (String error) {
	if (error.startsWith("error:")) error = error.substring(6);
	client.sendJSON(DeckaEve.DECKA_ERROR, error);
}
public void createAbilities () {
	mulligan = new ArrayList<Card>();
	abilities = new ArrayList<Ability>();
	abilities.add(new Ability(this, "fireball"));
	abilities.add(new Ability(this, "summon"));
	abilities.add(new Ability(this, "trade"));
}
public Ability getAbility (String stri) {
	for (Ability ability : abilities) {
		if (ability.type.equals(stri)) {
			return ability;
		}
	}
	return null;
}
public static void removePlayer (Client client) {
	for (int i=players.size()-1;i>-1;i--) {
		if (players.get(i).client.equals(client)) players.remove(players.get(i));
	}
}
public static DeckaPlayer getPlayerFromClient (Client client) {
	for (DeckaPlayer player : players) {
		if (player.client.equals(client)) {
			return player;
		}
	}
	return null;
}

public void drawCard (Card card) {
	if (hand.size()<5) {
		hand.add(card);
		//card.num = cardNum;
		System.out.println("draw card "+card.title);
		//if (room.activePlayer().equals(this) && card.cost<=crystalsActive) {
			//card.playable = true;
		//}
		client.sendJSON(DeckaEve.DRAW_CARD, card.stats());
		room.broadcastJSON(DeckaEve.DRAW_CARD, card.id());
		//cardNum++;
	} else {
		room.broadcastJSON(DeckaEve.BURN_CARD, card.stats());
	}
}
public Card previewRandCard () {
	//if (deckPlayable.size()==0) return Cards.randomCard(this);
	int inte = Decka.random.nextInt(deckPlayable.size());
	return deckPlayable.get(inte);
}
public Card retRandCard () {
	//if (deckPlayable.size()==0) return Cards.randomCard(this);
	int inte = Decka.random.nextInt(deckPlayable.size());
	Card ret = deckPlayable.get(inte);
	deckPlayable.remove(inte);
	drawnCards++;
	return ret;
}
public void drawRandCard () {
	if (deckPlayable.size()>0) {
		drawCard(retRandCard());
	} else {
		hero.takeDmg(deckCardsMax-drawnCards-1);
		drawnCards++;
		room.checkForDamage();
		room.checkForRemovableMinions();
	}
	//Card card = Cards.randomCard(this);
	//drawCard(card);
}
public void drawCard (String stri) {
	Card card = (stri.equals("") ? Cards.randomCard(this) : Cards.card(stri, this));
	drawCard(card);
}
public Minion addMinion (Card card, int pos) {
	Minion minion = new Minion(this, card);
	if (pos<0) pos = 0;
	room.dispatcher.dispatchEvent(new DeckaBoardEve(DeckaBoardEve.MINION_SUMMON_PRE, minion, pos));
	minions.add(pos, minion);
	List<String> crys = card.getMods("cry");
	for (String cry : crys) {
		if (cry.equals("drawacard")) {
			drawRandCard();
		}
	}
	room.broadcastJSON(DeckaEve.ADD_MINION, minion.stats());
	room.dispatcher.dispatchEvent(new DeckaBoardEve(DeckaBoardEve.MINION_SUMMON_POST, minion, pos));
	return minion;
}

public Boolean confirmMinion (Object id_, int pos) {
	if (!String.valueOf(id_).substring(0,1).equals(prefix)) {
		System.out.println("ERROR confirming minion, id:"+id_+" ,prefix"+prefix);
		client.sendJSON(DeckaEve.DECKA_ERROR, "Error confirming minion!"); return false;
	}
	//int cardNum = Util.INT(String.valueOf(id_).substring(2));
	Boolean minionFound = false;
	for (int i=hand.size()-1;i>=0;i--) {
		if (hand.get(i).id().equals(id_)) {
			if (minions.size()>=maxMinions) {
				client.sendJSON(DeckaEve.DECKA_ERROR, "No more than 7 minions on field!");
				return false;
			}
			minionFound = true;
			addMinion(hand.get(i), pos);
		}
	}
	if (!minionFound) {
		client.sendJSON(DeckaEve.DECKA_ERROR, "Card not found in hand!");
		return false;
	} else return true;
}
public void addRandMinion () {
	Card card = new Card();
	card.attack = (int) Decka.random.nextInt(15);
	card.health = (int) Decka.random.nextInt(15)+1;
	addMinion(card, minions.size());
}
public static Map<String, Object> customStat (String str, Object obj) {
	Map<String, Object> ret = new HashMap<String, Object>();
	ret.put(str, obj);
	return ret;
}
public Map<String, Object> info () {
	Map<String, Object> ret = new HashMap<String, Object>();
	ret.put("playerID", prefix);
	ret.put("nick", client.nick);
	ret.put("heroID", prefix+"hero");
	ret.put("fireballID", prefix+"fireball");
	ret.put("tradeID", prefix+"trade");
	ret.put("summonID", prefix+"summon");
	return ret;
}
public Map<String, Object> availablePlays (String flag, Map<String, Object> addTo) {
	if (addTo==null)  addTo = new HashMap<String, Object>();
	if (flag.equals("forPlayer")) {
		for (Card card : hand) {
			addTo.put(card.id(), customStat("playable", (room.activePlayer().equals(this)) ? crystalsActive>=card.cost : false));
		}
	}
	for (Minion minion : minions) {
		addTo.put(minion.id(), customStat("playable", minion.playable()));
	}
	addTo.put(prefix+"hero", customStat("playable", hero.playable()));
	addTo.put(prefix+"fireball", customStat("playable", (getAbility("fireball").playable) ? crystalsActive>=getAbility("fireball").cost : false));
	addTo.put(prefix+"trade", customStat("playable", (getAbility("trade").playable) ? crystalsActive>=getAbility("trade").cost : false));
	addTo.put(prefix+"summon", customStat("playable", (getAbility("summon").playable) ? crystalsActive>=getAbility("summon").cost : false));
	return addTo;
}
public void startTurn () {
	turn = true;
	for (Minion minion : minions) {
		//minion.playable = (minion.getAttack()>0) ? true : false;
		minion.newTurn();
	}
	hero.newTurn();
	for (Ability ability : abilities) {
		ability.playable = true;
	}
	if (isBot) {
		room.clock = 5000;
	}
}
public void endTurn () {
	for (Minion minion : minions) {
		minion.endTurn();
	}
	hero.endTurn();
	hero.attack = 0;
	for (Ability ability : abilities) {
		ability.playable = false;
	}
	for (Card card : hand) {
		//card.playable = false;
	}
	turn = false;
}
public Card getCard (Object stri) {
	for (Card card : hand) {
		if (card.id().equals(stri)) {
			return card;
		}
	}
	return null;
}
public void equipWeapon (Card card) {
	hero.weapon = new Character();
	hero.weapon.setCard(card);
	hero.weapon.attack = card.attack;
	//hero.weapon.portrait = card.portrait;
	hero.attack = hero.weapon.getAttack();
	hero.weapon.health = card.health;
	hero.weapon.player = this;
	hero.newTurn();
	hero.weapon.isWeapon = true;
}
public boolean minionHasMod (String search) {
	for (Minion minion : minions) {
		if (minion.hasMod(search)) return true;
	}
	return false;
}
@SuppressWarnings("unchecked")
public void startGame (String prefix_) {
	prefix = prefix_;
	minions = new ArrayList<Minion>();
	hand = new ArrayList<Card>();
	createAbilities();
	hero = new Hero(this);
	crystalsMAX=0;
	crystalsActive=0;
	cardNum = 0;
	drawnCards = 0;
	if (isBot) {
		deckPlayable.clear();
		for (int i=0;i<deckCardsMax;i++) {
			deckPlayable.add(Cards.randomCard(this));
		}
	} else {
		deckPlayable = (ArrayList<Card>) deck.clone();
	}
}
public Card pickCard (Object data) {
	//int inte = -1;
	//Card card = null;
	/*try {
		inte = Util.INT(data);
	} catch (Exception e) {
		client.sendJSON(DeckaEve.DECKA_ERROR, "Error picking card ("+data+")");
		return card;
	}*/
	for (Card card : choiceCards) {
		if (card.id().equals(data)) {
			deck.add(card);
			choiceCards.clear();
			return card;
		}
	}
	/*if (inte>-1 && inte<choiceCards.size()) {
		card = choiceCards.get(inte);
		deck.add(card);
		choiceCards.clear();
		return card;
	}*/
	return null;
}
public List<Object> allMinions () {
	List<Object> ret = new ArrayList<Object>();
	for (Minion minio : minions) {
		ret.add(minio.stats());
	}
	return ret;
}
public List<Object> handCards (String playerOrOppon) {
	List<Object> ret = new ArrayList<Object>();
	for (Card card : hand) {
		ret.add(playerOrOppon.equals("forPlayer") ? card.stats() : Util.o(card.id()));
	}
	return ret;
}
public List<Object> handCardIDs () {
	List<Object> ret = new ArrayList<Object>();
	for (Card card : hand) ret.add(card.id());
	return ret;
}
public Map<String, Object> getCurState (String playerOrOppon) {
	Map<String, Object> ret = new HashMap<String, Object>();
	ret.put("crystals", Util.list(crystalsActive, crystalsMAX));
	ret.put("hand", handCards(playerOrOppon));
	ret.put("minions", allMinions());
	ret.put("player", info());
	ret.put("hero", hero.stats());
	return ret;
}
public void createMulligan (int count) {
	for (int i=0;i<count;i++) {
		mulligan.add(retRandCard());
	}
	List<Object> ret = new ArrayList();
	for (Card card : mulligan) {
		ret.add(card.stats());
	}
	System.out.println("mulligan "+client.nick+" init "+mulligan.size());
	client.sendJSON(DeckaEve.MULLIGAN, Util.list(ret, room.clock));
}
@SuppressWarnings("unchecked")
public void mulliganConfirm (Object data) {
	if (mulligan.size()==0) {
		//System.out.println("mulligan "+client.nick+" size 0");
		return;}
	//System.out.println("mulligan confirm "+client.nick);
	List<String> inc;
	try {
		inc = (List<String>) data;
	} catch (Exception e) {
		client.sendJSON(DeckaEve.DECKA_ERROR, "ERROR mulliganConfirm");
		System.out.println("ERROR mulliganConfirm: ("+e.getMessage()+")");
		return;
	}
	System.out.println(inc);
	for (int i=mulligan.size()-1;i>=0;i--) {
		for (String id : inc) {
			if (id.equals(mulligan.get(i).id())) {
				mulligan.remove(i);
				mulligan.add(i, retRandCard());
			}
		}
	}
	List<Object> ret = new ArrayList();
	for (Card card : mulligan) {
		ret.add(card.stats());
	}
	hand = new ArrayList<Card>(mulligan);
	mulligan.clear();
	//System.out.println("mulligan final "+client.nick);
	client.sendJSON(DeckaEve.MULLIGAN_FINAL, ret);
	for (Card crd : hand) {
		room.broadcastJSON(DeckaEve.DRAW_CARD, crd.id());
	}
	if (room.otherPlayer(this).mulligan.size()==0 && room.state=="mulligan") room.mulliganEnd();
}
public void mulliganEnd () {
	
}
public String serializeDeck () {
	String ret = "";
	for (Card card : deck) {
		ret += card.serialize() + "^^";
	}
	if (ret.length()>2) ret = ret.substring(0,ret.length()-2);
	try {
		return URLEncoder.encode(ret, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		return "error encoding string";
	}
}
public void deserializeDeck (String curDeck) {
	String[] cards = curDeck.split("\\^\\^");
	deck.clear();
	for (String stri : cards) {
		deck.add(Card.deserialize(stri, this));
		//System.out.println("---"+stri);
	}
	//System.out.println(cards.);
}
public ArrayList<Object> deckToClient () {
	ArrayList<Object> ret = new ArrayList<Object>();
	for (Card card : deck) {
		ret.add(card.stats());
	}
	return ret;
}

}