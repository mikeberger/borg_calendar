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
package net.sf.borg.plugin.sync.google;

import java.awt.GridBagConstraints;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * provides the UI for Email Options.
 */
public class GoogleSyncOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	static public PrefName SYNCUSER = new PrefName("googlesync-user", "");
	static public PrefName SYNCPW = new PrefName("googlesync-pw", "");
	static public PrefName SYNCPW2 = new PrefName("googlesync-pw2", "");
	
	private JTextField username = new JTextField();
	private JPasswordField password = new JPasswordField();

	/**
	 * Instantiates a new sync options panel.
	 */
	public GoogleSyncOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		JLabel jLabel1 = new JLabel("Google User Name");
		this.add(jLabel1, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));
		jLabel1.setLabelFor(username);

		username.setColumns(30);
		this.add(username, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel portLabel = new JLabel("Google Password");
		this.add(portLabel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH));
		jLabel1.setLabelFor(password);

		this.add(password, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {

		Prefs.putPref(SYNCUSER, username.getText());
		try {
			sep(new String(password.getPassword()));
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {

		username.setText(Prefs.getPref(SYNCUSER));

		try {
			password.setText(gep());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	@Override
	public String getPanelName() {
		return "Sync Options";
	}

	public static void sep(String s) throws Exception {
		if ("".equals(s)) {
			Prefs.putPref(SYNCPW, s);
			return;
		}
		String p1 = Prefs.getPref(SYNCPW2);
		if ("".equals(p1)) {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecretKey key = keyGen.generateKey();
			BASE64Encoder b64enc = new BASE64Encoder();
			p1 = b64enc.encode(key.getEncoded());
			Prefs.putPref(SYNCPW2, p1);
		}

		BASE64Decoder b64dec = new BASE64Decoder();
		byte[] ba = b64dec.decodeBuffer(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, enc);
		os.write(s.getBytes());
		os.close();
		ba = baos.toByteArray();
		BASE64Encoder b64enc = new BASE64Encoder();
		Prefs.putPref(SYNCPW, b64enc.encode(ba));
	}

	public static String gep() throws Exception {
		String p1 = Prefs.getPref(SYNCPW2);
		String p2 = Prefs.getPref(SYNCPW);
		if ("".equals(p2))
			return p2;

		if ("".equals(p1)) {
			sep(p2); // transition case
			return p2;
		}

		BASE64Decoder b64dec = new BASE64Decoder();
		byte[] ba = b64dec.decodeBuffer(p1);
		SecretKey key = new SecretKeySpec(ba, "AES");
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		byte[] decba = b64dec.decodeBuffer(p2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = new CipherOutputStream(baos, dec);
		os.write(decba);
		os.close();

		return baos.toString();

	}

}
