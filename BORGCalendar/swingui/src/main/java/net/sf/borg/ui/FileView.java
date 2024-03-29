/*
This file is part of BORG.

    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by Mike Berger
 */
/*
 * helpscrn.java
 *
 * Created on October 5, 2003, 8:55 AM
 */

package net.sf.borg.ui;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.IOException;

/**
 * dockable view that shows information from a file
 */
class FileView extends DockableView implements Module {

	private static final long serialVersionUID = 1L;
	private JEditorPane jEditorPane;
	private JScrollPane jScrollPane1;

	/** window title */
	private final String title_;
	private final String file_;
	
	private boolean isInitialized = false;

	/**
	 * constructor
	 * @param file file to read text or html from
	 * @param title window title
	 */
	FileView(String file, String title) {
		title_ = title;
		file_ = file;
	}
	
	@Override
	public void showView()
	{
		if( !isInitialized)
		{
			initComponents();
			try {
				jEditorPane.setPage("file:///" + file_);
			} catch (java.io.IOException e1) {
				Errmsg.getErrorHandler().errmsg(e1);
			}
			isInitialized = true;
		}
		
		super.showView();
	}

	@Override
	public String getFrameTitle() {
		return title_;
	}

	/**
	 * initialize the UI
	 */
	private void initComponents()
	{
		setLayout(new GridBagLayout());
		jScrollPane1 = new javax.swing.JScrollPane();
		jEditorPane = new javax.swing.JEditorPane();
		jEditorPane.setEditable(false);
		jScrollPane1.setViewportView(jEditorPane);
		add(jScrollPane1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH, 1.0, 1.0));

		JPanel bPanel = new JPanel();
		JButton reloadButton = new JButton(Resource.getResourceString("reload"));
		reloadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Document doc = jEditorPane.getDocument();
				doc.putProperty(Document.StreamDescriptionProperty, null);
				try {
					jEditorPane.setPage("file:///" + file_);
				} catch (IOException e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
			
		});
		bPanel.add(reloadButton);
		add(bPanel, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 0.0));
	}

	@Override
	public void refresh() {
	  // empty
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}
	
	@Override
	public void print()
	{
		try {
			jEditorPane.print();
		} catch (PrinterException e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public String getModuleName() {
		return title_;
	}

	@Override
	public void initialize(MultiView parent) {
		parent.addHelpMenuItem(null, getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				showView();
			}
		});
		
	}
	
	@Override
	public ViewType getViewType() {
		return null;
	}


}
