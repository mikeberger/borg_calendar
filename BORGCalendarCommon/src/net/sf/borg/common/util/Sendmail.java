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
/*
 * Sendmail.java
 *
 * Created on October 20, 2003, 10:01 AM
 */

package net.sf.borg.common.util;

/**
 *
 * @author  mbb
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Sendmail provides very very primitive SMTP mail capability
public class Sendmail
{
    static
    {
        Version.addVersion("$Id$");
    }
    static public String sendmail(        String host,
    int port,
    String subject,
    String message,
    String from,
    String to) throws Exception
    {
        
        DataOutputStream os = null;
        BufferedReader is = null;
        Socket smtpSocket = null;
        StringBuffer sb = new StringBuffer();
        try
        {
            // connect to SMTP host
            smtpSocket = new Socket(host, port);
            os = new DataOutputStream(smtpSocket.getOutputStream());
            is = new BufferedReader( new InputStreamReader(smtpSocket.getInputStream()));
            
            if(smtpSocket == null || os == null || is == null)
            {
                throw new Exception( "Sendmail could not connect" );
            }
            
            Date dDate = new Date();
            SimpleDateFormat dFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z",Locale.US);
            
            // skip past the hosts greeting line
            String greeting = is.readLine();
            
            // send the required commands, processing a response for some as required
            String hostname = InetAddress.getLocalHost().getCanonicalHostName();
            sb.append( sendcmd("HELO " + hostname,os,is));
            sb.append( sendcmd("MAIL From:<" + from + ">",os,is));
            
            sb.append( sendcmd("RCPT To:<" + to + ">",os,is));
            
            sb.append( sendcmd("DATA",os,is));
            sb.append( senddata("DATE: " + dFormat.format(dDate),os));
            sb.append( senddata("From: " + from,os)) ;
            sb.append( senddata("To: " + to,os));
            
            sb.append( senddata("Subject: " + subject ,os));
            sb.append( senddata(escapeMsg(message),os));
            sb.append( senddata(".",os));
            
            sb.append( sendcmd("QUIT",os,is));
            
            // disconnect from SMTP host
            smtpSocket.close();
            
            return( sb.toString() );
        }
        catch ( Exception e )
        {
            try
            {
                // if something goes wrong - try to QUIT
                sendcmd("QUIT", os, is);
                smtpSocket.close();
            }
            catch ( Exception e2 )
            {}
            throw e;
        }
        
        
    }
    
    static private String sendcmd(String cmd, DataOutputStream os, BufferedReader is) throws Exception
    {
        // send a command
        String s = "Sending: " + cmd + "\n";
        os.writeBytes(cmd + "\r\n");
        
        // read 1 response line - can't handle more than that now
        String responseline = is.readLine();
        s += "Response: " + responseline + "\n";
        
        // error if we get an error code to something other than HELO
        if( (responseline.charAt(0) == '5' || responseline.charAt(0) == '4') && !cmd.equals("HELO"))
        {
            throw new Exception("Error sending mail - " + responseline );
        }
        
        return(s);
    }
    
    static private String senddata(String data, DataOutputStream os) throws Exception
    {
        // send data and don't wait for a response
        os.writeBytes(data + "\r\n");
        return( "Sending: " + data + "\n" );
    }
    
    private static String escapeMsg( String msg )
    {
        StringBuffer newmsg = new StringBuffer();
        
        for (int i = 0; i < msg.length(); i++)
        {
            
            // escape lone newlines
            if (msg.charAt(i) == '\n')
            {
                if( i == 0 || msg.charAt(i-1) != '\r')
                {
                    newmsg.append('\r');
                }
            }
            // escape lone periods
            else if( msg.charAt(i) == '.')
            {
                if( ( i == 0 || msg.charAt(i-1) == '\n') && msg.charAt(i+1) == '\n' )
                {
                    newmsg.append('.');
                }
            }
            newmsg.append(msg.charAt(i) );
          
        }
        
        return (newmsg.toString());
    }
    public Sendmail()
    {
    }
    
    
    
    
}
