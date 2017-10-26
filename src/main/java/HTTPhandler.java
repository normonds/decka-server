import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nooni.sockets.Socket;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;


public class HTTPhandler implements Handler {

	@Override
	public void addLifeCycleListener(Listener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStarting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeLifeCycleListener(Listener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	@ManagedOperation(value = "Starts the instance", impact = "ACTION")
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	@ManagedOperation(value = "Stops the instance", impact = "ACTION")
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	@ManagedOperation(value = "destroy associated resources", impact = "ACTION")
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	@ManagedAttribute(value = "the jetty server for this handler", readonly = true)
	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handle (String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		String incDomain = request.getParameter("location");
		response.setHeader("Server", "Custom");
		if (incDomain==null) {
			//response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			
			//response.getWriter().println("<h1>ERROR 404</h1>");
			baseRequest.setHandled(true);
			return;
		}
		response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
		
		response.addHeader("Access-Control-Allow-Origin", "http://"+incDomain);
        response.getWriter().println(Socket.getServerIsOnline()?"1":"0");
	}

	@Override
	public void setServer(Server arg0) {
		// TODO Auto-generated method stub

	}

}
