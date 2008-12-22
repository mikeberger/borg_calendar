package net.sf.borg.model.beans;

import java.util.Date;

// a bean that can be shown on the calendar
public interface CalendarBean {

	public String getText();
	public String getColor();
	public Date getDate();
	public Integer getDuration();	
	public boolean getTodo();
	public Date getNextTodo();
	
	
}
