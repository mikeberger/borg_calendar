/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.jdbc.JdbcDB;

/**
 * RunReport runs jasper reports
 *
 */
public class RunReport {

	/**
	 * Run a jasper report
	 * @param is the report file as an InputStream
	 * @param parms the input parameter map for the report
	 */
    @SuppressWarnings("unchecked")
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
                Errmsg.notice(Resource.getResourceString("no_reports"));
                return;
            }

            Method fr = jfillclass.getMethod("fillReport", new Class[] { InputStream.class, Map.class, Connection.class });
            Object jasperprint = fr.invoke(null, new Object[] { is, parms, conn });
         
            Method vr = jviewerclass.getMethod("viewReport", new Class[] { jprintclass, boolean.class });
            vr.invoke(null, new Object[] { jasperprint, new Boolean(false) });

       
        } catch (ClassNotFoundException cnf) {
            Errmsg.notice(Resource.getResourceString("borg_jasp"));
        } catch (NoClassDefFoundError r) {
            Errmsg.notice(Resource.getResourceString("borg_jasp"));
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    /**
     * Run a jasper report by name. The report definition file needs to be present in the borg jar
     * with name = /reports/<name>.jasper
     * @param name the report name
     * @param parms the input parameter map
     */
    @SuppressWarnings("unchecked")
    public static void runReport(String name, Map parms) {
        String resourcePath = "/reports/" + name + ".jasper";
        InputStream is = RunReport.class.getResourceAsStream(resourcePath);
        runReport(is, parms);
    }

}
