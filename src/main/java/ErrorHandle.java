import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.ErrorHandler;

/**
 * Custom Handler to serve error pages called by the HttpResponse.sendError method
 */
public class ErrorHandle extends ErrorHandler {
	
	/*@Override
	protected void writeErrorPage(HttpServletRequest request, Writer writer,
			int code, String message, boolean showStacks) throws IOException {
		// TODO Auto-generated method stub
		//response.setStatus(HttpServletResponse.SC_OK);
		//super.
		super.writeErrorPage(request., writer., HttpServletResponse.SC_NOT_FOUND, "msg", false);
	}*/
	@Override
	public void handle (String arg, org.eclipse.jetty.server.Request arg1, HttpServletRequest request, HttpServletResponse response) throws IOException {
		//response.setContentType("text/html;charset=utf-8");
        //response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        //response.getWriter().println("Not found");
	};
	/*@Override
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks)
            throws IOException {
        String uri = request.getRequestURI();
        
        
        //writeErrorPageHead(request, writer, HttpServletResponse.SC_NOT_FOUND, "page head");
        writeErrorPageMessage(request, writer, HttpServletResponse.SC_NOT_FOUND, "page message", uri);
        if (showStacks) {
            writeErrorPageStacks(request, writer);
        }
        writer.write("writer write");
    }*/
}