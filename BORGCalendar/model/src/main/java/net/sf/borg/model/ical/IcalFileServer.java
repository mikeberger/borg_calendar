package net.sf.borg.model.ical;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;

public class IcalFileServer {

	private static HttpServer server = null;
	

	public static void start() throws Exception {
		if (server != null)
			stop();
		
		HttpServer aServer = HttpServer.create(new InetSocketAddress(Prefs.getIntPref(PrefName.ICAL_PORT)), 0);
		aServer.createContext("/icals/borg.ics", new MyHandler());
		aServer.setExecutor(null);
		aServer.start();
		InetSocketAddress addr = aServer.getAddress();
		Errmsg.getErrorHandler().notice(Resource.getResourceString("server_started") + ": http://localhost:" + addr.getPort() + "/icals/borg.ics");
		server = aServer;
	}

	public static void stop() {
		if (server != null) {
			server.stop(0);
			server = null;
		}
	}

	public static void main(String[] args) throws Exception {
		start();
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			try {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -2);
				String response = ICal.exportIcalToString(cal.getTime());
				
				t.getResponseHeaders().add("Content-Disposition",
						"attachment; filename=borg.ics");
				byte[] bytes = response.getBytes();
				t.sendResponseHeaders(200, bytes.length);

				OutputStream os = t.getResponseBody();
				os.write(bytes);
				os.close();

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}

			
		}
	}

}