package net.sf.borg.ui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import net.sf.borg.common.util.Resource;
import net.sf.borg.model.ReminderTimes;

public class ReminderTimePanel extends JPanel {

	/**
	 * This is the default constructor
	 */
	public ReminderTimePanel() {
		super();
		snum = ReminderTimes.getNum();
		spinners = new JSpinner[snum];
		
		initialize();
	}
	
	private JSpinner spinners[];
	private int snum = 0;
	
	public void setTimes()
	{
		int arr[] = new int[snum];
		for( int i = 0; i < snum; i++)
		{
			Integer ii = (Integer)spinners[i].getValue();
			arr[i] = ii.intValue();
		}
		ReminderTimes.setTimes(arr);
		loadTimes();
	}
	
	private void loadTimes()
	{
		for( int i = 0; i < snum; i++)
		{
			spinners[i].setValue(new Integer(ReminderTimes.getTimes(i)));
		}
	}
	
	private void initialize() {
		String title = Resource.getPlainResourceString("Popup_Times") + " (" + 
				Resource.getPlainResourceString("Minutes") + ")";
		Border b = BorderFactory.createTitledBorder(this.getBorder(), title);
		setBorder(b);
		setLayout( new GridLayout(2,0));
		for( int i = 0; i < snum; i++)
		{
			spinners[i] = new JSpinner(new SpinnerNumberModel());
			spinners[i].setValue(new Integer(ReminderTimes.getTimes(i)));
			this.add(spinners[i]);
		}
	}

}
