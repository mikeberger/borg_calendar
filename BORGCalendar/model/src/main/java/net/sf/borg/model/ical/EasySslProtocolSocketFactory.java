package net.sf.borg.model.ical;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;


public class EasySslProtocolSocketFactory implements SecureProtocolSocketFactory
{    
  /** Log object for this class. */
	static private final Logger LOG = Logger.getLogger("net.sf.borg");

private SSLContext sslcontext = null;

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
    return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
  }

  
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
    return getSSLContext().getSocketFactory().createSocket(host, port);
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
   */
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
    throws IOException, UnknownHostException
  {
    return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
  }

  public boolean equals(Object obj)
  {
    return ((obj != null) && obj.getClass().equals(EasySslProtocolSocketFactory.class));
  }

  public int hashCode()
  {
    return EasySslProtocolSocketFactory.class.hashCode();
  }

  private static SSLContext createEasySSLContext()
  {
    try
    {
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(null, new TrustManager[] { new EasyX509TrustManager(null) }, null);

      return context;
    }
    catch (Exception e)
    {
      LOG.severe(e.getMessage());
      throw new HttpClientError(e.toString());
    }
  }

  private SSLContext getSSLContext()
  {
    if (this.sslcontext == null)
    {
      this.sslcontext = createEasySSLContext();
    }

    return this.sslcontext;
  }
}