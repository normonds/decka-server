package nooni.sockets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import nooni.events.EventDispatcher;

public class Client {
public static ArrayList<Client> clients = new ArrayList<Client>();
public int socketNum=-1, timeout;
public static int reconnectTimeout = 3/*secs*/, timeoutCONST = 300, timeoutCODE = 1011;/*secs*/;
public String id, nick;
public boolean connected, removed=false;
public Socket socket;
public long disconnTimestamp;
private EnterFrameTask timerTask;
private Timer timer;
public String token;
public static EventDispatcher dispatcher = new EventDispatcher();

class EnterFrameTask extends TimerTask {public void run () {enterFrame();}}
public void enterFrame () {
	timeout--;
	if (timeout<1) {
		timer.cancel();
		timer.purge();
		Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.TIMEOUT, this));
		Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.REMOVE, this));
		if (connected) socket.sess.close(timeoutCODE, "Timed Out");
		else System.out.println("reconn timed out");
		clients.remove(this);
	}
}
public Client () {
	connected = true;
}
public Client (int socketNum_, Socket sess_) {
	refreshTimeout();
	timer = new Timer();
	timerTask = new EnterFrameTask();
	timer.scheduleAtFixedRate(timerTask, 0, 1000);
	socketNum = socketNum_;
	socket = sess_;
	nick = "~Guest"+socketNum;
	id = UUID.randomUUID().toString();
}
public void refreshTimeout () {timeout = timeoutCONST;}
public static String connect (int socketNum_, Socket sess_) {
	//System.out.println("connecting client " + socketNum_ + " - " + sess_.num);
	Client newClient = new Client(socketNum_, sess_);
	clients.add(newClient);
	newClient.connected = true;
	return newClient.id;
}
public static void dispatchConnect (Client client) {
	Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.CONNECT, client));
}
public static String retAllClientNums () {
	String ret = "";
	for (Client client : clients) {
		if (!client.equals(null)) {
			ret+=","+client.socketNum;
		}
	}
	return ret;
}
public static Client get (int socketNum_) {
	//System.out.println("getClient:"+socketNum_+" from:"+retAllClientNums());
	for (Client client : clients) {
		if (!client.equals(null) && client.socketNum>-1 && client.socketNum==socketNum_) return client;
	}
	return null;
}
public static Boolean hasReconnectID (Object uniqueId_, int socketNum_, Socket socket_) {
	for (Client client : clients) {
		if (client.id.equals(uniqueId_)) {
			client.connected = true;
			client.refreshTimeout();
			client.socket = socket_;
			client.socketNum = socketNum_;
			Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.RECONNECT, client));
			return true;
		}
	}
	return false;
}
public void setDisconnected () {
	removed = true;
	connected = false;
	timeout = reconnectTimeout;
	disconnTimestamp = System.currentTimeMillis();
	Client.dispatcher.dispatchEvent(new ClientEve(ClientEve.DISCONNECT, this));
}

public void sendJSON (String event) {
	sendJSON(event, "");
}
public void sendJSON (String event, Object data) {
	if (connected) {
		refreshTimeout();
		//System.out.println("send json to "+ nick);
		if (socket!=null) socket.sendJSON(event, data);
	} //else System.out.println(nick + " not connected for send");
}
@Override	public String toString () {
	return "(class "+this.getClass().getSimpleName()+", ip:"+socket.sess.getRemoteAddress()+", nick: "+nick+")";
}

}