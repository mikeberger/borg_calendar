package net.sf.borg.ui;

import java.util.HashMap;

/**
 * a place for the UI to copy items for later pasting
 */
public class ClipBoard {
	
	private static final ClipBoard singleton = new ClipBoard();
	
	private final HashMap<Class<?>, Object> map = new HashMap<Class<?>, Object>();
	
	public static ClipBoard getReference() {
		return singleton;
	}
	
	public void put( Object o )
	{
		map.put(o.getClass(), o);
	}
	
	public Object get( Class<?> c)
	{
		return map.get(c);
	}

}
