package net.sf.borg.ui.options;

import java.awt.GridBagConstraints;
import javax.swing.*;

import net.sf.borg.common.Prefs;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.LimitDocument;

public class AddressOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 1L;
	private JCheckBox emailvalidation;
	private JTextField phoneRegex;
	private JComboBox<String> phoneLoc;

	
	public AddressOptionsPanel(){
		
		emailvalidation = new JCheckBox();
			
		String [] locations = {Resource.getResourceString("None"),
				Resource.getResourceString("Germany"),
                    Resource.getResourceString("United_Kingdom"), 
                        Resource.getResourceString("United_States"),
                            Resource.getResourceString("Custom")};
		
		phoneLoc = new JComboBox<String>(locations);
		
		this.setLayout(new java.awt.GridBagLayout());
		this.setName(Resource.getResourceString("Address_Options"));
		
		ResourceHelper.setText(emailvalidation, "Email_Validation");
		this.add(emailvalidation,
				GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
		
		JLabel jl1 = new JLabel();
		ResourceHelper.setText(jl1, "Phone_Validation");
		jl1.setLabelFor(phoneLoc);
		this.add(jl1,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		
		
		phoneLoc.setEditable(false);
		phoneLoc.setSelectedIndex(0);
		this.add(phoneLoc, GridBagConstraintsFactory.create(1, 0,
				GridBagConstraints.BOTH, 1.0, 0.0));
		
		phoneRegex = new JTextField(new LimitDocument(250), null, 25);
		this.add(phoneRegex, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 0.0, 0.0));
		
		JLabel jl2 = new JLabel();
		ResourceHelper.setText(jl2, "Custom_Regular_Expression");
		jl2.setLabelFor(phoneRegex);
		this.add(jl2,
				GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
	
	}
	
	@Override
	public String getPanelName() {
		return Resource.getResourceString("Address_Options");
	}

	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub
		OptionsPanel.setBooleanPref(emailvalidation, PrefName.EMAIL_VALIDATION);
		String phonereg =" ";
		int i = phoneLoc.getSelectedIndex();
		if (i == 0)
		{
			phonereg=" ";
			Prefs.putPref(PrefName.PHONE_VALIDATION, 0);
		}
		else if (i == 1)
		{
//			//Germany
//			(0xx) xxxxxxxx
//			(0xxx) xxxxxxxx
//			(0xxxx) xxxxxxx
//			(03xxxx) xxxxxx
			
			phonereg="^((((0?\\d{2})|(\\(0?\\d{2})\\))?[- ]?\\d{8})|" +
						"(((0?\\d{3})|(\\(0?\\d{3})\\))?[- ]?\\d{8})|" +
							"(((0?\\d{4})|(\\(0?\\d{4})\\))?[- ]?\\d{7})|" +
								"(((0?3(\\d{4}))|(\\(0?3(\\d{4}))\\))?[- ]?\\d{6}))$";
			Prefs.putPref(PrefName.PHONE_VALIDATION, 1);
		}
		else if (i == 2)
		{
			//United Kingdom
//			(020) xxxx xxxx	London
//			(029) xxxx xxxx	Cardiff
//			(0113) xxx xxxx	Leeds
//			(0116) xxx xxxx	Leicester
//			(0131) xxx xxxx	Edinburgh
//			(0151) xxx xxxx	Liverpool
//			(01382)  xxxxxx	Dundee
//			(01386)  xxxxxx	Evesham
//			(01865)  xxxxxx	Oxford
//			(01792)  xxxxxx	Swansea
//			(01204)   xxxxx	Bolton
//			(015396)  xxxxx	Sedbergh
//			(016977)   xxxx	Brampton

			phonereg="^((((0?2(0|9))|(\\(0?2(0|9))\\))?[- ]?\\d{4}[- ]?\\d{4})|" +
						"(((0?\\d{3})|(\\(0?\\d{3})\\))?[- ]?\\d{3}[- ]?\\d{4})|" +
							"(((0?\\d{4})|(\\(0?\\d{4})\\))?[- ]?\\d{6})|" +
								"(((0?\\d{5})|(\\(0?\\d{5})\\))?[- ]?\\d{5})|" +
									"(((0?\\d{6})|(\\(0?\\d{6})\\))?[- ]?\\d{4}))$";
			Prefs.putPref(PrefName.PHONE_VALIDATION, 2);
		}
		else if (i == 3)
		{
			//United States
			phonereg="^(1)?[- ]?\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
			Prefs.putPref(PrefName.PHONE_VALIDATION, 3);
		}
		else
		{
			Prefs.putPref(PrefName.PHONE_VALIDATION, 4);
			phonereg = phoneRegex.getText();
		}
		
		Prefs.putPref(PrefName.PHONE_REGEX, phonereg);
	}

	@Override
	public void loadOptions() {
		OptionsPanel.setCheckBox(emailvalidation, PrefName.EMAIL_VALIDATION);
		phoneLoc.setSelectedIndex(Prefs.getIntPref(PrefName.PHONE_VALIDATION));
		phoneRegex.setText(Prefs.getPref(PrefName.PHONE_REGEX));
	}
}