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

    private static final Color STCOLOR = new Color(240, 250, 250);

    private class StripedRenderer extends JLabel implements TableCellRenderer {

	public StripedRenderer() {
	    super();
	    setOpaque(true); // MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table,
		Object obj, boolean isSelected, boolean hasFocus, int row,
		int column) {

	    JLabel l = null;
	    
	    if( obj instanceof Date )
	    {
		l = (JLabel) defDaterend_.getTableCellRendererComponent(table, obj,
			    isSelected, hasFocus, row, column);
	    }
	    else
	    {
		l = (JLabel) defrend_.getTableCellRendererComponent(table, obj,
			    isSelected, hasFocus, row, column);
	    }
	    this.setForeground(l.getForeground());
	    if( isSelected )
	    {
		this.setBackground(l.getBackground());		
	    }
	    else if (row % 2 == 0) {
		this.setBackground(STCOLOR);
	    } else {
		this.setBackground(Color.WHITE);
	    }

	    if (obj instanceof Integer) {
		this.setText(((Integer) obj).toString());
		this.setHorizontalAlignment(CENTER);
	    }
	    else if (obj instanceof Date) {
		this.setText(l.getText());
		this.setHorizontalAlignment(CENTER);
	    }
	    else
	    {
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
	this.setDefaultRenderer(Object.class, new StripedRenderer());
	this.setDefaultRenderer(Date.class, new StripedRenderer());
	this.setDefaultRenderer(Integer.class, new StripedRenderer());
    }
}
