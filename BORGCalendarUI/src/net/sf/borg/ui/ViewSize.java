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

// this class contains information about a View's location, size, and if it is maximized
// it contains methods to convert this data to and from a String - to be used as a preference string
public class ViewSize {

    private int x = -1;
    private int y = -1;
    private int width = -1;
    private int height = -1;
    private boolean maximized = false;
    
    public ViewSize()
    {       
    }
    
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public boolean isMaximized() {
        return maximized;
    }
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    
    static public ViewSize fromString( String s )
    {
        ViewSize vs = new ViewSize();
        String toks[] = s.split(",");
        vs.x = Integer.parseInt(toks[0]);
        vs.y = Integer.parseInt(toks[1]);
        vs.width = Integer.parseInt(toks[2]);
        vs.height = Integer.parseInt(toks[3]);
        if( toks[4].equals("Y"))
            vs.maximized = true;
        else
            vs.maximized = false;
        
        return( vs );
    }
    
    public String toString()
    {
        return( Integer.toString(x) + "," + Integer.toString(y) + "," + 
                Integer.toString(width) + "," + Integer.toString(height) + ","
                + ((maximized == true)?"Y":"N"));
    }
}
