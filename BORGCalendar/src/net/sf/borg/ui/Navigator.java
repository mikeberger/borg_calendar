package net.sf.borg.ui;

import java.util.Calendar;

public interface Navigator {

	public void next();
	public void prev();
	public void today();
	public String getNavLabel();
	public void goTo(Calendar cal);
}
