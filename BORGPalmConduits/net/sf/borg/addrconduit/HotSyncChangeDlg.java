package net.sf.borg.addrconduit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
//import java.awt.Rectangle;

import net.sf.borg.addrconduit.ImageCanvas;
import net.sf.borg.addrconduit.OptionsPanel;


//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."
/**
 * A basic extension of the java.awt.Dialog class that implements the
 * HotSync "Custom..." "Change..." dialog for a particular conduit for
 * sync configuration.
 */
public class HotSyncChangeDlg extends Dialog
{
    /**  boolean to set the chosen SYNC type as default */
    boolean setDefault = false;
        
    /** Constant String indicating no action should be taken by conduit
    */
    static final String SYNC_NOTHING = "Do Nothing";

    /** Constant String indicating conduit should synchronize the data
    */
	static final String SYNC_SYNC = "Synchronize the files";

    /** Constant String indicating HandHeld should overwrite Desktop
    */
    static final String SYNC_HH_TO_PC = "Handheld overwrites Desktop";

    /** Constant String indicating Desktop should overwrite HandHeld
    */
    static final String SYNC_PC_TO_HH = "Desktop overwrites Handheld";

    /**
     * One and only constructor.  Creates the dialog and lays out its contents
     * and establish component listeners.
     * 
     * @param parent Frame object who is this dialog's parent.
     * @param sync String representing the current sync state.
     * @param label String representing the conduit name.
     */
	public HotSyncChangeDlg(Frame parent, String sync, String label)
	{
		super(parent);

        Image im;
        ImageCanvas canvas;
        
        Panel buttonPanel = new Panel();
        Panel optionsPanel = new OptionsPanel();
		GridBagConstraints gbc = new GridBagConstraints();
        
		setTitle("Change HotSync Action");
		setResizable(false);
		setLayout(new BorderLayout());
		
		buttonPanel.setLayout(new GridBagLayout());
		// ok button
		ok.setLabel("OK");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(20, 10, 2, 40);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(ok, gbc);
		// cancel button
		cancel.setLabel("Cancel");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(2, 10, 2, 40);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(cancel, gbc);
		// chkdef checkbox
		chkdef.setLabel("Set as default");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(20, 10, 2, 15);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.NONE;
		buttonPanel.add(chkdef, gbc);
		
		optionsPanel.setLayout(new GridBagLayout());
		// checkbox label
		checkbox_label.setText(labeltxt + label);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(checkbox_label, gbc);
		// sync_sync image
		im = getToolkit().getImage("Sync0.gif");
		ImageCanvas.waitForImage(this, im);
	    canvas = new ImageCanvas(im);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(canvas, gbc);
    	// sync_sync checkbox
   		sync_sync.setCheckboxGroup(Group1);
		sync_sync.setLabel(SYNC_SYNC);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 20);
		gbc.gridx = 1;
		gbc.gridy = 1;
    	gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(sync_sync, gbc);
		// pc2hh image
		im = getToolkit().getImage("Sync1.gif");
		ImageCanvas.waitForImage(this, im);
	    canvas = new ImageCanvas(im);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(canvas, gbc);
		// pc2hh checkbox
   		pc2hh.setCheckboxGroup(Group1);
		pc2hh.setLabel("Desktop WIPES Handheld!!!");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 20);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(pc2hh, gbc);
		// hh2pc image
		im = getToolkit().getImage("Sync2.gif");
		ImageCanvas.waitForImage(this, im);
	    canvas = new ImageCanvas(im);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		//optionsPanel.add(canvas, gbc);
		// hh2pc checkbox
   		hh2pc.setCheckboxGroup(Group1);
		hh2pc.setLabel("Handheld overwrites Desktop");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 20);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		//optionsPanel.add(hh2pc, gbc);
		// do_nothing image
		im = getToolkit().getImage("Sync3.gif");
		ImageCanvas.waitForImage(this, im);
	    canvas = new ImageCanvas(im);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(canvas, gbc);
		// Do_nothing checkbox
   		do_nothing.setCheckboxGroup(Group1);
    	do_nothing.setLabel(SYNC_NOTHING);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(10, 20, 25, 20);
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		optionsPanel.add(do_nothing, gbc);

		add(buttonPanel, "East");
		add(optionsPanel, "Center");
		
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymMouse aSymMouse = new SymMouse();

		ok.addMouseListener(aSymMouse);
		cancel.addMouseListener(aSymMouse);

		checkbox_label.setBounds(12,12,(int)((labeltxt.length() + label.length())*5.2),24);
		checkbox_label.setText(labeltxt + label);

		if(sync.equals(SYNC_SYNC)) 
		    sync_sync.setState(true);
		else if(sync.equals(SYNC_PC_TO_HH)) 
		    pc2hh.setState(true);
		else if(sync.equals(SYNC_HH_TO_PC)) 
		    hh2pc.setState(true);
		else if(sync.equals(SYNC_NOTHING)) 
		    do_nothing.setState(true);
        syncChanged = false;
        
        setVisible(false);
        setModal(true);
        
        pack(); // resize dialog to fit components nicely
	}
	

    /**
     * Shows or hides the component depending on the boolean flag b.
     * @param b  if true, show the dialog and set its location on its parent.
     */
    public void setVisible(boolean b)
	{
		if(b)
		{
			//Rectangle bounds = getParent().getBounds();
			//Rectangle abounds = getBounds();
			setLocation(50, 50);
		}
		super.setVisible(b);
	}

	//{{DECLARE_CONTROLS
	java.awt.Button ok = new java.awt.Button();
	java.awt.Button cancel = new java.awt.Button();
	java.awt.Checkbox chkdef = new java.awt.Checkbox();
	java.awt.Checkbox sync_sync = new java.awt.Checkbox();
	java.awt.CheckboxGroup Group1 = new java.awt.CheckboxGroup();
	java.awt.Checkbox pc2hh = new java.awt.Checkbox();
	java.awt.Checkbox hh2pc = new java.awt.Checkbox();
	java.awt.Checkbox do_nothing = new java.awt.Checkbox();
	java.awt.Label checkbox_label = new java.awt.Label();
	//}}

    String labeltxt = " HotSync Action for ";
    String syncTemporary = SYNC_SYNC;
    boolean syncChanged = false;
    
	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == HotSyncChangeDlg.this)
				HotSyncChangeDlg_WindowClosing(event);
		}
	}
	
	void HotSyncChangeDlg_WindowClosing(java.awt.event.WindowEvent event)
	{
		setVisible(false);
	}

	class SymMouse extends java.awt.event.MouseAdapter
	{
		public void mouseClicked(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == ok)
				ok_MouseClicked(event);
			else if (object == cancel)
				cancel_MouseClicked(event);
		}
	}

	void ok_MouseClicked(java.awt.event.MouseEvent event)
	{
		if ( sync_sync.getState() )
		   syncTemporary = SYNC_SYNC;
		else if ( hh2pc.getState() )
		    syncTemporary = SYNC_HH_TO_PC;
		else if ( pc2hh.getState() )
		    syncTemporary = SYNC_PC_TO_HH;
		else if ( do_nothing.getState() )
		    syncTemporary = SYNC_NOTHING;
		syncChanged = true;
		setDefault = chkdef.getState();
		this.dispose();	 
	}

	void cancel_MouseClicked(java.awt.event.MouseEvent event)
	{
		syncChanged = false;
		setDefault = false;
		this.dispose();
	}
}
