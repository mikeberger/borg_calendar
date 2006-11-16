package net.sf.borg.common.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

import de.wannawork.jcalendar.JCalendarComboBox;

public class DateEditor implements TableCellEditor , ChangeListener
{
	private JCalendarComboBox cb_ = new JCalendarComboBox();
	private ArrayList listeners = new ArrayList();
	public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4) 
	{
	    Date d = (Date)arg1;
	    if( d == null )
		d = new Date();
	    Calendar cal = new GregorianCalendar();
	    cal.setTime(d);
	    cb_.setCalendar(cal);
	    cb_.addChangeListener(this);
	    return cb_;
	}

	public void addCellEditorListener(CellEditorListener arg0) {
	    listeners.add(arg0);
	}

	public void cancelCellEditing() { 
	    //System.out.println("cancel");
	}

	public Object getCellEditorValue() {
	   return cb_.getCalendar().getTime();
	}

	public boolean isCellEditable(EventObject arg0) {
	    return true;
	}

	public void removeCellEditorListener(CellEditorListener arg0) {
	    listeners.remove(arg0);
	}

	public boolean shouldSelectCell(EventObject arg0) {
	    return true;
	}

	public boolean stopCellEditing() {
	    return true;
	}

	public void stateChanged(ChangeEvent arg0) {
	    
	   // deep copy listeners to avoid co-modification
	   ArrayList l2 = new ArrayList();
	   Iterator it = listeners.iterator();
	   while(it.hasNext())
	   {
	       CellEditorListener cl = (CellEditorListener)it.next();
	       l2.add(cl);
	   }
	   
	   it = l2.iterator();
	   while(it.hasNext())
	   {
	       CellEditorListener cl = (CellEditorListener)it.next();
	       cl.editingStopped(arg0);
	   }
	}
	
	
}
