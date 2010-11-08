package net.sf.borg.common;

// This example is from the book _Java in a Nutshell_ by David Flanagan.
// modified by Mike Berger. No license appllies to this source file

import java.io.*;
import java.net.*;

/**
 * SocketClient sends text messages over a socket
 */
public class SocketClient {
		

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
				System.out.println("Connection closed by server.");
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

	
}
