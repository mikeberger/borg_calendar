package net.sf.borg.common;

// This example is from the book _Java in a Nutshell_ by David Flanagan.
// modified by Mike Berger. No license appllies to this source file

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * SocketClient sends text messages over a socket
 */

public class SocketClient {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");


	/**
	 * Send a msg.
	 * 
	 * @param host the host
	 * @param port the port
	 * @param msg the msg
	 * 
	 * @return the response string
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String sendMsg(String host, int port, String msg) throws IOException {
		Socket s = null;
		String line = null;
		try {
			s = new Socket(host, port);
			BufferedReader sin = new BufferedReader(new InputStreamReader(s
					.getInputStream()));
			PrintStream sout = new PrintStream(s.getOutputStream());
			sout.println(msg);
			line = sin.readLine();
			// Check if connection is closed (i.e. for EOF)
			if (line == null) {
				log.info("Connection closed by server.");
			}
		} catch (IOException e) {
			if (s != null)
				s.close();
			throw e;
		}
		// Always be sure to close the socket
		finally {
			try {
				if (s != null)
					s.close();
			} catch (IOException e2) {
				// empty
			}
		}
		
		return line;
	}


	static public void sendMessage(String msg) {
		int port = Prefs.getIntPref(PrefName.SOCKETPORT);
		if (port != -1) {
			String resp;
			try {
				resp = sendMsg("localhost", port, msg);
				if (resp != null && resp.equals("ok")) {
					// do nothing
				}
			} catch (IOException e) {
				// empty
			}
	
		}
	}


	static public void sendLogMessage(String msg) {
		sendMessage("log:" + msg);
	}

	
}
