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

 * Copyright 2004 by Mohan Embar - http://www.thisiscool.com/

 */



package net.sf.borg.ui;



import java.awt.BorderLayout;

import java.awt.FlowLayout;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;

import java.io.InputStream;

import java.net.URL;



import javax.swing.JButton;

import javax.swing.JDialog;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;



import net.sf.borg.common.app.AppHelper;

import net.sf.borg.common.io.IOHelper;

import net.sf.borg.common.io.OSServicesHome;

import net.sf.borg.common.util.Errmsg;

import net.sf.borg.common.util.Prefs;

import net.sf.borg.common.util.Resource;



/**

 * Note: This GUI dialog isn't managed by NetBeans 

 * @author Mohan Embar

 */

public class MemDialog extends JDialog

{

	public MemDialog(JFrame parent)

	{

		super(parent, Resource.getResourceString("dlgMemoryFiles"), true);

		init();

	}

	

	public final void setMemento(String data)

	{

		txar.setText(data);

		txar.setSelectionStart(0);

		txar.setSelectionEnd(0);

	}



	public final String getMemento()

	{

		return txar.getText();

	}



	// private //

	private JTextArea txar;

	

	private void init()

	{

		JPanel pnlAll = new JPanel();

		pnlAll.setLayout(new BorderLayout());

		

		JLabel lbl = new JLabel(Resource.getResourceString("lblMemFiles"));

		pnlAll.add(lbl, BorderLayout.NORTH);



		JPanel pnlMain = new JPanel();

		pnlMain.setLayout(new BorderLayout());

		pnlAll.add(pnlMain, BorderLayout.CENTER);

		

		txar = new JTextArea(20,20);

		txar.setEditable(false);

		

		JScrollPane scrlpn = new JScrollPane();

		scrlpn.setViewportView(txar);

		pnlMain.add(scrlpn, BorderLayout.CENTER);

		

		JPanel pnlButtons = new JPanel();

		pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));

		pnlMain.add(pnlButtons, BorderLayout.SOUTH);

		

		JButton bn = new JButton(Resource.getResourceString("Load_From_File")+"...");

		if (!AppHelper.isApplet())

			pnlButtons.add(bn);

		bn.addActionListener

		(

			new ActionListener()

			{

				public void actionPerformed(ActionEvent e)

				{

					try

					{

						load();

					}

					catch (Exception ex)

					{

						Errmsg.errmsg(ex);

					}

				}

			}

		);



		bn = new JButton(Resource.getResourceString("Load_From_URL")+"...");

		pnlButtons.add(bn);

		bn.addActionListener

		(

			new ActionListener()

			{

				public void actionPerformed(ActionEvent e)

				{

					try

					{

						loadFromURL();

					}

					catch (Exception ex)

					{

						Errmsg.errmsg(ex);

					}

				}

			}

		);



		bn = new JButton(Resource.getResourceString("Save")+"...");

		if (!AppHelper.isApplet())

			pnlButtons.add(bn);

		bn.addActionListener

		(

			new ActionListener()

			{

				public void actionPerformed(ActionEvent e)

				{

					try

					{

						save();

					}

					catch (Exception ex)

					{

						Errmsg.errmsg(ex);

					}

				}

			}

		);



		bn = new JButton(Resource.getResourceString("Close"));

		pnlButtons.add(bn);

		bn.addActionListener

		(

			new ActionListener()

			{

				public void actionPerformed(ActionEvent e)

				{

					setVisible(false);

				}

			}

		);



		getContentPane().add(pnlAll);

		pack();

	}

	

	private void load() throws Exception

	{

		InputStream istr =

			OSServicesHome

				.getInstance()

				.getServices()

				.fileOpen(".", "borg_mem.dat");

		loadCommon(istr);

	}

	

	private void loadFromURL() throws Exception

	{

		String prevurl = Prefs.getPref("lastImpUrlDat", "");

		String urlst =

			JOptionPane.showInputDialog(

				Resource.getResourceString("enturl"),

				prevurl);

		if( urlst == null || urlst.length()==0 ) return;

		Prefs.putPref("lastImpUrlDat", urlst);

		URL url = new URL(urlst);

		loadCommon(IOHelper.openStream(url));

	}

				

	private void loadCommon(InputStream istr) throws Exception

	{

		String data = IOHelper.loadMemoryFromStream(istr);

		setMemento(data);

	}



	private void save() throws Exception

	{

		OSServicesHome

			.getInstance()

			.getServices()

			.fileSave

			(

				".",

				new ByteArrayInputStream(txar.getText().getBytes()),

				"borg_mem.dat"

			);

	}

}

