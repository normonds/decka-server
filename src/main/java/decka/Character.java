package decka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nooni.Util;
import nooni.events.EventDispatcher;

public class Character extends EventDispatcher {
public int healthBase, healthMax, attackBase, takeDmg=0;
private Card card;
public ArrayList<String[]> mods = new ArrayList<String[]>();
public Character weapon;
public int attack=-1, health=-1, num, attackAura=0, healthAura=0, attacksLeft=0, frozen=0;
public Boolean isWeapon = false, isHero = false, isMinion=false;//, playable=false;
public DeckaPlayer player;

public Character () {}

public void addMod (String mod) {
	mods.add(new String[]{mod});
}
public void modAttackAura (int i) {	attackAura += i;}
public void modHealthAura (int i) {	healthAura += i;}
public void modAttack (int i) {
	attack += i;	if (attack<0) attack = 0;
}
public void modhealth (int i) {
	health += i;	if (health<0) health = 0;
}
public int getAttack () {	return attack+attackAura;}
public int getHealth () {	return health+healthAura;}
public Boolean playable () {
	if (hasMod("freeze") || hasMod("cantattack")) {
		attacksLeft = 0;
	}
	return (attacksLeft>0 && getAttack()>0);
}
public void attackUsed () {attacksLeft--;}
public void setFrozen () {
	if (player.turn) {
		frozen = -2;
	} else {
		frozen = -1;
	}
}
public void newTurn () {
	if (isHero) {
		if (weapon!=null && weapon.getAttack()>0) {
			attacksLeft = 1;
			attack = weapon.getAttack();
		}
	} else attacksLeft = 1;
	if (frozen<0) frozen += 1;
}
public void endTurn () {
	attacksLeft=0;
	if (frozen<0) frozen += 1;
	if (hasMod("freeze") && frozen==0) {
		System.out.println("removeing freeze");
		removeMod("freeze");
		player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, Util.map(id(), Util.map("mods", modsForClient())));
	}
}
public Map<String,Object> stats (String stri) {
	String[] striArr = stri.split(",");
	Map<String, Object> ret = new HashMap<String, Object>();
	return stats(ret, striArr);
}
public Map<String,Object> stats (Map<String, Object> ret, String[] stri) {
	if (Util.indexOf(stri, "attack")>-1) {		ret.put("attack", getAttack()); ret.put("attackCol", attackCol());				}
	if (Util.indexOf(stri, "health")>-1) {		
		ret.put("health", getHealth()); ret.put("healthCol", healthCol());
		if (takeDmg!=0) ret.put("takedmg", takeDmg);
	}
	if (Util.indexOf(stri, "id")>-1) {				ret.put("id", id());							}
	if (Util.indexOf(stri, "playable")>-1) {	ret.put("playable", playable());		}
	if (Util.indexOf(stri, "weapon")>-1 && weapon!=null) {	ret.put("weapon", weapon.stats());	}
	if (Util.indexOf(stri, "mods")>-1) {	ret.put("mods", modsForClient());		}
	return ret;
}
public Map<String, Object> stats () {
	Map<String, Object> ret = new HashMap<String, Object>();
	ret.put("attack", attack);
	ret.put("attackCol", attackCol());
	ret.put("health", health);
	ret.put("healthCol", healthCol());
	if (isMinion) {
		ret.put("pos", player.minions.indexOf(this));
		if (mods.size()>0) {
			ret.put("mods", modsForClient());
		}
		
		ret.put("card", getCard().statsForMinion());
	}
	if (isWeapon || isMinion) {
		ret.put("portrait", getCard().portrait);
	}
	if (!isWeapon) {
		ret.put("playable", playable());
		ret.put("id", id());
	}
	if (weapon!=null) {
		ret.put("weapon", weapon.stats());
	}
	return ret;
}
public String healthCol () {
	if (getHealth()<healthBase) return "red";
	else if (getHealth()>healthBase) return "lime";
	else return "white";
}
public String attackCol () {
	if (getAttack()<attackBase) return "lime";
	else if (getAttack()>attackBase) return "lime";
	else return "white";
}

public String id () {
	if (isHero) return player.prefix+"hero";
	else return player.prefix+"m"+num;
}
public static String randPortrait () {
	String[] mods = new String[]{"artemis","hephaestus","heroin","lamp","neptune","nike","splendid","zanda"};
	return "portrait."+mods[Decka.random.nextInt(mods.length)];
}
public static String randMod () {
	String[] mods = new String[]{"taunt","stealth","shield","freeze","silence","rattle","lightning"};
	return mods[Decka.random.nextInt(mods.length)];
}
public Card getCard () {
	if (card==null) {
		card = new Card();
		card.portrait = randPortrait();
		card.attack = attack<0?1:attack;
		card.health = health<0?1:health;
		card.cost = 1;
		card.title = "Default Card";
		card.descr = "some description";
		return card;
	} else return card;
}
public void setCard (Card inc) {
	card = inc;
}
public Map<String, Object> modsForClient () {
	Map<String, Object> ret = new HashMap<String, Object>();
	for (String[] mod : mods) {
		if (mod.length>0) {
			if (mod[0].equals("cry") ) {
				ret.put("lightning", true);
			} else ret.put(mod[0], true);
		}
	}
	return ret;
}

public void makeAttack (Character attackee) {
	if (weapon!=null) {
		weapon.health -= 1;
	}
	if (hasMod("stealth")) {
		removeMod("stealth");
		player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS_DELAYED, Util.map(id(), stats("mods")));
		//player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, ret);
	}
	attackUsed();
	attackee.takeDmg(-getAttack());
	takeDmg(-attackee.getAttack());
	if (getHealth()<0) health = 0;
	//if (attackee.getHealth()<0) attackee.health = 0;
}
public void broadcastUpdate (String stri) {
	Map<String, Map<String, Object>> ret = new HashMap<String, Map<String, Object>>();
	ret.put(id(), stats(stri));
	player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, ret);
}
public void takeDmg (int attack_) {
	if (attack_<0 && hasMod("shield")) {
		removeMod("shield");
	} else if (attack_>0 && health==healthMax) {
		return;
	} else if (attack_!=0) {
		health += attack_;
		if (health>healthMax) { // dont overheal
			attack_ -= (health-healthMax);
			health = healthMax;
		}
		takeDmg = attack_;
		//player.room.broadcastJSON(DeckaEve.TAKE_DMG, Util.o(id(), attack_));
		//player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, ret);
	}
	//broadcastUpdate(new String[]{"attack","health","playable"});
}
public void removeMod (String needle) {
	for (int i=mods.size()-1;i>=0;i--) {
		if (mods.get(i)[0].equals(needle)) {
			mods.remove(i);
			player.room.broadcastJSON(DeckaEve.UPDATE_ITEMS, Util.map(id(), Util.map("mods", Util.map(needle, false))));
		}
	}
}
public Boolean hasMod (String needle) {
	for (String[] mod : mods) {
		System.out.println(Arrays.asList(mod) + " - " + needle);
		if (mod[0].equals(needle)) {
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