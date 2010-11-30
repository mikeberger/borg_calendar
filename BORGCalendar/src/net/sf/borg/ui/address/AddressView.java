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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Address;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import com.toedter.calendar.JDateChooser;

/**
 * UI for editing a single address record.
 * 
 */
public class AddressView extends DockableView {

	private static final long serialVersionUID = 1L;

	// address being edited
	private Address addr_;

	// the address fields
	private JDateChooser birthdayChooser;
	private JTextField cityText;
	private JTextField companyText;
	private JTextField countryText;
	private JTextField emailText;
	private JTextField faxText;
	private JTextField firstNameText;
	private JTextField homePageText;
	private JTextField lastNameText;
	private JTextField nickNameText;
	private JTextArea notesText;
	private JTextField pagerText;
	private JTextField screenNameText;
	private JTextField stateText;
	private JTextField streetAddresText;
	private JTextField webPageText;
	private JTextField workCityText;
	private JTextField workCountryText;
	private JTextField workPhoneText;
	private JTextField workStateText;
	private JTextField workStreetAddressText;
	private JTextField workZipText;
	private JTextField zipCodeText;

	// link panel
	private LinkPanel linkPanel;

	// the main tabbed pane
	private JTabbedPane theTabbedPane; // @jve:decl-index=0:visual-constraint="57,1330"

	/**
	 * constructor
	 * 
	 * @param addr
	 *            address to load into the UI
	 */
	public AddressView(Address addr) {
		super();

		addr_ = addr;

		addModel(AddressModel.getReference());

		initComponents(); // init the GUI widgets

		// display the window
		showaddr();

	}

