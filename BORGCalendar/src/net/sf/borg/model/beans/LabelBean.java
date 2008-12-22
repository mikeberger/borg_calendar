package net.sf.borg.model.beans;

import java.util.Date;



public class LabelBean implements CalendarBean {

	private String color;
	private Date date;
	private String Text;
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public Integer getDuration() {
		return new Integer(0);
	}
	public Date getNextTodo() {	
		return null;
	}
	public boolean getTodo() {
		return false;
	}
	
	
}
