package decka;

public class Ability {
public Boolean playable;
public DeckaPlayer player;
public String type;
public int cost = 2;

public Ability (DeckaPlayer player_, String type_) {
	player = player_;
	type = type_;
	playable = false;
}
public String id () {
	return player.prefix+type;
}
}