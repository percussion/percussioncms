/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow.mail;

import com.percussion.extension.IPSExtensionErrors;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

/**
 * This is the default implementation of the Mail Program interface
 * <code>IPSMailProgram</code> that needs to be implemented by any mail plugin
 * to be used by the workflow engine. This implementation uses the JAVAX mail
 * provided by Sun.
 */
public class PSJavaxMailProgram implements IPSMailProgram
{
   /**
    * Constructor
    */
   public PSJavaxMailProgram()
   {
   }

   /*
    * Method required by the interface.
    */
   public void init()
           throws PSMailException
   {
   }

   /*
    * Method required by the interface.
    */
   public void sendMessage(IPSMailMessageContext messageContext)
           throws PSMailException
   {
      // create some properties and get the default Session
      Properties props = new Properties();
      props.put("mail.smtp.host", messageContext.getSmtpHost());
      Session session = Session.getDefaultInstance(props, null);
      try
      {
         Message msg = new MimeMessage(session);
         String mailDomain = messageContext.getMailDomain();
         if ((null == mailDomain) || mailDomain.length() == 0)
         {
            throw new PSMailException(IPSExtensionErrors.MAIL_DOMAIN_EMPTY);
         }
         String mailCc = messageContext.getCc();

         if (StringUtils.isNotBlank(messageContext.getFrom()))
         {
            msg.setFrom(makeAddress(messageContext.getFrom(), mailDomain)[0]);
         }

         msg.setRecipients(Message.RecipientType.TO,
                 makeAddress(messageContext.getTo(), mailDomain));

         if ((null != mailCc) &&  mailCc.length() > 0)
         {
            msg.setRecipients(Message.RecipientType.CC,
                    makeAddress(mailCc, mailDomain));
         }

         msg.setSubject(messageContext.getSubject());
         msg.setSentDate(new java.util.Date());
         msg.setText(messageContext.getBody());

         Transport.send(msg);
      }
      catch (AddressException e)
      {
         throw new PSMailException(e);
      }
      catch (MessagingException e)
      {
         throw new PSMailException(e);
      }
   }

   /**
    * Helper routine that construct an array of internet address from a
    * comma-separated list of users or roles, appending the mail domain name to
    * any address that does not contain a "@".
    *
    * @param sUserList  comma-separated list of users.
    * @param mailDomain name of the mail domain. May optionally contain a
    *                   leading "@".
    *
    * @throws AddressException, if it cannot make Internet Addresses.
    *
    */
   private InternetAddress[] makeAddress(String sUserList,
                                         String mailDomain)
           throws AddressException
   {
      ArrayList l = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(sUserList, ",");
      String sToken = null;
      String userAddressString = null;
      if (!mailDomain.startsWith("@"))
      {
         mailDomain = "@" + mailDomain;
      }
      while(tokenizer.hasMoreElements())
      {
         sToken = tokenizer.nextToken().trim();
         if (0 != sToken.length() )
         {
            if (-1 == sToken.indexOf('@'))
            {
               userAddressString = sToken +  mailDomain;
            }
            else
            {
               userAddressString = sToken;
            }

            InternetAddress userInetAddress =
                    new InternetAddress(userAddressString);
            l.add(userInetAddress);
         }
      }
      InternetAddress[] inetAddressArray = new InternetAddress[l.size()];

      for (int i = 0; i < l.size(); i++ )
      {
         inetAddressArray[i] = (InternetAddress) l.get(i);
      }

      return inetAddressArray;
   }


   /*
    * Method required by the interface.
    */
   public void terminate()
           throws PSMailException
   {
   }
}
