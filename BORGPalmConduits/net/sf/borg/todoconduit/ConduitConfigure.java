package  net.sf.borg.todoconduit;

import java.awt.Dialog;
import java.awt.Frame;

import palm.conduit.ConfigureConduitInfo;
import palm.conduit.SyncProperties;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."

/**
 * Generic object that handles conduit configuration when invoked
 * via the HotSync "Custom..." menu option for configuring conduits.
 * For addrcond sample, this object displays the dialog for configuration
 * and stores configuration information into a properties file 
 * (hotsync can only store some standard configuration information
 * for a conduit such as sync state and default sync method.  It cannot
 * store any additional information, so the conduit has to manage it
 * itself.)
 */

public class ConduitConfigure {

    /** Name of the conduit as provided through the constructor */
    private String name;

    /** Path to the configuration properties file on the PC */
    private ConfigureConduitInfo info;

    /**
     * int indicating the chosen SYNC type, as described by SyncProperties
     * 
     * @see palm.conduit.SyncProperties
     */
    public int saveState = SyncProperties.SYNC_FAST;
    
    /**
     * boolean indicating if the passed-in data has been changed or not by this
     * class
     */
    public boolean dataChanged = false;

    /** boolean indicating whether the chosen SYNC type is the Default */
    public boolean setDefault = false;
    
    
    /**
     * Constructor. Takes ConfigureConduitInfo and the name of the conduit
     * as arguments.
     * 
     * @param info A ConfigureConduitInfo object that contains info stored by hotsync about
     * the sync configuration.
     * @param name a String object represents the name of the conduit (should not be modified
     * by this class).
     */
    public ConduitConfigure(ConfigureConduitInfo info, String name) 
    {
        this.info = info;
        this.name = name;
    }

    /**
     * Creates the configuration dialog box and processes the results from it.
     */
    public void createDialog() 
    {
        // The Configuration dialog
        net.sf.borg.todoconduit.HotSyncChangeDlg configureDialog;

        Frame f = new Frame();
    
    	// Create the HotSync Change dialog
		configureDialog = new HotSyncChangeDlg(f, readProps(), name);
		configureDialog.setVisible(true);
		
		if ( configureDialog.syncChanged )
		{
		    Dialog dlg = new Dialog(f);
		    dlg.getToolkit().beep();
		    writeProps(configureDialog.syncTemporary);
		    setDefault = configureDialog.setDefault;
		    dataChanged = true;
		}
	}

	/**
	 * Function to convert the sync type stored within the ConduitConfigureInfo
	 * member variable to a String value that the HotSyncChangeDlg can understand
	 * and use for sync type indentification.
	 * 
	 * @return String with a value representing
	 * the sync type that is contained within the ConfigureConduitInfo member
	 * variable.
	 * @see palm.conduit.SyncProperties
	 */
	private String readProps() {

        String condSyncType = "";

        switch(info.syncTemporary)
        {
            case SyncProperties.SYNC_FAST:
                condSyncType = HotSyncChangeDlg.SYNC_SYNC;
                break;
            case SyncProperties.SYNC_PC_TO_HH:
                condSyncType = HotSyncChangeDlg.SYNC_PC_TO_HH;
                break;
            case SyncProperties.SYNC_HH_TO_PC:
                condSyncType = HotSyncChangeDlg.SYNC_HH_TO_PC;
                break;
            case SyncProperties.SYNC_DO_NOTHING:
            default:
                condSyncType = HotSyncChangeDlg.SYNC_NOTHING;
                break;
        }
        return condSyncType;
    }

	/**
	 * Function to convert a String value that the HotSyncChangeDlg provides for
	 * for sync type indentification to its palm.conduit.SyncProperties int 
	 * equivalent for storage in the ConduitConfigureInfo member variable.
	 * 
	 * @param condSyncType String with a value representing
	 * a sync type chosen by the end user.
	 * @see palm.conduit.SyncProperties
	 */
    private void writeProps(String condSyncType) 
    {
   		if(condSyncType.equals(HotSyncChangeDlg.SYNC_HH_TO_PC)) 
        { 
		    saveState = SyncProperties.SYNC_HH_TO_PC; 
        }
   		else if(condSyncType.equals(HotSyncChangeDlg.SYNC_PC_TO_HH)) 
        { 
		    saveState = SyncProperties.SYNC_PC_TO_HH; 
        }
   		else if(condSyncType.equals(HotSyncChangeDlg.SYNC_SYNC)) 
        { 
		    saveState = SyncProperties.SYNC_FAST; 
        }
		else 
		{ 
		    // default
            saveState = SyncProperties.SYNC_DO_NOTHING; 
		}
	}
}
