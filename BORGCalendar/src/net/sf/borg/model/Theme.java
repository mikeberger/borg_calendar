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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Warning;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.entity.BorgOption;

@XmlRootElement(name = "Theme")
@XmlAccessorType(XmlAccessType.FIELD)
/**
 * Each Theme instance holds the values for the user tunable UI colors
 * The Theme class also manages the persistence of Themes in the database and keeps a cache of Theme objects
 * Themes are persisted as options in the OPTIONS table
 */
public class Theme {

	// the preference which holds the name of the current (active) theme
 	static private PrefName CURRENT_THEME = new PrefName("current_theme", "BORG");
	
 	// the default theme name. A default theme is always kept and cannot be deleted.
	// giving the default theme a name is easier than having it empty.
	private final static String DEFAULT_THEME_NAME ="BORG";
	
	// theme cache
	private static Map<String,Theme> themes = null;
	
	/**
	 * delete a theme by name
	 * @param name the Theme name
	 * @throws Exception
	 */
	public static void delete(String name) throws Exception
	{
		if (name == null || name.isEmpty())
			return;
		
		// do not allow the default theme to be deleted
		if( name.equals(DEFAULT_THEME_NAME))
			return;
		
		// delete from cache
		if( themes == null)
			loadThemes();
		themes.remove(name);
		
		// delete from db
		BorgOption option = new BorgOption(getKey(name), null);
		JdbcDB.setOption(option);
		
		// if the active theme is deleted, then make the default active
		if( name.equals(Prefs.getPref(CURRENT_THEME)))
			Prefs.putPref(CURRENT_THEME, DEFAULT_THEME_NAME);
		
	}
	
	/**
	 * return the active Theme
	 */
	public static final Theme getCurrentTheme()
	{
		String cur = Prefs.getPref(CURRENT_THEME);
		Theme t = getTheme(cur);
		
		// if the active theme is not found(unexpected), then
		// create a theme with default values and return it
		// this is not expected to happen
		if( t == null)
		{
			t = new Theme();
			t.setName(cur);
		}
		return t;
	}
	
	/**
	 * map the theme name to a persistence key for a theme
	 * @param name - the name
	 * @return - the persistence key
	 */
	private static String getKey(String name)
	{
		return "THEME_" + name;
	}
	
