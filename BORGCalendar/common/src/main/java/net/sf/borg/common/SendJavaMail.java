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

package net.sf.borg.common;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * utility class to send out email via java mail
 */

public class SendJavaMail {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	/**
	 * boilerplate Authenticator.
	 */
	private static class MyAuthenticator extends Authenticator {

		private String username;
		private String password;

		public MyAuthenticator(String user, String pass) {
			username = user;
			password = pass;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	}

	/**
	 * Send an mail.
	 * 
	 * @param host
	 *            the smtp host
	 * @param msgText
	 *            the email text
	 * @param subject
	 *            the subject
	 * @param from
	 *            the from address
	 * @param to
	 *            the to address
	 * @param user
	 *            the smtp user
	 * @param pass
	 *            the smtp password
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void sendMail(String host, String msgText, String subject,
			String from, String to, String user, String pass) throws Exception {

		// create some properties and get the default Session
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		String port = Prefs.getPref(PrefName.EMAILPORT);
		props.put("mail.smtp.port", port);
		String ed = Prefs.getPref(PrefName.EMAILDEBUG);
		if (ed.equals("1"))
			props.put("mail.debug", "true");
		if (Prefs.getBoolPref(PrefName.ENABLETLS))
			props.put("mail.smtp.starttls.enable", "true");

		Authenticator auth = null;
		if (user != null && !user.equals("") && pass != null
				&& !pass.equals("")) {
			auth = new MyAuthenticator(user, pass);
			props.put("mail.smtp.auth", "true");
		}
		Session session = Session.getInstance(props, auth);

		//session.setDebug(true);

		try {
			// create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = { new InternetAddress(to) };
			msg.setRecipients(Message.RecipientType.TO, address);
			// msg.setSubject(Resource.getResourceString("Reminder_Notice"));
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setText(msgText);

			Transport.send(msg);
		} catch (MessagingException mex) {
			processMessagingException(mex);
			throw mex;
		}
	}

	/**
	 * Send a multipart mime email with attachments
	 * 
	 * @param host
	 *            the smtp host
	 * @param msgText
	 *            the email text
	 * @param subject
	 *            the subject
	 * @param from
	 *            the from address
	 * @param to
	 *            the to address
	 * @param user
	 *            the smtp user
	 * @param pass
	 *            the smtp password
	 * @param attachments
	 * 			  array of filenames to attach
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void sendMailWithAttachments(String host, String msgText,
			String subject, String from, String to, String user, String pass, String[] attachments)
			throws Exception {

		// create some properties and get the default Session
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		String port = Prefs.getPref(PrefName.EMAILPORT);
		props.put("mail.smtp.port", port);
		String ed = Prefs.getPref(PrefName.EMAILDEBUG);
		if (ed.equals("1"))
			props.put("mail.debug", "true");
		if (Prefs.getBoolPref(PrefName.ENABLETLS))
			props.put("mail.smtp.starttls.enable", "true");

		Authenticator auth = null;
		if (user != null && !user.equals("") && pass != null
				&& !pass.equals("")) {
			auth = new MyAuthenticator(user, pass);
			props.put("mail.smtp.auth", "true");
		}
		Session session = Session.getInstance(props, auth);

		//session.setDebug(true);

		try {
			// create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = { new InternetAddress(to) };
			msg.setRecipients(Message.RecipientType.TO, address);
			// msg.setSubject(Resource.getResourceString("Reminder_Notice"));
			msg.setSubject(subject);
			msg.setSentDate(new Date());

			Multipart mp = new MimeMultipart();

			MimeBodyPart textpart = new MimeBodyPart();
			textpart.setText(msgText);
			mp.addBodyPart(textpart);
			
			for( String filename : attachments)
			{
				File file = new File(filename);
				MimeBodyPart attpart = new MimeBodyPart();
			    DataSource source = 
			      new FileDataSource(file);
			    attpart.setDataHandler(
			      new DataHandler(source));
			    attpart.setFileName(file.getName());
			    mp.addBodyPart(attpart);
			}

			// add the Multipart to the message
			msg.setContent(mp);
			Transport.send(msg);
		} catch (MessagingException mex) {
			processMessagingException(mex);
			throw mex;
		}
	}

	/**
	 * Process a messaging exception - print out something useful to stdout
	 * 
	 * @param mex
	 *            the MessagingException
	 */
	static private void processMessagingException(MessagingException mex) {
		StringBuffer buf = new StringBuffer();
		buf.append("\n--Exception handling in BORG.SendJavaMail\n");

		mex.printStackTrace();
		Exception ex = mex;
		do {
			if (ex instanceof SendFailedException) {
				SendFailedException sfex = (SendFailedException) ex;
				Address[] invalid = sfex.getInvalidAddresses();
				if (invalid != null) {
					buf.append("    ** Invalid Addresses\n");

					for (int i = 0; i < invalid.length; i++)
						buf.append("         " + invalid[i] + "\n");

				}
				Address[] validUnsent = sfex.getValidUnsentAddresses();
				if (validUnsent != null) {
					buf.append("    ** ValidUnsent Addresses\n");

					for (int i = 0; i < validUnsent.length; i++)
						buf.append("         " + validUnsent[i] + "\n");

				}
				Address[] validSent = sfex.getValidSentAddresses();
				if (validSent != null) {
					buf.append("    ** ValidSent Addresses\n");

					for (int i = 0; i < validSent.length; i++)
						buf.append("         " + validSent[i] + "\n");

				}
			}
			
			if (ex instanceof MessagingException)
				ex = ((MessagingException) ex).getNextException();
			else
				ex = null;
		} while (ex != null);
		
		log.severe(buf.toString());
	}

}
