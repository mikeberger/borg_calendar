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

 Copyright 2004 by ==Quiet==
 */

package net.sf.borg.common.util;

import net.sf.borg.common.app.AppHelper;

/**
 * 
 * A home for our Preferences implementation.
 *  
 */
class PrefsHome {

    /**
     * 
     * Our singleton implementation.
     *  
     */
    public static PrefsHome getInstance() {

        return instance;
    }

    /**
     * 
     * Retrieves our preferences implementation.
     *  
     */
    public final IPrefs getPrefs() {

        return impl;
    }

    // package //
    final void setPrefs(IPrefs prefs) {

        impl = prefs;
    }

    // private //
    private static final PrefsHome instance = new PrefsHome();

    private static IPrefs impl;

    private PrefsHome() {

        if (AppHelper.isApplication())
            impl = new PrefsImpl();
        else
            impl = new MemPrefsImpl();
    }
}