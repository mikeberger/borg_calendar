package net.sf.borgweb.form;

import java.util.Collection;

public class MonthDTO {

	private String title;
	private int firstDay;
	private int lastDay;
	private Collection[] days = new Collection[31];
	
	public int getFirstDay() {
		return firstDay;
	}
	public void setFirstDay(int firstDay) {
		this.firstDay = firstDay;
	}
	public int getLastDay() {
		return lastDay;
	}
	public void setLastDay(int lastDay) {
		this.lastDay = lastDay;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Collection[] getDays() {
		return days;
	}
	public void setDays(Collection[] days) {
		this.days = days;
	}
}
