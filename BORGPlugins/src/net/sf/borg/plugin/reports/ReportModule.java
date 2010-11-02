package net.sf.borg.plugin.reports;

import java.awt.Component;

import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;

public class ReportModule implements Module {

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Reports");
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {

		parent.addPluginSubMenu(RunReport.getReportMenu());
	}

	@Override
	public void print() {
		// do nothing
	}

}
