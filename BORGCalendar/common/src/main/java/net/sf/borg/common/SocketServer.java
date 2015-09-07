package net.sf.borg.common;
// This example is from the book _Java in a Nutshell_ by David Flanagan.
// modified by Mike Berger - no license applies to this source file
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * SocketServer is a thread that listens on a socket and starts a thread for
 * each incoming connection. Each connection thread calls back to the SocketHandler
 * to process each incoming message
 */

public class SocketServer extends Thread {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");

    
	// This class is the thread that handles all communication with a client
	static private class Connection extends Thread {
	    protected Socket client;
	    protected BufferedReader in;
	    protected PrintStream out;
	    private SocketHandler handler_1;

	    // Initialize the streams and start the thread
	    public Connection(Socket client_socket, SocketHandler handler) {
	        this.client = client_socket;
	        this.handler_1 = handler;
	        try { 
	            this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
	            this.out = new PrintStream(this.client.getOutputStream());
	        }
	        catch (IOException e) {
	            try { this.client.close(); } catch (IOException e2) {  /* empty */}
	            log.severe("Exception while getting socket streams: " + e);
	            return;
	        }
	        this.setName("Socket Connection");
	        this.start();
	    }
	    
	    // Provide the service.
	    // Read a line, reverse it, send it back.  
	    @Override
	    public void run() {
	       
	        try {
	            for(;;) {
	                // read in a line
	                String line = this.in.readLine();
	                if (line == null) break;
	                String output = this.handler_1.processMessage(line);
	                this.out.println(output);
	            }
	        }
	        catch (IOException e) {  /* empty */ }
	        finally { try {this.client.close();} catch (IOException e2) {  /* empty */ } }
	    }
	}
	
    protected ServerSocket listen_socket;
    
    /** the socket handler that will be called for each incoming message */
    private SocketHandler handler_;
    
    private static void fail(Exception e, String msg) {
        log.severe(msg + ": " +  e);
    }
    
    /**
     * Create a ServerSocket to listen for connections on;  start the thread.
     * 
     * @param port the port
     * @param handler the handler to call back with messages
     */
    public SocketServer(int port, SocketHandler handler) {
        this.handler_ = handler;
        try { this.listen_socket = new ServerSocket(port); }
        catch (IOException e) { fail(e, "Exception creating server socket"); }
        log.info("Server: listening on port " + port);
        this.setName("Socket Server");
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
                Socket client_socket = this.listen_socket.accept();
                new Connection(client_socket, this.handler_);
            }
        }
        catch (IOException e) { 
            fail(e, "Exception while listening for connections");
        }
    }
    
}


