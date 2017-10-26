package decka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.StringUtil;

import nooni.Util;
import nooni.events.EventDispatcher;
import nooni.sockets.Client;

public class DeckaRoom {
public static int count = 0;
public EventDispatcher dispatcher = new EventDispatcher();
public int clock = 0, activePlayerIndex = -1, turnLength = 90000, mulliganTimeout = 5000;
public String state = "preinit", name;
public boolean playable = true;
public static ArrayList<String> plusMinus = new ArrayList<String>();
public static ArrayList<DeckaRoom> deckaRooms = new ArrayList<DeckaRoom>();
public ArrayList<DeckaPlayer> players = new ArrayList<DeckaPlayer>();

public DeckaRoom (String name_) {
	if (name_.equals("")) {
		count++;
		name = "room"+count;
	} else {
		name = name_;
	}
	deckaRooms.add(this);
	System.out.println("creating room "+name+"("+deckaRooms.size()+")");
}
public void enterFrame () {
	if (state.equals("started") || state.equals("mulligan")) {
		if (clock>0) clock -= Decka.frameDelay;
		else if (state.equals("mulligan")) {
			mulliganEnd();
		} else switchTurns();
	} else if (state=="ended") {
	}
}
public DeckaPlayer passivePlayer () {
	return (activePlayerIndex==0)?players.get(1):players.get(0);}
public DeckaPlayer activePlayer () {
	return players.get(activePlayerIndex);}
public DeckaPlayer otherPlayer (DeckaPlayer player) {
	return players.get((players.indexOf(player)==0)?1:0);}
public void endTurn (DeckaPlayer player) {
	if (activePlayer().equals(player)) {
		switchTurns();
	}
}
public void newTurnCrystals () {
	if (activePlayer().crystalsMAX<=9) {
		activePlayer().crystalsMAX++;
	}
	activePlayer().crystalsActive=activePlayer().crystalsMAX;
}

public void startGame () {
	System.out.println("start game");
	broadcastJSON(DeckaEve.START_GAME);
	activePlayerIndex = Decka.random.nextBoolean()?0:1;
	if (players.get(0).isBot) activePlayerIndex = 0;
	else if (players.get(1).isBot) activePlayerIndex = 1;
	players.get(0).startGame("a");
	players.get(1).startGame("b");
	state = "mulligan";
	clock = mulliganTimeout;
	broadcastJSON(DeckaEve.CLOCK, Math.round(clock/1000));
	activePlayer().client.sendJSON(DeckaEve.PLAYERS_INFO, Util.o(activePlayer().info(), passivePlayer().info()));
	passivePlayer().client.sendJSON(DeckaEve.PLAYERS_INFO, Util.o(passivePlayer().info(), activePlayer().info()));
	
	activePlayer().createMulligan(3);
	passivePlayer().createMulligan(4);
	//activePlayer().client.sendJSON(DeckaEve.MULLIGAN, Util.list());
	//passivePlayer().client.sendJSON(DeckaEve.MULLIGAN, Util.list());
	
	//System.out.println(Minion.randMinion(activePlayer()).stats());
	//broadcastJSON(DeckaEve.ADD_MINION, Minion.randMinion(activePlayer()).stats());
	activePlayer().addRandMinion();
	//activePlayer().addRandMinion();
	//activePlayer().addRandMinion();
	passivePlayer().addRandMinion();
	//passivePlayer().addRandMinion();
	//passivePlayer().addRandMinion();
	//passivePlayer().addRandMinion();
	for (int i=0;i<0;i++) {
		//players.get(0).drawRandCard();
		//players.get(1).drawRandCard();
		//players.get(0).client.sendJSON(DeckaEve.DRAW_CARD, Cards.randomCard());
		//players.get(1).client.sendJSON(DeckaEve.DRAW_CARD, Cards.randomCard());
	}
}
public void mulliganEnd () {
	broadcastJSON(DeckaEve.MULLIGAN_END);
	state = "started";
	System.out.println("mulligan end");
	activePlayer().mulliganConfirm(new ArrayList<Card>());
	passivePlayer().mulliganConfirm(new ArrayList<Card>());
	Map<String, Map<String, Object>> ret = new HashMap<String, Map<String, Object>>();
	ret.put(activePlayer().hero.id(), activePlayer().hero.stats());
	ret.put(passivePlayer().hero.id(), passivePlayer().hero.stats());
	broadcastJSON(DeckaEve.UPDATE_ITEMS, ret);
	switchTurns();
	broadcastAvailablePlays(activePlayer());
}
public void switchTurns () {
	if (!playable) return;
	System.out.println("switchTurns");
	clock = turnLength;
	activePlayerIndex = (activePlayerIndex==0)?1:0;
	newTurnCrystals();
	
	passivePlayer().client.sendJSON(DeckaEve.END_TURN);
	passivePlayer().endTurn();
	activePlayer().startTurn();
	activePlayer().client.sendJSON(DeckaEve.START_TURN);
	activePlayer().client.sendJSON(DeckaEve.CLOCK, Math.round(clock/1000));
	broadcastJSON(DeckaEve.CRYSTALS, Util.o(activePlayer().prefix, activePlayer().crystalsActive, activePlayer().crystalsMAX));
	
	broadcastAvailablePlays(passivePlayer());
	activePlayer().drawRandCard();
	if (!playable) return;
	if (passivePlayer().hero.weapon!=null) {
		broadcastJSON(DeckaEve.UPDATE_ITEMS, Util.map(passivePlayer().hero.id(), passivePlayer().hero.stats("attack")));
	}
	if (activePlayer().hero.weapon!=null) {
		broadcastJSON(DeckaEve.UPDATE_ITEMS, Util.map(activePlayer().hero.id(), activePlayer().hero.stats("attack")));
	}
	broadcastAvailablePlays(activePlayer(), "delayed", /*activePlayer().crystalsActive==1?"all":*/"");
}
public static void enterStaticFrame () {
	for (DeckaRoom room : deckaRooms) {room.enterFrame();}
}
public static void movePlayer (DeckaPlayer player, DeckaRoom targRoom) {
	DeckaRoom srcRoom = getRoomFromClient(player);
	//System.out.println(client.nick);
	if (srcRoom!=null) {
		srcRoom.remove(player);
		//System.out.println("removing "+client.nick+" from "+srcRoom.name);
	}
	targRoom.add(player);
	player.room = targRoom;
	//System.out.println(client.nick	+" enters "+targRoom.name);
}
public static void queuePlayer (DeckaPlayer player) {
	//System.out.println(inMatch(client) + " - " + hasAvailableRoom());
	if (!inMatchRoom(player)) {
		DeckaRoom moveTo = getAvailableRoom();
		if (moveTo==null) {
			moveTo=new DeckaRoom("");
			
			if (Decka.useBot) {
				//Client client = ;
				PlayerBot bot = new PlayerBot(new Client());
				movePlayer(bot, moveTo);
			}
		}
		movePlayer(player, moveTo);
		if (moveTo.players.size()>1 ) {
			moveTo.startGame();
		} else {
			player.client.sendJSON(DeckaEve.QUEUED);
		}
		//movePlayer(player, moveTo);
		//if (hasAvailableRoom() && moveTo!=null && moveTo.players.size()<2) {
			//player.client.sendJSON(DeckaEve.OPPONENT_INFO, otherPlayer().client.name);
			//moveTo.broadcast(DeckaEve.DRAW_CARD, );
		//} else {
			
		//}
	}
}
public static void cancelQueue (DeckaPlayer player) {
	if (inMatchRoom(player)) {
		DeckaRoom room = getRoomFromClient(player);
		room.remove(player);
		movePlayer(player, getLobby());
		player.client.sendJSON(DeckaEve.CANCEL_QUEUE);
		System.out.println("canceling que");
	}
}
public static Boolean inMatchRoom (DeckaPlayer player) {
	DeckaRoom room = DeckaRoom.getRoomFromClient(player);
	if (room!=null && !room.name.equals("lobby")) {
		return true;
	}
	return false;
}
public static DeckaRoom getRoomFromClient (DeckaPlayer player) {
	for (DeckaRoom room : deckaRooms) {
		if (room.players.indexOf(player)>-1) {
			return room;
		}}
	return null;
}
public static Boolean hasAvailableRoom () {
	for (DeckaRoom room : DeckaRoom.deckaRooms) {
		if (room.players.size()<2 
				&& !room.name.equals("lobby")) {
			return true;
		}}
	return false;
}
public static DeckaRoom getAvailableRoom () {
	for (DeckaRoom room : DeckaRoom.deckaRooms) {
		if (room.players.size()<2 && !room.name.equals("lobby")) {
			return room;
		}}
	return null;
}
public static DeckaRoom getLobby () {
	for (DeckaRoom room : DeckaRoom.deckaRooms) {
		if (room.name.equals("lobby")) {
			return room;
		}}
	return null;
}

public void add (DeckaPlayer player) {players.add(player);}
public Boolean remove (DeckaPlayer player) {return players.remove(player);}
public void broadcastJSON (String event) {
	broadcastJSON(event, "");
}
public void broadcastJSON (String event, Object data) {
	for (DeckaPlayer player : players) {
		player.client.sendJSON(event, data);
	}
}
public void broadcast (String event, Object data1, Object data2) {
	ArrayList<Object> ret = new ArrayList<Object>();
	ret.add(data1);
	ret.add(data2);
	broadcastJSON(event, ret);
}
public void endGameOnRemove (DeckaPlayer player) {
	broadcastJSON(DeckaEve.PLAYER_REMOVED, player.client.nick);
	if (players.size()>1) endGame(otherPlayer(player));
}
public void endGame (DeckaPlayer winner) {
	broadcastJSON(DeckaEve.MATCH_END, winner.prefix);
	playable = false;
	players = null;
	state = "ended";
	System.out.println("ending match");
	deckaRooms.remove(this);
}
public DeckaPlayer playerFromPrefix (String str) {
	if (str.equals(activePlayer().prefix)) {
		return activePlayer();
	} else if (str.equals(passivePlayer().prefix)) {
		return passivePlayer();
	}
	return null;
	//return str.equals(activePlayer().prefix) ? activePlayer() : passivePlayer();
}
public Character getChar (String id) {
	DeckaPlayer player = playerFromPrefix(id.substring(0, 1));
	if (player.equals(null)) {
		System.out.println("ERROR: no player found with prefix "+id.substring(0, 1));
		return null;
	}
	if (id.equals(player.hero.id())) {
		return player.hero;
	}
	for (Minion minion : player.minions) {
		if (minion.id().equals(id)) {
			return minion;
		}
	}
	return null;
}
public Boolean playAllowed (DeckaPlayer player) {
	if (!player.equals(activePlayer())) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "Enemy's turn!");
		return false;
	}
	return true;
}
public void sevenMinionWarning (DeckaPlayer player) {
	player.client.sendJSON(DeckaEve.DECKA_ERROR, "You can't have more than 7 minions");
}
public void broadcastAvailablePlays (DeckaPlayer player) {
	broadcastAvailablePlays(player, "delayed", "");
}
public Map<String, Object> changedAvailablePlays (Map<String, Object> prev, Map<String, Object> current) {
	Map<String, Object> ret = new HashMap<String, Object>();
	if (prev==null || prev.size()==0) return current;
	for (Map.Entry<String, Object> entry : current.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (!prev.containsKey(key) || (prev.containsKey(key) && !prev.get(key).equals(value))) {ret.put(key, value);}
	}
	return ret;
}
public void broadcastAvailablePlays (DeckaPlayer player, String delayed, String all) {
	Map<String, Object> retPlayer = new HashMap<String, Object>();
	Map<String, Object> retOppon = new HashMap<String, Object>();
	retPlayer = changedAvailablePlays(player.prevUpdatedPlaysPlayer, player.availablePlays("forPlayer", null));
	retOppon = changedAvailablePlays(player.prevUpdatedPlaysOppon, player.availablePlays("forOppon", null));
	player.prevUpdatedPlaysPlayer = player.availablePlays("forPlayer", null);
	player.prevUpdatedPlaysOppon = player.availablePlays("forOppon", null);
	if (retPlayer.size()>0) player.client.sendJSON(DeckaEve.UPDATE_PLAYABLES, retPlayer);
	if (retOppon.size()>0) otherPlayer(player).client.sendJSON(DeckaEve.UPDATE_PLAYABLES, retOppon);
	if (all.equals("all")) {
		player.client.sendJSON(DeckaEve.UPDATE_PLAYABLES, player.availablePlays("forPlayer", null));
		otherPlayer(player).client.sendJSON(DeckaEve.UPDATE_PLAYABLES, player.availablePlays("forOppon", null));
	}
}
@SuppressWarnings("unchecked")
public void clientSpellCast (Object data, DeckaPlayer player) {
	System.out.println("spellCast (" +data.getClass().getName()+") "+data);
	String errorStr = "";
	if (!playable) return;
	if (!playAllowed(player)) {return;}
	// check if abilities
	if (data.getClass().getName().equals("java.util.ArrayList")) {
		ArrayList<Object> datas = (ArrayList<Object>) data;
		if (!datas.get(0).getClass().getName().equals("java.lang.String")) {
			player.error("id not a string");
			System.out.println("ERROR spellCast: id not a string");
			return;
		}
		
		String abilityStr = ((String)datas.get(0)).substring(1);
		Ability ability = player.getAbility(abilityStr);
		if (ability!=null) {
			
			if (!ability.playable) {
				player.error("You already played this ability this turn!");return;
			} else if (ability.cost>player.crystalsActive) {
				player.error("Not enough crystals!");return;
			}
			
			if (abilityStr.equals("trade")) {
				player.hero.takeDmg(-2);
				player.drawRandCard();
			} else if (abilityStr.equals("summon")) {
				if (player.minions.size()<7) {
					Card card = new Card(Cards.cards.get("Champion"), player);
					card.portrait = CardStats.randPortMinion();
					player.addMinion(card, player.minions.size());
					player.getAbility("summon").playable = false;
					
				} else {sevenMinionWarning(player);return;}
			} else if (abilityStr.equals("fireball")) {
				if (datas.size()<2) {
					player.error("Error casting fireball, target not specified!");return;
				} else if (!datas.get(1).getClass().getName().equals("java.lang.String")) {
					player.error("Error casting fireball, target not properly specified!");return;
				} else if (getChar((String)datas.get(1))==null) {
					player.error("Error casting fireball, target not found!");	return;
				}
				castProjectile(ability.id(), (String)datas.get(1), -2);

			} else {
				player.error("Ability not found! ("+data+")");return;
			}

			if (state.equals("ended")) {
				return;
			}
			ability.playable = false;
			player.crystalsActive -= ability.cost;
			broadcastAvailablePlays(player);
			broadcastJSON(DeckaEve.CRYSTALS, Util.o(player.prefix, player.crystalsActive, player.crystalsMAX));
			checkForDamage();
			checkForRemovableMinions();
			return;
		}
		
		Card card = player.getCard(datas.get(0));
		
		if (card==null) {
			player.error("card not found, id:"+datas.get(0));
			System.out.println("ERROR spellCast: card not found, id:"+datas.get(0));
			return;
		}
		
		if (card.isBasicMinion()) {
			if (!datas.get(1).getClass().getName().equals("java.lang.Double")) {
				System.out.println("ERROR: spellCast 3rd argument is not double ("+datas.get(1)+")");
				player.error("New minion position error!");
				return;
			}
		} else if (datas.size()>1 && !datas.get(1).getClass().getName().equals("java.lang.String")) {
			System.out.println("ERROR: spellCast target id is not string ("+datas.get(1)+")");
			player.error("Cast on target error!");
			return;
		} else if (datas.size()>1 && getChar((String)datas.get(1))==null) {
			System.out.println("ERROR: spellCast target not found ("+datas.get(1)+")");
			player.error("Target not found!");
			return;
		} else if (datas.size()>2 && !datas.get(2).getClass().getName().equals("java.lang.Double")) {
			System.out.println("ERROR: spellCast 3rd argument is not double ("+datas.get(2)+")");
			player.error("New minion position error!");
		}
		
		if (card.cost>player.crystalsActive) {
			player.error("Not enough crystals!");
			return;
		}
		
		if (card.isWeapon()) {
			player.equipWeapon(card);
			broadcastJSON(DeckaEve.UPDATE_ITEMS, Util.map(player.hero.id(), player.hero.stats()));
		} else if (card.isBasicMinion()) {
			if (!player.confirmMinion(datas.get(0), Util.INT(datas.get(1).toString()))) {return;	}
		} else if (datas.size()==1) {
			castSingleSpell(player, card);
		} else if (datas.size()==2) {
			errorStr = castTargetSpell(player, card, getChar((String)datas.get(1)));
		} else if (datas.size()==3) {
			errorStr = createTargetMinion(player, card, getChar((String)datas.get(1)), Util.INT(datas.get(2).toString()));
		}
	
		if (errorStr.startsWith("error:")) {
			 player.error(errorStr);
			 return;
		}
		player.hand.remove(card);
		broadcastJSON(DeckaEve.REMOVE_CARD, card.stats());
		player.crystalsActive -= card.cost;
		broadcastAvailablePlays(player);
		broadcastJSON(DeckaEve.CRYSTALS, Util.o(player.prefix, player.crystalsActive, player.crystalsMAX));

	} else {
		player.error("Error casting spell!");
		System.out.println("ERROR: incoming spellCast data not ArrayList and not a String");
	}
	checkForDamage();
	checkForRemovableMinions();
}
public void castProjectile (String src, String targ, int dmg) {
	broadcastJSON(DeckaEve.PROJECTILE, Util.o(src, targ));
	getChar(targ).takeDmg(dmg);
	checkForDamage();
	checkForRemovableMinions();
}
public String applyTargMod (Card card, Character targChar) {
	for (String[] mod : card.targMods) {
		if (mod.length>1) {
			if (mod[0].equals("minion") && targChar.isMinion==false) return "error:Target is not a Minion!";
			if (mod[1].contains("/")) { // buffing
				Object ids = basicStatModOnChar(mod, targChar);
				broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, ids);
			} else if (Util.indexOf(new String[]{"shield","taunt","stealth","freeze","cantattack"},mod[1])>-1) {
				System.out.println("giving "+mod[1]);
				//Object ids = basicStatModOnChar(mod, targChar);
				
				targChar.addMod(mod[1]);
				if (mod[1].equals("freeze")) targChar.setFrozen();
				
				broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, Util.map(targChar.id(), targChar.stats("playable,mods")));
			} else { // dmg or heal
				if (plusMinus.contains(mod[1].substring(0, 1))) {
					System.out.println("damage or heal " + mod[1]);
					targChar.takeDmg(Integer.parseInt(mod[1]));
					checkForDamage(DeckaEve.UPDATE_ITEMS);
					//checkForDamage();
					//broadcastJSON(DeckaEve.UPDATE_ITEMS, Util.map(targChar.id(), targChar.stats()));
				}
			}
		}
	}
	return "";
}
public String createTargetMinion (DeckaPlayer player, Card card, Character targChar, int pos) {
	System.out.println("createTargetMinion");
	String retStr = applyTargMod(card, targChar);
	if (retStr.startsWith("error")) {
		//playerError(player, retStr.substring(6));
		return retStr;
	}
	player.addMinion(card, pos);
	return "";
}
public String castTargetSpell (DeckaPlayer player, Card card, Character targChar) {
	System.out.println("castTargetSpell");
	String retStr = applyTargMod(card, targChar);
	if (retStr.startsWith("error:")) {
		playerError(player, retStr);
	}
	return retStr;
//	for (String[] mod : card.mods) {
//		if (mod.length>1) {
//			if (mod[1].contains("/")) { // buffing
//				Object ids = basicStatModOnChar(mod, targ);
//				broadcastJSON(DeckaEve.UPDATE_ITEMS, ids);
//			} else { // dmg or heal
//				if (plusMinus.contains(mod[1].substring(0, 1))) {
//					targ.takeDmg(Integer.parseInt(mod[1]));
//				}
//			} 
//		}
//	}
}
public void castSingleSpell (DeckaPlayer player, Card card) {
	System.out.println("castSingleSpell");
	for (String[] mod : card.mods) {
		if (mod.length==1) {
			
		} else if (mod.length>1) {
			if (mod[1].contains("/")) { // buffing
				Object ids = basicStatModOnChar(mod, null);
				broadcastJSON(DeckaEve.UPDATE_ITEMS, ids);
			} else { // dmg or heal
				if (plusMinus.contains(mod[1].substring(0, 1))) {
					for (Character chr : allCharacters(mod[0])) {chr.takeDmg(Integer.parseInt(mod[1]));}
				}
			}
		}
	}
	
}
public Map<String,Object> basicStatModOnChar (String[] mod, Character targChr) {
	String[] buffStats = mod[1].split("/");
	int healthMod = 0;
	int attackMod = 0;
	Map<String,Object> ids = new HashMap<String,Object>();
	
	if (buffStats[0].length()>0) {attackMod = Integer.parseInt(buffStats[0]);}
	if (buffStats[1].length()>0) {healthMod = Integer.parseInt(buffStats[1]);}
	
	Map<String,Object> ret;
	if (targChr!=null) {
		ids.put(targChr.id(), statModOnChar(targChr, buffStats, attackMod, healthMod));
	} else {
		for (Character chr : allCharacters(mod[0])) {
			ret = statModOnChar(chr, buffStats, attackMod, healthMod);
			if (ret.size()>0) ids.put(chr.id(), ret);
		}
	}
	return ids;
}
public Map<String,Object> statModOnChar (Character chr, String[] buffStats, int attackMod, int healthMod) {
	Map<String,Object> ret = new HashMap<String,Object>();
	if (buffStats[0].length()>0) {
		chr.attack = plusMinus.contains(buffStats[0].substring(0, 1)) ? chr.attack+attackMod : attackMod;
		ret = chr.stats(ret, new String[]{"attack"});
	}
	if (buffStats[1].length()>0) {
		chr.health = plusMinus.contains(buffStats[1].substring(0, 1)) ? chr.health+healthMod : healthMod;
		ret = chr.stats(ret, new String[]{"health"});
	}
	return ret;
}
@SuppressWarnings("unchecked")
public void attack (Object data, DeckaPlayer player) {
	ArrayList<String> inc;
	try {
		inc = (ArrayList<String>) data;
	} catch (Exception e) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "DECKA ROOM ATTACK ERROR");
		System.out.println("DECKA ROOM ATTACK ERROR: ("+e.getMessage()+")");
		return;
	}
	//player.client.sendJSON(DeckaEve.DECKA_ERROR, "attack "+data);
	Character attacker = getChar(inc.get(0));
	Character target = getChar(inc.get(1));
	//System.out.println("attack:"+attacker.id()+", "+attacker.playable);
	
	if (attacker==null) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "attacker is null");
		return;
	}
	if (target==null) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "target is null");
		return;
	}
	if (!playAllowed(player)) {return;}
	if (!attacker.player.equals(player)) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "You can't attack with opponents characters!");
		return;
	}
	if (attacker.attacksLeft<1) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "You can't play minion this turn!");
		return;
	}
	if (attacker.player.equals(target.player)) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "You can't attack your own characters!");
		return;
	}
	if (otherPlayer(player).minionHasMod("taunt") && !target.hasMod("taunt")) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "You must attack minion with taunt!");
		return;
	}
	if (target.hasMod("stealth")) {
		player.client.sendJSON(DeckaEve.DECKA_ERROR, "You can't attack stealthed minions!");
		return;
	}
	broadcastJSON(DeckaEve.CHARACTER_ATTACK, inc);
	attacker.makeAttack(target);
	
	checkForDamage();
	
	if (attacker.weapon!=null) { // update weapon play
		broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, Util.map(attacker.id(), attacker.stats("weapon,playable,attack")));
		if (attacker.weapon.health<1) { // remove weapon
			attacker.weapon = null;
			attacker.attack = 0;
			broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, Util.map(attacker.id(), attacker.stats()));
		}
	}
	//Map<String, Map<String, Object>> ret = new HashMap<String, Map<String, Object>>();
	//ret.put(attacker.id(), attacker.stats(new String[]{"attack","health","playable"}));
	//ret.put(target.id(), target.stats(new String[]{"attack","health"}));
	//broadcastJSON(DeckaEve.UPDATE_ITEMS, ret);
	
	broadcastAvailablePlays(player);
	
	checkForRemovableMinions();
	//System.out.println("attack " + data);
}
public void checkForDamage () {
	checkForDamage(DeckaEve.UPDATE_ITEMS_DELAYED);
}
public void checkForDamage (String delayedOrInstant) {
	ArrayList<Character> inc = allCharacters("");
	Map<String, Map<String, Object>> healthList = new HashMap<String, Map<String, Object>>();
	for (Character chr : inc) {
		if (chr.takeDmg!=0) {
			healthList.put(chr.id(), chr.stats("health"));
			chr.takeDmg = 0;
		}
	}
	//if (dmgList.size()>0) broadcastJSON(DeckaEve.TAKE_DMG, dmgList);
	if (healthList.size()>0) broadcastJSON(delayedOrInstant, healthList);
}
public void checkForRemovableMinions () {
	for (Minion minion : allMinions()) {
		if (minion.health<1) {
			broadcastJSON(DeckaEve.MINION_REMOVE, minion.id());
			removeMinion(minion);
		}
	}
	if (activePlayer().hero.getHealth()<=0) {
		endGame(passivePlayer());
	} else if (passivePlayer().hero.getHealth()<=0) {
		endGame(activePlayer());
	}
}
public void removeMinion (DeckaPlayer playa, Minion minion) {
	System.out.println("removing "+minion);
	int removedIndex = playa.minions.indexOf(minion);
	dispatcher.dispatchEvent(new DeckaBoardEve(DeckaBoardEve.MINION_DEATH_PRE, minion));
	minion.onRemove();
	playa.minions.remove(minion);
	dispatcher.dispatchEvent(new DeckaBoardEve(DeckaBoardEve.MINION_DEATH_POST, minion, removedIndex));
}
public void removeMinion (Minion minion) {
	int i;
	for (i=activePlayer().minions.size()-1;i>=0;i--) {
		if (activePlayer().minions.get(i).equals(minion)) {
			removeMinion(activePlayer(), minion);
			return;
		}
	}
	for (i=passivePlayer().minions.size()-1;i>=0;i--) {
		if (passivePlayer().minions.get(i).equals(minion)) {
			removeMinion(passivePlayer(), minion);
			return;
		}
	}
	System.out.println("minion for remove not found " + minion.id());
}
public ArrayList<Character> allCharacters (String mod) {
	ArrayList<Character> ret = new ArrayList<Character>();
	if (!mod.contains("enemy")) {
		if (!mod.contains("hero")) {
			for (Minion minion : activePlayer().minions) {
				ret.add(minion);
			}
		}
		if (!mod.contains("mino")) {ret.add(activePlayer().hero);}
	}
	if (!mod.contains("friend")) {
		if (!mod.contains("hero")) {
			for (Minion minion : passivePlayer().minions) {
				ret.add(minion);
			}
		}		
		if (!mod.contains("mino")) {ret.add(passivePlayer().hero);}
	}
	return ret;
}
public ArrayList<Minion> allMinions () {
	ArrayList<Minion> ret = new ArrayList<Minion>();
	for (Minion minion : activePlayer().minions) {
		ret.add(minion);
	}
	for (Minion minion : passivePlayer().minions) {
		ret.add(minion);
	}
	return ret;
}
public Object refreshPlayer (DeckaPlayer player) {
	return Util.list(player.getCurState("forPlayer"), otherPlayer(player).getCurState("forOppon"), Math.round(clock/1000));
}

public void playerError (DeckaPlayer player, String error) {
	player.client.sendJSON(DeckaEve.DECKA_ERROR, error);
}

}