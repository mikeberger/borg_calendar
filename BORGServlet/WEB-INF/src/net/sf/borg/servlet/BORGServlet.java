package net.sf.borg.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.borg.model.db.remote.server.BorgHandler;

public class BORGServlet extends HttpServlet
{
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		synchronized (LOCK)
		{
			if (handler == null)
			{
				String url = config.getInitParameter("dburl");
				handler = new BorgHandler(url);
			}
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException 
	{
		resp.setContentType("text/plain");
		resp.getWriter().print("Try doing a POST, not a GET.");
		resp.getWriter().flush();
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException 
	{
	    // Read the string request off of the stream.
		BufferedReader rdr = req.getReader();
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		int nch;
		while ((nch = rdr.read()) != -1)
		    ostr.write(nch);
		ostr.close();
		String request = new String(ostr.toByteArray());
		
		// Run it through our handler, getting the XML reply.
		String replyString = null;
		synchronized (handler)
		{
			replyString = handler.execute(request);
		}

		// Pump this back to the client.
		resp.setContentType("text/xml");
		resp.getWriter().print(replyString);
		resp.getWriter().flush();
	}
	
	// private //
	private static Object LOCK = new Object();
	private static BorgHandler handler = null;
}
