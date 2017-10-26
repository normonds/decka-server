package nooni.sockets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JSONsocketData {
public String event;
//private Long id;
//private Boolean children;
public Object data = "";
public static JSONsocketData fromJSONstr (String message) {
	try {
		//System.out.println("MAKING JSON str:"+message);
		return new Gson().fromJson(message, JSONsocketData.class);
	} catch (JsonSyntaxException exception) {
		System.out.println("JSON SYNTAX ERROR:" + exception.getMessage() + " string:"+message);
		return null;
	}
}
public static String toJSONstr (String eve, Object obj) {
	try {
		return new Gson().toJson(new Object[]{eve, obj});
	} catch (Error err) {
		System.out.println("CANT MAKE JSON OBJECT, event:"+eve+", obj:"+obj+" "+err.getLocalizedMessage());
		return null;
	}	
}
public JSONsocketData (String eve, Object dat) {
	//if (data==null) dat = new Object();
	//if (eve==null) eve = "";
	//System.out.println("JSONsocketData:"+eve+", "+dat);
	event = eve; data = dat;
	//return this;
}

//public String getEvent () { return event; }
///public Long getId () { return id; }
//public Boolean getChildren () { return children; }
//public Object getData () { return data; }
public void setEvent (String event) {
	//System.out.println("SETTING EVENT:"+event);
	this.event = event;
}
//public void setId (Long id) { this.id = id; }
//public void setChildren (Boolean children) { this.children = children; }
public void setData (Object data) {
	//System.out.println("SETTING DATA:"+data);
	this.data = data;
}
public String json () {
	return new Gson().toJson(new Object[]{event, data});
}

public String toString () {
    return String.format("event:"+event+" data:"+data);
}
}