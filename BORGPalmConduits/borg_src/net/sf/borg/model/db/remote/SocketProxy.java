package net.sf.borg.model.db.remote;

import net.sf.borg.common.J13Helper;
import net.sf.borg.common.SocketClient;

public class SocketProxy {

	static private int port_;
	
	static public String execute(String strXml)
			throws Exception {
		String newMsg = J13Helper.replace(strXml,"\n","%NEWLINE%");
		//System.out.println("[REQUEST] "+newMsg);
		String resp = SocketClient.sendMsg("localhost", port_, newMsg);
		//System.out.println("[RESPONSE] "+resp);
		return J13Helper.replace(resp, "%NEWLINE%", "\n");

	}

	public static void setPort(int port) {
		SocketProxy.port_ = port;
	}

}
