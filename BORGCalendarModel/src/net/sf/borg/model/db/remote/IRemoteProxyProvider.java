package net.sf.borg.model.db.remote;

public interface IRemoteProxyProvider
{
	
/////////////////////////////////////////////////////
// nested class Credentials

public static class Credentials
{
public Credentials(String username, String password)
{
	this.username = username;
	this.password = password;
}

public final String getUsername()	{return username;}
public final String getPassword()	{return password;}

// private //
private String username, password;
}

// end nested class Credentials
/////////////////////////////////////////////////////

public IRemoteProxy createProxy(String url);
public Credentials getCredentials();
}
