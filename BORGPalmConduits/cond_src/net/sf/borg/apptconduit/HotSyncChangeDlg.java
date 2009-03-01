package net.sf.borg.apptconduit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
//import java.awt.Rectangle;


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

    /** Constant String indicating HandHeld should overwrite Desktop
    */
    static final String SYNC_HH_TO_PC = "Quick Sync and WIPE";

    /** Constant String indicating Desktop should overwrite HandHeld
    */
    static final String SYNC_PC_TO_HH = "Desktop **WIPES** and Overwrites Handheld";

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
        
        Panel buttonPanel = new Panel();
        Panel optionsPanel = new Panel();
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
		
		
		// pc2hh checkbox
   		pc2hh.setCheckboxGroup(Group1);
		pc2hh.setLabel("Desktop **WIPES** and Overwrites Handheld");
		gbc.gridx = 0;
		gbc.gridy = 0;	
		optionsPanel.add(pc2hh, gbc);
		
		// hh2pc checkbox
   		hh2pc.setCheckboxGroup(Group1);
		hh2pc.setLabel("Quick Sync and WIPE (under development)");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		optionsPanel.add(hh2pc, gbc);
		
		// Do_nothing checkbox
   		do_nothing.setCheckboxGroup(Group1);
    	do_nothing.setLabel(SYNC_NOTHING);
		gbc.gridx = 0;
		gbc.gridy = 3;
		optionsPanel.add(do_nothing, gbc);

		add(buttonPanel, "East");
		add(optionsPanel, "Center");
		
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymMouse aSymMouse = new SymMouse();

		ok.addMouseListener(aSymMouse);
		cancel.addMouseListener(aSymMouse);

		if(sync.equals(SYNC_PC_TO_HH)) 
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
	java.awt.CheckboxGroup Group1 = new java.awt.CheckboxGroup();
	java.awt.Checkbox pc2hh = new java.awt.Checkbox();
	java.awt.Checkbox hh2pc = new java.awt.Checkbox();
	java.awt.Checkbox do_nothing = new java.awt.Checkbox();
	
	//}}

    String labeltxt = " HotSync Action for ";
    String syncTemporary = SYNC_HH_TO_PC;
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
		if ( hh2pc.getState() )
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
