package decka;

import nooni.sockets.Client;

public class PlayerBot extends DeckaPlayer {
	
public PlayerBot (Client clientRef) {
	super(clientRef);
	isBot = true;
}

}