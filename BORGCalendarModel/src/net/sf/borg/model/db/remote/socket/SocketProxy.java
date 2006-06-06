package net.sf.borg.model.db.remote.socket;

import net.sf.borg.common.util.SocketClient;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;

public class SocketProxy implements IRemoteProxy {

	public SocketProxy(String url){}
	public String execute(String strXml, IRemoteProxyProvider provider)
			throws Exception {
		String newMsg = strXml.replace('\n',' ');
		return SocketClient.sendMsg("localhost", 2929, newMsg);

	}

}
