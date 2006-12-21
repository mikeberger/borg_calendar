package net.sf.borg.common.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class StripedTable extends JTable {

    private TableCellRenderer defrend_ = null;

    private TableCellRenderer defDaterend_ = null;

    private TableCellRenderer defBoolRend_ = null;

    private static Color STCOLOR = Color.white;

    public static void setStripeColor(Color c) {
	STCOLOR = c;
    }

    private class StripedRenderer extends JLabel implements TableCellRenderer {

	public StripedRenderer() {
	    super();
	    setOpaque(true); // MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table,
		Object obj, boolean isSelected, boolean hasFocus, int row,
		int column) {

	    JLabel l = null;

	    if (obj instanceof Date) {
		l = (JLabel) defDaterend_.getTableCellRendererComponent(table,
			obj, isSelected, hasFocus, row, column);
	    } else if (obj instanceof Boolean) {
		Component c = defBoolRend_.getTableCellRendererComponent(table,
			obj, isSelected, hasFocus, row, column);
		
		if (isSelected) {
		    return c;
		} else if (row % 2 == 0) {
		    c.setBackground(STCOLOR);
		} else {
		    c.setBackground(Color.WHITE);
		}
		return c;
	    } else {
		l = (JLabel) defrend_.getTableCellRendererComponent(table, obj,
			isSelected, hasFocus, row, column);
	    }
	    this.setForeground(l.getForeground());
	    if (isSelected) {
		this.setBackground(l.getBackground());
	    } else if (row % 2 == 0) {
		this.setBackground(STCOLOR);
	    } else {
		this.setBackground(Color.WHITE);
	    }

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

	    return this;
	}
    }

    public StripedTable() {
	super();
	defrend_ = this.getDefaultRenderer(String.class);
	defDaterend_ = this.getDefaultRenderer(Date.class);
	defBoolRend_ = this.getDefaultRenderer(Boolean.class);
	this.setDefaultRenderer(Object.class, new StripedRenderer());
	this.setDefaultRenderer(Date.class, new StripedRenderer());
	this.setDefaultRenderer(Integer.class, new StripedRenderer());
	this.setDefaultRenderer(Boolean.class, new StripedRenderer());
    }
}
