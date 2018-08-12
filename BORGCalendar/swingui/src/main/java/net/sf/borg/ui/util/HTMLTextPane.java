package net.sf.borg.ui.util;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;


public class HTMLTextPane extends JTextPane {
	
	private static final long serialVersionUID = 1L;
	private DynamicHTMLEditorKit kit;
	private HTMLDocumentListener docListener;
	
	public HTMLTextPane() {
		initComponent();
	}
	
	public HTMLTextPane(Document doc) {
		setDocument(doc);
		initComponent();
	}
	
	public void initComponent() {
		kit = new DynamicHTMLEditorKit();
		docListener = new HTMLDocumentListener(this);
		
		setEditorKit(kit);
        setContentType("text/html");
        setTrueFont(UIManager.getFont("Label.font"));
        
        getDocument().addDocumentListener(docListener);
        
        addHyperlinkListener(new HyperlinkListener() {
            @Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String url = evt.getURL().toString();
                    try {
						Desktop.getDesktop().browse(URI.create(url));
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
			public void mouseClicked(MouseEvent evt) {
            	if( evt.getButton() == MouseEvent.BUTTON3)
            	{
                    setCaretPosition(viewToModel2D(evt.getPoint()));
            	}
            }
        });
	}
	
	@Override
	public void setCursor(Cursor cursor) {
        if (kit != null && kit.getHTMLLinkcontroller().getNeedsCursorChange()) {
            super.setCursor(cursor);
        }
    }
	
	/**
	 * This modifies the HTML content of the JEditorPane to display the desired font.
	 * 
	 * @param font
	 * 			A Font type
	 */
	public void setTrueFont(Font font)
	{
		String bodyRule = "body { font-family: " + font.getFamily() + "; " +
				"font-size: " + font.getSize() + "pt; }";
		((HTMLDocument)getDocument()).getStyleSheet().addRule(bodyRule);
		//Sets the font for when the JEditorPane is switched to plain text.
		setFont(font);
	}
	
	public String getPlainText() {
		try {
			return getDocument().getText(0, getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	@Override
	public void setText(String text) {
		try {
			docListener.setInitializingDoc(true);
			Document doc = getDocument();
			doc.remove(0, doc.getLength());
			doc.insertString(0, text, null);
			
			if (doc instanceof HTMLDocument)
			    docListener.resetHyperlinkList((HTMLDocument)doc);
			
			docListener.setInitializingDoc(false);
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
