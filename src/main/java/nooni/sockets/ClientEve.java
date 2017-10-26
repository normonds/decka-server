package nooni.sockets;

import nooni.events.Eve;

public class ClientEve extends Eve {
public static final String TIMEOUT = "timeout";
public static final String DISCONNECT = "disconnect";
public static final String REMOVE = "remove";
public static final String RECONNECT = "reconnect";
public static final String CONNECT = "connect";
public static final String LOGOUT = "logout";
public static final String MESSAGE = "message";
public JSONsocketData json;
public Client client;

public ClientEve (String eventType, Object eventTarget) {
	super(eventType, eventTarget);
	target =  eventTarget;
	type = eventType;
}
public ClientEve (String eventType, Client eventTarget) {
	super(eventType, eventTarget);
	client = eventTarget;
	target = eventTarget;
	type = eventType;
}
public ClientEve (String eventType, Client eventTarget, JSONsocketData data_) {
	super(eventType, eventTarget);
	client =  eventTarget;
	type = eventType;
	json = data_;
}
@Override	public String toString () {
	return "(Object "+this.getClass().getName()+", client:"+client+", json:"+json+")";
}
}