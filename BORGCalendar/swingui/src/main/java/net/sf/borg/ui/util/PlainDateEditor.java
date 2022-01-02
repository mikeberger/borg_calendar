package net.sf.borg.ui.util;

import java.util.Date;

import javax.swing.JLabel;
import javax.swing.event.CaretEvent;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JTextFieldDateEditor;

public class PlainDateEditor extends JTextFieldDateEditor implements IDateEditor {

	
	private static final long serialVersionUID = -209404652507744986L;

	protected void setDate(Date date, boolean firePropertyChange) {
		
		super.setDate(date, firePropertyChange);
		setForeground(new JLabel().getForeground());
		
	}
	
	public void caretUpdate(CaretEvent event) {
		
	}

}
