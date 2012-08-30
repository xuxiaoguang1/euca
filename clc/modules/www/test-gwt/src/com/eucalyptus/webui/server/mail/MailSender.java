package com.eucalyptus.webui.server.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class MailSender {
	
	public void send(String mail, String subject, String text) {
		Properties props = new Properties();
		
		props.setProperty("mail.transport.protocol", "smtp");
	    props.setProperty("mail.host", MailSenderInfo.instance().getHost());
	    props.setProperty("mail.user", MailSenderInfo.instance().getUser());
	    props.setProperty("mail.password", MailSenderInfo.instance().getPwd());
	      
        Session session = Session.getDefaultInstance(props, null);

        Transport transport;
		try {
			transport = session.getTransport();
	        MimeMessage message = new MimeMessage(session);
	        message.setSubject(subject);
	        message.setContent(text, "text/plain");
	        //message.addRecipient(Message.RecipientType.TO, new InternetAddress("elvis@presley.org"));
	
	        transport.connect();
	        transport.sendMessage(message,
	            message.getRecipients(Message.RecipientType.TO));
	        transport.close();
        
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
