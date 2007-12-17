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
package net.sf.borg.ui.address;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Date;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.beans.Address;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.link.LinkPanel;

import com.toedter.calendar.JDateChooser;

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

public class AddressView extends DockableView {

	private Address addr_;

	private LinkPanel attPanel;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JDateChooser bdchooser;

	private javax.swing.JTextField cntext;

	private javax.swing.JTextField cntext1;

	private javax.swing.JTextField comptext;

	private javax.swing.JTextField cttext;

	// save a task

	private javax.swing.JTextField cttext1;

	private javax.swing.JTextField emtext;

	private javax.swing.JTextField fntext;

	private javax.swing.JTextField fxtext;

	private javax.swing.JTextField hptext;

	private javax.swing.JButton jButton2;

	private javax.swing.JLabel jLabel1;

	private javax.swing.JLabel jLabel10;

	private javax.swing.JLabel jLabel11;

	private javax.swing.JLabel jLabel12;

	private javax.swing.JLabel jLabel13;

	private javax.swing.JLabel jLabel14;

	private javax.swing.JLabel jLabel15;

	private javax.swing.JLabel jLabel16;

	private javax.swing.JLabel jLabel17;

	private javax.swing.JLabel jLabel18;

	private javax.swing.JLabel jLabel19;

	private javax.swing.JLabel jLabel2;

	private javax.swing.JLabel jLabel20;

	private javax.swing.JLabel jLabel21;

	private javax.swing.JLabel jLabel22;

	private javax.swing.JLabel jLabel3;

	private javax.swing.JLabel jLabel4;

	private javax.swing.JLabel jLabel5;

	private javax.swing.JLabel jLabel6;

	private javax.swing.JLabel jLabel7;

	private javax.swing.JLabel jLabel8;

	private javax.swing.JLabel jLabel9;

	private javax.swing.JPanel jPanel1;

	private javax.swing.JPanel jPanel2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	private javax.swing.JPanel jPanel5;

	private JPanel jPanel6 = null;

	private javax.swing.JTabbedPane jTabbedPane1; // @jve:decl-index=0:visual-constraint="57,1330"

	private javax.swing.JTextField lntext;

	private javax.swing.JTextField nntext;

	private javax.swing.JTextArea notestext;

	private javax.swing.JTextField pgtext;

	private javax.swing.JTextField satext;

	private javax.swing.JTextField satext1;

	private javax.swing.JTextField sntext;

	private javax.swing.JTextField sttext;

	private javax.swing.JTextField sttext1;

	private javax.swing.JTextField wbtext;

	private javax.swing.JTextField wptext;

	private javax.swing.JTextField zctext;

	private javax.swing.JTextField zctext1;

	public AddressView(Address addr) {
		super();
		addr_ = addr;
		addModel(AddressModel.getReference());

		initComponents(); // init the GUI widgets

		// display the window
		showaddr();

	}

	public PrefName getFrameSizePref() {
		return PrefName.ADDRVIEWSIZE;
	}

