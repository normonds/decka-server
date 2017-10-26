package nooni.events;

public class Eve extends Object {
//public static final String TEST_EVENT = "TEST_EVENT";
public Object type;
public Object target;
public Object data;
public String dispatcher;
public Eve (String eventType, Object eventTarget) {
	target = eventTarget;
	type = eventType;
}
}