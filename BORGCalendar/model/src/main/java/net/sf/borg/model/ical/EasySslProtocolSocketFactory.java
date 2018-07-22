package net.sf.borg.model.ical;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;



public class EasySslProtocolSocketFactory implements SecureProtocolSocketFactory
{    
  
  /**
   * Constructor for EasySSLProtocolSocketFactory.
   */
  public EasySslProtocolSocketFactory()
  {
    super();
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
   */
  public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
    throws IOException, UnknownHostException
  {
    return SSLSocketFactory.getDefault().createSocket(host, port, clientHost, clientPort);
  }

  /**
   * Attempts to get a new socket connection to the given host within the given time limit.
   * 
   * <p>
   * To circumvent the limitations of older JREs that do not support connect timeout a  controller thread is executed.
   * The controller thread attempts to create a new socket  within the given limit of time. If socket constructor does
   * not return until the  timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
   * </p>
   *
   * @param host the host name/IP
   * @param port the port on the host
   * @param localAddress the local host name/IP to bind the socket to
   * @param localPort the port on the local machine
   * @param params {@link HttpConnectionParams Http connection parameters}
   *
   * @return Socket a new socket
   *
   * @throws IOException if an I/O error occurs while creating the socket
   * @throws UnknownHostException if the IP address of the host cannot be determined
   * @throws ConnectTimeoutException DOCUMENT ME!
   * @throws IllegalArgumentException DOCUMENT ME!
   */
  public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort,
    final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException
  {
    if (params == null)
    {
      throw new IllegalArgumentException("Parameters may not be null");
    }

    int timeout = params.getConnectionTimeout();

    if (timeout == 0)
    {
      return createSocket(host, port, localAddress, localPort);
    }
    else
    {
      // To be eventually deprecated when migrated to Java 1.4 or above
      return ControllerThreadSocketFactory.createSocket(this, host, port, localAddress, localPort, timeout);
    }
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
   */
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    return SSLSocketFactory.getDefault().createSocket(host, port);
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
   */
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
    throws IOException, UnknownHostException
  {
    return SSLSocketFactory.getDefault().createSocket(host, port, InetAddress.getLocalHost(), port);
  }

  public boolean equals(Object obj)
  {
    return ((obj != null) && obj.getClass().equals(EasySslProtocolSocketFactory.class));
  }

  public int hashCode()
  {
    return EasySslProtocolSocketFactory.class.hashCode();
  }

  

  
}