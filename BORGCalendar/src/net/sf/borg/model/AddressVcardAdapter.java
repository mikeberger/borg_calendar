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
package net.sf.borg.model;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.model.beans.Address;

public class AddressVcardAdapter {

    static public void exportVcardFiles(String path) throws Exception {
	
	Collection addrs = AddressModel.getReference().getAddresses();
	Iterator it = addrs.iterator();
	while( it.hasNext())
	{
	    Address a = (Address) it.next();
	    String name = a.getFirstName() != null ? a.getFirstName() : "";
	    if( a.getLastName() != null)
	    {
		if( !name.equals(""))
		{
		    name += " ";
		}
		name += a.getLastName();
	    }
	    FileWriter fw = new FileWriter(path + "/" + name + ".vcf");
	    fw.write("BEGIN:VCARD\n");
	    fw.write("VERSION:2.1\n");
	    fw.write("N:" + name + "\n");
	    if( a.getHomePhone() != null )
		fw.write("TEL;HOME:" + a.getHomePhone() + "\n");
	    if( a.getWorkPhone() != null )
		fw.write("TEL;WORK:" + a.getWorkPhone() + "\n");
	    if( a.getEmail() != null)
		fw.write("EMAIL;INTERNET:" + a.getEmail() + "\n");
	    fw.write("END:VCARD\n");
	    fw.close();
	    
	}
    }
    
    static public void importVcard(Reader r) throws Exception {
        BufferedReader br = new BufferedReader(r);
        AddressModel am = AddressModel.getReference();
        Address addr = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;

            if (line.equals("BEGIN:VCARD")) {
                addr = am.newAddress();
                continue;
            }
            else if (line.equals("END:VCARD")) {
                if (addr != null) {
                    am.saveAddress(addr);
                    addr = null;
                    continue;
                }
            }

            if (addr == null)
                continue;

            int col = line.indexOf(":");
            if (col == -1)
                continue;

            String prop = line.substring(0, col);
            String value = line.substring(col + 1);
            String baseprop = prop;
            int semi = prop.indexOf(";");
            if (semi != -1)
                baseprop = prop.substring(0, semi);

            if (baseprop.equals("N")) {
                int start = 0;
                int end = 0;
                String stt = null;
                for (int i = 0;end != -1; i++) {
                    end = value.indexOf(";", start);
                    if (end == -1 )
                        stt = value.substring(start);
                    else
                        stt = value.substring(start, end);
                    
                    if (!stt.equals("")) {
                        if (i == 0) {
                            addr.setLastName(stt);
                        }
                        else if (i == 1) {
                            addr.setFirstName(stt);
                        }
                    }
                    start = end + 1;
                }

            }
            else if (baseprop.equals("NICKNAME")) {
                addr.setNickname(value);
            }
            else if (baseprop.equals("ORG")) {
                int ind = value.indexOf(";");
                if (ind != -1)
                    addr.setCompany(value.substring(0, ind));
            }
            else if (baseprop.equals("NOTE")) {
                addr.setNotes(value);
            }
            else if (baseprop.equals("TEL")) {
                if (prop.indexOf("FAX") != -1) {
                    addr.setFax(value);
                }
                else if (prop.startsWith("TEL;HOME") || prop.startsWith("TEL;PREF")) {
                    addr.setHomePhone(value);
                }
                else if (prop.startsWith("TEL;WORK") || prop.startsWith("TEL;CELL")) {
                    addr.setWorkPhone(value);
                }
                else if (prop.startsWith("TEL;PAGER")) {
                    addr.setPager(value);
                }
            }
            else if (baseprop.equals("ADR")) {
                // home/work are reversed due to an existing bug in GUI
                boolean home = false;
                if (prop.indexOf("WORK") != -1)
                    home = true;
                int start = 0;
                int end = 0;
                String stt = null;
                for (int i = 0;end != -1; i++) {
                    
                    end = value.indexOf(";", start);
                    if (end == -1 )
                        stt = value.substring(start);
                    else
                        stt = value.substring(start, end);
                    
                    if (!stt.equals("")) {
                        if (i == 2 && home) {
                            addr.setStreetAddress(stt);
                        }
                        else if (i == 2 && !home) {
                            addr.setWorkStreetAddress(stt);
                        }
                        else if (i == 3 && home) {
                            addr.setCity(stt);
                        }
                        else if (i == 3 && !home) {
                            addr.setWorkCity(stt);
                        }
                        else if (i == 4 && home) {
                            addr.setState(stt);
                        }
                        else if (i == 4 && !home) {
                            addr.setWorkState(stt);
                        }
                        else if (i == 5 && home) {
                            addr.setZip(stt);
                        }
                        else if (i == 5 && !home) {
                            addr.setWorkZip(stt);
                        }
                        else if (i == 6 && home) {
                            addr.setCountry(stt);
                        }
                        else if (i == 6 && !home) {
                            addr.setWorkCountry(stt);
                        }
                    }
                    start = end + 1;
                }

            }
            else if (baseprop.equals("URL")) {
                addr.setWebPage(value);
            }
            else if (baseprop.equals("BDAY")) {
                Date bd = df.parse(value);
                addr.setBirthday(bd);
            }
            else if (baseprop.equals("EMAIL")) {
                addr.setEmail(value);
            }
        }

    }
}