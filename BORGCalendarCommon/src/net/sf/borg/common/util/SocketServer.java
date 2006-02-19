package net.sf.borg.common.util;
// This example is from the book _Java in a Nutshell_ by David Flanagan.
// modified by Mike Berger - no license applies to this source file
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread {
    protected int port;
    protected ServerSocket listen_socket;
    private SocketHandler handler_;
    
    // Exit with an error message, when an exception occurs.
    private static void fail(Exception e, String msg) {
        System.err.println(msg + ": " +  e);
        //System.exit(1);
    }
    
    // Create a ServerSocket to listen for connections on;  start the thread.
    public SocketServer(int port, SocketHandler handler) {
        this.port = port;
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
    
    // Start the server up, listening on an optionally specified port
    /*public static void main(String[] args) {
        int port = 0;
        if (args.length == 1) {
            try { port = Integer.parseInt(args[0]);  }
            catch (NumberFormatException e) { port = 0; }
        }
        new SocketServer(port);
    }*/
}

// This class is the thread that handles all communication with a client
class Connection extends Thread {
    protected Socket client;
    protected BufferedReader in;
    protected PrintStream out;
    private SocketHandler handler_;

    // Initialize the streams and start the thread
    public Connection(Socket client_socket, SocketHandler handler) {
        client = client_socket;
        handler_ = handler;
        try { 
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintStream(client.getOutputStream());
        }
        catch (IOException e) {
            try { client.close(); } catch (IOException e2) { ; }
            System.err.println("Exception while getting socket streams: " + e);
            return;
        }
        this.start();
    }
    
    // Provide the service.
    // Read a line, reverse it, send it back.  
    public void run() {
       
        try {
            for(;;) {
                // read in a line
                String line = in.readLine();
                if (line == null) break;
                String output = handler_.processMessage(line);
                out.println(output);
            }
        }
        catch (IOException e) { ; }
        finally { try {client.close();} catch (IOException e2) {;} }
    }
}
