/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.workflow.mail;

import com.percussion.extension.IPSExtensionErrors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

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
   @SuppressFBWarnings("SMTP_HEADER_INJECTION")
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
      } catch (MessagingException e)
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
