/* This file was originally based on Sun's TableSorter */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

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
// this class based on something from the web
// don't remember where it was from

package net.sf.borg.common.ui;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.borg.common.util.Version;

public class TableSorter extends DefaultTableModel
{
    
    // DefaulTableModel base class still holds the real data array
    // this object holds a mapping of the display position to the
    // row in the real data array
    int             indexes[]; // provides the row in the real table data given the row from the
    // screen (the displayed position)
    // so, indexes[row] is the row of the real data in the
    // data array corresponding to row in the displayed table
    Vector          sortingColumns = new Vector();
    boolean         ascending_ = true;
    Class types_[];
    boolean canEdit_[];
    
    static
    {
        Version.addVersion("$Id$");
    }
    
    public TableSorter( String[] s , Class[] types, boolean[] canEdit)
    {
        super(s,0);
        indexes = new int[0]; // for consistency
        types_ = types;
        canEdit_ = canEdit;
    }
    
    public boolean isSorted()
    {
        if( sortingColumns.size() != 0 )
               return(true);
        return(false);
    }
    
    public TableSorter( String[] s , Class[] types)
    {
        super(s,0);
        indexes = new int[0]; // for consistency
        types_ = types;
        canEdit_ = null;
    }
    
    public Class getColumnClass(int columnIndex)
    {
        return types_ [columnIndex];
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if( canEdit_ == null ) return(false);
        return canEdit_ [columnIndex];
    }
    
    public void removeRow(int row )
    {
        super.removeRow(indexes[row]);
        reallocateIndexes();
    }
    
    // get the real row in the table data given the screen position
    public int getMappedIndex(int index)
    {
        return( indexes[index] );
    }
    
    // get value from real dat array, given screen position
    // table object calls this for each screen position to
    // get data from its own array to put into it
    public Object getValueAt(int row, int col )
    {
        return( super.getValueAt(indexes[row], col ) );
    }
    
    public void setValueAt(Object o, int row, int col )
    {
        super.setValueAt(o,indexes[row], col );
    }
    
    private int compareRowsByColumn(int row1, int row2, int column)
    {
        Class type = getColumnClass(column);
        
        // Check for nulls.
        // row1 and row2 are indexes into the real data - so call
        // the base class routine to directly access the data
        Object o1 = super.getValueAt(row1, column);
        Object o2 = super.getValueAt(row2, column);
        
        // If both values are null, return 0.
        if (o1 == null && o2 == null)
        {
            return 0;
        } else if (o1 == null)
        { // Define null less than everything.
            return -1;
        } else if (o2 == null)
        {
            return 1;
        }
        
        /*
         * We copy all returned values from the getValue call in case
         * an optimised model is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */
        
        if (type.getSuperclass() == java.lang.Number.class)
        {
            Number n1 = (Number)super.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number)super.getValueAt(row2, column);
            double d2 = n2.doubleValue();
            
            if (d1 < d2)
            {
                return -1;
            } else if (d1 > d2)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == java.util.Date.class)
        {
            Date d1 = (Date)super.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date)super.getValueAt(row2, column);
            long n2 = d2.getTime();
            
            if (n1 < n2)
            {
                return -1;
            } else if (n1 > n2)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == String.class)
        {
            String s1 = (String)super.getValueAt(row1, column);
            String s2    = (String)super.getValueAt(row2, column);
            
            int result = s1.compareTo(s2);
            
            
            if (result < 0)
            {
                return -1;
            } else if (result > 0)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == Boolean.class)
        {
            Boolean bool1 = (Boolean)super.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean)super.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();
            
            if (b1 == b2)
            {
                return 0;
            } else if (b1)
            { // Define false < true
                return 1;
            } else
            {
                return -1;
            }
        } else
        {
            Object v1 = super.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = super.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);
            
            if (result < 0)
            {
                return -1;
            } else if (result > 0)
            {
                return 1;
            } else
            {
                return 0;
            }
        }
    }
    
    private int compare(int row1, int row2)
    {
        for (int level = 0; level < sortingColumns.size(); level++)
        {
            Integer column = (Integer)sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0)
            {
                return ascending_ ? result : -result;
            }
        }
        return 0;
    }
    
    private void reallocateIndexes()
    {
        int rowCount = getRowCount();
        
        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];
        
        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++)
        {
            indexes[row] = row;
        }
    }
    
    public void tableChanged(TableModelEvent e)
    {
        reallocateIndexes();
        fireTableDataChanged();
    }
    
    public void sort()
    {
        n2sort();
    }
    
    private void n2sort()
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            for (int j = i+1; j < getRowCount(); j++)
            {
                // compare the real data and if out of sequence
                // swap the displayed row indexes - never touch the
                // real data array
                if (compare(indexes[i], indexes[j]) == 1)
                {
                    swap(i, j);
                }
            }
        }
    }
    
    private void swap(int i, int j)
    {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }
    
    public void sortByColumn(int column)
    {
        sortByColumn(column, true);
    }
    
    public void sortByColumn(int column, boolean ascending)
    {
        
        this.ascending_ = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort();
        
    }
    
    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(JTable table)
    {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1)
                {
                    //System.out.println("Sorting ...");
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    boolean ascending = (shiftPressed == 0);
                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
