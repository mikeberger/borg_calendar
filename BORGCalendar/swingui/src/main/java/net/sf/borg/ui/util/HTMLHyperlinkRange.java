package net.sf.borg.ui.util;

public class HTMLHyperlinkRange {
	private int start;
	private int length;
	
	public HTMLHyperlinkRange(int s, int len) {
		setStart(s);
		setLength(len);
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return start + length;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setStart(int newStart) {
		start = newStart;
	}
	
	public void setLength(int newLength) {
		length = newLength;
	}
	
	public boolean isInRange(int offset) {
		return (offset >= getStart() && offset <= getEnd());
	}
}