	public String getFrameTitle() {
		return Resource.getPlainResourceString("Address_Book_Entry");
	}

	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			GridBagConstraints gridBagConstraints56 = new GridBagConstraints();
			jPanel6 = new JPanel();
			jPanel6.setLayout(new GridBagLayout());
			gridBagConstraints56.gridx = 1;
			gridBagConstraints56.gridy = 0;
			gridBagConstraints56.gridwidth = 0;
			gridBagConstraints56.weightx = 1.0;
			gridBagConstraints56.weighty = 1.0;
			gridBagConstraints56.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints56.insets = new java.awt.Insets(4, 4, 4, 4);
			jPanel6.add(notestext, gridBagConstraints56);
		}
		return jPanel6;
	}

	// End of variables declaration//GEN-END:variables

	public JMenuBar getMenuForFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{

		jTabbedPane1 = new javax.swing.JTabbedPane();
		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		fntext = new javax.swing.JTextField();
		lntext = new javax.swing.JTextField();
		nntext = new javax.swing.JTextField();
		sntext = new javax.swing.JTextField();
		hptext = new javax.swing.JTextField();
		wptext = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();
		pgtext = new javax.swing.JTextField();
		jLabel8 = new javax.swing.JLabel();
		fxtext = new javax.swing.JTextField();
		jLabel9 = new javax.swing.JLabel();
		emtext = new javax.swing.JTextField();
		jLabel14 = new javax.swing.JLabel();
		wbtext = new javax.swing.JTextField();
		jLabel21 = new javax.swing.JLabel();
		comptext = new javax.swing.JTextField();
		jLabel22 = new javax.swing.JLabel();
		bdchooser = new JDateChooser();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jLabel10 = new javax.swing.JLabel();
		satext = new javax.swing.JTextField();
		jLabel11 = new javax.swing.JLabel();
		cttext = new javax.swing.JTextField();
		jLabel12 = new javax.swing.JLabel();
		sttext = new javax.swing.JTextField();
		jLabel13 = new javax.swing.JLabel();
		cntext = new javax.swing.JTextField();
		jLabel15 = new javax.swing.JLabel();
		zctext = new javax.swing.JTextField();
		jPanel5 = new javax.swing.JPanel();
		jLabel16 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		jLabel18 = new javax.swing.JLabel();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		zctext1 = new javax.swing.JTextField();
		cntext1 = new javax.swing.JTextField();
		sttext1 = new javax.swing.JTextField();
		cttext1 = new javax.swing.JTextField();
		satext1 = new javax.swing.JTextField();
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints29a = new GridBagConstraints();
		GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints43 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints46 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints47 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints49 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints50 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints52 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints53 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints54 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints55 = new GridBagConstraints();
		notestext = new javax.swing.JTextArea();
		jPanel4 = new javax.swing.JPanel();
		jButton2 = new javax.swing.JButton();

		jTabbedPane1.setPreferredSize(new java.awt.Dimension(540, 400));
		jPanel1.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(jLabel1, "First_Name:");
		jLabel1.setLabelFor(fntext);

		ResourceHelper.setText(jLabel2, "Last_Name:");
		jLabel2.setLabelFor(lntext);

		ResourceHelper.setText(jLabel3, "Nickname:");
		jLabel3.setLabelFor(nntext);

		ResourceHelper.setText(jLabel4, "Screen_Name:");
		jLabel4.setLabelFor(sntext);

		ResourceHelper.setText(jLabel5, "Home_Phone:");
		jLabel5.setLabelFor(hptext);

		ResourceHelper.setText(jLabel6, "Work_Phone:");
		jLabel6.setLabelFor(wptext);

		ResourceHelper.setText(jLabel7, "Pager:");
		jLabel7.setLabelFor(pgtext);

		ResourceHelper.setText(jLabel8, "Fax:");
		jLabel8.setLabelFor(fxtext);

		ResourceHelper.setText(jLabel9, "Email:");
		jLabel9.setLabelFor(emtext);

		ResourceHelper.setText(jLabel14, "Web_Page:");
		jLabel14.setLabelFor(wbtext);

		ResourceHelper.setText(jLabel21, "Company");
		jLabel21.setLabelFor(comptext);

		ResourceHelper.setText(jLabel22, "Birthday");
		jLabel22.setLabelFor(bdchooser);

		jTabbedPane1.addTab(Resource.getResourceString("contact"), jPanel1);

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("HomeAddress")));
		ResourceHelper.setText(jLabel10, "Home_Street_Address");
		jLabel10.setLabelFor(satext);

		ResourceHelper.setText(jLabel11, "Home_City:");
		jLabel11.setLabelFor(cttext);

		ResourceHelper.setText(jLabel12, "Home_State:");
		jLabel12.setLabelFor(sttext1);

		ResourceHelper.setText(jLabel13, "Home_Country:");
		jLabel13.setLabelFor(cntext1);

		ResourceHelper.setText(jLabel15, "Home_Zip_Code:");
		jLabel15.setLabelFor(zctext1);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		jPanel5.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("WorkAddress")));
		ResourceHelper.setText(jLabel16, "Work_Street_Address");
		jLabel16.setLabelFor(satext1);

		jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(jLabel17, "Work_City:");
		jLabel17.setLabelFor(cttext1);

		jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(jLabel18, "Work_State:");
		jLabel18.setLabelFor(sttext);

		jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(jLabel19, "Work_Zip_Code:");
		jLabel19.setLabelFor(zctext);

		jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(jLabel20, "Work_Country:");
		jLabel20.setLabelFor(cntext);

		jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		satext1.setMinimumSize(new java.awt.Dimension(4, 50));

		jTabbedPane1.addTab(Resource.getResourceString("Address"), jPanel2);

		jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(jButton2, "Save");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jPanel4.add(jButton2);

		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.gridy = 0;
		gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints8.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.gridy = 1;
		gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints10.gridx = 0;
		gridBagConstraints10.gridy = 2;
		gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 3;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 4;
		gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints13.gridx = 0;
		gridBagConstraints13.gridy = 5;
		gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints13.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints14.gridx = 0;
		gridBagConstraints14.gridy = 6;
		gridBagConstraints14.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints14.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints15.gridx = 0;
		gridBagConstraints15.gridy = 7;
		gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints15.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints16.gridx = 0;
		gridBagConstraints16.gridy = 8;
		gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints16.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints17.gridx = 0;
		gridBagConstraints17.gridy = 9;
		gridBagConstraints17.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints17.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints18.gridx = 0;
		gridBagConstraints18.gridy = 10;
		gridBagConstraints18.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints18.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints19.gridx = 0;
		gridBagConstraints19.gridy = 11;
		gridBagConstraints19.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints19.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints20.gridx = 1;
		gridBagConstraints20.gridy = 0;
		gridBagConstraints20.weightx = 1.0;
		gridBagConstraints20.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints21.gridx = 1;
		gridBagConstraints21.gridy = 9;
		gridBagConstraints21.weightx = 1.0;
		gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints22.gridx = 1;
		gridBagConstraints22.gridy = 6;
		gridBagConstraints22.weightx = 1.0;
		gridBagConstraints22.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints23.gridx = 1;
		gridBagConstraints23.gridy = 10;
		gridBagConstraints23.weightx = 1.0;
		gridBagConstraints23.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints24.gridx = 1;
		gridBagConstraints24.gridy = 1;
		gridBagConstraints24.weightx = 1.0;
		gridBagConstraints24.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints25.gridx = 1;
		gridBagConstraints25.gridy = 2;
		gridBagConstraints25.weightx = 1.0;
		gridBagConstraints25.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints26.gridx = 1;
		gridBagConstraints26.gridy = 5;
		gridBagConstraints26.weightx = 1.0;
		gridBagConstraints26.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints27.gridx = 1;
		gridBagConstraints27.gridy = 7;
		gridBagConstraints27.weightx = 1.0;
		gridBagConstraints27.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints28.gridx = 1;
		gridBagConstraints28.gridy = 4;
		gridBagConstraints28.weightx = 1.0;
		gridBagConstraints28.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints29.gridx = 1;
		gridBagConstraints29.gridy = 11;
		gridBagConstraints29.weightx = 1.0;
		gridBagConstraints29.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints29a.gridx = 2;
		gridBagConstraints29a.gridy = 11;
		gridBagConstraints29a.weightx = 0.06;
		gridBagConstraints29a.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints30.gridx = 1;
		gridBagConstraints30.gridy = 8;
		gridBagConstraints30.weightx = 1.0;
		gridBagConstraints30.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints31.gridx = 1;
		gridBagConstraints31.gridy = 3;
		gridBagConstraints31.weightx = 1.0;
		gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints32.gridx = 0;
		gridBagConstraints32.gridy = 1;
		gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints32.weightx = 1.0D;
		gridBagConstraints32.weighty = 1.0D;
		gridBagConstraints33.gridx = 0;
		gridBagConstraints33.gridy = 0;
		gridBagConstraints33.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints33.weightx = 1.0D;
		gridBagConstraints33.weighty = 1.0D;
		gridBagConstraints34.gridx = 0;
		gridBagConstraints34.gridy = 0;
		gridBagConstraints34.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints35.gridx = 0;
		gridBagConstraints35.gridy = 1;
		gridBagConstraints35.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints36.gridx = 0;
		gridBagConstraints36.gridy = 2;
		gridBagConstraints36.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints36.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints37.gridx = 0;
		gridBagConstraints37.gridy = 3;
		gridBagConstraints37.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints38.gridx = 0;
		gridBagConstraints38.gridy = 4;
		gridBagConstraints38.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints39.gridx = 0;
		gridBagConstraints39.gridy = 0;
		gridBagConstraints39.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints40.gridx = 0;
		gridBagConstraints40.gridy = 1;
		gridBagConstraints40.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints41.gridx = 0;
		gridBagConstraints41.gridy = 2;
		gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints42.gridx = 0;
		gridBagConstraints42.gridy = 3;
		gridBagConstraints42.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints43.gridx = 0;
		gridBagConstraints43.gridy = 4;
		gridBagConstraints43.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints45.gridx = 1;
		gridBagConstraints45.gridy = 2;
		gridBagConstraints45.weightx = 1.0;
		gridBagConstraints45.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints46.gridx = 1;
		gridBagConstraints46.gridy = 1;
		gridBagConstraints46.weightx = 1.0;
		gridBagConstraints46.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints47.gridx = 1;
		gridBagConstraints47.gridy = 4;
		gridBagConstraints47.weightx = 1.0;
		gridBagConstraints47.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints49.gridx = 1;
		gridBagConstraints49.gridy = 2;
		gridBagConstraints49.weightx = 1.0;
		gridBagConstraints49.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints50.gridx = 1;
		gridBagConstraints50.gridy = 1;
		gridBagConstraints50.weightx = 1.0;
		gridBagConstraints50.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints51.gridx = 1;
		gridBagConstraints51.gridy = 3;
		gridBagConstraints51.weightx = 1.0;
		gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints52.gridx = 1;
		gridBagConstraints52.gridy = 3;
		gridBagConstraints52.weightx = 1.0;
		gridBagConstraints52.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints53.gridx = 1;
		gridBagConstraints53.gridy = 4;
		gridBagConstraints53.weightx = 1.0;
		gridBagConstraints53.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints54.gridx = 1;
		gridBagConstraints54.gridy = 0;
		gridBagConstraints54.weightx = 1.0;
		gridBagConstraints54.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints55.gridx = 1;
		gridBagConstraints55.gridy = 0;
		gridBagConstraints55.weightx = 1.0;
		gridBagConstraints55.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jPanel1.add(jLabel1, gridBagConstraints8);
		jPanel2.add(jPanel5, gridBagConstraints32);
		jPanel3.add(jLabel16, gridBagConstraints34);
		jPanel5.add(jLabel10, gridBagConstraints39);

		jPanel1.add(jLabel2, gridBagConstraints9);
		jPanel2.add(jPanel3, gridBagConstraints33);
		jPanel3.add(jLabel17, gridBagConstraints35);
		jPanel5.add(jLabel11, gridBagConstraints40);
		jPanel1.add(jLabel3, gridBagConstraints10);
		jPanel1.add(jLabel4, gridBagConstraints11);
		jPanel5.add(jLabel18, gridBagConstraints41);
		jTabbedPane1.addTab(Resource.getResourceString("Notes"), getJPanel6());

		attPanel = new LinkPanel();
		jTabbedPane1
				.addTab(Resource.getResourceString("links"), attPanel);
		// jTabbedPane1.addTab(null, null, getJPanel6(), null);
		jPanel3.add(jLabel12, gridBagConstraints36);
		jPanel3.add(jLabel13, gridBagConstraints37);
		jPanel5.add(jLabel20, gridBagConstraints42);
		jPanel1.add(jLabel5, gridBagConstraints12);
		jPanel3.add(jLabel15, gridBagConstraints38);
		jPanel5.add(jLabel19, gridBagConstraints43);
		jPanel1.add(jLabel6, gridBagConstraints13);
		jPanel3.add(zctext1, gridBagConstraints47);
		jPanel5.add(sttext, gridBagConstraints45);
		jPanel1.add(jLabel7, gridBagConstraints14);
		jPanel3.add(sttext1, gridBagConstraints49);
		jPanel5.add(cttext, gridBagConstraints46);
		jPanel1.add(jLabel8, gridBagConstraints15);
		jPanel3.add(cttext1, gridBagConstraints50);
		jPanel5.add(cntext, gridBagConstraints51);
		jPanel1.add(jLabel9, gridBagConstraints16);
		jPanel3.add(cntext1, gridBagConstraints52);
		jPanel5.add(zctext, gridBagConstraints53);
		jPanel1.add(jLabel14, gridBagConstraints17);
		jPanel3.add(satext1, gridBagConstraints54);
		jPanel5.add(satext, gridBagConstraints55);
		jPanel1.add(jLabel21, gridBagConstraints18);
		jPanel1.add(jLabel22, gridBagConstraints19);
		jPanel1.add(fntext, gridBagConstraints20);
		jPanel1.add(wbtext, gridBagConstraints21);
		jPanel1.add(pgtext, gridBagConstraints22);
		jPanel1.add(comptext, gridBagConstraints23);
		jPanel1.add(lntext, gridBagConstraints24);
		jPanel1.add(nntext, gridBagConstraints25);
		jPanel1.add(wptext, gridBagConstraints26);
		jPanel1.add(fxtext, gridBagConstraints27);
		jPanel1.add(hptext, gridBagConstraints28);
		jPanel1.add(bdchooser, gridBagConstraints29);

		jPanel1.add(emtext, gridBagConstraints30);
		jPanel1.add(sntext, gridBagConstraints31);

		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		setLayout(new GridBagLayout());
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridy = 1;
		gridBagConstraints7.weightx = 1.0;
		gridBagConstraints7.weighty = 1.0;
		gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints7.gridx = 0;
		gridBagConstraints7.gridy = 0;
		add(jPanel4, gridBagConstraints6);
		add(jTabbedPane1, gridBagConstraints7);

	}// GEN-END:initComponents

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		saveaddr();
	}// GEN-LAST:event_jButton2ActionPerformed

	public void refresh() {
	}

	private void saveaddr() {
		if (fntext.getText().equals("") || lntext.getText().equals("")) {
			Errmsg.notice(Resource
					.getResourceString("First_and_Last_name_are_Required"));
			return;
		}

		addr_.setFirstName(fntext.getText());
		addr_.setLastName(lntext.getText());
		addr_.setNickname(nntext.getText());
		addr_.setEmail(emtext.getText());
		addr_.setScreenName(sntext.getText());
		addr_.setWorkPhone(wptext.getText());
		addr_.setHomePhone(hptext.getText());
		addr_.setFax(fxtext.getText());
		addr_.setPager(pgtext.getText());
		addr_.setWebPage(wbtext.getText());
		addr_.setNotes(notestext.getText());
		addr_.setStreetAddress(satext.getText());
		addr_.setCity(cttext.getText());
		addr_.setState(sttext.getText());
		addr_.setCountry(cntext.getText());
		addr_.setZip(zctext.getText());
		addr_.setWorkStreetAddress(satext1.getText());
		addr_.setWorkCity(cttext1.getText());
		addr_.setWorkState(sttext1.getText());
		addr_.setWorkCountry(cntext1.getText());
		addr_.setWorkZip(zctext1.getText());
		addr_.setCompany(comptext.getText());
		addr_.setBirthday(bdchooser.getDate());

		try {
			AddressModel.getReference().saveAddress(addr_);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	private void showaddr() {
		fntext.setText(addr_.getFirstName());
		lntext.setText(addr_.getLastName());
		nntext.setText(addr_.getNickname());
		emtext.setText(addr_.getEmail());
		sntext.setText(addr_.getScreenName());
		wptext.setText(addr_.getWorkPhone());
		hptext.setText(addr_.getHomePhone());
		fxtext.setText(addr_.getFax());
		pgtext.setText(addr_.getPager());
		wbtext.setText(addr_.getWebPage());
		notestext.setText(addr_.getNotes());
		satext.setText(addr_.getStreetAddress());
		cttext.setText(addr_.getCity());
		sttext.setText(addr_.getState());
		cntext.setText(addr_.getCountry());
		zctext.setText(addr_.getZip());
		satext1.setText(addr_.getWorkStreetAddress());
		cttext1.setText(addr_.getWorkCity());
		sttext1.setText(addr_.getWorkState());
		cntext1.setText(addr_.getWorkCountry());
		zctext1.setText(addr_.getWorkZip());
		comptext.setText(addr_.getCompany());

		Date bd = addr_.getBirthday();
		bdchooser.setDate(bd);
		
		attPanel.setOwner(addr_);

	}
}
