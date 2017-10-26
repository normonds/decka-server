package decka;

import java.util.ArrayList;

import nooni.sockets.Client;

public class Room {
public static int count = 0;
public String name;
public static ArrayList<Room> rooms = new ArrayList<Room>();
public ArrayList<Client> clients;
//public static ArrayList<Client> allClients = new ArrayList<Client>();
	
//public Room (String name_) {
//	count++;
//	name = name_;
//	rooms.add(this);
//	clients = new ArrayList<Client>();
//}
//public static Room getRoomFromClient (Client client) {
//	for (Room room : rooms) {
//		if (room.clients.indexOf(client)>-1) {
//			return room;
//		}
//	}
//	return null;
//}
//
//public static Boolean has () {
//	//for (Client client : clients) {if (clientInc.equals(client)) return true;}
//	return true;
//}
//public Boolean has (Client clientInc) {
//	for (Client client : clients) {if (clientInc.equals(client)) return true;}
//	return false;
//}
//public void add (Client client) {clients.add(client);}
//public Boolean remove (Client client) {return clients.remove(client);}
//
//public void broadcastJSON (String event) {
//	broadcastJSON(event, "");
//}
//public void broadcastJSON (String event, Object data) {
//	for (Client client : clients) {
//		client.sendJSON(event, data);
//	}
//}
//public void broadcast (String event, Object data1, Object data2) {
//	ArrayList<Object> ret = new ArrayList<Object>();
//	ret.add(data1);
//	ret.add(data2);
//	broadcastJSON(event, ret);
//}

}