package net.sf.borg.ui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;

public class HTMLLimitDocument extends HTMLDocument {
	private static final long serialVersionUID = 1L;
	int maxLength; // max characters allowed

	public void setMaxLength(int newMaxLength) {
		maxLength = newMaxLength;
	}

	@Override
	public void insertString(int offs, String str, AttributeSet attr)
			throws BadLocationException {
		if (getLength() + str.length() > this.maxLength) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			super.insertString(offs, str, attr);
		}
	}
}
