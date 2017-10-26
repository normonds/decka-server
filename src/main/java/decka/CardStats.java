package decka;

import java.util.HashMap;
import java.util.Map;

public class CardStats {
public static final String SPELL = "spell";
public static final String MINION = "minion";
public static final String WEAPON = "weapon";
public int attack=0, armor=0, cost=0;

public static String[] portraits = {"budhapig","crow","dummyvase","flyingpig","frog","horse","ninja","owl","scarecrow","stoneman","voodoo","wookie"};
public String portrait="portrait.neptune.png", type="minion", title="undefined", descr="undefined", mods="", targMods="";
public CardStats (){

}
/*public CardStats (int attack_, int armor_, int cost_, String portrait_, String type_, String title_, String descr_) {
	attack=attack_;
	armor=armor_;
	cost=cost_;
	portrait=portrait_;
	type=type_;
	title=title_;
	descr=descr_;
}*/
public static String randPortSpell () {
	return "spell."+Decka.random.nextInt(10)+".png";
}
public static String randPortMinion () {
	return "minion."+Decka.random.nextInt(10)+".png";
}
public static String randPortWeapon () {
	return "weapon."+Decka.random.nextInt(10)+".png";
}
public static CardStats parse (Map<String, Object> inc) {
	CardStats ret = new CardStats();
	if (inc.containsKey("attack")) ret.attack = (int) inc.get("attack");
	if (inc.containsKey("armor")) ret.armor = (int) inc.get("armor");
	if (inc.containsKey("cost")) ret.cost = (int) inc.get("cost");
	if (inc.containsKey("portrait")) ret.portrait = (String) inc.get("portrait");
	if (inc.containsKey("type")) ret.type = (String) inc.get("type");
	if (inc.containsKey("title")) ret.title = (String) inc.get("title");
	if (inc.containsKey("descr")) ret.descr = (String) inc.get("descr");
	if (inc.containsKey("mods")) ret.mods = (String) inc.get("mods");
	if (inc.containsKey("targMods")) ret.targMods = (String) inc.get("targMods");
	return ret;
}
public Map<String, Object> get () {
	Map<String, Object> ret = new HashMap<String, Object>();
	ret.put("attack", attack);
	ret.put("armor", armor);
	ret.put("cost", cost);
	ret.put("portrait", portrait);
	ret.put("type", type);
	ret.put("title", title);
	ret.put("descr", descr);
	ret.put("mods", mods);
	ret.put("targMods", targMods);
	return ret;
}

}
