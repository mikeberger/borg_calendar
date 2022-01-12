package net.sf.borg.ui.util;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import java.util.Date;

public class PlainDateEditor extends JTextFieldDateEditor implements IDateEditor {

	
	private static final long serialVersionUID = -209404652507744986L;

	protected void setDate(Date date, boolean firePropertyChange) {
		
		super.setDate(date, firePropertyChange);
		setForeground(new JLabel().getForeground());
		
	}
	
	public void caretUpdate(CaretEvent event) {
		
	}

}
