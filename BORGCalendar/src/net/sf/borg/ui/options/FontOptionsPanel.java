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

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.NwFontChooserS;

/**
 * Provides the UI for editing Font options
 */
public class FontOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = -6568983838009839140L;

	/**
	 * Instantiates a new font options panel.
	 */
	public FontOptionsPanel() {
		this.setLayout(new FlowLayout());

		JButton apptFontButton = new JButton();
		ResourceHelper.setText(apptFontButton, "set_appt_font");
		apptFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		apptFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(PrefName.APPTFONT);
			}
		});
		this.add(apptFontButton);

		JButton defFontButton = new JButton();
		ResourceHelper.setText(defFontButton, "set_def_font");
		defFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		defFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(PrefName.DEFFONT);
			}
		});
		this.add(defFontButton);

		JButton dayFontButton = new JButton();
		ResourceHelper.setText(dayFontButton, "dview_font");
		dayFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		dayFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(PrefName.DAYVIEWFONT);
			}
		});
		this.add(dayFontButton);

		JButton weekFontButton = new JButton();
		ResourceHelper.setText(weekFontButton, "wview_font");
		weekFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		weekFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(PrefName.WEEKVIEWFONT);
			}
		});
		this.add(weekFontButton);

		JButton monthFontButton = new JButton();
		ResourceHelper.setText(monthFontButton, "mview_font");
		monthFontButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		monthFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(PrefName.MONTHVIEWFONT);
			}
		});
		this.add(monthFontButton);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		// empty
	}

	/**
	 * bring up a font chooser UI and let the user change a font
	 * 
	 * @param fontname
	 *            the preference name associated with the font
	 */
	private void fontActionPerformed(PrefName fontname) {

		// get font from pref name
		Font pf = Font.decode(Prefs.getPref(fontname));

		// choose a new font
		Font f = NwFontChooserS.showDialog(null, null, pf);
		if (f == null) {
			return;
		}

		// get the font name and store
		String s = NwFontChooserS.fontString(f);
		Prefs.putPref(fontname, s);

		// if the default font is changing then try to update the entire UI
		// will not likely be pretty
		if (fontname == PrefName.DEFFONT) {
			NwFontChooserS.setDefaultFont(f);
			SwingUtilities.updateComponentTreeUI(this);
		}

		// notify listeners - the font change takes place immediately
		// the apply button is not involved
		Prefs.notifyListeners();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		// empty
	}

}
