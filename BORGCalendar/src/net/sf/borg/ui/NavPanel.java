package net.sf.borg.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.borg.common.Resource;
import net.sf.borg.ui.util.DateDialog;

public class NavPanel extends JPanel {
    private Navigator nav_ = null;

    private JButton label = new JButton();

    public NavPanel(Navigator nav) {

	nav_ = nav;

	setLayout(new GridBagLayout());

	JButton Prev = new JButton();
	Prev.setMaximumSize(new Dimension(16,16));
	Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Back16.gif")));
	// ResourceHelper.setText(Prev, "<<__Prev");
	Prev.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		nav_.prev();
		label.setText(nav_.getNavLabel());
	    }
	});

	JButton Next = new JButton();
	Next.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Forward16.gif")));
	// ResourceHelper.setText(Next, "Next__>>");
	Next.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
	Next.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		nav_.next();
		label.setText(nav_.getNavLabel());
	    }
	});

	JButton Today = new JButton();
	Today.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Home16.gif")));
	Today.setToolTipText(Resource.getPlainResourceString("Today"));
	// ResourceHelper.setText(Today, "Today");
	Today.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		nav_.today();
		label.setText(nav_.getNavLabel());
	    }
	});

	JButton Goto = new JButton();
	Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Undo16.gif")));
	Goto.setToolTipText(Resource.getPlainResourceString("Go_To"));
	// ResourceHelper.setText(Goto, "Go_To");
	Goto.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		DateDialog dlg = new DateDialog(null);
		dlg.setCalendar(new GregorianCalendar());
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
		    return;
		nav_.goTo(dlgcal);
		label.setText(nav_.getNavLabel());
	    }
	});

	//label.setFont(new java.awt.Font("Dialog", 0, 24));
	label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	label.setText(nav_.getNavLabel());

	GridBagConstraints cons = new GridBagConstraints();
	cons.fill = GridBagConstraints.NONE;
	cons.gridx = 0;
	cons.gridy = 0;
	add(Today, cons);

	cons.gridx = 1;
	add(Prev, cons);

	cons.gridx = 2;
	cons.weightx = 1.0;
	cons.fill = GridBagConstraints.HORIZONTAL;
	add(label, cons);

	cons.fill = GridBagConstraints.NONE;
	cons.weightx = 0.0;
	cons.gridx = 3;
	add(Next, cons);
	
	cons.gridx = 4;
	add(Goto, cons);

    }
    
    public void setLabel(String l)
    {
	label.setText(l);
    }

}
