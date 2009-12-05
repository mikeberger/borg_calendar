/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.common;
// This example is from the book _Java in a Nutshell_ by David Flanagan.
// modified by Mike Berger - no license applies to this source file
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SocketServer is a thread that listens on a socket and starts a thread for
 * each incoming connection. Each connection thread calls back to the SocketHandler
 * to process each incoming message
 */
public class SocketServer extends Thread {
    
	// This class is the thread that handles all communication with a client
	private class Connection extends Thread {
	    protected Socket client;
	    protected BufferedReader in;
	    protected PrintStream out;
	    private SocketHandler handler_1;

	    // Initialize the streams and start the thread
	    public Connection(Socket client_socket, SocketHandler handler) {
	        client = client_socket;
	        handler_1 = handler;
	        try { 
	            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	            out = new PrintStream(client.getOutputStream());
	        }
	        catch (IOException e) {
	            try { client.close(); } catch (IOException e2) {  /* empty */}
	            System.err.println("Exception while getting socket streams: " + e);
	            return;
	        }
	        this.start();
	    }
	    
	    // Provide the service.
	    // Read a line, reverse it, send it back.  
	    @Override
	    public void run() {
	       
	        try {
	            for(;;) {
	                // read in a line
	                String line = in.readLine();
	                if (line == null) break;
	                String output = handler_1.processMessage(line);
	                out.println(output);
	            }
	        }
	        catch (IOException e) {  /* empty */ }
	        finally { try {client.close();} catch (IOException e2) {  /* empty */ } }
	    }
	}
	
    protected ServerSocket listen_socket;
    
    /** the socket handler that will be called for each incoming message */
    private SocketHandler handler_;
    
    private static void fail(Exception e, String msg) {
        System.err.println(msg + ": " +  e);
    }
    
    /**
     * Create a ServerSocket to listen for connections on;  start the thread.
     * 
     * @param port the port
     * @param handler the handler to call back with messages
     */
    public SocketServer(int port, SocketHandler handler) {
        this.handler_ = handler;
        try { listen_socket = new ServerSocket(port); }
        catch (IOException e) { fail(e, "Exception creating server socket"); }
        System.out.println("Server: listening on port " + port);
        this.start();
    }
    
    // The body of the server thread.  Loop forever, listening for and
    // accepting connections from clients.  For each connection, 
    // create a Connection object to handle communication through the
    // new Socket.
    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while(true) {
                Socket client_socket = listen_socket.accept();
                new Connection(client_socket, handler_);
            }
        }
        catch (IOException e) { 
            fail(e, "Exception while listening for connections");
        }
    }
    
}


