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
/*
 * mversion.java
 *
 * Created on March 18, 2002, 12:59 PM
 */

/**
 *
 * @author  MBERGER
 * @version 
 */
package net.sf.borg.common.util;

// mversion keeps track of classes CVS Id info
// the various classes invoke mversion.addVersion() in a static block
// and pass version info.
// originally thought that this would have all classes info - but it turns
// out that classes only invoke the static blocks when loaded and the static
// blocks alone don't force the loading
// so mversion can provide info on loaded class versions
public class Version {

    static String verstring = "";
    
    /** Creates new mversion */
    public Version() {
    }

    static public void addVersion(java.lang.String id) {
        verstring += id;
        verstring += "\n";
    }
    
    static public String getVersion()
    {
        return(verstring);
    }
    
}

