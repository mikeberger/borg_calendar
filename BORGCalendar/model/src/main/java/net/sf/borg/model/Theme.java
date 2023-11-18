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
package net.sf.borg.model;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Warning;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Option;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "Theme")
@XmlAccessorType(XmlAccessType.FIELD)
/**
 * Each Theme instance holds the values for the user tunable UI colors The Theme
 * class also manages the persistence of Themes in the database and keeps a
 * cache of Theme objects Themes are persisted as options in the OPTIONS table
 */
@Data
public class Theme {

	static public final String COLOR1 = "color1";
	static public final String COLOR2 = "color2";
	static public final String COLOR3 = "color3";
	static public final String COLOR4 = "color4";
	static public final String COLOR5 = "color5";
	static public final String TASKCOLOR = "taskColor";
	static public final String BIRTHDAYCOLOR = "birthdayColor";
	static public final String HOLIDAYCOLOR = "holidayColor";

	// the preference which holds the name of the current (active) theme
	static private PrefName CURRENT_THEME = new PrefName("current_theme", "BORG");

	// the default theme name. A default theme is always kept and cannot be
	// deleted.
	// giving the default theme a name is easier than having it empty.
	private final static String DEFAULT_THEME_NAME = "BORG";
	private final static String DEFAULT_DARK_THEME_NAME = "BORG_DARK";

	// theme cache
	private static Map<String, Theme> themes = null;

	static {
		new OptionListener();
	}

	/**
	 * class that listens for an import of the options and triggers a sync of
	 * the themes cache
	 *
	 */
	private static class OptionListener implements Model.Listener {

		public OptionListener() {
			OptionModel.getReference().addListener(this);
		}

		@Override
		public void update(ChangeEvent event) {
			Theme.sync();
		}

	}

	/**
	 * convert a string containing a color to an integer 
	 * code calling this method should handle the value "strike" outside of this method if needed
	 */
	public int colorFromString(String s) {

		if( s == null ) return getTextColor4();

		// includes legacy mappings of color strings to use tunable colors
		// should only be in this class
		if (s.equals("red") || s.equals(COLOR1)) {
			return getTextColor1();
		} else if (s.equals("blue")|| s.equals(COLOR2)) {
			return getTextColor2();
		} else if (s.equals("green")|| s.equals(COLOR3)) {
			return getTextColor3();
		} else if (s.equals("black")|| s.equals(COLOR4)) {
			return getTextColor4();
		} else if (s.equals("white")|| s.equals(COLOR5)) {
			return getTextColor5();
		} else if (s.equals(TASKCOLOR)) {
			return getTaskTextColor();
		} else if (s.equals(BIRTHDAYCOLOR)) {
			return getBirthdayTextColor();
		} else if (s.equals(HOLIDAYCOLOR)) {
			return getHolidayTextColor();
		}
		
		// if string is a number, return it
		try {
			Integer i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			return 0; // black
		}

	}

	/**
	 * delete a theme by name
	 * 
	 * @param name
	 *            the Theme name
	 * @throws Exception
	 */
	public static void delete(String name) throws Exception {
		if (name == null || name.isEmpty())
			return;

		// do not allow the default theme to be deleted
		if (name.equals(DEFAULT_THEME_NAME))
			return;

		// delete from cache
		if (themes == null)
			loadThemes();
		themes.remove(name);

		// delete from db
		Option option = new Option(getKey(name), null);
		OptionModel.getReference().setOption(option);

		// if the active theme is deleted, then make the default active
		if (name.equals(Prefs.getPref(CURRENT_THEME)))
			Prefs.putPref(CURRENT_THEME, DEFAULT_THEME_NAME);

	}

	/**
	 * return the active Theme
	 */
	public static final Theme getCurrentTheme() {
		String cur = Prefs.getPref(CURRENT_THEME);
		Theme t = getTheme(cur);

		// if the active theme is not found(unexpected), then
		// create a theme with default values and return it
		// this is not expected to happen
		if (t == null) {
			t = new Theme();
			t.setName(cur);
		}
		return t;
	}

	/**
	 * map the theme name to a persistence key for a theme
	 * 
	 * @param name
	 *            - the name
	 * @return - the persistence key
	 */
	private static String getKey(String name) {
		return "THEME_" + name;
	}

	/**
	 * find a Theme by name
	 * 
	 * @param name
	 *            the Theme name
	 * @return the Theme or null
	 */
	public static final Theme getTheme(String name) {
		if (themes == null)
			loadThemes();
		return themes.get(name);
	}

