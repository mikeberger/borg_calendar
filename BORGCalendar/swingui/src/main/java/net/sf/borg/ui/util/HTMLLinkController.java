package net.sf.borg.ui.util;

import java.awt.event.MouseEvent;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit.LinkController;


public class HTMLLinkController extends LinkController {
	

	private static final long serialVersionUID = 1L;
	private boolean needsCursorChange;
	
	public HTMLLinkController() {
		needsCursorChange = true;
	}
	
	public boolean getNeedsCursorChange() {
		return needsCursorChange;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
        JTextPane textPane = (JTextPane) e.getSource();

        if (textPane.isEditable() &&
        	SwingUtilities.isLeftMouseButton(e) &&
        	e.getClickCount() == 1) {
            textPane.setEditable(false);
            super.mouseClicked(e);
            textPane.setEditable(true);
        }

    }
	
    @Override
	public void mouseMoved(MouseEvent e) {
    	JTextPane textPane = (JTextPane) e.getSource();

        if (textPane.isEditable()) {
        	needsCursorChange = false;
            textPane.setEditable(false);
            needsCursorChange = true;
            super.mouseMoved(e);
            needsCursorChange = false;
            textPane.setEditable(true);
            needsCursorChange = true;
        }
    }
}