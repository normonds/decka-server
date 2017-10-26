package decka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nooni.Util;
import nooni.events.EventDispatcher;

public class Card extends EventDispatcher {
private int num;
public int attack, health, cost, id, baseArmor, baseAttack, baseCost;
public String portrait, type, title, descr;
public ArrayList<String[]> mods = new ArrayList<String[]>();
public ArrayList<String[]> targMods = new ArrayList<String[]>();
public DeckaPlayer player;
//public Boolean playable=false;
public static Card deserialize (String inc, DeckaPlayer player) {
	//	String stri = "descr:2:";
	//	String[] strin = stri.split(":");
	//	ArrayList<String> arr = new ArrayList<String>();
	//	arr.addAll(Arrays.asList(strin));
	//	System.out.println(arr);
	Card card = new Card(player);
	String[] split = inc.split(",");
	for (String para : split) {
		String[] paramsplit = para.split(":");
		if (para.startsWith("c")) 	card.cost = Util.INT(paramsplit[1]);
		else if (para.startsWith("a")) card.attack = Util.INT(para.substring(2));
		else if (para.startsWith("h")) card.health = Util.INT(para.substring(2));
		else if (para.startsWith("portrait")) card.portrait = para.substring(9);
		else if (para.startsWith("type")) card.type = para.substring(5);
		else if (para.startsWith("title")) card.title = para.substring(6);
		else if (para.startsWith("descr")) card.descr = para.substring(6);
		else if (para.startsWith("mods")) {
			String[] mods = para.substring(5).split("\\^");
			for (String singleMod : mods) {
				String[] singelModSplit = singleMod.split(":");
				if (singelModSplit.length==1) {
					card.mods.add(new String[]{singelModSplit[0]});
				} else card.mods.add(new String[]{singelModSplit[0], singelModSplit[1]});
			}
		} else if (para.startsWith("targMods")) {
			String[] targMods = para.substring(9).split("\\^");
			for (String singleMod : targMods) {
				String[] singelModSplit = singleMod.split(":");
				if (singelModSplit.length==1) {
					card.targMods.add(new String[]{singelModSplit[0]});
				} else card.targMods.add(new String[]{singelModSplit[0], singelModSplit[1]});
			}
		}
	}
	System.out.println(inc);
	System.out.println(card.serialize());
	return card;
}
public String serialize () {
	String modsOut = "";
	for (String[] mod : mods) {
		if (mod.length==2) modsOut += (mod[0]+":"+mod[1]+"^");
		else modsOut += (mod[0]+"^");
	}
	if (modsOut.length()>0) modsOut = modsOut.substring(0, modsOut.length()-1);
	String targModsOut = "";
	for (String[] mod : targMods) {
		if (mod.length==2) targModsOut += (mod[0]+":"+mod[1]+"^");
		else targModsOut += (mod[0]+"^");
	}
	if (targModsOut.length()>0) targModsOut = targModsOut.substring(0, targModsOut.length()-1);
	String ret = "c:"+cost+",a:"+attack+",h:"+health+",portrait:"+portrait+",type:"+type+",title:"+title+",descr:"+descr;
	if (mods.size()>0) ret += ",mods:"+modsOut;
	if (targMods.size()>0) ret += ",targMods:"+targModsOut;
	return ret;
}
public void init (DeckaPlayer player_) {
	player = player_;
	num = player.cardNum;
	player.cardNum++;
}
public Card () {}
public Card (DeckaPlayer player_) {	init(player_);}
public Card (Map<String, Object> map, DeckaPlayer player_) {
	init(player_);
	type = (String) map.get("type");
	if (map.containsKey("armor")) {
		baseArmor = health = Util.INT(map.get("armor"));
	}
	if (map.containsKey("attack")) {
		baseAttack = attack = Util.INT(map.get("attack"));
	}
	baseCost = cost = Util.INT(map.get("cost"));
	portrait = (String) map.get("portrait");
	title = (String) map.get("title");
	descr = (String) map.get("descr");
	
	
	if (map.containsKey("mods")) {
		String[] split = ((String)map.get("mods")).split(",");
		for (String splitee : split) {
			if (splitee.length()>0) {
				mods.add(splitee.split(":"));
			}
		}
	}
	if (map.containsKey("targMods")) {
		String[] targSplit = ((String)map.get("targMods")).split(",");
		for (String splitee : targSplit) {
			if (splitee.length()>0) {
				targMods.add(splitee.split(":"));
			}
		}
	}
	
	//Cards.descrToMods(map.get("mods"));
	//spellType = (String)map.get("spellType");
}
//public CardStats cardDefaultStats () {
//	return Cards.card(title);
//}
public Map<String, Object> stats () {
	Map<String, Object> ret = new HashMap<String, Object>();
	//ret.put("playerID", playerID);
	ret.put("id", id());
	ret.put("type", type);
	ret.put("targMods", targMods.size()>0);
	ret.put("cost", cost);
	ret.put("armor", health);
	ret.put("attack", attack);
	ret.put("title", title);
	ret.put("descr", descr);
	ret.put("portrait", portrait);
	ret.put("playable", player.crystalsActive>=cost);
	return ret;
}
public String id () {return player.prefix+"c"+num;}
public boolean isBasicMinion () {
	if (type.equals("minion") && targMods.size()==0) return true;
	else return false;
}
public boolean isWeapon () {
	if (type.indexOf("weapon")>-1) {
		return true;
	} else return false;
}
public Object statsForMinion () {
	Map<String, Object> ret = new HashMap<String, Object>();
	//ret.put("playerID", playerID);
	ret.put("type", type);
	ret.put("cost", cost);
	ret.put("armor", health);
	ret.put("attack", attack);
	ret.put("title", title);
	ret.put("descr", descr);
	ret.put("portrait", portrait);
	return ret;
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