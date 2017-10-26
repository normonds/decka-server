package decka;

import nooni.events.Eve;

public class DeckaBoardEve extends Eve {
public static final String MINION_SUMMON_PRE = "minion_summon_pre";
public static final String MINION_SUMMON_POST = "minion_summon_post";
public static final String MINION_DEATH_PRE = "minion_death_pre";
public static final String MINION_DEATH_POST = "minion_death_post";
public static final String CHAR_ATTACK = "char_attack";

public static final String SPELL_CAST_PRE = "spell_cast_pre";
public static final String SPELL_CAST_POST = "spell_cast_post";
public static final String CARD_PLAY = "card_play";

public Minion minion;
public int position;

public DeckaBoardEve (String eventType, Object eventTarget) {
	super(eventType, eventTarget);
}
public DeckaBoardEve (String eventType, Minion eventTarget) {
	super(eventType, eventTarget);
	minion = eventTarget;
}
public DeckaBoardEve (String eventType, Minion minion_, int position_) {
	super(eventType, minion_);
	minion = minion_;
	position = position_;
}

}