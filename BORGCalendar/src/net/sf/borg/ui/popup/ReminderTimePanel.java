/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.popup;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import net.sf.borg.common.Resource;
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
		String title = Resource.getResourceString("Popup_Times") + " (" + 
				Resource.getResourceString("Minutes") + ")";
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
