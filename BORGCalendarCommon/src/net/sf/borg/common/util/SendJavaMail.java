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
 * Sendmail.java
 *
 * Created on October 20, 2003, 10:01 AM
 */

package net.sf.borg.common.util;

/**
 * 
 * @author mbb
 */

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendJavaMail {

    public static void main(String[] args) {
	sendMail("biff", "test msg", "mbb@e", "quiet@patmedia.net", "mbb",
		"xxx");
    }

    private static class MyAuthenticator extends Authenticator {
	private String username;

	private String password;

	public MyAuthenticator(String user, String pass) {
	    username = user;
	    password = pass;
	}

	public PasswordAuthentication getPasswordAuthentication() {
	    return new PasswordAuthentication(username, password);
	}
    }

    public static void sendMail(String host, String msgText, String from,
	    String to, String user, String pass) {

	// create some properties and get the default Session
	Properties props = new Properties();
	props.put("mail.smtp.host", host);
	String ed = Prefs.getPref(PrefName.EMAILDEBUG);
	if (ed.equals("1"))
	    props.put("mail.debug", "true");

	Authenticator auth = null;
	if (user != null && !user.equals("") && pass != null
		&& !pass.equals("")) {
	    auth = new MyAuthenticator(user, pass);
	    props.put("mail.smtp.auth", "true");
	}
	Session session = Session.getDefaultInstance(props, auth);

	// session.setDebug(debug);

	try {
	    // create a message
	    Message msg = new MimeMessage(session);
	    msg.setFrom(new InternetAddress(from));
	    InternetAddress[] address = { new InternetAddress(to) };
	    msg.setRecipients(Message.RecipientType.TO, address);
	    msg.setSubject(Resource.getPlainResourceString("Reminder_Notice"));
	    msg.setSentDate(new Date());
	    msg.setText(msgText);

	    Transport.send(msg);
	} catch (MessagingException mex) {
	    System.out.println("\n--Exception handling in BORG.SendJavaMail");

	    mex.printStackTrace();
	    System.out.println();
	    Exception ex = mex;
	    do {
		if (ex instanceof SendFailedException) {
		    SendFailedException sfex = (SendFailedException) ex;
		    Address[] invalid = sfex.getInvalidAddresses();
		    if (invalid != null) {
			System.out.println("    ** Invalid Addresses");

			for (int i = 0; i < invalid.length; i++)
			    System.out.println("         " + invalid[i]);

		    }
		    Address[] validUnsent = sfex.getValidUnsentAddresses();
		    if (validUnsent != null) {
			System.out.println("    ** ValidUnsent Addresses");
			if (validUnsent != null) {
			    for (int i = 0; i < validUnsent.length; i++)
				System.out
					.println("         " + validUnsent[i]);
			}
		    }
		    Address[] validSent = sfex.getValidSentAddresses();
		    if (validSent != null) {
			System.out.println("    ** ValidSent Addresses");
			if (validSent != null) {
			    for (int i = 0; i < validSent.length; i++)
				System.out.println("         " + validSent[i]);
			}
		    }
		}
		System.out.println();
		if (ex instanceof MessagingException)
		    ex = ((MessagingException) ex).getNextException();
		else
		    ex = null;
	    } while (ex != null);
	}
    }

    public static void sendCalMail(String host, String msgText, String from,
	    String to, String user, String pass, String cal, String vcal) {

	// create some properties and get the default Session
	Properties props = new Properties();
	props.put("mail.smtp.host", host);
	String ed = Prefs.getPref(PrefName.EMAILDEBUG);
	if (ed.equals("1"))
	    props.put("mail.debug", "true");

	Authenticator auth = null;
	if (user != null && !user.equals("") && pass != null
		&& !pass.equals("")) {
	    auth = new MyAuthenticator(user, pass);
	    props.put("mail.smtp.auth", "true");
	}
	Session session = Session.getDefaultInstance(props, auth);

	// session.setDebug(debug);

	try {

	    MimeMultipart mp = new MimeMultipart();
	    MimeBodyPart b1 = new MimeBodyPart();
	    b1.setContent(msgText, "text/plain");
	    mp.addBodyPart(b1);

	    MimeBodyPart b2 = new MimeBodyPart();
	    b2.setContent(cal, "text/calendar");
	    b2.setFileName("meeting.ics");
	    b2.setDescription(Resource
		    .getPlainResourceString("Reminder_Notice"));

	    b2.setContentID("calendar_message");

	    b2
		    .setHeader("Content-Class",
			    "urn:content-classes:calendarmessage");
	    b2.setHeader("Content-ID", "calendar_message");

	    mp.addBodyPart(b2);

	    MimeBodyPart b3 = new MimeBodyPart();
	    b3.setContent(vcal, "text/calendar");
	    b3.setFileName("meeting.vcs");
	    b3.setDescription(Resource
		    .getPlainResourceString("Reminder_Notice"));
	    b3.setContentID("calendar_message");

	    b3
		    .setHeader("Content-Class",
			    "urn:content-classes:calendarmessage");
	    b3.setHeader("Content-ID", "calendar_message");

	    mp.addBodyPart(b3);

	    MimeMessage msg = new MimeMessage(session);
	    msg.setFrom(new InternetAddress(from));
	    InternetAddress[] address = { new InternetAddress(to) };
	    msg.setRecipients(Message.RecipientType.TO, address);
	    msg.setSubject(Resource.getPlainResourceString("Reminder_Notice"));
	    msg.setSentDate(new Date());
	    msg.addHeaderLine("METHOD=REQUEST");
	    msg.addHeaderLine("charset=UTF-8");
	    msg.addHeaderLine("component=VEVENT");

	    msg.setContent(mp);

	    Transport.send(msg);
	} catch (MessagingException mex) {
	    System.out.println("\n--Exception handling in BORG.SendJavaMail");

	    mex.printStackTrace();
	    System.out.println();
	    Exception ex = mex;
	    do {
		if (ex instanceof SendFailedException) {
		    SendFailedException sfex = (SendFailedException) ex;
		    Address[] invalid = sfex.getInvalidAddresses();
		    if (invalid != null) {
			System.out.println("    ** Invalid Addresses");

			for (int i = 0; i < invalid.length; i++)
			    System.out.println("         " + invalid[i]);

		    }
		    Address[] validUnsent = sfex.getValidUnsentAddresses();
		    if (validUnsent != null) {
			System.out.println("    ** ValidUnsent Addresses");
			if (validUnsent != null) {
			    for (int i = 0; i < validUnsent.length; i++)
				System.out
					.println("         " + validUnsent[i]);
			}
		    }
		    Address[] validSent = sfex.getValidSentAddresses();
		    if (validSent != null) {
			System.out.println("    ** ValidSent Addresses");
			if (validSent != null) {
			    for (int i = 0; i < validSent.length; i++)
				System.out.println("         " + validSent[i]);
			}
		    }
		}
		System.out.println();
		if (ex instanceof MessagingException)
		    ex = ((MessagingException) ex).getNextException();
		else
		    ex = null;
	    } while (ex != null);
	}
    }

}
