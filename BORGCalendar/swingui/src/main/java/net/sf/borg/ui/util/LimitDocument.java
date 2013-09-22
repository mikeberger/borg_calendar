package net.sf.borg.ui.util;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * document that limits length
 */
public class LimitDocument extends PlainDocument {
	private static final long serialVersionUID = 1L;
	int maxLength; // max characters allowed

	/**
	 * constructor
	 * @param maxLen max characters allowed
	 */
	public LimitDocument(int max) {
		this.maxLength = max;
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