/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

/**
 * a JTable that renders rows in alternating colors
 */
public class StripedTable extends JTable {

	private static final long serialVersionUID = 1L;

	/**
	 * renderer that alternates colors
	 */
	private class StripedRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public StripedRenderer() {
			super();
			setOpaque(true); // MUST do this for background to show up.
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (obj instanceof Boolean) {
				Component c = defaultBooleanRenderer.getTableCellRendererComponent(table,
						obj, isSelected, hasFocus, row, column);
				// stripe non-selected even rows
				if (!isSelected && row % 2 == 0) {
					c.setBackground(stripeColor);
				}
				return c;
			}

			JLabel l;
			
			if (obj instanceof Date) {
				l = (JLabel) defaultDateRenderer.getTableCellRendererComponent(table,
						obj, isSelected, hasFocus, row, column);
			} else {
				l = (JLabel) defaultStringRenderer.getTableCellRendererComponent(table, obj,
						isSelected, hasFocus, row, column);
			}
			this.setForeground(l.getForeground());
			
			// stripe non-selected even rows
			if (!isSelected && row % 2 == 0) {
				this.setBackground(stripeColor);
			} else {
				this.setBackground(l.getBackground());
			}

			// center date and int
			if (obj instanceof Integer) {
				this.setText(((Integer) obj).toString());
				this.setHorizontalAlignment(CENTER);
			} else if (obj instanceof Date) {
				this.setText(l.getText());
				this.setHorizontalAlignment(CENTER);
			} else {
				this.setText(l.getText());
				this.setHorizontalAlignment(l.getHorizontalAlignment());
			}

			this.setBorder(new EmptyBorder(4, 2, 4, 2));
			return this;
		}
	}

	// initialize table stripe color
	private static Color stripeColor = Color.white;

	static {
		int rgb = Prefs.getIntPref(PrefName.UCS_STRIPE);
		setStripeColor(new Color(rgb));
	}

	/**
	 * set the striping color
	 * @param c the striping color
	 */
	public static void setStripeColor(Color c) {
		stripeColor = c;
	}

	// default renderers
	private TableCellRenderer defaultBooleanRenderer = null;
	private TableCellRenderer defaultDateRenderer = null;
	private TableCellRenderer defaultStringRenderer = null;

	/**
	 * constructor
	 */
	public StripedTable() {
		super();
		
		// save original renderers
		defaultStringRenderer = this.getDefaultRenderer(String.class);
		defaultDateRenderer = this.getDefaultRenderer(Date.class);
		defaultBooleanRenderer = this.getDefaultRenderer(Boolean.class);
		
		// register our striping renderer
		this.setDefaultRenderer(Object.class, new StripedRenderer());
		this.setDefaultRenderer(Date.class, new StripedRenderer());
		this.setDefaultRenderer(Integer.class, new StripedRenderer());
		this.setDefaultRenderer(Boolean.class, new StripedRenderer());
	}
}
