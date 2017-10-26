package nooni.sockets;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebSocketImplementation extends Socket {

@OnWebSocketConnect
public void onConnectSocket (Session session) {
	_onConnect(session);
}

@OnWebSocketClose
public void onCloseSocket (int statusCode, String reason) {
	_onClose(statusCode, reason);
}

@OnWebSocketMessage
public void onMessageSocket (String message) {
	_receiveMessage(message);
}

@OnWebSocketError
public void onErrorSocket (Throwable t) {
	_onError(t);
}

}