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
 
Copyright 2003 by ==Quiet==
 */
package net.sf.borg.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;

import net.sf.borg.common.app.AppHelper;
import net.sf.borg.common.util.Errmsg;

public class Borglet extends JApplet {
   public void init() {
   	  try
   	  { 
		AppHelper.setType(AppHelper.APPLET);
		AppHelper.setCodeBase(getCodeBase());
		
		JButton bn = new JButton("Launch BORG Applet");
		bn.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Borg.main(new String[] {});
				}
			}
		);

		getContentPane().add(bn);
   	  }
   	  catch (Exception e)
   	  {
   	  	Errmsg.errmsg(e);
   	  }
   } 
   
   public void destroy() {
 
   }
}