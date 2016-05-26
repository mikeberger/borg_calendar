package net.sf.borg.model;

public class SpecialDay {
	
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
