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

 Copyright 2003-2011 by Mike Berger
 */
package net.sf.borg.ui.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.Theme;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.ColorChooserButton;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * Provides the UI for editing color options
 */
public class ColorOptionsPanel extends OptionsPanel implements Prefs.Listener {

	private static final long serialVersionUID = 2184312216077136324L;

	/*
	 * the user color scheme buttons that let the user set colors
	 */
	private ColorChooserButton btn_ucs_birthdays;
	private ColorChooserButton btn_ucs_black;
	private ColorChooserButton btn_ucs_blue;
	private ColorChooserButton btn_ucs_defaultBg;
	private ColorChooserButton btn_ucs_defaultFg;
	private ColorChooserButton btn_ucs_green;
	private ColorChooserButton btn_ucs_halfday;
	private ColorChooserButton btn_ucs_holiday;
	private ColorChooserButton btn_ucs_holidays;
	private ColorChooserButton btn_ucs_red;
	private ColorChooserButton btn_ucs_reminderBg;
	private ColorChooserButton btn_ucs_stripe;
	private ColorChooserButton btn_ucs_tasks;
	private ColorChooserButton btn_ucs_today;
	private ColorChooserButton btn_ucs_vacation;
	private ColorChooserButton btn_ucs_weekday;
	private ColorChooserButton btn_ucs_weekend;
	private ColorChooserButton btn_ucs_white;
	private ColorChooserButton btn_tray_fg;
	private ColorChooserButton btn_tray_bg;

	private JCheckBox cb_ucs_marktodo;
	private JCheckBox cb_ucs_ontodo;
	private JCheckBox gradientApptBox = new JCheckBox();
	private JTextField tf_ucs_marker;

	private JComboBox<String> themeChooser = new JComboBox<String>();

	/**
	 * Instantiates a new color options panel.
	 */
	public ColorOptionsPanel() {
		this.setLayout(new GridBagLayout());

		JPanel themePanel = new JPanel();
		themePanel.setLayout(new GridBagLayout());

		themePanel.add(new JLabel(Resource.getResourceString("Theme") + ":"),
				GridBagConstraintsFactory.create(0, 0));

		themeChooser.setEditable(false);
		themePanel.add(themeChooser, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));

		themeChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// switch the displayed theme, but not the active (current)
				// theme
				String name = (String) themeChooser.getSelectedItem();
				if (name == null)
					return;
				Theme t = Theme.getTheme(name);
				showTheme(t);
			}

		});

		JButton saveButton = new JButton();
		saveButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// create a new theme and store in the db
				String name = JOptionPane.showInputDialog(Resource
						.getResourceString("New_Theme_Name"));
				if (name == null)
					return;

				Theme newTheme = new Theme();
				setTheme(newTheme); // set the new theme from based on the
									// currently displayed theme
				newTheme.setName(name);
				try {
					// save the theme
					newTheme.save();
					themeChooser.addItem(name);
					themeChooser.setSelectedItem(name);
				} catch (Warning e1) {
					Errmsg.getErrorHandler().notice(e1.getLocalizedMessage());
					return;
				} catch (Exception e1) {
					Errmsg.getErrorHandler().errmsg(e1);
					return;
				}
			}

		});

		themePanel.add(saveButton, GridBagConstraintsFactory.create(2, 0));

		JButton delButton = new JButton();
		delButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));
		delButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// delete the selected theme
				try {
					Theme.delete((String) themeChooser.getSelectedItem());
					loadThemes();
				} catch (Exception e1) {
					Errmsg.getErrorHandler().errmsg(e1);
					return;
				}
			}

		});
		themePanel.add(delButton, GridBagConstraintsFactory.create(3, 0));

		this.add(themePanel,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new GridLayout(12, 2));

		cb_ucs_ontodo = new JCheckBox();
		ResourceHelper.setText(cb_ucs_ontodo, "ucolortext1");
		cb_ucs_marktodo = new JCheckBox();
		ResourceHelper.setText(cb_ucs_marktodo, "ucolortext2");
		tf_ucs_marker = new JTextField("! ");
		btn_ucs_red = new ColorChooserButton(
				Resource.getResourceString("ucolortext4"), Color.WHITE);
		btn_ucs_blue = new ColorChooserButton(
				Resource.getResourceString("ucolortext5"), Color.WHITE);
		btn_ucs_green = new ColorChooserButton(
				Resource.getResourceString("ucolortext6"), Color.WHITE);
		btn_ucs_black = new ColorChooserButton(
				Resource.getResourceString("ucolortext7"), Color.WHITE);
		btn_ucs_white = new ColorChooserButton(
				Resource.getResourceString("ucolortext8"), Color.WHITE);
		btn_ucs_tasks = new ColorChooserButton(
				Resource.getResourceString("ucolortext9"), Color.WHITE);
		btn_ucs_holidays = new ColorChooserButton(
				Resource.getResourceString("ucolortext10"), Color.WHITE);
		btn_ucs_birthdays = new ColorChooserButton(
				Resource.getResourceString("ucolortext11"), Color.WHITE);
		btn_ucs_defaultBg = new ColorChooserButton(
				Resource.getResourceString("ucolortext12"), Color.WHITE);
		btn_ucs_defaultFg = new ColorChooserButton(
				Resource.getResourceString("defaultfg"), Color.WHITE);
		btn_ucs_holiday = new ColorChooserButton(
				Resource.getResourceString("ucolortext13"), Color.WHITE);
		btn_ucs_halfday = new ColorChooserButton(
				Resource.getResourceString("ucolortext14"), Color.WHITE);
		btn_ucs_vacation = new ColorChooserButton(
				Resource.getResourceString("ucolortext15"), Color.WHITE);
		btn_ucs_today = new ColorChooserButton(
				Resource.getResourceString("ucolortext16"), Color.WHITE);
		btn_ucs_weekend = new ColorChooserButton(
				Resource.getResourceString("ucolortext17"), Color.WHITE);
		btn_ucs_weekday = new ColorChooserButton(
				Resource.getResourceString("ucolortext18"), Color.WHITE);
		btn_ucs_stripe = new ColorChooserButton(
				Resource.getResourceString("stripecolor"), Color.WHITE);
		btn_tray_fg = new ColorChooserButton(
				Resource.getResourceString("tray_fg"), Color.WHITE);
		btn_tray_bg = new ColorChooserButton(
				Resource.getResourceString("tray_bg"), Color.WHITE);
		btn_ucs_reminderBg = new ColorChooserButton(
				Resource.getResourceString("reminder_bg"), Color.WHITE);
		JButton btn_ucs_restore = new JButton(
				Resource.getResourceString("restore_defaults"));

		btn_ucs_restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Theme t = new Theme();

				btn_ucs_red.setColorProperty(new Color(t.getTextColor1()));
				btn_ucs_blue.setColorProperty(new Color(t.getTextColor2()));
				btn_ucs_green.setColorProperty(new Color(t.getTextColor3()));
				btn_ucs_black.setColorProperty(new Color(t.getTextColor4()));
				btn_ucs_white.setColorProperty(new Color(t.getTextColor5()));
				btn_ucs_tasks.setColorProperty(new Color(t.getTaskTextColor()));
				btn_ucs_holidays.setColorProperty(new Color(t
						.getHolidayTextColor()));
				btn_ucs_birthdays.setColorProperty(new Color(t
						.getBirthdayTextColor()));
				btn_ucs_defaultBg.setColorProperty(new Color(t.getDefaultBg()));
				btn_ucs_defaultFg.setColorProperty(new Color(t.getDefaultFg()));
				btn_ucs_today.setColorProperty(new Color(t.getTodayBg()));
				btn_ucs_holiday.setColorProperty(new Color(t.getHolidayBg()));
				btn_ucs_vacation.setColorProperty(new Color(t.getVacationBg()));
				btn_ucs_halfday.setColorProperty(new Color(t.getHalfdayBg()));
				btn_ucs_weekend.setColorProperty(new Color(t.getWeekendBg()));
				btn_ucs_weekday.setColorProperty(new Color(t.getWeekdayBg()));
				btn_tray_fg.setColorProperty(new Color(t.getTrayIconFg()));
				btn_tray_bg.setColorProperty(new Color(t.getTrayIconBg()));
				btn_ucs_reminderBg.setColorProperty(new Color(t.getReminderBg()));
			}
		});

		colorPanel.add(btn_ucs_red);
		colorPanel.add(btn_ucs_defaultBg);
		colorPanel.add(btn_ucs_defaultFg);
		colorPanel.add(btn_ucs_blue);
		colorPanel.add(btn_ucs_today);
		colorPanel.add(btn_ucs_green);
		colorPanel.add(btn_ucs_holiday);
		colorPanel.add(btn_ucs_black);
		colorPanel.add(btn_ucs_halfday);
		colorPanel.add(btn_ucs_white);
		colorPanel.add(btn_ucs_vacation);
		colorPanel.add(btn_ucs_tasks);
		colorPanel.add(btn_ucs_weekend);
		colorPanel.add(btn_ucs_holidays);
		colorPanel.add(btn_ucs_weekday);
		colorPanel.add(btn_ucs_birthdays);
		colorPanel.add(btn_ucs_stripe);
		colorPanel.add(btn_tray_fg);
		colorPanel.add(btn_tray_bg);
		colorPanel.add(btn_ucs_reminderBg);
		colorPanel.add(btn_ucs_restore);
		colorPanel.add(cb_ucs_ontodo);

		JPanel njp = new JPanel();
		njp.setLayout(new BorderLayout());
		njp.add(cb_ucs_marktodo, BorderLayout.WEST);
		njp.add(tf_ucs_marker, BorderLayout.CENTER);
		colorPanel.add(njp);

		gradientApptBox.setText(Resource.getResourceString("gradient_appts"));
		colorPanel.add(gradientApptBox);

		this.add(colorPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

		loadThemes();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(gradientApptBox, PrefName.GRADIENT_APPTS);
		OptionsPanel.setBooleanPref(cb_ucs_ontodo, PrefName.UCS_ONTODO);
		OptionsPanel.setBooleanPref(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

		Prefs.putPref(PrefName.UCS_MARKER, tf_ucs_marker.getText());

		/*
		 * When changes are applied, save the current theme and make the
		 * currently shown theme active
		 */
		Theme t = new Theme();
		setTheme(t);

		try {
			t.save();
			Theme.setCurrentTheme(t);
			loadThemes();
		} catch (Warning e1) {
			Errmsg.getErrorHandler().notice(e1.getLocalizedMessage());
			return;
		} catch (Exception e1) {
			Errmsg.getErrorHandler().errmsg(e1);
			return;
		}

		
	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("UserColorScheme");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {

		loadThemes();

		OptionsPanel.setCheckBox(gradientApptBox, PrefName.GRADIENT_APPTS);
		OptionsPanel.setCheckBox(cb_ucs_ontodo, PrefName.UCS_ONTODO);
		OptionsPanel.setCheckBox(cb_ucs_marktodo, PrefName.UCS_MARKTODO);
		tf_ucs_marker.setText(Prefs.getPref(PrefName.UCS_MARKER));

	}

	/**
	 * clear the theme chooser and load all themes as chooser options. show the
	 * active theme
	 */
	private void loadThemes() {
		themeChooser.removeAllItems();
		Collection<String> themeNames = Theme.getThemeNames();

		for (String name : themeNames) {
			themeChooser.addItem(name);
		}

		Theme t = Theme.getCurrentTheme();

		themeChooser.setSelectedItem(t.getName());
		showTheme(t);

	}

	/**
	 * set the data items in a theme object from the values shown on the UI
	 * 
	 * @param t
	 *            - the Theme to be set
	 */
	private void setTheme(Theme t) {
		t.setName((String) themeChooser.getSelectedItem());

		t.setTextColor1(btn_ucs_red.getColorProperty().getRGB());
		t.setTextColor2(btn_ucs_blue.getColorProperty().getRGB());
		t.setTextColor3(btn_ucs_green.getColorProperty().getRGB());
		t.setTextColor4(btn_ucs_black.getColorProperty().getRGB());
		t.setTextColor5(btn_ucs_white.getColorProperty().getRGB());

		t.setTaskTextColor(btn_ucs_tasks.getColorProperty().getRGB());
		t.setHolidayTextColor(btn_ucs_holidays.getColorProperty().getRGB());
		t.setBirthdayTextColor(btn_ucs_birthdays.getColorProperty().getRGB());

		t.setDefaultBg(btn_ucs_defaultBg.getColorProperty().getRGB());
		t.setDefaultFg(btn_ucs_defaultFg.getColorProperty().getRGB());
		t.setHolidayBg(btn_ucs_holiday.getColorProperty().getRGB());
		t.setHalfdayBg(btn_ucs_halfday.getColorProperty().getRGB());
		t.setVacationBg(btn_ucs_vacation.getColorProperty().getRGB());

		t.setTodayBg(btn_ucs_today.getColorProperty().getRGB());
		t.setWeekendBg(btn_ucs_weekend.getColorProperty().getRGB());
		t.setWeekdayBg(btn_ucs_weekday.getColorProperty().getRGB());
		t.setStripeBg(btn_ucs_stripe.getColorProperty().getRGB());
		t.setTrayIconFg(btn_tray_fg.getColorProperty().getRGB());
		t.setTrayIconBg(btn_tray_bg.getColorProperty().getRGB());
		t.setReminderBg(btn_ucs_reminderBg.getColorProperty().getRGB());

	}

	/**
	 * set the UI from the values in a Theme
	 * 
	 * @param t
	 *            - the Theme to show on the UI
	 */
	private void showTheme(Theme t) {
		btn_ucs_red.setColorProperty(new Color(t.getTextColor1()));
		btn_ucs_blue.setColorProperty(new Color(t.getTextColor2()));
		btn_ucs_green.setColorProperty(new Color(t.getTextColor3()));
		btn_ucs_black.setColorProperty(new Color(t.getTextColor4()));
		btn_ucs_white.setColorProperty(new Color(t.getTextColor5()));

		btn_ucs_tasks.setColorProperty(new Color(t.getTaskTextColor()));
		btn_ucs_holidays.setColorProperty(new Color(t.getHolidayTextColor()));
		btn_ucs_birthdays.setColorProperty(new Color(t.getBirthdayTextColor()));

		btn_ucs_defaultBg.setColorProperty(new Color(t.getDefaultBg()));
		btn_ucs_defaultFg.setColorProperty(new Color(t.getDefaultFg()));
		btn_ucs_today.setColorProperty(new Color(t.getTodayBg()));
		btn_ucs_holiday.setColorProperty(new Color(t.getHolidayBg()));
		btn_ucs_halfday.setColorProperty(new Color(t.getHalfdayBg()));
		btn_ucs_vacation.setColorProperty(new Color(t.getVacationBg()));
		btn_ucs_weekend.setColorProperty(new Color(t.getWeekendBg()));
		btn_ucs_weekday.setColorProperty(new Color(t.getWeekdayBg()));
		btn_ucs_stripe.setColorProperty(new Color(t.getStripeBg()));
		btn_tray_fg.setColorProperty(new Color(t.getTrayIconFg()));
		btn_tray_bg.setColorProperty(new Color(t.getTrayIconBg()));
		btn_ucs_reminderBg.setColorProperty(new Color(t.getReminderBg()));
	}

	@Override
	public void prefsChanged() {
		loadThemes();
	}
}
