package net.sf.borg.ui.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.sf.borg.common.Errmsg;

public class HTMLDocumentListener implements DocumentListener {

	private boolean initializingDoc;

	private JEditorPane textPane;

	private List<HTMLHyperlinkRange> hyperlinkList;

	public static final String linkRegex = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/))"
			+ "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov"
			+ "|mil|biz|info|mobi|name|aero|jobs|museum|edu"
			+ "|travel|[a-z]{2}))(:[\\d]{1,5})?"
			+ "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?"
			+ "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
			+ "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)"
			+ "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?"
			+ "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*"
			+ "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";
	
	private final Pattern pattern = Pattern.compile(linkRegex);

	public HTMLDocumentListener(JEditorPane newTextPane) {
		hyperlinkList = new ArrayList<HTMLHyperlinkRange>();
		textPane = newTextPane;
		initializingDoc = false;
	}

	private boolean isExistingLink(int offset) {
		for (HTMLHyperlinkRange hl : hyperlinkList) {
			if (hl.getStart() == offset)
				return true;
		}

		return false;
	}

	private boolean isChangeInHyperlink(DocumentEvent evt) {
		for (HTMLHyperlinkRange hl : hyperlinkList) {
			if (hl.isInRange(evt.getOffset())
					&& hl.isInRange(textPane.getCaretPosition()))
				return true;

			if (evt.getType() == EventType.INSERT
					&& evt.getOffset() == hl.getEnd())
				return true;
		}
		return false;
	}

	private void addToHyperlinkList(HTMLHyperlinkRange link) {
		hyperlinkList.add(link);
	}

	private void removeFromHyperlinkList(HTMLHyperlinkRange linkRange) {
		hyperlinkList.remove(linkRange);
	}

	private void updateHyperlinkList(int offset, int len) {
		for (HTMLHyperlinkRange hl : hyperlinkList) {
			if (hl.getStart() < offset)
				continue;
			hl.setStart(hl.getStart() + len);
		}
	}

	public void resetHyperlinkList(HTMLDocument doc) {
		hyperlinkList.clear();
		checkForHyperlinks(doc);
	}

	private HTMLHyperlinkRange getHyperlinkRange(int start) {
		for (HTMLHyperlinkRange hl : hyperlinkList)
			if (start >= hl.getStart() && start <= hl.getEnd())
				return hl;

		return null;
	}

	private void createHyperlink(HTMLDocument doc, int offset, String url) {
		SimpleAttributeSet a = new SimpleAttributeSet();
		a.addAttribute("DUMMY_ATTRIBUTE_NAME", "DUMMY_ATTRIBUTE_VALUE");
		doc.setCharacterAttributes(offset, url.length(), a, false);

		Element elem = doc.getCharacterElement(offset);
		String html = "<a href='" + url + "'>" + url + "</a>";

		try {
			doc.setOuterHTML(elem, html);
			addToHyperlinkList(new HTMLHyperlinkRange(offset, url.length()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void invalidateHyperlink(HTMLDocument doc, DocumentEvent evt,
			boolean wasEnter) {
		HTMLHyperlinkRange linkRange = getHyperlinkRange(evt.getOffset());

		Element element = doc.getCharacterElement(linkRange.getStart());
		replaceLinkElementWithText(doc, element);

		// Pressing Enter is weird, it splits a hyperlink into two, so we have
		// to
		// invalidate the second one as well.
		if (wasEnter) {
			replaceLinkElementWithText(doc,
					doc.getCharacterElement(textPane.getCaretPosition()));
		}

		removeFromHyperlinkList(linkRange);
	}

	private void checkForHyperlinks(HTMLDocument doc) {
		Matcher matcher;
		try {
			matcher = pattern.matcher(
					doc.getText(0, doc.getLength()));

			while (matcher.find())
				if (!isExistingLink(matcher.start()))
					createHyperlink(doc, matcher.start(), matcher.group());

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void setInitializingDoc(boolean newState) {
		initializingDoc = newState;
	}

	@Override
	public void insertUpdate(DocumentEvent evt) {
		final DocumentEvent e = evt;

		// We always want to update the hyperlink ranges
		updateHyperlinkList(e.getOffset(), e.getLength());

		// Ignore some document change events when initializing a doc
		if (initializingDoc)
			return;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (e.getDocument() instanceof HTMLDocument
						&& e.getType() != EventType.CHANGE) {
					HTMLDocument doc = (HTMLDocument) e.getDocument();
					String text = "";

					try {
						text = doc.getText(e.getOffset(), e.getLength());
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}

					if (isChangeInHyperlink(e))
						invalidateHyperlink(doc, e, text.charAt(0) == '\n');
					else if (e.getLength() == 1) {
						if (text.charAt(0) == ' ' || text.charAt(0) == '\n'
								|| text.charAt(0) == '\t') {
							checkForHyperlinks(doc);
						}
					}
				}
			}
		});
	}

	@Override
	public void changedUpdate(DocumentEvent evt) {
		// empty
	}

	@Override
	public void removeUpdate(DocumentEvent evt) {
		final DocumentEvent e = evt;

		// We always want to update the hyperlink ranges
		updateHyperlinkList(e.getOffset(), -e.getLength());

		// Ignore some document change events when initializing a doc
		if (initializingDoc)
			return;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (e.getDocument() instanceof HTMLDocument) {
					HTMLDocument doc = (HTMLDocument) e.getDocument();

					if (isChangeInHyperlink(e))
						invalidateHyperlink(doc, e, false);
				}
			}
		});
	}

	private static void replaceLinkElementWithText(HTMLDocument doc, Element element) {
		String plaintext = null;

		try {
			Object tag = element.getAttributes().getAttribute(
					StyleConstants.NameAttribute);
			if (tag != null && tag == HTML.Tag.CONTENT) {
				int startOffset = element.getStartOffset();
				int endOffset = element.getEndOffset();
				int length = endOffset - startOffset;
				plaintext = doc.getText(startOffset, length).trim();
			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}

		if (plaintext != null) {
			try {
				doc.setOuterHTML(element, plaintext);
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}
		}
	}

}