package com.mbcsoft.platform.ui;

import java.util.HashSet;
import java.util.Set;

public class ViewType {
	
	
	static Set<ViewType> vals = new HashSet<ViewType>();
	
	public static Set<ViewType> values()
	{
		return vals;
	}
	
	public static void addValue(ViewType vt)
	{
		vals.add(vt);
	}

}
