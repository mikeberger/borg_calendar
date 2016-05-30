package net.sf.borg.model;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.entity.LabelEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class SpecialDay {

	private static final String CANADA = "CAN";
	private static final String US = "US";
	private static final String GLOBAL = "GLOBAL";
	static final String TRUE = "true";
	public static final boolean SHOW_US_HOLIDAYS = Prefs.getPref(PrefName.SHOWUSHOLIDAYS).equals(TRUE);
	public static final boolean SHOW_CAN_HOLIDAYS = Prefs.getPref(PrefName.SHOWCANHOLIDAYS).equals(TRUE);
	private static final String PURPLE = "purple";
	private String name = "";
	 private int month;
	 private int day;
	 private boolean isFreeDay = false;
	 private String region = "";
	 
	 public SpecialDay(String name, int day, int month, boolean isFreeDay, String region) {
		 setName(name);
		 setDay(day);
		 setMonth(month);
		 setFreeDay(isFreeDay);
		 setRegion(region);
	 }

	private static List<SpecialDay> initSpecialDays(int year, int month) {

		List<SpecialDay> specialDays = new ArrayList<>();

		// American
		specialDays.add(new SpecialDay("Halloween", 31, 9, false, US));
		specialDays.add(new SpecialDay("Independence_Day ", 4, 6, true, US));
		specialDays.add(new SpecialDay("Ground_Hog_Day", 2, 1, false, US));
		specialDays.add(new SpecialDay("Valentine's_Day", 14, 1, false, US));
		specialDays.add(new SpecialDay("St._Patrick's_Day", 17, 2, false, US));
		specialDays.add(new SpecialDay("Veteran's_Day", 11, 10, false, US));
		specialDays.add(new SpecialDay("Labor_Day", nthdom(year, month, Calendar.MONDAY, 1), 8, true, US));
		specialDays.add(new SpecialDay("Martin_Luther_King_Day", nthdom(year, month, Calendar.MONDAY, 3), 0, false, US));
		specialDays.add(new SpecialDay("Presidents_Day", nthdom(year, month, Calendar.MONDAY, 3), 1, false, US));
		specialDays.add(new SpecialDay("Memorial_Day", nthdom(year, month, Calendar.MONDAY, -1), 4, true, US));
		specialDays.add(new SpecialDay("Columbus_Day", nthdom(year, month, Calendar.MONDAY, 2), 9, false, US));
		specialDays.add(new SpecialDay("Mother's_Day", nthdom(year, month, Calendar.SUNDAY, 2), 4, false, US));
		specialDays.add(new SpecialDay("Father's_Day", nthdom(year, month, Calendar.SUNDAY, 3), 5, false, US));
		specialDays.add(new SpecialDay("Thanksgiving", nthdom(year, month, Calendar.THURSDAY, 4), 10, true, US));

		// Canadian
		specialDays.add(new SpecialDay("Canada_Day", 1, 6, false, CANADA));
		specialDays.add(new SpecialDay("Boxing_Day", 26, 11, false, CANADA));
		specialDays.add(new SpecialDay("Civic_Holiday", nthdom(year, month, Calendar.MONDAY, 1), 7, false, CANADA));
		specialDays.add(new SpecialDay("Remembrance_Day", 11, 10, false, CANADA));
		specialDays.add(new SpecialDay("Labour_Day_(Can)", nthdom(year, month, Calendar.MONDAY, 1), 8, false, CANADA));
		specialDays.add(new SpecialDay("Commonwealth_Day", nthdom(year, month, Calendar.MONDAY, 2), 2, false, CANADA));
		specialDays.add(new SpecialDay("Thanksgiving_(Can)", nthdom(year, month, Calendar.MONDAY, 2), 9, false, CANADA));

		// Common
		specialDays.add(new SpecialDay("New_Year's_Day", 1, 0, true, GLOBAL));
		specialDays.add(new SpecialDay("Christmas", 25, 11, true, GLOBAL));

		return specialDays;
	}

	static LabelEntity getPossibleSpecialDayLabel(int year, int month, int day, Day dayToGet) {
		LabelEntity specialDayLabel = new LabelEntity();
		specialDayLabel.setDate(new GregorianCalendar(year, month, day, 00, 00).getTime());
		specialDayLabel.setColor(PURPLE);
		specialDayLabel.setText(null);

		for (SpecialDay currentSpecialDay : initSpecialDays(year, month)) {

			if (checkIfDayHasSpecialDayToShow(month, day, currentSpecialDay, US)) {
				setHolidayLabelToDay(dayToGet, specialDayLabel, currentSpecialDay);
			}
			if (checkIfDayHasSpecialDayToShow(month, day, currentSpecialDay, CANADA)) {
				setHolidayLabelToDay(dayToGet, specialDayLabel, currentSpecialDay);
			}

			if (currentSpecialDay.getRegion().equals(GLOBAL) && currentSpecialDay.isSpecialDay(day, month)) {
				setHolidayLabelToDay(dayToGet, specialDayLabel, currentSpecialDay);
			}
			if(currentSpecialDay.getRegion().equals(CANADA) && SHOW_CAN_HOLIDAYS && checkIfDayIsVictoriaDay(year, month, day)) {
				specialDayLabel.setText(Resource.getResourceString("Victoria_Day"));
			}

			if (specialDayLabel.getText() != null) {
				return specialDayLabel;
			}
		}
		return null;
	}

	private static boolean checkIfDayHasSpecialDayToShow(int month, int day, SpecialDay currentSpecialDay, String region) {
		if(region.equals(US))
			return currentSpecialDay.getRegion().equals(region) && SHOW_US_HOLIDAYS && currentSpecialDay.isSpecialDay(day, month);
		else if(region.equals(CANADA))
			return currentSpecialDay.getRegion().equals(region) && SHOW_CAN_HOLIDAYS && currentSpecialDay.isSpecialDay(day, month);
		return false;
	}

	private static boolean checkIfDayIsVictoriaDay(int year, int month, int day) {
		if (month == 4) {
			GregorianCalendar gc = new GregorianCalendar(year, month, 25);
            int diff = gc.get(Calendar.DAY_OF_WEEK);
            diff += 5;
            if (diff > 7)
                diff -= 7;
            if (day == 25 - diff) {
                return true;
            }
        }
		return false;
	}

	private static void setHolidayLabelToDay(Day ret, LabelEntity specialDayLabel, SpecialDay current) {
		ret.setHoliday(current.isFreeDay() ? 1 : 0);
		specialDayLabel.setText(Resource.getResourceString(current.getName()));
	}

	/**
	 * compute nth day of month for calculating when certain holidays fall.
	 *
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param dayofweek
	 *            the day of the week
	 * @param week
	 *            the week of the month
	 *
	 * @return the date
	 */
	private static int nthdom(int year, int month, int dayofweek, int week) {
		GregorianCalendar cal = new GregorianCalendar(year, month, 1);
		cal.set(Calendar.DAY_OF_WEEK, dayofweek);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
		return cal.get(Calendar.DATE);
	}

	public boolean isSpecialDay(int day, int month) {
	  		return (day == this.day && month == this.month) ? true : false;
	 }

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public boolean isFreeDay() {
		return isFreeDay;
	}

	public void setFreeDay(boolean isFreeDay) {
		this.isFreeDay = isFreeDay;
	}

	 
}