	/**
	 * find a Theme by name
	 * @param name the Theme name
	 * @return the Theme or null
	 */
	public static final Theme getTheme(String name) {
		if( themes == null )
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
	 * load all themes from the database into the cache. should only be called to initialized the cache once
	 */
	private static void loadThemes()
	{
		
		themes = new HashMap<String,Theme>();
		
		// find all options that hold themes based on the key
		try {
			Collection<BorgOption> options = JdbcDB.getOptions();
			for( BorgOption option : options )
			{
				if( option.getKey().startsWith("THEME_"))
				{
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
		
		// if no themes exist in the database, then create the default one and persist it
		if( themes.isEmpty())
		{
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
		
	}

//	public static void main(String args[]) throws Exception
//	{
//		Theme t = new Theme();
//		t.setName("test");
//		JdbcDB.connect("jdbc:hsqldb:mem:xx");
//		t.save();
//		String s = JdbcDB.getOption(Theme.getKey(t.getName()));
//		System.out.println(s);
//	}

	/**
	 * Set a theme to be the active theme
	 * @param t the Theme
	 * @throws Warning
	 * @throws Exception
	 */
	public static void setCurrentTheme(Theme t) throws Warning, Exception
	{
		String name = t.getName();
		if( name == null) return;
		
		// persist the theme if it has not yet been saved
		if( getTheme(name) == null)
		{
			t.save();
		}
		Prefs.putPref(CURRENT_THEME, name);
	}
	
	// the Theme colors and default values
	private int birthdayTextColor = 10027008;
	private int defaultBg = 240*256*256+240*256+240;
	private int defaultFg = 0;
	private int halfdayBg = 13421823;
	private int holidayBg = 255*256*256+225*256+196;
	private int holidayTextColor = 10027212;
	private String name;
	private int stripeBg = 15792890;
	private int taskTextColor = 13158;
	private int textColor1 = 13369395;
	private int textColor2 = 6684876;
	private int textColor3 = 39168;
	private int textColor4 = 13107;
	private int textColor5 = 16250609;
	private int todayBg = 255*256*256+200*256+200;
	private int vacationBg = 13434828;
	private int weekdayBg = 16777164;
	private int weekendBg = 255*256*256+225*256+196;

	public int getBirthdayTextColor() {
		return birthdayTextColor;
	}
	
	public int getDefaultBg() {
		return defaultBg;
	}
	
	public int getHalfdayBg() {
		return halfdayBg;
	}

	public int getHolidayBg() {
		return holidayBg;
	}

	public int getHolidayTextColor() {
		return holidayTextColor;
	}

	public String getName() {
		return name;
	}

	public int getStripeBg() {
		return stripeBg;
	}

	public int getTaskTextColor() {
		return taskTextColor;
	}

	public int getTextColor1() {
		return textColor1;
	}

	public int getTextColor2() {
		return textColor2;
	}

	public int getTextColor3() {
		return textColor3;
	}

	public int getTextColor4() {
		return textColor4;
	}

	public int getTextColor5() {
		return textColor5;
	}

	public int getTodayBg() {
		return todayBg;
	}

	public int getVacationBg() {
		return vacationBg;
	}

	public int getWeekdayBg() {
		return weekdayBg;
	}

	public int getWeekendBg() {
		return weekendBg;
	}

	/**
	 * save the current theme in the database
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	public void save() throws Exception {
		if (name == null || name.isEmpty())
			return;
		
		if( themes == null)
			loadThemes();
		
		// save cache entry
		themes.put(name, this);

		// marshall to XML and persist as an option in the DB
		JAXBContext context = JAXBContext.newInstance(Theme.class);
		Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
		StringWriter sw = new StringWriter();
		m.marshal(this, sw);
		BorgOption option = new BorgOption(getKey(name), sw.toString());
		JdbcDB.setOption(option);
	}

	public void setBirthdayTextColor(int birthdayTextColor) {
		this.birthdayTextColor = birthdayTextColor;
	}

	public void setDefaultBg(int defaultBg) {
		this.defaultBg = defaultBg;
	}

	public void setHalfdayBg(int halfdayBg) {
		this.halfdayBg = halfdayBg;
	}

	public void setHolidayBg(int holidayBg) {
		this.holidayBg = holidayBg;
	}

	public void setHolidayTextColor(int holidayTextColor) {
		this.holidayTextColor = holidayTextColor;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStripeBg(int stripeBg) {
		this.stripeBg = stripeBg;
	}

	public void setTaskTextColor(int taskTextColor) {
		this.taskTextColor = taskTextColor;
	}

	public void setTextColor1(int textColor1) {
		this.textColor1 = textColor1;
	}

	public void setTextColor2(int textColor2) {
		this.textColor2 = textColor2;
	}

	public void setTextColor3(int textColor3) {
		this.textColor3 = textColor3;
	}

	public void setTextColor4(int textColor4) {
		this.textColor4 = textColor4;
	}

	public void setTextColor5(int textColor5) {
		this.textColor5 = textColor5;
	}

	public void setTodayBg(int todayBg) {
		this.todayBg = todayBg;
	}

	public void setVacationBg(int vacationBg) {
		this.vacationBg = vacationBg;
	}

	public void setWeekdayBg(int weekdayBg) {
		this.weekdayBg = weekdayBg;
	}

	public void setWeekendBg(int weekendBg) {
		this.weekendBg = weekendBg;
	}

	public int getDefaultFg() {
		return defaultFg;
	}

	public void setDefaultFg(int defaultFg) {
		this.defaultFg = defaultFg;
	}
}
