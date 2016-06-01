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
	private static final String TRUE = "true";
	private static final boolean SHOW_US_HOLIDAYS = Prefs.getPref(PrefName.SHOWUSHOLIDAYS).equals(TRUE);
	private static final boolean SHOW_CAN_HOLIDAYS = Prefs.getPref(PrefName.SHOWCANHOLIDAYS).equals(TRUE);
	private static final String PURPLE = "purple";

	private String name = "";
	private int month;
	private int day;
	private boolean isFreeDay = false;
	private String region = "";
	 
	 private SpecialDay(String name, int day, int month, boolean isFreeDay, String region) {
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
		specialDays.add(new SpecialDay("Labor_Day", nthDOM(year, month, Calendar.MONDAY, 1), 8, true, US));
		specialDays.add(new SpecialDay("Martin_Luther_King_Day", nthDOM(year, month, Calendar.MONDAY, 3), 0, false, US));
		specialDays.add(new SpecialDay("Presidents_Day", nthDOM(year, month, Calendar.MONDAY, 3), 1, false, US));
		specialDays.add(new SpecialDay("Memorial_Day", nthDOM(year, month, Calendar.MONDAY, -1), 4, true, US));
		specialDays.add(new SpecialDay("Columbus_Day", nthDOM(year, month, Calendar.MONDAY, 2), 9, false, US));
		specialDays.add(new SpecialDay("Mother's_Day", nthDOM(year, month, Calendar.SUNDAY, 2), 4, false, US));
		specialDays.add(new SpecialDay("Father's_Day", nthDOM(year, month, Calendar.SUNDAY, 3), 5, false, US));
		specialDays.add(new SpecialDay("Thanksgiving", nthDOM(year, month, Calendar.THURSDAY, 4), 10, true, US));

		// Canadian
		specialDays.add(new SpecialDay("Canada_Day", 1, 6, false, CANADA));
		specialDays.add(new SpecialDay("Boxing_Day", 26, 11, false, CANADA));
		specialDays.add(new SpecialDay("Civic_Holiday", nthDOM(year, month, Calendar.MONDAY, 1), 7, false, CANADA));
		specialDays.add(new SpecialDay("Remembrance_Day", 11, 10, false, CANADA));
		specialDays.add(new SpecialDay("Labour_Day_(Can)", nthDOM(year, month, Calendar.MONDAY, 1), 8, false, CANADA));
		specialDays.add(new SpecialDay("Commonwealth_Day", nthDOM(year, month, Calendar.MONDAY, 2), 2, false, CANADA));
		specialDays.add(new SpecialDay("Thanksgiving_(Can)", nthDOM(year, month, Calendar.MONDAY, 2), 9, false, CANADA));

		// Common
		specialDays.add(new SpecialDay("New_Year's_Day", 1, 0, true, GLOBAL));
		specialDays.add(new SpecialDay("Christmas", 25, 11, true, GLOBAL));

		return specialDays;
	}

	static LabelEntity getPossibleSpecialDayLabel(int year, int month, int day, Day dayToGet) {
		for (SpecialDay currentSpecialDay : initSpecialDays(year, month)) {
			LabelEntity specialDayLabel = checkAndGetSpecialDayLabel(year, month, day, dayToGet, currentSpecialDay);
			if (specialDayLabel != null)
				return specialDayLabel;
		}
		return null;
	}

	private static LabelEntity checkAndGetSpecialDayLabel(int year, int month, int day, Day dayToGet, SpecialDay currentSpecialDay) {
		LabelEntity specialDayLabel = new LabelEntity();
		specialDayLabel.setDate(new GregorianCalendar(year, month, day, 0, 0).getTime());
		specialDayLabel.setColor(PURPLE);
		specialDayLabel.setText(null);

		if (checkIfDayHasSpecialDayToShow(month, day, currentSpecialDay, US) || checkIfDayHasSpecialDayToShow(month, day, currentSpecialDay, CANADA)) {
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
	 * @param dayOfWeek
	 *            the day of the week
	 * @param week
	 *            the week of the month
	 *
	 * @return the date
	 */
	private static int nthDOM(int year, int month, int dayOfWeek, int week) {
		GregorianCalendar cal = new GregorianCalendar(year, month, 1);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
		return cal.get(Calendar.DATE);
	}

	private boolean isSpecialDay(int day, int month) {
	  		return day == this.day && month == this.month;
	 }

	private String getRegion() {
		return region;
	}

	private void setRegion(String region) {
		this.region = region;
	}

	private String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	private void setMonth(int month) {
		this.month = month;
	}

	private void setDay(int day) {
		this.day = day;
	}

	private boolean isFreeDay() {
		return isFreeDay;
	}

	private void setFreeDay(boolean isFreeDay) {
		this.isFreeDay = isFreeDay;
	}
}
