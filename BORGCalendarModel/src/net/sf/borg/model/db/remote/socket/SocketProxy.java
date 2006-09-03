package net.sf.borg.model.db.remote.socket;

import net.sf.borg.common.util.J13Helper;
import net.sf.borg.common.util.SocketClient;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;

public class SocketProxy implements IRemoteProxy {

	private int port_;
	public SocketProxy(String url, int port){ port_ = port; }
	public String execute(String strXml, IRemoteProxyProvider provider)
			throws Exception {
		String newMsg = J13Helper.replace(strXml,"\n","%NEWLINE%");
		//System.out.println("[REQUEST] "+newMsg);
		String resp = SocketClient.sendMsg("localhost", port_, newMsg);
		//System.out.println("[RESPONSE] "+resp);
		return J13Helper.replace(resp, "%NEWLINE%", "\n");

	}

}
