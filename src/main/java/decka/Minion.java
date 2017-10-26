package decka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nooni.events.Eve;
import nooni.events.EveListener;

public class Minion extends Character {

public Minion (DeckaPlayer playa, Card card_) {
	super();
	init(playa);
	setCard(card_);
	mods = getCard().mods;
	attack = getCard().attack;
	health = getCard().health;
	healthBase = healthMax = health;
	attackBase = attack;
	
	playa.room.dispatcher.addEventListener(DeckaBoardEve.MINION_SUMMON_PRE, minionSummonPre);
	playa.room.dispatcher.addEventListener(DeckaBoardEve.MINION_SUMMON_POST, minionSummonPost);
	playa.room.dispatcher.addEventListener(DeckaBoardEve.MINION_DEATH_POST, minionDeathPost);
	playa.room.dispatcher.addEventListener(DeckaBoardEve.MINION_DEATH_PRE, minionDeathPre);
}
public void init (DeckaPlayer playa) {
	isMinion = true; player = playa; num = player.minionCount; player.minionCount++;
}
public EveListener minionSummonPre = new EveListener () { 		@Override public void onEvent (Eve e) {DeckaBoardEve deckaEve=(DeckaBoardEve) e;
	if (hasMod("adjaura") && !deckaEve.minion.id().equals(id())) { // remove if insert
		Map<String, Map<String, Object>> attackList = new HashMap<String, Map<String, Object>>();
		System.out.println(id() + ", minionSummonPre:"+this);
		//System.out.println("minionSummonPre "+ getCard().title + "  " + left() + " pos:"+deckaEve.position+", thisPos:"+player.minions.indexOf(this));
		if (left()!=null && player.minions.indexOf(left())+1==deckaEve.position) {
			left().modAttackAura(-2);
			attackList.put(left().id(), left().stats("attack"));
		}
		if (right()!=null && player.minions.indexOf(right())==deckaEve.position) {
			right().modAttackAura(-2);
			attackList.put(right().id(), right().stats("attack"));
		}
		//System.out.println("minionSummonPost "+attackList);
		if (attackList.size()>0) player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, attackList);
	}
}};
public EveListener minionSummonPost = new EveListener () { 		@Override public void onEvent (Eve e) {DeckaBoardEve deckaEve=(DeckaBoardEve) e;
	if (hasMod("juggler")) {
		if (!deckaEve.minion.id().equals(id()) && deckaEve.minion.player==player) {
			String targId;
			int randTarg = Decka.random.nextInt(player.room.otherPlayer(player).minions.size()+1)-1;
			if (randTarg==-1) {targId = player.room.otherPlayer(player).hero.id();}
			else {targId=player.room.otherPlayer(player).minions.get(randTarg).id();}
			player.room.castProjectile(id(), targId, -1);
		}
	}
	if (hasMod("adjaura")) {
		Map<String, Map<String, Object>> attackList = new HashMap<String, Map<String, Object>>();
		if (deckaEve.minion.id().equals(id())) {
			if (left()!=null) {
				left().modAttackAura(2);
				attackList.put(left().id(), left().stats("attack"));
			}
			if (right()!=null) {
				right().modAttackAura(2);
				attackList.put(right().id(), right().stats("attack"));
			}
			if (attackList.size()>0) player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, attackList);
		} else {
			if (left()!=null && left().index()==deckaEve.position) {
				left().modAttackAura(2);
				attackList.put(left().id(), left().stats("attack"));
			}
			if (right()!=null && right().index()==deckaEve.position) {
				right().modAttackAura(2);
				attackList.put(right().id(), right().stats("attack"));
			}
		}
		//System.out.println("minionSummonPost "+attackList);
		if (attackList.size()>0) player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, attackList);
	}
	player.room.broadcastAvailablePlays(player);
}};
public EveListener minionDeathPost = new EveListener () { 			@Override public void onEvent (Eve e) {DeckaBoardEve deckaEve=(DeckaBoardEve) e;
	if (hasMod("ghoul")) {
		attack++;
		Map<String, Map<String, Object>> attackList = new HashMap<String, Map<String, Object>>();
		attackList.put(id(), stats("attack"));
		player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, attackList);
	}
	if (hasMod("adjaura") && !deckaEve.minion.id().equals(id())) {
		Map<String, Map<String, Object>> attackList = new HashMap<String, Map<String, Object>>();
		if (left()!=null && deckaEve.position==index()) { // buff left
			left().modAttackAura(2);
			attackList.put(left().id(), left().stats("attack"));
		}
		if (right()!=null && deckaEve.position==index()+1) { // buff right
			right().modAttackAura(2);
			attackList.put(right().id(), right().stats("attack"));
		}
		//System.out.println("minionDeathPost "+attackList);
		if (attackList.size()>0) player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, attackList);
	}
	player.room.broadcastAvailablePlays(player);
}};
public EveListener minionDeathPre = new EveListener () { 		@Override public void onEvent (Eve e) {DeckaBoardEve deckaEve=(DeckaBoardEve) e;
	if (hasMod("adjaura")) { // remove adjacent auras
		Map<String, Map<String, Object>> attackList = new HashMap<String, Map<String, Object>>();
		if (deckaEve.minion.id().equals(id())) {
			if (left()!=null) { // debbuff left
				left().modAttackAura(-2);
				attackList.put(left().id(), left().stats("attack"));
			}
			if (right()!=null) {
				right().modAttackAura(-2);
				attackList.put(right().id(), right().stats("attack"));
			}
		} else {

		}
		//System.out.println("minionDeathPre "+attackList);
		if (attackList.size()>0) player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, attackList);
	}
}};
public void onRemove () {
	List<String> ret = getMods("rattle");
	for (String mod : ret) {
		if (mod.equals("drawacard")) {
			player.drawRandCard();
		}
	}
	player.room.dispatcher.removeEventListener(DeckaBoardEve.MINION_SUMMON_PRE, minionSummonPre);
	player.room.dispatcher.removeEventListener(DeckaBoardEve.MINION_SUMMON_POST, minionSummonPost);
	player.room.dispatcher.removeEventListener(DeckaBoardEve.MINION_DEATH_POST, minionDeathPost);
	player.room.dispatcher.removeEventListener(DeckaBoardEve.MINION_DEATH_PRE, minionDeathPre);
}
public Minion left () {
	System.out.println(id() + ", "+this);
	if (player.minions.indexOf(this)>0) return player.minions.get(player.minions.indexOf(this)-1);
	else return null;
}
public Minion right () {
	System.out.println(id() + ", "+this);
	if (player.minions.indexOf(this)<player.minions.size()-1) {
		return player.minions.get(player.minions.indexOf(this)+1);
	} else return null;
}
public int index () {
	return player.minions.indexOf(this);
}
public Boolean hasMod (String srch) {
	for (String[] mod : mods) {
		if (mod[0].equals(srch)) {
			return true;
		}
	}
	return false;
}
public List<String> getMods (String srch) {
	List<String> ret = new ArrayList<String>();
	for (String[] mod : mods) {
		if (mod[0].equals(srch)) {
			ret.add(mod[1]);
		}
	}
	return ret;
}

}