package net.sf.borg.ui;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class RunReport {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		runReport("customer", new HashMap());

	}
	
	public static void runReport(String name, Map parms) 
	{
	    	if( parms == null ) parms = new HashMap();
		try{
			String resourcePath = "/reports/" + name + ".jasper";
			//Class.forName( "com.mysql.jdbc.Driver" );
			Connection conn = JdbcDB.getConnection();
			if( conn == null )
			{
			    Errmsg.notice(Resource.getPlainResourceString("no_reports"));
			    return;
			}
			InputStream is = RunReport.class.getResourceAsStream(resourcePath);
			JasperPrint jasperPrint = JasperFillManager.fillReport(is, parms, conn);	 
			JasperViewer.viewReport(jasperPrint,false);
			conn.close();
		}
		catch(NoClassDefFoundError r)
		{
			Errmsg.notice(r.getMessage());
		}
		catch( Exception e)
		{
			Errmsg.errmsg(e);
		}
	}

}