	@Override
	public String getFrameTitle() {
		return Resource.getResourceString("Address_Book_Entry");
	}

	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	/**
	 * initialize the UI
	 */
	// this method is generated code that was cleaned up. It is much better, but still, some stupid label names remain
	private void initComponents()
	{

		theTabbedPane = new JTabbedPane();
		JPanel contactPanel = new JPanel();
		firstNameText = new JTextField();
		lastNameText = new JTextField();
		nickNameText = new JTextField();
		screenNameText = new JTextField();
		homePageText = new JTextField();
		workPhoneText = new JTextField();
		pagerText = new JTextField();
		faxText = new JTextField();
		emailText = new JTextField();
		webPageText = new JTextField();
		companyText = new JTextField();
		birthdayChooser = new JDateChooser();
		JPanel addressPanel = new JPanel();
		JPanel homeAddressPanel = new JPanel();
		streetAddresText = new JTextField();
		cityText = new JTextField();
		stateText = new JTextField();
		countryText = new JTextField();
		zipCodeText = new JTextField();
		JPanel workAddressPanel = new JPanel();
		workZipText = new JTextField();
		workCountryText = new JTextField();
		workStateText = new JTextField();
		workCityText = new JTextField();
		workStreetAddressText = new JTextField();
		notesText = new JTextArea();
		JPanel buttonPanel = new JPanel();
		JButton saveButton = new JButton();
		
		setLayout(new GridBagLayout());


		//
		// CONTACT
		//
		contactPanel.setLayout(new java.awt.GridBagLayout());

		JLabel jLabel1 = new JLabel();
		ResourceHelper.setText(jLabel1, "First_Name:");
		jLabel1.setLabelFor(firstNameText);
		contactPanel.add(jLabel1, GridBagConstraintsFactory.create(0, 0));

		JLabel jLabel2 = new JLabel();
		ResourceHelper.setText(jLabel2, "Last_Name:");
		jLabel2.setLabelFor(lastNameText);
		contactPanel.add(jLabel2, GridBagConstraintsFactory.create(0, 1));

		JLabel jLabel3 = new JLabel();
		ResourceHelper.setText(jLabel3, "Nickname:");
		jLabel3.setLabelFor(nickNameText);
		contactPanel.add(jLabel3, GridBagConstraintsFactory.create(0, 2));

		JLabel jLabel4 = new JLabel();
		ResourceHelper.setText(jLabel4, "Screen_Name:");
		jLabel4.setLabelFor(screenNameText);
		contactPanel.add(jLabel4, GridBagConstraintsFactory.create(0, 3));

		JLabel jLabel5 = new JLabel();
		ResourceHelper.setText(jLabel5, "Home_Phone:");
		jLabel5.setLabelFor(homePageText);
		contactPanel.add(jLabel5, GridBagConstraintsFactory.create(0, 4));

		JLabel jLabel6 = new JLabel();
		ResourceHelper.setText(jLabel6, "Work_Phone:");
		jLabel6.setLabelFor(workPhoneText);
		contactPanel.add(jLabel6, GridBagConstraintsFactory.create(0, 5));

		JLabel jLabel7 = new JLabel();
		ResourceHelper.setText(jLabel7, "Pager:");
		jLabel7.setLabelFor(pagerText);

		JLabel jLabel8 = new JLabel();
		ResourceHelper.setText(jLabel8, "Fax:");
		jLabel8.setLabelFor(faxText);
		contactPanel.add(jLabel8, GridBagConstraintsFactory.create(0, 7));

		JLabel jLabel9 = new JLabel();
		ResourceHelper.setText(jLabel9, "Email:");
		jLabel9.setLabelFor(emailText);
		contactPanel.add(jLabel9, GridBagConstraintsFactory.create(0, 8));

		JLabel jLabel14 = new JLabel();
		ResourceHelper.setText(jLabel14, "Web_Page:");
		jLabel14.setLabelFor(webPageText);
		contactPanel.add(jLabel14, GridBagConstraintsFactory.create(0, 9));

		JLabel jLabel21 = new JLabel();
		ResourceHelper.setText(jLabel21, "Company");
		jLabel21.setLabelFor(companyText);
		contactPanel.add(jLabel21, GridBagConstraintsFactory.create(0, 10));

		JLabel jLabel22 = new JLabel();
		ResourceHelper.setText(jLabel22, "Birthday");
		jLabel22.setLabelFor(birthdayChooser);
		contactPanel.add(jLabel22, GridBagConstraintsFactory.create(0, 11));

		JLabel jLabel10 = new JLabel();
		ResourceHelper.setText(jLabel10, "Home_Street_Address");
		jLabel10.setLabelFor(streetAddresText);

		JLabel jLabel11 = new JLabel();
		ResourceHelper.setText(jLabel11, "Home_City:");
		jLabel11.setLabelFor(cityText);

		JLabel jLabel12 = new JLabel();
		ResourceHelper.setText(jLabel12, "Home_State:");
		jLabel12.setLabelFor(workStateText);

		JLabel jLabel13 = new JLabel();
		ResourceHelper.setText(jLabel13, "Home_Country:");
		jLabel13.setLabelFor(workCountryText);

		JLabel jLabel15 = new JLabel();
		ResourceHelper.setText(jLabel15, "Home_Zip_Code:");
		jLabel15.setLabelFor(workZipText);

		JLabel jLabel16 = new JLabel();
		ResourceHelper.setText(jLabel16, "Work_Street_Address");
		jLabel16.setLabelFor(workStreetAddressText);

		jLabel16.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel jLabel17 = new JLabel();
		ResourceHelper.setText(jLabel17, "Work_City:");
		jLabel17.setLabelFor(workCityText);
		contactPanel.add(jLabel7, GridBagConstraintsFactory.create(0, 6));

		jLabel17.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel17.setHorizontalTextPosition(SwingConstants.RIGHT);

		JLabel jLabel18 = new JLabel();
		ResourceHelper.setText(jLabel18, "Work_State:");
		jLabel18.setLabelFor(stateText);

		jLabel18.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel jLabel19 = new JLabel();
		ResourceHelper.setText(jLabel19, "Work_Zip_Code:");
		jLabel19.setLabelFor(zipCodeText);

		jLabel19.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel jLabel20 = new JLabel();
		ResourceHelper.setText(jLabel20, "Work_Country:");
		jLabel20.setLabelFor(countryText);

		jLabel20.setHorizontalAlignment(SwingConstants.RIGHT);
		workStreetAddressText.setMinimumSize(new java.awt.Dimension(4, 50));

		contactPanel.add(firstNameText, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(lastNameText, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(nickNameText, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(screenNameText, GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(homePageText, GridBagConstraintsFactory.create(1, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(workPhoneText, GridBagConstraintsFactory.create(1, 5,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(pagerText, GridBagConstraintsFactory.create(1, 6,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(faxText, GridBagConstraintsFactory.create(1, 7,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(emailText, GridBagConstraintsFactory.create(1, 8,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(webPageText, GridBagConstraintsFactory.create(1, 9,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(companyText, GridBagConstraintsFactory.create(1, 10,
				GridBagConstraints.BOTH, 1.0, 0.0));
		contactPanel.add(birthdayChooser, GridBagConstraintsFactory.create(1,
				11, GridBagConstraints.BOTH, 1.0, 0.0));

		theTabbedPane.addTab(Resource.getResourceString("contact"),
				contactPanel);

		//
		// ADDRESS
		//	
		addressPanel.setLayout(new java.awt.GridBagLayout());
		
		homeAddressPanel.setLayout(new java.awt.GridBagLayout());

		homeAddressPanel.setBorder(new TitledBorder(Resource
				.getResourceString("HomeAddress")));

		homeAddressPanel.add(jLabel16, GridBagConstraintsFactory.create(0, 0));
		homeAddressPanel.add(jLabel17, GridBagConstraintsFactory.create(0, 1));
		homeAddressPanel.add(jLabel12, GridBagConstraintsFactory.create(0, 2));
		homeAddressPanel.add(jLabel13, GridBagConstraintsFactory.create(0, 3));
		homeAddressPanel.add(jLabel15, GridBagConstraintsFactory.create(0, 4));

		homeAddressPanel.add(workStreetAddressText, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		homeAddressPanel.add(workCityText, GridBagConstraintsFactory.create(1,
				1, GridBagConstraints.BOTH, 1.0, 0.0));
		homeAddressPanel.add(workStateText, GridBagConstraintsFactory.create(1,
				2, GridBagConstraints.BOTH, 1.0, 0.0));
		homeAddressPanel.add(workCountryText, GridBagConstraintsFactory.create(
				1, 3, GridBagConstraints.BOTH, 1.0, 0.0));
		homeAddressPanel.add(workZipText, GridBagConstraintsFactory.create(1,
				4, GridBagConstraints.BOTH, 1.0, 0.0));


		addressPanel.add(homeAddressPanel, GridBagConstraintsFactory.create(0,
				0, GridBagConstraints.BOTH, 1.0, 1.0));

		workAddressPanel.setLayout(new java.awt.GridBagLayout());

		workAddressPanel.setBorder(new TitledBorder(Resource
				.getResourceString("WorkAddress")));

		workAddressPanel.add(jLabel10, GridBagConstraintsFactory.create(0, 0));
		workAddressPanel.add(jLabel11, GridBagConstraintsFactory.create(0, 1));
		workAddressPanel.add(jLabel18, GridBagConstraintsFactory.create(0, 2));
		workAddressPanel.add(jLabel20, GridBagConstraintsFactory.create(0, 3));
		workAddressPanel.add(jLabel19, GridBagConstraintsFactory.create(0, 4));

		workAddressPanel.add(streetAddresText, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		workAddressPanel.add(cityText, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		workAddressPanel.add(stateText, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));
		workAddressPanel.add(countryText, GridBagConstraintsFactory.create(1,
				3, GridBagConstraints.BOTH, 1.0, 0.0));
		workAddressPanel.add(zipCodeText, GridBagConstraintsFactory.create(1,
				4, GridBagConstraints.BOTH, 1.0, 0.0));

		addressPanel.add(workAddressPanel, GridBagConstraintsFactory.create(0,
				1, GridBagConstraints.BOTH, 1.0, 1.0));


		theTabbedPane.addTab(Resource.getResourceString("Address"),
				addressPanel);

		//
		// NOTES
		//
		JPanel notesPanel = new JPanel();
		notesPanel.setLayout(new GridBagLayout());
		notesPanel.add(notesText, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));
		theTabbedPane.addTab(Resource.getResourceString("Notes"), notesPanel);

		//
		// LINKS
		//
		linkPanel = new LinkPanel();
		theTabbedPane.addTab(Resource.getResourceString("links"), linkPanel);

		//
		// BUTTON
		//
		saveButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(saveButton, "Save");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveaddr();
			}
		});

		buttonPanel.add(saveButton);
		add(buttonPanel, GridBagConstraintsFactory.create(0, 1));

		// TABS
		theTabbedPane.setPreferredSize(new java.awt.Dimension(540, 400));
		add(theTabbedPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	@Override
	public void refresh() {
	  // empty
	}

	/**
	 * save the current address data from the UI into the database
	 */
	private void saveaddr() {

		if (firstNameText.getText().equals("")
				|| lastNameText.getText().equals("")) {
			Errmsg.notice(Resource
					.getResourceString("First_and_Last_name_are_Required"));
			return;
		}

		addr_.setFirstName(firstNameText.getText());
		addr_.setLastName(lastNameText.getText());
		addr_.setNickname(nickNameText.getText());
		addr_.setEmail(emailText.getText());
		addr_.setScreenName(screenNameText.getText());
		addr_.setWorkPhone(workPhoneText.getText());
		addr_.setHomePhone(homePageText.getText());
		addr_.setFax(faxText.getText());
		addr_.setPager(pagerText.getText());
		addr_.setWebPage(webPageText.getText());
		addr_.setNotes(notesText.getText());
		addr_.setStreetAddress(streetAddresText.getText());
		addr_.setCity(cityText.getText());
		addr_.setState(stateText.getText());
		addr_.setCountry(countryText.getText());
		addr_.setZip(zipCodeText.getText());
		addr_.setWorkStreetAddress(workStreetAddressText.getText());
		addr_.setWorkCity(workCityText.getText());
		addr_.setWorkState(workStateText.getText());
		addr_.setWorkCountry(workCountryText.getText());
		addr_.setWorkZip(workZipText.getText());
		addr_.setCompany(companyText.getText());
		addr_.setBirthday(birthdayChooser.getDate());

		try {
			AddressModel.getReference().saveAddress(addr_);

			this.close();
			
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * load the current addrsss data into the UI
	 */
	private void showaddr() {
		firstNameText.setText(addr_.getFirstName());
		lastNameText.setText(addr_.getLastName());
		nickNameText.setText(addr_.getNickname());
		emailText.setText(addr_.getEmail());
		screenNameText.setText(addr_.getScreenName());
		workPhoneText.setText(addr_.getWorkPhone());
		homePageText.setText(addr_.getHomePhone());
		faxText.setText(addr_.getFax());
		pagerText.setText(addr_.getPager());
		webPageText.setText(addr_.getWebPage());
		notesText.setText(addr_.getNotes());
		streetAddresText.setText(addr_.getStreetAddress());
		cityText.setText(addr_.getCity());
		stateText.setText(addr_.getState());
		countryText.setText(addr_.getCountry());
		zipCodeText.setText(addr_.getZip());
		workStreetAddressText.setText(addr_.getWorkStreetAddress());
		workCityText.setText(addr_.getWorkCity());
		workStateText.setText(addr_.getWorkState());
		workCountryText.setText(addr_.getWorkCountry());
		workZipText.setText(addr_.getWorkZip());
		companyText.setText(addr_.getCompany());

		Date bd = addr_.getBirthday();
		birthdayChooser.setDate(bd);

		linkPanel.setOwner(addr_);

	}

	@Override
	public void update(ChangeEvent event) {
		// empty
	}
}
