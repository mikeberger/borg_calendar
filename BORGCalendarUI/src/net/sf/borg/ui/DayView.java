/*
This file is part of BORG.

    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.PrintHelper;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;

// weekView handles the printing of a single week
class DayView extends View implements Navigator
{

    private DayPanel dayPanel;


    static void printDay(int month, int year, int date) throws Exception
    {

        // use the Java print service
        // this relies on dayPanel.print to fill in a Graphic object and respond to the Printable API
        DayPanel cp = new DayPanel(month,year,date);
        PrintHelper.printPrintable(cp);
    }

    private void printDay() throws Exception
    {
        PrintHelper.printPrintable(dayPanel);
    }

    public void print()
    {
        try
        {
            printDay();
        }
        catch( Exception e )
        {
            Errmsg.errmsg(e);
        }
    }

    public DayView(int month, int year, int date)
    {
        super();
        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
        dayPanel = new DayPanel(month,year,date);
        dayPanel.setBackground(Color.WHITE);
        dayPanel.setPreferredSize(new Dimension(800,600));

        // for the preview, create a JFrame with the preview panel and print menubar
        JMenuBar menubar = new MainMenu(this).getMenuBar();
        
        menubar.setBorder(new BevelBorder(BevelBorder.RAISED));

        setJMenuBar(menubar);
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints cons = new java.awt.GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.fill = java.awt.GridBagConstraints.BOTH;
        cons.weightx = 1.0;
        cons.weighty = 1.0;

        getContentPane().add(dayPanel, cons);
        
        cons = new java.awt.GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 1;
        cons.fill = java.awt.GridBagConstraints.BOTH;
        cons.weightx = 0.0;
        cons.weighty = 0.0;
        
        getContentPane().add(getNavPanel(), cons);
        
        ResourceHelper.setTitle(this, "Day_View");
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        pack();
        setVisible(true);

        manageMySize(PrefName.DAYVIEWSIZE);
    }


    public void destroy()
    {
        this.dispose();
    }

    public void refresh()
    {
    	dayPanel.clearData();
    	dayPanel.repaint();
    }
    
	public void next() {
		dayPanel.next();
		
	}

	public void prev() {
		dayPanel.prev();
		
	}

	public void today() {
		dayPanel.today();
		
	}

	public void goTo(Calendar cal) {
		dayPanel.goTo(cal);
	}
	
	private JPanel navPanel = null;
	private JPanel getNavPanel() {
		if (navPanel == null) {
			GridLayout gridLayout62 = new GridLayout();
			navPanel = new JPanel();
			navPanel.setLayout(gridLayout62);
			gridLayout62.setRows(1);
			JButton Prev = new JButton();
			Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/resource/Back16.gif")));
			ResourceHelper.setText(Prev, "<<__Prev");
			Prev.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					prev();
				}
			});
			
			JButton Next = new JButton();
			Next.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/resource/Forward16.gif")));
			ResourceHelper.setText(Next, "Next__>>");
			Next.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			Next.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					next();
				}
			});
			
			JButton Today = new JButton();
			Today.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/resource/Home16.gif")));
			ResourceHelper.setText(Today, "Today");
			Today.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					today();
				}
			});

			JButton Goto = new JButton();
			Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/resource/Undo16.gif")));
			ResourceHelper.setText(Goto, "Go_To");
			Goto.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					DateDialog dlg = new DateDialog(null);
					dlg.setCalendar(new GregorianCalendar());
					dlg.setVisible(true);
					Calendar dlgcal = dlg.getCalendar();
					if (dlgcal == null)
						return;
					goTo(dlgcal);
				}
			});
			navPanel.add(Prev, null);
			navPanel.add(Today, null);
			navPanel.add(Goto, null);
			navPanel.add(Next, null);
		}
		return navPanel;
	}
}



