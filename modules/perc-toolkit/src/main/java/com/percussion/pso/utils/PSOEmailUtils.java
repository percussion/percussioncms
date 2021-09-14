package com.percussion.pso.utils;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PSOEmailUtils {

	  /**
	    * Logger for this class
	    */
	private static final Logger log = LogManager.getLogger(PSOEmailUtils.class);

	/***
	 * Takes a comma seperated list of email addresses and returns a list of 
	 * Address instances.
	 * @param list
	 * @return
	 * @throws AddressException
	 */
	 private static List<InternetAddress> splitEmailAddresses(final String list) throws AddressException{
			
		 ArrayList<InternetAddress> ret = new ArrayList<>();
		 
		 if(list.contains(","))
         {
        	 String[] to_addresses = list.split(",");
         
        	 for(String email : to_addresses){
        		 ret.add(new InternetAddress(email));
        	 }
         }else{
        	 ret.add(new InternetAddress(list));
         }
        
		 return ret;
	 }
	 
	 /***
	  * Sends an email using the specified parameters and the SMTP configuration
	  * defined in the system /Workflow/rxworkflow.properties file,(the default) or some oether properties file
	  * 
	  * @param from_line
	  * @param to_line
	  * @param cc_line
	  * @param bcc_line
	  * @param subject
	  * @param body
	  */
	@SuppressFBWarnings("PATH_TRAVERSAL_IN")
	public static void sendEmail(String from_line, String to_line, String cc_line, String bcc_line, String subject, String body)
	{
		try
		{
			String propFile = null;
			
			Properties rxconfigProps = new Properties();
			
			propFile = PSServer.getRxFile(PSServer.BASE_CONFIG_DIR + "/Workflow/rxworkflow.properties");

			try(FileInputStream fis = new FileInputStream(propFile)) {
				rxconfigProps.load(fis);
			}
		     String smtpHost = rxconfigProps.getProperty("SMTP_HOST");
	         Properties props = System.getProperties();
	         props.put("mail.host", smtpHost);
	         props.put("mail.transport.protocol", "SMTP");
	         Session session = Session.getDefaultInstance(props, null);
	         
	         Message message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(from_line));	         
	         message.addRecipients(Message.RecipientType.TO, splitEmailAddresses(to_line).toArray(new Address[0]));

	         if(cc_line != null)
	            message.addRecipients(Message.RecipientType.CC, splitEmailAddresses(cc_line).toArray(new Address[0]));

			 if(bcc_line != null)
			 	message.addRecipients(Message.RecipientType.BCC, splitEmailAddresses(bcc_line).toArray(new Address[0]));

			 message.setSubject(SecureStringUtils.stripAllLineBreaks(subject));
	         message.setText(body);
	         Transport.send(message);
	  }
	  catch(Exception e)
	  {
			log.error(PSExceptionUtils.getMessageForLog(e));
			log.debug(e);
	  }
	}
	
	
	
}
