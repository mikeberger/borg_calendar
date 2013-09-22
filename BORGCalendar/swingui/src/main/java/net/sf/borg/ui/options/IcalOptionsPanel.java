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

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.ui.options;

import java.awt.GridBagConstraints;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ical.IcalModule;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import biz.source_code.base64Coder.Base64Coder;

public class IcalOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private JTextField port = new JTextField();
	private JSpinner exportyears = new JSpinner(new SpinnerNumberModel(2, 1,
			100, 1));
	private JCheckBox skipBox = new JCheckBox();
	private JTextField ftpserver = new JTextField();
	private JTextField ftppath = new JTextField();
	private JTextField ftpusername = new JTextField();
	private JPasswordField ftppassword = new JPasswordField();

	public IcalOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		this.add(new JLabel(Resource.getResourceString("years_to_export")),
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(exportyears,
				GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		this.add(new JLabel(Resource.getResourceString("server_port")),
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		this.add(port, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		skipBox.setText(Resource.getResourceString("skip_borg_ical"));
		this.add(skipBox,GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftpserver")),
				GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		this.add(ftpserver, GridBagConstraintsFactory.create(1, 3,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftppath")),
				GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		this.add(ftppath, GridBagConstraintsFactory.create(1, 4,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		this.add(new JLabel(Resource.getResourceString("ftpusername")),
				GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		this.add(ftpusername, GridBagConstraintsFactory.create(1, 5,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		JLabel pl = new JLabel(Resource.getResourceString("ftppassword"));
		this.add(pl,
				GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));
		pl.setLabelFor(ftppassword);
		this.add(ftppassword, GridBagConstraintsFactory.create(1, 6,
				GridBagConstraints.BOTH, 1.0, 0.0));
		ftppassword.setEditable(true);

	}

	@Override
	public void applyChanges() {

		// validate that port is a number
		try {
			int socket = Integer.parseInt(port.getText());
			Prefs.putPref(IcalModule.PORT, new Integer(socket));
		} catch (NumberFormatException e) {
			Errmsg.getErrorHandler().notice(
					Resource.getResourceString("port_warning"));
			;
			port.setText(((Integer) IcalModule.PORT.getDefault()).toString());
			Prefs.putPref(IcalModule.PORT, IcalModule.PORT.getDefault());
			return;
		}

		Prefs.putPref(IcalModule.FTPUSER, ftpusername.getText());
		Prefs.putPref(IcalModule.FTPSERVER, ftpserver.getText());
		Prefs.putPref(IcalModule.FTPPATH, ftppath.getText());
		try {
			sep(new String(ftppassword.getPassword()));
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		Prefs.putPref(IcalModule.EXPORTYEARS, exportyears.getValue());

		OptionsPanel.setBooleanPref(skipBox, IcalModule.SKIP_BORG);

	}

	@Override
	public void loadOptions() {

		int p = Prefs.getIntPref(IcalModule.PORT);
		port.setText(Integer.toString(p));

		exportyears.setValue(Prefs.getIntPref(IcalModule.EXPORTYEARS));

		skipBox.setSelected(Prefs.getBoolPref(IcalModule.SKIP_BORG));

		ftpusername.setText(Prefs.getPref(IcalModule.FTPUSER));
		ftpserver.setText(Prefs.getPref(IcalModule.FTPSERVER));
		ftppath.setText(Prefs.getPref(IcalModule.FTPPATH));

		try {
			ftppassword.setText(gep());
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("ical_options");
	}

	public static void sep(String s) throws Exception {
		if ("".equals(s)) {
			Prefs.putPref(IcalModule.FTPPW, s);
			return;
		}
		String p1 = Prefs.getPref(IcalModule.FTPPW2);
		if ("".equals(p1)) {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecretKey key = keyGen.generateKey();
			p1 = new String(Base64Coder.encode(key.getEncoded()));
			Prefs.putPref(IcalModule.FTPPW2, p1);
		}

		byte[] ba = Base64Coder.decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(s.getBytes());
		os.close();
		ba = baos.toByteArray();
		Prefs.putPref(IcalModule.FTPPW, new String(Base64Coder.encode(ba)));
	}

	public static String gep() throws Exception {
		String p1 = Prefs.getPref(IcalModule.FTPPW2);
		String p2 = Prefs.getPref(IcalModule.FTPPW);
		if ("".equals(p2))
			return p2;

		if ("".equals(p1)) {
			sep(p2); // transition case
			return p2;
		}

		byte[] ba = Base64Coder.decode(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		byte[] decba = Base64Coder.decode(p2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();

	}

}
