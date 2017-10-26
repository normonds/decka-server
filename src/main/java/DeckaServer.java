import decka.Decka;
import nooni.sockets.WebSocketImplementation;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class DeckaServer {

public static void main (String[] args) throws Exception {
	//System.out.println("WebSocketTest main");
	Decka decka = new Decka();
	decka.init();
	//Decka.init();
	Server server = new Server(8080);
	WebSocketHandler wsHandler = new WebSocketHandler () {
		@Override public void configure (WebSocketServletFactory factory) {
			factory.register(WebSocketImplementation.class);
		}
	};
	System.out.println("hello");
	//server.setHandler(wsHandler);
	//server.setHandler(new HTTPhandler());
	Handler http = new HTTPhandler();
	Handler errors = new ErrorHandle();
	HandlerCollection handlerCollection = new HandlerCollection();
	handlerCollection.setHandlers(new Handler[] {http, wsHandler, errors});
	server.setHandler(handlerCollection);
	
	WebSocketImplementation.server = server;
	WebSocketImplementation.startTime = System.currentTimeMillis();
	
	server.start();
	server.join();
}
}