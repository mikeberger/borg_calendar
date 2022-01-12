package net.sf.borg.ui.util;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class DynamicHTMLEditorKit extends HTMLEditorKit {
	
	
	private static final long serialVersionUID = 1L;
	private HTMLLinkController linkController = new HTMLLinkController();
	
    @Override
	public void install(JEditorPane c) {    	
        MouseListener[] oldMouseListeners = c.getMouseListeners();
        MouseMotionListener[] oldMouseMotionListeners = c.getMouseMotionListeners();
        
        super.install(c);

        for (MouseListener l: c.getMouseListeners()) {
            c.removeMouseListener(l);
        }
        for (MouseListener l: oldMouseListeners) {
            c.addMouseListener(l);
        }

        for (MouseMotionListener l: c.getMouseMotionListeners()) {
            c.removeMouseMotionListener(l);
        }
        for (MouseMotionListener l: oldMouseMotionListeners) {
            c.addMouseMotionListener(l);
        }

        c.addMouseListener(linkController);
        c.addMouseMotionListener(linkController);
    }
    
    public HTMLLinkController getHTMLLinkcontroller() {
    	return linkController;
    }

}