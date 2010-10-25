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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.ColorChooserButton;
import net.sf.borg.ui.util.StripedTable;

/**
 * Provides the UI for editing color options
 */
public class ColorOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 2184312216077136324L;

	/*
	 * the user color scheme buttons that let the user set colors
	 */
	private ColorChooserButton btn_ucs_birthdays;

	private ColorChooserButton btn_ucs_black;
	private ColorChooserButton btn_ucs_blue;
	private ColorChooserButton btn_ucs_default;
	private ColorChooserButton btn_ucs_green;
	private ColorChooserButton btn_ucs_halfday;
	private ColorChooserButton btn_ucs_holiday;
	private ColorChooserButton btn_ucs_holidays;
	private ColorChooserButton btn_ucs_red;
	private ColorChooserButton btn_ucs_stripe;
	private ColorChooserButton btn_ucs_tasks;
	private ColorChooserButton btn_ucs_today;
	private ColorChooserButton btn_ucs_vacation;
	private ColorChooserButton btn_ucs_weekday;
	private ColorChooserButton btn_ucs_weekend;
	private ColorChooserButton btn_ucs_white;
	private JCheckBox cb_ucs_marktodo;

	private JCheckBox cb_ucs_ontodo;
	private JCheckBox gradientApptBox = new JCheckBox();

	private JTextField tf_ucs_marker;

	/**
	 * Instantiates a new color options panel.
	 */
	public ColorOptionsPanel() {
		this.setLayout(new GridLayout(10, 2));

		cb_ucs_ontodo = new JCheckBox();
		ResourceHelper.setText(cb_ucs_ontodo, "ucolortext1");
		cb_ucs_marktodo = new JCheckBox();
		ResourceHelper.setText(cb_ucs_marktodo, "ucolortext2");
		tf_ucs_marker = new JTextField("! ");
		btn_ucs_red = new ColorChooserButton(Resource
				.getResourceString("ucolortext4"), Color.WHITE);
		btn_ucs_blue = new ColorChooserButton(Resource
				.getResourceString("ucolortext5"), Color.WHITE);
		btn_ucs_green = new ColorChooserButton(Resource
				.getResourceString("ucolortext6"), Color.WHITE);
		btn_ucs_black = new ColorChooserButton(Resource
				.getResourceString("ucolortext7"), Color.WHITE);
		btn_ucs_white = new ColorChooserButton(Resource
				.getResourceString("ucolortext8"), Color.WHITE);
		btn_ucs_tasks = new ColorChooserButton(Resource
				.getResourceString("ucolortext9"), Color.WHITE);
		btn_ucs_holidays = new ColorChooserButton(Resource
				.getResourceString("ucolortext10"), Color.WHITE);
		btn_ucs_birthdays = new ColorChooserButton(Resource
				.getResourceString("ucolortext11"), Color.WHITE);
		btn_ucs_default = new ColorChooserButton(Resource
				.getResourceString("ucolortext12"), Color.WHITE);
		btn_ucs_holiday = new ColorChooserButton(Resource
				.getResourceString("ucolortext13"), Color.WHITE);
		btn_ucs_halfday = new ColorChooserButton(Resource
				.getResourceString("ucolortext14"), Color.WHITE);
		btn_ucs_vacation = new ColorChooserButton(Resource
				.getResourceString("ucolortext15"), Color.WHITE);
		btn_ucs_today = new ColorChooserButton(Resource
				.getResourceString("ucolortext16"), Color.WHITE);
		btn_ucs_weekend = new ColorChooserButton(Resource
				.getResourceString("ucolortext17"), Color.WHITE);
		btn_ucs_weekday = new ColorChooserButton(Resource
				.getResourceString("ucolortext18"), Color.WHITE);
		btn_ucs_stripe = new ColorChooserButton(Resource
				.getResourceString("stripecolor"), Color.WHITE);

		JButton btn_ucs_restore = new JButton(Resource
				.getResourceString("restore_defaults"));

		btn_ucs_restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btn_ucs_red.setColorProperty(new Color(
						((Integer) PrefName.UCS_RED.getDefault()).intValue()));
				btn_ucs_blue.setColorProperty(new Color(
						((Integer) PrefName.UCS_BLUE.getDefault()).intValue()));
				btn_ucs_green
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_GREEN.getDefault())
										.intValue()));
				btn_ucs_black
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_BLACK.getDefault())
										.intValue()));
				btn_ucs_white
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_WHITE.getDefault())
										.intValue()));
				btn_ucs_tasks.setColorProperty(new Color(
						((Integer) PrefName.UCS_NAVY.getDefault()).intValue()));
				btn_ucs_holidays
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_PURPLE.getDefault())
										.intValue()));
				btn_ucs_birthdays
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_BRICK.getDefault())
										.intValue()));
				btn_ucs_default.setColorProperty(new Color(
						((Integer) PrefName.UCS_DEFAULT.getDefault())
								.intValue()));
				btn_ucs_today
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_TODAY.getDefault())
										.intValue()));
				btn_ucs_holiday.setColorProperty(new Color(
						((Integer) PrefName.UCS_HOLIDAY.getDefault())
								.intValue()));
				btn_ucs_vacation.setColorProperty(new Color(
						((Integer) PrefName.UCS_VACATION.getDefault())
								.intValue()));
				btn_ucs_halfday.setColorProperty(new Color(
						((Integer) PrefName.UCS_HALFDAY.getDefault())
								.intValue()));
				btn_ucs_weekend.setColorProperty(new Color(
						((Integer) PrefName.UCS_WEEKEND.getDefault())
								.intValue()));
				btn_ucs_weekday.setColorProperty(new Color(
						((Integer) PrefName.UCS_WEEKDAY.getDefault())
								.intValue()));
				btn_ucs_stripe
						.setColorProperty(new Color(
								((Integer) PrefName.UCS_STRIPE.getDefault())
										.intValue()));
			}
		});

		this.add(btn_ucs_red);
		this.add(btn_ucs_default);
		this.add(btn_ucs_blue);
		this.add(btn_ucs_today);
		this.add(btn_ucs_green);
		this.add(btn_ucs_holiday);
		this.add(btn_ucs_black);
		this.add(btn_ucs_halfday);
		this.add(btn_ucs_white);
		this.add(btn_ucs_vacation);
		this.add(btn_ucs_tasks);
		this.add(btn_ucs_weekend);
		this.add(btn_ucs_holidays);
		this.add(btn_ucs_weekday);
		this.add(btn_ucs_birthdays);
		this.add(btn_ucs_stripe);
		this.add(btn_ucs_restore);
		this.add(cb_ucs_ontodo);

		JPanel njp = new JPanel();
		njp.setLayout(new BorderLayout());
		njp.add(cb_ucs_marktodo, BorderLayout.WEST);
		njp.add(tf_ucs_marker, BorderLayout.CENTER);
		this.add(njp);

		gradientApptBox.setText(Resource.getResourceString("gradient_appts"));
		this.add(gradientApptBox);
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

		Integer ucsi = new Integer((btn_ucs_red.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_RED, ucsi.toString());
		ucsi = new Integer((btn_ucs_blue.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BLUE, ucsi.toString());
		ucsi = new Integer((btn_ucs_green.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_GREEN, ucsi.toString());
		ucsi = new Integer((btn_ucs_black.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BLACK, ucsi.toString());
		ucsi = new Integer((btn_ucs_white.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WHITE, ucsi.toString());

		ucsi = new Integer((btn_ucs_tasks.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_NAVY, ucsi.toString());
		ucsi = new Integer((btn_ucs_holidays.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_PURPLE, ucsi.toString());
		ucsi = new Integer((btn_ucs_birthdays.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BRICK, ucsi.toString());

		ucsi = new Integer((btn_ucs_default.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_DEFAULT, ucsi.toString());
		ucsi = new Integer((btn_ucs_holiday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_HOLIDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_halfday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_HALFDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_vacation.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_VACATION, ucsi.toString());
		ucsi = new Integer((btn_ucs_today.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_TODAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_weekend.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WEEKEND, ucsi.toString());
		ucsi = new Integer((btn_ucs_weekday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WEEKDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_stripe.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_STRIPE, ucsi.toString());
		StripedTable.setStripeColor(new Color(ucsi.intValue()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {

		OptionsPanel.setCheckBox(gradientApptBox, PrefName.GRADIENT_APPTS);

		OptionsPanel.setCheckBox(cb_ucs_ontodo, PrefName.UCS_ONTODO);
		OptionsPanel.setCheckBox(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

		tf_ucs_marker.setText(Prefs.getPref(PrefName.UCS_MARKER));
		int mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_RED));
		btn_ucs_red.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLUE));
		btn_ucs_blue.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_GREEN));
		btn_ucs_green.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLACK));
		btn_ucs_black.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WHITE));
		btn_ucs_white.setColorProperty(new Color(mins));

		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_NAVY));
		btn_ucs_tasks.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_PURPLE));
		btn_ucs_holidays.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BRICK));
		btn_ucs_birthdays.setColorProperty(new Color(mins));

		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_DEFAULT));
		btn_ucs_default.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_TODAY));
		btn_ucs_today.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HOLIDAY));
		btn_ucs_holiday.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HALFDAY));
		btn_ucs_halfday.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_VACATION));
		btn_ucs_vacation.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKEND));
		btn_ucs_weekend.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKDAY));
		btn_ucs_weekday.setColorProperty(new Color(mins));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_STRIPE));
		btn_ucs_stripe.setColorProperty(new Color(mins));

	}

	@Override
	public String getPanelName() {
		return Resource.getResourceString("UserColorScheme");
	}
}