	/**
	 * return a list of the persisted theme names
	 */
	public static Collection<String> getThemeNames() {
		if (themes == null)
			loadThemes();
		return themes.keySet();
	}

	/**
	 * sync with the db. called if the options table is changed by something
	 * other than the UI (such as import)
	 */
	public static void sync() {
		loadThemes();

		// notify listeners that Prefs may have changed
		Prefs.notifyListeners();
	}

	/**
	 * load all themes from the database into the cache. should only be called
	 * to initialized the cache once
	 */
	private static void loadThemes() {

		themes = new HashMap<String, Theme>();

		// find all options that hold themes based on the key
		try {
			Collection<Option> options = OptionModel.getReference().getOptions();
			for (Option option : options) {
				if (option.getKey().startsWith("THEME_")) {
					// load from XML
					JAXBContext context = JAXBContext.newInstance(Theme.class);
					Unmarshaller u = context.createUnmarshaller();
					Theme theme = (Theme) u.unmarshal(new StringReader(option.getValue()));
					themes.put(theme.getName(), theme);
				}
			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}

		// if no default themes exist in the database, then create them
		if (themes.get(DEFAULT_THEME_NAME) == null) {
			Theme t = new Theme();
			t.setName(DEFAULT_THEME_NAME);
			themes.put(t.getName(), t);
			try {
				t.save();
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
			Prefs.putPref(CURRENT_THEME, DEFAULT_THEME_NAME);
			
		}
		if (themes.get(DEFAULT_DARK_THEME_NAME) == null) {
			Theme t = new Theme();
			t.birthdayTextColor = -13395457;
			t.defaultBg = -12632257;
			t.defaultFg = -3355444;
			t.halfdayBg = -13421824;
			t.holidayBg = -13421773;
			t.holidayTextColor = -10066432;
			t.reminderBg = -16777216;
			t.stripeBg = -13421773;
			t.taskTextColor = -16711681;
			t.textColor1 = -52429;
			t.textColor2 = -16724788;
			t.textColor3 = -10027162;
			t.textColor4 = -3355444;
			t.textColor5 = -6710887;
			t.todayBg = -16751002;
			t.vacationBg = -16764160;
			t.weekdayBg = -12632257;
			t.weekendBg = -12632257;
			t.trayIconBg = -13421773;
			t.trayIconFg = -3355444;
			
			t.setName(DEFAULT_DARK_THEME_NAME);
			themes.put(t.getName(), t);
			try {
				t.save();
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
			
		}

	}

	/**
	 * Set a theme to be the active theme
	 * 
	 * @param t
	 *            the Theme
	 * @throws Warning
	 * @throws Exception
	 */
	public static void setCurrentTheme(Theme t) throws Warning, Exception {
		String name = t.getName();
		if (name == null)
			return;

		// persist the theme if it has not yet been saved
		if (getTheme(name) == null) {
			t.save();
		}
		Prefs.putPref(CURRENT_THEME, name);
	}

	// the Theme colors and default values
	private int birthdayTextColor = 10027008;
	private int defaultBg = 240 * 256 * 256 + 240 * 256 + 240;
	private int defaultFg = 0;
	private int halfdayBg = 13421823;
	private int holidayBg = 255 * 256 * 256 + 225 * 256 + 196;
	private int holidayTextColor = 10027212;
	private String name;
	private int reminderBg = 0xFFFF99;
	private int stripeBg = 15792890;
	private int taskTextColor = 13158;
	private int textColor1 = 13369395;
	private int textColor2 = 6684876;
	private int textColor3 = 39168;
	private int textColor4 = 13107;
	private int textColor5 = 16250609;
	private int todayBg = 255 * 256 * 256 + 200 * 256 + 200;
	private int vacationBg = 13434828;
	private int weekdayBg = 16777164;
	private int weekendBg = 255 * 256 * 256 + 225 * 256 + 196;
	private int trayIconBg = 255 * 256 * 256 + 255 * 256 + 255;
	private int trayIconFg = 153;

	/**
	 * save the current theme in the database
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	public void save() throws Exception {
		if (name == null || name.isEmpty())
			return;

		if (themes == null)
			loadThemes();

		// save cache entry
		themes.put(name, this);

		// marshall to XML and persist as an option in the DB
		JAXBContext context = JAXBContext.newInstance(Theme.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		m.marshal(this, sw);
		Option option = new Option(getKey(name), sw.toString());
		OptionModel.getReference().setOption(option);
	}

}
