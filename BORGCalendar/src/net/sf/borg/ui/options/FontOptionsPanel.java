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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.NwFontChooserS;

/**
 * Provides the UI for editing Font options
 */
public class FontOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = -6568983838009839140L;
	
	JTextField monthFontText = new JTextField();
	JTextField dayFontText = new JTextField();
	JTextField weekFontText = new JTextField();
	JTextField printFontText = new JTextField();
	JTextField defaultFontText = new JTextField();

	/**
	 * Instantiates a new font options panel.
	 */
	public FontOptionsPanel() {
		
		monthFontText.setEditable(false);
		dayFontText.setEditable(false);
		weekFontText.setEditable(false);
		printFontText.setEditable(false);
		defaultFontText.setEditable(false);
		
		this.setLayout(new GridBagLayout());
		
		JButton defFontButton = new JButton();
		ResourceHelper.setText(defFontButton, "set_def_font");
		defFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		defFontButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(defaultFontText);
			}
		});
		this.add(defFontButton, GridBagConstraintsFactory.create(0,0, GridBagConstraints.BOTH));
	
		JButton apptFontButton = new JButton();
		ResourceHelper.setText(apptFontButton, "set_appt_font");
		apptFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		apptFontButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(monthFontText);
			}
		});
		this.add(apptFontButton, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		JButton dayFontButton = new JButton();
		ResourceHelper.setText(dayFontButton, "dview_font");
		dayFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		dayFontButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(dayFontText);
			}
		});
		this.add(dayFontButton, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));

		JButton weekFontButton = new JButton();
		ResourceHelper.setText(weekFontButton, "wview_font");
		weekFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		weekFontButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(weekFontText);
			}
		});
		this.add(weekFontButton, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		JButton printFontButton = new JButton();
		ResourceHelper.setText(printFontButton, "mview_font");
		printFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		printFontButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(printFontText);
			}
		});
		this.add(printFontButton, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		
		this.add(defaultFontText, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		this.add(monthFontText, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		this.add(weekFontText, GridBagConstraintsFactory.create(1, 3, GridBagConstraints.BOTH, 1.0, 0.0));
		this.add(dayFontText, GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH, 1.0, 0.0));
		this.add(printFontText, GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH, 1.0, 0.0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		
		String origDefaultFont = Prefs.getPref(PrefName.DEFFONT);
		
		Prefs.putPref(PrefName.APPTFONT, monthFontText.getText());
		Prefs.putPref(PrefName.DEFFONT, defaultFontText.getText());
		Prefs.putPref(PrefName.WEEKVIEWFONT, weekFontText.getText());
		Prefs.putPref(PrefName.DAYVIEWFONT, dayFontText.getText());
		Prefs.putPref(PrefName.PRINTFONT, printFontText.getText());
		
		// if the default font is changing then try to update the entire UI
		// will not likely be pretty
		if (!origDefaultFont.equals(defaultFontText.getText())) {
			Font f = Font.decode(defaultFontText.getText());
			NwFontChooserS.setDefaultFont(f);
			SwingUtilities.updateComponentTreeUI(this);
		}

		// notify listeners - the font change takes place immediately
		// the apply button is not involved
		Prefs.notifyListeners();

	}

	/**
	 * bring up a font chooser UI and let the user change a font
	 * 
	 * @param fontname
	 *            the preference name associated with the font
	 */
	private void fontActionPerformed(JTextField fontText) {

		// get font from pref name
		Font pf = Font.decode(fontText.getText());

		// choose a new font
		Font f = NwFontChooserS.showDialog(null, null, pf);
		if (f == null) {
			return;
		}

		// get the font name and store
		String s = NwFontChooserS.fontString(f);
		fontText.setText(s);
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		monthFontText.setText(Prefs.getPref(PrefName.APPTFONT));
		defaultFontText.setText(Prefs.getPref(PrefName.DEFFONT));
		weekFontText.setText(Prefs.getPref(PrefName.WEEKVIEWFONT));
		dayFontText.setText(Prefs.getPref(PrefName.DAYVIEWFONT));
		printFontText.setText(Prefs.getPref(PrefName.PRINTFONT));
	}
	
	@Override
	public String getPanelName() {
		return Resource.getResourceString("fonts");
	}

}
