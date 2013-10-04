
package net.sf.borg.ui.options;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JCheckBox;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;

/**
 * Options for which views to open on startup
 * 
 */
public class StartupViewsOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = -4357089819869820396L;

	static private PrefName getPrefForView( ViewType vt )
	{
		String def = "false";
		if( vt == ViewType.MONTH)
			def = "true";
		return new PrefName("startview_" + vt.toString(), def);
	}
	
	static public boolean getStartPref(ViewType vt)
	{
		return Prefs.getBoolPref(getPrefForView(vt));
	}
	
	static public void setStartPref(ViewType vt, boolean start)
	{
		Prefs.putPref(getPrefForView(vt), start ? "true" : "false");
	}
	
	private HashMap<ViewType, JCheckBox> boxMap = new HashMap<ViewType, JCheckBox>();
	
	/**
	 * Instantiates a new Todo Options Panel.
	 */
	public StartupViewsOptionsPanel() {
		this.setLayout(new GridLayout(0,2));
		
		for( ViewType vt : MultiView.ViewType.values())
		{
			Module m = MultiView.getMainView().getModuleForView(vt);
			if( m != null )
			{
				JCheckBox cb = new JCheckBox(m.getModuleName());
				boxMap.put(vt, cb);
				this.add(cb);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		for( Entry<ViewType, JCheckBox> e : boxMap.entrySet() )
		{
			setStartPref(e.getKey(), e.getValue().isSelected());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#getPanelName()
	 */
	@Override
	public String getPanelName() {
		return Resource.getResourceString("startupViews");	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		for( Entry<ViewType, JCheckBox> e : boxMap.entrySet() )
		{
			boolean start = getStartPref(e.getKey());
			e.getValue().setSelected(start);
		}
	}

}
