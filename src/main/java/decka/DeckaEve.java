package decka;

import nooni.sockets.Client;
import nooni.sockets.ClientEve;
import nooni.sockets.JSONsocketData;

public class DeckaEve extends ClientEve {
public static final String DECKA_ERROR = "decka.decka_error";

public static final String REQUEST_CONCEDE = "decka.request_concede";
public static final String REQUEST_CLEAR_DECKLIST = "decka.request_clear_decklist";
public static final String REQUEST_CHANGE_NICK = "decka.request_change_nick";
public static final String REQUEST_CARD = "decka.request_card";
public static final String REQUEST_CHOICE_CARDS = "decka.request_choice_cards";
public static final String REQUEST_QUEUE = "decka.request_queue";
public static final String REQUEST_CANCEL_QUEUE = "decka.request_cancel_queue";
public static final String REQUEST_ATTACK = "decka.request_attack";
public static final String REQUEST_SPELL_CAST = "decka.request_spell_cast";
public static final String REQUEST_CONFIRM_MINION = "decka.request_confirm_minion";
public static final String REQUEST_DECKLIST = "decka.request_decklist";
public static final String REQUEST_PICK_CARD = "decka.request_pick_card";
public static final String REQUEST_REFRESH_STAGE = "decka.request_refresh_stage";
public static final String REQUEST_MULLIGAN_CONFIRM = "decka.request_mulligan_confirm";
public static final String REQUEST_AUTH_INIT = "decka.request_auth_init";

public static final String PICK_CARDS = "decka.pick_cards";
public static final String PICK_CARD = "decka.pick_card";
public static final String DECKLIST_LOBBY = "decka.decklist_lobby";
public static final String CHOICE_CARDS_COMPLETE = "decka.choice_cards_complete";
public static final String MULLIGAN_END = "decka.mulligan_end";
public static final String MULLIGAN_FINAL = "decka.mulligan_final";
public static final String MULLIGAN = "decka.mulligan";
public static final String CANCEL_QUEUE = "decka.cancel_queue";
public static final String DECKLIST = "decka.decklist";
public static final String DRAW_CARD = "decka.draw_card";
public static final String DRAW_CHOICE_CARDS = "decka.draw_choice_cards";
public static final String QUEUED = "decka.queued";
public static final String START_GAME = "decka.start_game";
public static final String END_GAME = "decka.end_game";
public static final String START_TURN = "decka.start_turn";
public static final String END_TURN = "decka.end_turn";
public static final String REQUEST_END_TURN = "decka.request_end_turn";
public static final String CRYSTALS = "decka.crystals";
public static final String PLAYER_DISCONNECT = "decka.player_disconnect";
public static final String PLAYER_RECONNECT = "decka.player_reconnect";
public static final String PLAYERS_INFO = "decka.players_info";
public static final String REFRESH_ROOM = "decka.refresh_room";
public static final String PLAYER_REMOVED = "decka.player_removed";
public static final String MATCH_END = "decka.match_end";
public static final String ADD_MINION = "decka.add_minion";
public static final String REMOVE_CARD = "decka.remove_card";
public static final String UPDATE_ITEMS = "decka.update_items";
public static final String CHARACTER_ATTACK = "decka.character_attack";
public static final String MINION_REMOVE = "decka.minion_remove";
public static final String TAKE_DMG = "decka.take_dmg";

public static final String PROJECTILE = "decka.projectile";
public static final String UPDATE_ITEMS_DELAYED = "decka.update_items_delayed";
public static final String UPDATE_PLAYABLES = "decka.update_playables";

public static final String CHAT_MSG = "chat.msg";
public static final String BURN_CARD = "decka.burn_card";

public static final String CLOCK = "decka.clock";

//public static final String REFRESH_STAGE = "decka.refresh_stage";

public DeckaEve (String eventType, Client client, JSONsocketData eventTarget) {
	super(eventType, client, eventTarget);
	target =  eventTarget;
	type = eventType;
}

}