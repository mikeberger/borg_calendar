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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PasswordHelper;

/**
 * provides the UI for Email Options.
 */
class EmailOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 795364188303457966L;

	private final JCheckBox emailbox = new JCheckBox();
	private final JCheckBox dailybox = new JCheckBox();

	private final JTextField emailtext = new JTextField();
	private final JTextField emailfrom = new JTextField();

	private JSpinner emailtimebox = new JSpinner();

	private final JPasswordField smpw = new JPasswordField();

	private final JTextField smtpport = new JTextField();

	private final JTextField smtptext = new JTextField();

	private final JCheckBox tlsbox = new JCheckBox();

	private final JTextField usertext = new JTextField();

	/**
	 * Instantiates a new email options panel.
	 */
	public EmailOptionsPanel() {
		this.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(emailbox, "Enable_Email");
		this.add(emailbox, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(dailybox, "Enable_Daily_Email");
		this.add(dailybox, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		JLabel jLabel1 = new JLabel();
		ResourceHelper.setText(jLabel1, "SMTP_Server");
		this.add(jLabel1, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
		jLabel1.setLabelFor(smtptext);

		smtptext.setColumns(30);
		this.add(smtptext, GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel portLabel = new JLabel();
		ResourceHelper.setText(portLabel, "SMTP_Port");
		this.add(portLabel, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		jLabel1.setLabelFor(smtpport);

		smtpport.setColumns(30);
		this.add(smtpport, GridBagConstraintsFactory.create(1, 3, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel userlabel = new JLabel();
		ResourceHelper.setText(userlabel, "SMTP_user");
		this.add(userlabel, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		userlabel.setLabelFor(usertext);

		this.add(usertext, GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH));

		JLabel passlabel = new JLabel();
		ResourceHelper.setText(passlabel, "SMTP_password");
		this.add(passlabel, GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		passlabel.setLabelFor(smpw);

		this.add(smpw, GridBagConstraintsFactory.create(1, 5, GridBagConstraints.BOTH));

		JLabel jLabel2 = new JLabel();
		ResourceHelper.setText(jLabel2, "Your_Email_Address");
		this.add(jLabel2, GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));
		jLabel2.setLabelFor(emailtext);

		emailtext.setColumns(30);
		this.add(emailtext, GridBagConstraintsFactory.create(1, 6, GridBagConstraints.BOTH, 1.0, 0.0));

		this.add(new JLabel(Resource.getResourceString("EmailFrom")),
				GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH));

		emailfrom.setColumns(30);
		this.add(emailfrom, GridBagConstraintsFactory.create(1, 7, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel remtimelabel = new JLabel();
		ResourceHelper.setText(remtimelabel, "reminder_time");
		remtimelabel.setLabelFor(emailtimebox);
		this.add(remtimelabel, GridBagConstraintsFactory.create(0, 8, GridBagConstraints.BOTH));

		emailtimebox = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor de = new JSpinner.DateEditor(emailtimebox, "HH:mm");
		emailtimebox.setEditor(de);
		this.add(emailtimebox, GridBagConstraintsFactory.create(1, 8, GridBagConstraints.BOTH, 1.0, 0.0));

		tlsbox.setText(Resource.getResourceString("enable_tls"));
		this.add(tlsbox, GridBagConstraintsFactory.create(0, 9, GridBagConstraints.BOTH));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {

		OptionsPanel.setBooleanPref(emailbox, PrefName.EMAILENABLED);
		OptionsPanel.setBooleanPref(dailybox, PrefName.DAILYEMAILENABLED);
		OptionsPanel.setBooleanPref(tlsbox, PrefName.ENABLETLS);

		// only save email options if email is enabled
		if (emailbox.isSelected()) {
			Prefs.putPref(PrefName.EMAILSERVER, smtptext.getText());
			Prefs.putPref(PrefName.EMAILPORT, smtpport.getText());
			Prefs.putPref(PrefName.EMAILADDR, emailtext.getText());
			Prefs.putPref(PrefName.EMAILFROM, emailfrom.getText());
			Prefs.putPref(PrefName.EMAILUSER, usertext.getText());

			try {
				String pw = new String(smpw.getPassword());
				if (pw != null && !pw.isEmpty()) {

					String kpw = PasswordHelper.getReference().getEncryptionKeyWithoutTimeout("Encrypt EMail Password");
					if (kpw != null) {
						EncryptionHelper helper = new EncryptionHelper(kpw);
						Prefs.putPref(PrefName.EMAILPASS, helper.encrypt(pw));
						smpw.setText("");
					}
				}
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}

		}

		Date d = (Date) emailtimebox.getValue();
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		Prefs.putPref(PrefName.EMAILTIME, Integer.valueOf(hour * 60 + min));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {

		// email time to day
		int emmins = Prefs.getIntPref(PrefName.EMAILTIME);
		Calendar cal = new GregorianCalendar(1980, 1, 1, 0, 0, 0);
		cal.add(Calendar.MINUTE, emmins);
		emailtimebox.setValue(cal.getTime());

		OptionsPanel.setCheckBox(emailbox, PrefName.EMAILENABLED);
		OptionsPanel.setCheckBox(dailybox, PrefName.DAILYEMAILENABLED);
		OptionsPanel.setCheckBox(tlsbox, PrefName.ENABLETLS);

		// email server and address
		smtptext.setText(Prefs.getPref(PrefName.EMAILSERVER));
		smtpport.setText(Prefs.getPref(PrefName.EMAILPORT));
		emailtext.setText(Prefs.getPref(PrefName.EMAILADDR));
		emailfrom.setText(Prefs.getPref(PrefName.EMAILFROM));
		usertext.setText(Prefs.getPref(PrefName.EMAILUSER));

		smpw.setText("");

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("EmailParameters");
	}

}
