package nooni.sockets;

import java.util.ArrayList;
import nooni.Util;
import nooni.sockets.Client;
import nooni.sockets.ClientEve;
import nooni.sockets.JSONclientEvent;
import nooni.sockets.JSONserverEvent;
import nooni.sockets.JSONsocketData;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;

//import decka.Decka;

public class Socket {
public static Server server;
public static long startTime;
public static ArrayList<Session> sessions = new ArrayList<Session>();

public static int sessionCount = 0;
public static int idleTimeout = 60*60*1000; //ms
public Session sess;
public int num;

public static Boolean getServerIsOnline () {
	return server.isRunning();
}
@SuppressWarnings("static-access")
public void _onConnect (Session session) {
	num = sessionCount++;
	sess = session;
	sessions.add(session);
    //System.out.println("Connect: " + session.getRemoteAddress().getAddress());
    sess.setIdleTimeout(idleTimeout);
    //sess.setMaximumMessageSize(64*1024);
    sendJSON(JSONserverEvent.HELLO, "Hello Webbrowser, this is Server"
			+", active connections:"+sessions.size()
			+", totalconnections:"+sessionCount
			+", uptime:"+Util.milisToTime((System.currentTimeMillis()-startTime))
			//+", server:"+server.getVersion()
			+", idleTimeout:"+session.getIdleTimeout()
			+", reconnect timeout:"+Client.reconnectTimeout
			+")");
    sendJSON(JSONserverEvent.SERVER_SIGNATURE, startTime);
}
private void sendMessage (String str) {
	try {
		if (sess.isOpen()) 
			sess.getRemote().sendString(str);
	} catch (Exception e) {
		System.out.println("SEND MESSAGE ERROR:"+e.getClass()+" ("+e.getMessage()+") while SENDING MESSAGE: "+str);
	}/* catch (IllegalStateException e) {
		System.out.println("SEND MESSAGE ERROR:IllegalStateException ("+e.getMessage()+") while SENDING MESSAGE: "+str);
    } catch (IOException e) {
    	System.out.println("SEND MESSAGE ERROR:IOException ("+e.getMessage()+") while SENDING MESSAGE: "+str);
    }*/
}
public void sendJSON (String event, Object obj) {
	sendMessage(JSONsocketData.toJSONstr(event, obj));
}
public void receiveMessage (JSONsocketData data) {
	if (data==null || data.event==null || data.event.equals("")) {
		System.out.println("SOCKET ON MESSAGE NULL, session:"+num+", remoteIP:"+sess.getRemoteAddress());
		return;
	}
	//System.out.println("Message: " + data);
	if (data.event.equals(JSONclientEvent.LOGOUT)) {
		Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.LOGOUT, Client.get(num)));
		sess.close();
		//System.out.println("logout");
		//Client.get(id).di
	} else if (data.event.equals(JSONclientEvent.NETWRAP_CONNECT)) { // send client reconnect id
		if (data.data.getClass().getName().equals("java.util.ArrayList")) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> datas = (ArrayList<Object>) data.data;
		
			if (datas.get(0).equals("") || !Client.hasReconnectID(datas.get(0), num, this)) { // new connect
				//System.out.println("NETWRAP_CONNECT:new connect");
				sendJSON(JSONserverEvent.UNIQUE_ID, Client.connect(num, this)); // new client
				try {
					Client.get(num).token = (String) datas.get(1);
				} catch (Exception e) {
					
				}
				//String ret = Util.urlGET(Decka.gameUrl+"facebook.authorize.php?token="+datas.get(1));
				String ret = "";
				if (ret.startsWith("nick:")) {
					Client.get(num).nick = ret.substring(5);
				}
				System.out.println(ret);
				sendJSON(JSONserverEvent.NICK, Client.get(num).nick);
				Client.dispatchConnect(Client.get(num));
			} else { // reconnect
				//System.out.println("NETWRAP_CONNECT:reconnect");
				sendJSON(JSONserverEvent.RECONNECT, "");
			}
		} else {
			sendJSON(JSONserverEvent.ERROR, "NETWRAP_CONNECT received data must be array ("+
					data.data.getClass().getName()+")");
		}
		
	} else {
		Client.get(num).refreshTimeout();
		Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.MESSAGE, Client.get(num), data));
	}
}
public void _receiveMessage (String message) {
	receiveMessage(JSONsocketData.fromJSONstr(message));
}
public void _onClose (int statusCode, String reason) {
	if (statusCode==Client.timeoutCODE) {}
	else if (Client.get(num)!=null) Client.get(num).setDisconnected();
	boolean removed = sessions.remove(sess);
	System.out.println("disconnec: code=" + statusCode + " reason=" + reason + " id:"+num+" removed:"+removed);
}
public void _onError (Throwable t) {
	switch (t.getMessage()) {
		case "Timeout on Read" : case "Connection has been disconnected" : 
			//System.out.println("client disconnect " + Client.get(id) + " " + t.getMessage());// + ", " + t.getLocalizedMessage());
			return;
		default : break;
	}
	System.out.println("WebSocket Error: " + t.getMessage());// + ", " + t.getLocalizedMessage());
}

}