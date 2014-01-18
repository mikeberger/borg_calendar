package net.sf.borg.ui.options;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

public class AddressOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 1L;
	private JCheckBox emailvalidation;

	
	public AddressOptionsPanel(){
		
		emailvalidation = new JCheckBox();
		
		this.setLayout(new java.awt.GridBagLayout());
		this.setName(Resource.getResourceString("Address_Options"));
		
		ResourceHelper.setText(emailvalidation, "Email_Validation");
		this.add(emailvalidation,
				GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
		
	}
	
	@Override
	public String getPanelName() {
		return Resource.getResourceString("Address_Options");
	}

	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(emailvalidation, PrefName.EMAIL_VALIDATION);
	}

	@Override
	public void loadOptions() {
		OptionsPanel.setCheckBox(emailvalidation, PrefName.EMAIL_VALIDATION);
	}
}