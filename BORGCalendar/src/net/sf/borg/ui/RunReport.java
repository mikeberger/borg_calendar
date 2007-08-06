package net.sf.borg.ui;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.jdbc.JdbcDB;

public class RunReport {

    /**
         * @param args
         */
    public static void main(String[] args) throws Exception {

	runReport("customer", new HashMap());

    }

    public static void runReport(String name, Map parms) {	
	    String resourcePath = "/reports/" + name + ".jasper";
	    InputStream is = RunReport.class.getResourceAsStream(resourcePath);
	    runReport(is,parms);	
    }

    
    public static void runReport(InputStream is, Map parms) {

	if (parms == null)
	    parms = new HashMap();
	try {
	    ClassLoader cl = ClassLoader.getSystemClassLoader();
	    Class jprintclass = cl.loadClass("net.sf.jasperreports.engine.JasperPrint");
	    Class jfillclass = cl.loadClass("net.sf.jasperreports.engine.JasperFillManager");
	    Class jviewerclass = cl.loadClass("net.sf.jasperreports.view.JasperViewer");
	    
	    Connection conn = JdbcDB.getConnection();
	    if (conn == null) {
		Errmsg.notice(Resource.getPlainResourceString("no_reports"));
		return;
	    }
	    
	    Method fr = jfillclass.getMethod("fillReport", new Class[] {
		    InputStream.class, Map.class, Connection.class });
	    Object jasperprint = fr.invoke(null,
		    new Object[] { is, parms, conn });
	    // JasperPrint jasperPrint = JasperFillManager.fillReport(is,
                // parms, conn);
	    Method vr = jviewerclass.getMethod("viewReport", new Class[] {
		    jprintclass, boolean.class });
	    vr.invoke(null, new Object[] { jasperprint, new Boolean(false) });

	    // JasperViewer.viewReport(jasperPrint,false);
	    // conn.close();
	} catch (ClassNotFoundException cnf)
	{
	    Errmsg.notice(Resource.getPlainResourceString("borg_jasp"));   
	} catch (NoClassDefFoundError r) {
	    Errmsg.notice(Resource.getPlainResourceString("borg_jasp"));
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }

}
