package nooni.events;

import java.util.ArrayList;

public class EventDispatcher extends Object {
public String EventDispatcherName;
public ArrayList<EventListenerObject> eventListeners = new ArrayList<EventListenerObject>();

public synchronized void addEventListener (String eventType, EveListener listener) {
	EventListenerObject entry = new EventListenerObject();
	entry.eventType = eventType;
	entry.listener = listener;
    eventListeners.add(entry);
}
public synchronized void removeEventListener (String eventType, EveListener listener) {
	EventListenerObject entry = new EventListenerObject();
	entry.eventType = eventType;
	entry.listener = listener;
    eventListeners.remove(entry);
}
@SuppressWarnings("unused")
public synchronized void removeAllListeners () {
	for (EventListenerObject arrList : eventListeners) {
		//for (EventListenerObject arrList2 : arrList) {arrList2 = null;}
		arrList = null;
	}
	eventListeners.clear();
}
public synchronized void dispatchEvent (Eve event) {
	event.dispatcher = this.toString();
	//System.out.println(event.getClass().cast(obj));
    try {
		for (int i=eventListeners.size()-1;i>=0;i--) {
			if (eventListeners.get(i).eventType.equals(event.type)) {
				//((EventListener) eventListeners.get(i).get(1)).getClass().cast(arg0)
				eventListeners.get(i).listener.onEvent(event);
			}
		}
    } catch (NullPointerException e) {
    	e.printStackTrace();
    }
}

}