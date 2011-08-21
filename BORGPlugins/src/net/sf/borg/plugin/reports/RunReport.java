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
package net.sf.borg.plugin.reports;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.entity.Project;
import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.EntitySelector;

/**
 * RunReport runs jasper reports
 * 
 */
public class RunReport {

	/**
	 * return true if Jasper reports is in the classpath
	 */
	public static boolean hasJasper()
	{
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			cl.loadClass("net.sf.jasperreports.engine.JasperPrint");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * create the Report Menu or return null if reports not supported
	 * @return the Report JMenu or null
	 */
	public static JMenu getReportMenu() {

		if( !hasJasper())
			return null;
		
		JMenu m = new JMenu();
		m.setText(Resource.getResourceString("reports"));

		JMenuItem prr = new JMenuItem();
		prr.setText(Resource.getResourceString("project_report"));
		prr.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					Project p = EntitySelector.selectProject();
					if (p == null)
						return;
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("pid", new Integer(p.getKey()));
					Collection<?> allChildren = TaskModel.getReference()
							.getAllSubProjects(p.getKey());
					Iterator<?> it = allChildren.iterator();
					for (int i = 2; i <= 10; i++) {
						if (!it.hasNext())
							break;
						Project sp = (Project) it.next();
						map.put("pid" + i, new Integer(sp.getKey()));
					}
					RunReport.runReport("proj", map);
				} catch (NoClassDefFoundError r) {
					Errmsg.notice(Resource.getResourceString("borg_jasp"));
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}

		});
		m.add(prr);
		
		JMenuItem gnt = new JMenuItem();
		gnt.setText(Resource.getResourceString("GANTT"));
		gnt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					Project p = EntitySelector.selectProject();
					if (p == null)
						return;
					GanttFrame.showChart(p);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}

		});
		m.add(gnt);

		JMenuItem otr = new JMenuItem();
		otr.setText(Resource.getResourceString("open_tasks"));
		otr.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RunReport.runReport("open_tasks", null);
			}

		});
		m.add(otr);

		JMenuItem otpr = new JMenuItem();
		otpr.setText(Resource.getResourceString("open_tasks_proj"));
		otpr.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RunReport.runReport("opentasksproj", null);
			}

		});
		m.add(otpr);

		JMenuItem customrpt = new JMenuItem();
		customrpt.setText(Resource.getResourceString("select_rpt"));
		customrpt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					InputStream is = IOHelper.fileOpen(".", Resource
							.getResourceString("select_rpt"));
					if (is == null)
						return;
					RunReport.runReport(is, null);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}

		});
		m.add(customrpt);
		return m;
	}

	/**
	 * Run a jasper report
	 * 
	 * @param is
	 *            the report file as an InputStream
	 * @param parmsIn
	 *            the input parameter map for the report
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void runReport(InputStream is, Map parmsIn) {
	  Map parms = parmsIn;
		if (parms == null)
			parms = new HashMap();
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			Class jprintclass = cl
					.loadClass("net.sf.jasperreports.engine.JasperPrint");
			Class jfillclass = cl
					.loadClass("net.sf.jasperreports.engine.JasperFillManager");
			Class jviewerclass = cl
					.loadClass("net.sf.jasperreports.view.JasperViewer");

			Connection conn = JdbcDB.getConnection();
			if (conn == null) {
				Errmsg.notice(Resource.getResourceString("no_reports"));
				return;
			}

			Method fr = jfillclass.getMethod("fillReport", new Class[] {
					InputStream.class, Map.class, Connection.class });
			Object jasperprint = fr.invoke(null,
					new Object[] { is, parms, conn });

			Method vr = jviewerclass.getMethod("viewReport", new Class[] {
					jprintclass, boolean.class });
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
	 * Run a jasper report by name. The report definition file needs to be
	 * present in the borg jar with name = /reports/<name>.jasper
	 * 
	 * @param name
	 *            the report name
	 * @param parms
	 *            the input parameter map
	 */
	public static void runReport(String name, Map<String,Integer> parms) {
		String resourcePath = "/reports/" + name + ".jasper";
		InputStream is = RunReport.class.getResourceAsStream(resourcePath);
		runReport(is, parms);
	}

}
