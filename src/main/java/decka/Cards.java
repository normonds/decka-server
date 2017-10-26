package decka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nooni.Util;
import nooni.events.EventDispatcher;

public class Cards extends EventDispatcher {
public static Map<String, Map<String, Object>> cards;
//public static Map<String, String> mappings = new HashMap<String, String>();
//mappings.put("drawcard");
public Cards () {}

@SuppressWarnings("unchecked")
public static Card randomCard (DeckaPlayer player) {
	int rand = Decka.random.nextInt(cards.size());
	Card retCard = new Card((Map<String, Object>) cards.values().toArray()[rand], player);
	return retCard;
}
public static Card card (String stri, DeckaPlayer player) {
	Card ret = new Card(cards.get(stri), player);
	return ret;
}
public static CardStats cardStats (String nm) {
	//return CardStats.parse(cards.get(nm));
	if (cards.containsKey(nm)) {
		return CardStats.parse(cards.get(nm));
	} else return null;
}
//public static String descrToMods (String mods) {
//	return mods.replace(" ", "").toLowerCase();
//}
public static String printAll () {
	String ret = "";
	for (Map<String, Object> card  : cards.values()) {
		ret += "-card:"+card.toString()+"\n";
	}
	return "---CARDS---:\n"+ret+"\n-----------";
}
public static Object choiceCards (DeckaPlayer player) {
	List<Object> ret = new ArrayList<Object>();
	Boolean same = false;
	Card card;
	if (player.choiceCards.size()>0) { same = true; }
	for (int i=0;i<6;i++) {
		if (!same) {
			//card = Cards.randomCard(player);
			card = Cards.randomGeneratedCard(player);
			player.choiceCards.add(card);
		} else card = player.choiceCards.get(i);
		ret.add(card.stats());
	}
	//System.out.println("choiceCards, same:"+same);
	return ret;
}
/*
 * RANDOM CARD GENERATION
 */
public static Card randomGeneratedCard (DeckaPlayer player) {
	int type = Decka.random.nextInt(3);
	Card ret = new Card();
	switch (type) {
		case 0 : ret = randomWeapon(player); break;
		case 1 : ret = randomSpell(player); break;
		case 2 : ret = randomMinion(player); break;
//		case 3 : ret = randomSpell(player); break;
//		case 2 : ret = randomConcentration(player); break;
//		case 3 : ret = randomSkillstrike(player); break;
		//case 2 : ret
	}
	return ret;
}
public static Card randomWeapon (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(4)+1;
	int[] ret = Cards.minionStatsFromCost(stats.cost);
	stats.type = "weapon";
	stats.attack = ret[0];
	stats.armor = ret[1];
	stats.title = "Weapon";
	stats.portrait = "weapon."+(stats.cost==10?0:stats.cost)+".png";//"portrait.neptune.png";
	stats.descr = "";
	//stats = randomCardStats(stats);
	return new Card(stats.get(), player);
}
public static Card randomSpell (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(4)+1;
	//int[] ret = Cards.spellStatsFromCost(stats.cost);
	stats.type = "spell";
	//stats.attack = ret[0];
	//stats.armor = ret[1];
	stats.title = "";
	stats.portrait = "spell."+(stats.cost==10?0:stats.cost)+".png";//CardStats.randPortSpell(); //"portrait.neptune.png";
	stats.descr = "";
	stats = randomSpellStats(stats);
	return new Card(stats.get(), player);
}
public static CardStats randomSpellStats (CardStats stats) {
	int type = Decka.random.nextInt(1);
	switch (type) {
		case 0 : stats = basicFireball(stats);
	}
	return stats;
}
public static CardStats basicFireball (CardStats stats) {
	//	switch (stats.cost) {
	//case 0 : ret = randomWeapon(player); break;
	//case 0 : ret = randomSpell(player); break;
	int dmg = 1+Decka.random.nextInt((int)Math.round(stats.cost*1.5)+1);
	stats.targMods = "minion:-"+dmg;
	stats.attack = dmg;
	stats.title += "Pyroball ";
	stats.descr += "Do " + dmg + " damage. ";
	return stats;
}
public static Card randomMinion (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(4)+1;
	int[] ret = Cards.minionStatsFromCost(stats.cost);
	stats.attack = ret[0];
	stats.armor = ret[1];
	stats.title = "Minion";
	stats.portrait = "minion."+(stats.cost==10?0:stats.cost)+".png";//"portrait.neptune.png";
	stats.descr = "";
	stats = randomCardStats(stats);
	return new Card(stats.get(), player);
}
public static CardStats randomCardStats (CardStats stats) {
	int type = Decka.random.nextInt(2);
	switch (type) {
		//case 0 : stats = buffShield(stats); break;
		//case 1 : stats = buffAttackArmor(stats); break;
		//case 2 : stats = buffTaunt(stats); break;
		//case 0 : stats = buffDamage(stats); break;
		//case 0 : stats = buffStealth(stats); break;
		case 0 : stats = buffCantAttack(stats); break;
		//case 0 : stats = buffFreeze(stats); break;
		//case 1 : ret = randomFireball(player); break;
		//case 2 : ret = randomConcentration(player); break;
		//case 3 : ret = randomSkillstrike(player); break;
	}
	return stats;
}
public static CardStats buffDamage (CardStats stats) {
	int dmg = Decka.random.nextInt(stats.cost)+1;
	if (Decka.random.nextBoolean()) {
		dmg = 0-dmg;
	}
	stats.targMods = "char:"+Util.plusOrMinus(dmg);
	stats.descr = "Do "+Math.abs(dmg)+" "+(dmg>0?"heal":"damage")+" to a character";
	return stats;
}
public static CardStats buffCantAttack (CardStats stats) {
	stats.targMods = "minion:cantattack";
	stats.descr = "Make Minion non-attacking";
	return stats;
}
public static CardStats buffTaunt (CardStats stats) {
	stats.targMods = "minion:taunt";
	stats.descr = "Give minion taunt";
	return stats;
}
public static CardStats buffFreeze (CardStats stats) {
	stats.targMods = "char:freeze";
	stats.descr = "Freeze character";
	return stats;
}
public static CardStats buffStealth (CardStats stats) {
	stats.targMods = "minion:stealth";
	stats.descr = "Give minion stealth";
	return stats;
}
public static CardStats buffShield (CardStats stats) {
	stats.targMods = "minion:shield";
	stats.descr = "Give minion divine shield";
	return stats;
}
public static CardStats buffAttackArmor (CardStats stats) {
	int maxVariationAttack = stats.attack<5 ? stats.attack : 5;
	int maxVariationArmor = stats.armor<5 ? stats.attack : 5;
	
	int attack = (maxVariationAttack>0?Decka.random.nextInt(maxVariationAttack):0)-Math.round((maxVariationAttack-1)/2);
	int armor = (maxVariationArmor>0?Decka.random.nextInt(maxVariationArmor):0)-Math.round((maxVariationArmor-1)/2);
	if (attack==0&&armor==0) {return stats;}
	stats.targMods = "minion:"+Util.plusOrMinus(attack)+"/"+Util.plusOrMinus(armor);
	stats.descr = "Give minion "+Util.plusOrMinus(attack)+"/"+Util.plusOrMinus(armor);
	stats.attack -= Math.round((maxVariationAttack-1)/2);
	stats.armor -= Math.round((maxVariationArmor-1)/2);
	return stats;
}
/*
 * MINION END
 */
public static Card randomFireball (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(7)+1;
	stats.type = CardStats.SPELL;
	stats.title = "Fyroball";
	//stats.portrait = "portrait.hephaestus.png";
	stats.descr = "Do "+fireballStatsFromCost(stats.cost)+" damage to target.";
	return new Card(stats.get(), player);
}
public static Card randomSkillstrike (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(10)+1;
	stats.type = CardStats.SPELL;
	stats.title = "Skillstrike";
	stats.portrait = "portrait.nike.png";
	int dmg = skillstrikeStatsFromCost(stats.cost);
	stats.mods = "charMod-mino:-"+Integer.toString(dmg);
	stats.descr = "Do "+dmg+" damage to minions.";
	return new Card(stats.get(), player);
}
public static Card randomConcentration (DeckaPlayer player) {
	CardStats stats = new CardStats();
	stats.cost = Decka.random.nextInt(10)+1;
	stats.type = CardStats.SPELL;
	stats.title = "Concentration";
	int dmg = concentrationStatsFromCost(stats.cost); 
	stats.mods = "charMod-enemy:-"+Integer.toString(dmg);
	stats.descr = "Do "+dmg+" damage to enemies.";
	return new Card(stats.get(), player);
}
public static int[] minionStatsFromCost (int cost) {
	int[] ret = new int[2];
	//int sum = cost*2;
	ret[0] = cost+Decka.random.nextInt(3)-1; // attack
	ret[1] = cost+Decka.random.nextInt(3)-1; // health
	if (ret[1]<1) {ret[1] = 1;}
	return ret;
}
public static int skillstrikeStatsFromCost (int cost) {
	switch (cost) {
	case 1 : return 1;
	case 2 : return 1;
	case 3 : return 2;
	case 4 : return 2;
	case 5 : return 3;
	case 6 : return 3;
	case 7 : return 4;
	case 8 : return 4;
	case 9 : return 5;
	case 10 : return 5;
	}
	return (int) Math.round(cost*1.5);
}
public static int concentrationStatsFromCost (int cost) {
	switch (cost) {
	case 1 : return 1;
	case 2 : return 1;
	case 3 : return 2;
	case 4 : return 2;
	case 5 : return 3;
	case 6 : return 3;
	case 7 : return 4;
	case 8 : return 4;
	case 9 : return 5;
	case 10 : return 5;
	}
	return (int) Math.round(cost*1.5);
}
public static int fireballStatsFromCost (int cost) {
	switch (cost) {
	case 1 : return 2;
	case 2 : return 3;
	case 3 : return 4;
	case 4 : return 6;
	case 5 : return 7;
	case 6 : return 9;
	case 7 : return 10;
	}
	return (int) Math.round(cost*1.5);
}


}