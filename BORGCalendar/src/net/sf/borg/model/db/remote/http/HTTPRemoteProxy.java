package net.sf.borg.model.db.remote.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.sf.borg.common.Warning;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;




public class HTTPRemoteProxy implements IRemoteProxy
{
	static {
		ProtocolSocketFactory psf = new EasySSLProtocolSocketFactory();
		Protocol.registerProtocol("https", 
				new Protocol("https", psf, 8443));
	}
	
	public HTTPRemoteProxy(String url)
	{
		this.url = url;

        httpclient = new HttpClient();

        HttpClientParams params = httpclient.getParams(); 
        params.setParameter(CredentialsProvider.PROVIDER, prompter);
        params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	}
	
	public String execute(String strXml, IRemoteProxyProvider provider)
			throws Exception
	{
        PostMethod post = new PostMethod(url);
        post.setRequestEntity(new StringRequestEntity(strXml));
        post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
        post.setDoAuthentication(true);

        if (httpState != null)
        	httpclient.setState(httpState);
        	// restore any previous state, which should include credentials
        
        prompter.init(provider);
        
        try {
            int result = httpclient.executeMethod(post);
            
            if (result != HttpStatus.SC_OK)
            	throw new Warning(HttpStatus.getStatusText(result));
            
            httpState = httpclient.getState();
            	// remember any cookies (i.e. session id) for subsequent
            	// invocations
            
            InputStream istr = post.getResponseBodyAsStream();
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            
            try
            {
	            int n;
	            while ((n = istr.read()) != -1)
	            	ostr.write(n);
            }
            finally
            {
            	istr.close();
            	ostr.close();
            }
            
            return new String(ostr.toByteArray());
            /*
            return post.getResponseBodyAsString();
            */
        }
        finally {
            post.releaseConnection();
        }
	}
	
	// private //
	private String url;
	private HttpClient httpclient;
	private HttpState httpState;
	private AuthPrompter prompter = new AuthPrompter();
	
	/////////////////////////////////////////////////////////////////
	// inner class AuthPrompter

    private class AuthPrompter implements CredentialsProvider
    {
        // CredendialsProvider overrides
        public Credentials getCredentials(
            final AuthScheme authscheme, 
            final String host, 
            int port, 
            boolean proxy)
            throws CredentialsNotAvailableException 
        {
            if (authscheme == null)
                return null;
            
            // If we've got cached credentials and our current invocation
            // count is positive, it means that our previous login attempt
            // failed and that our credentials are bad. Nuke them.
            if (invokeCount>0)
            	creds = null;
            
            if( invokeCount > 2 )
            	throw new CredentialsNotAvailableException();
            
            ++invokeCount;
            
            if (creds == null)
            	creds = proxyProvider.getCredentials();
            
            return new UsernamePasswordCredentials(creds.getUsername(), creds
					.getPassword());    
        }
        
        // "internal" //
        AuthPrompter()
        {}
        
        final void init(IRemoteProxyProvider provider)
        {
        	this.proxyProvider = provider;
        	invokeCount = 0;
        }
        
        // private //
        private IRemoteProxyProvider proxyProvider;
        private int invokeCount;
        private IRemoteProxyProvider.Credentials creds;
    }
 
    // end inner class AuthPrompter
	/////////////////////////////////////////////////////////////////
}
