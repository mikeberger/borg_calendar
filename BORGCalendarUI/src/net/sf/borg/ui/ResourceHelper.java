/*
  This file is part of BORG.

  BORG is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  BORG is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with BORG; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  Copyright 2003 by Mike Berger
*/

package net.sf.borg.ui;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import net.sf.borg.common.util.Resource;

/**
 * Helps parse resource strings (containing text and optional
 * mnemonics and accelerators) for GUI widgets.
 * @author membar
 */
public class ResourceHelper
{
public static void setText(JMenu mnu, String resourceKey)
{
	ComponentParms parms = parseParms(resourceKey);
	mnu.setText(parms.getText());
	if (parms.getKeyEvent() != -1)
		mnu.setMnemonic(parms.getKeyEvent());
}

public static void setText(JMenuItem mnuitm, String resourceKey)
{
	ComponentParms parms = parseParms(resourceKey);
	mnuitm.setText(parms.getText());
	if (parms.getKeyEvent() != -1)
		mnuitm.setMnemonic(parms.getKeyEvent());
	if (parms.getKeyStroke() != null)
		mnuitm.setAccelerator(parms.getKeyStroke());
}

public static void setText(AbstractButton button, String resourceKey)
{
	ComponentParms parms = parseParms(resourceKey);
	button.setText(parms.getText());
	if (parms.getKeyEvent() != -1)
		button.setMnemonic(parms.getKeyEvent());
}

public static void setText(JLabel label, String resourceKey)
{
	ComponentParms parms = parseParms(resourceKey);
	label.setText(parms.getText());
	if (parms.getKeyEvent() != -1)
		label.setDisplayedMnemonic(parms.getKeyEvent());
}

public static void addTab(JTabbedPane tabbedPane, String resourceKey,
		JComponent comp)
{
	ComponentParms parms = parseParms(resourceKey);
	tabbedPane.add(parms.getText(), comp);
	if (parms.getKeyEvent() != -1)
		tabbedPane.setMnemonicAt(tabbedPane.getTabCount()-1, parms.getKeyEvent());
}

public static String getText(String resourceKey)
{
	ComponentParms parms = parseParms(resourceKey);
	return parms.getText();
}

public static void setTitle(JFrame frame, String resourceKey)
{
	frame.setTitle(getText(resourceKey));
}

public static void setTitle(JDialog dlg, String resourceKey)
{
	dlg.setTitle(getText(resourceKey));
}

// private //
private ResourceHelper()
{}

private static ComponentParms parseParms(String resourceKey)
{
	String parmsText = Resource.getResourceString(resourceKey);
	
	if (parmsText.startsWith("Goto"))
		parmsText = parmsText.substring(0);

	String text = parmsText;
	int mnemonic = -1;
	KeyStroke accel = null;
	int pos;
	if ((pos = parmsText.indexOf('|')) != -1)
	{
		text = parmsText.substring(0,pos);
		String parmsTextRem = parmsText.substring(pos+1);
		String mnemonicText = parmsTextRem;

		if ((pos = parmsTextRem.indexOf('|')) != -1)
		{
			mnemonicText = parmsTextRem.substring(0,pos);
			String accelText = parmsTextRem.substring(pos+1);
			accel = KeyStroke.getKeyStroke(accelText);
		}

		if (mnemonicText.length() > 0)
			mnemonic = KeyStroke.getKeyStroke(mnemonicText).getKeyCode();
	}
	return new ComponentParms(text,mnemonic,accel);
}

//////////////////////////////////////////////////////////////////
// nested class ComponentParms

private static class ComponentParms
{
final int getKeyEvent()
{
	return keyEvent;
}

final KeyStroke getKeyStroke()
{
	return keyStroke;
}

final String getText()
{
	return text;
}

// "internal" //
ComponentParms(String text, int keyEvent, KeyStroke keyStroke)
{
	this.text = text;
	this.keyEvent = keyEvent;
	this.keyStroke = keyStroke;
}

// private //
private String text;
private int keyEvent;
private KeyStroke keyStroke;
}

// end nested class ComponentParms
//////////////////////////////////////////////////////////////////
}
