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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow.mail;

import java.io.Serializable;

/**
 * This class implements the interface <code>IPSMailMessageContext</code> is the
 * container for all components required by the mail message that is sent by the
 * workflow engine.
 */
public class PSMailMessageContext implements IPSMailMessageContext,
        Serializable {

   private static final long serialVersionUID = 3307633540189186300L;

   public PSMailMessageContext(String from, String to, String cc,
                               String subject, String body, String url, String mailDomain,
                               String smtpHost) {
      this(from, to, cc, subject, body, url, mailDomain, smtpHost, null,
              null, null, null, null, null);
   }

   public PSMailMessageContext(String from, String to, String cc,
                               String subject, String body, String url, String mailDomain,
                               String smtpHost, String smtpUserName, String smtpPassword,
                               String smtpIsTLSEnabled, String smtpPortNumber,
                               String smtpSSLPortNumber, String smtpBounceAddr) {
      if (to == null || to.trim().length() < 1) {
         throw new IllegalArgumentException(
                 "Recipient's address must not be "
                         + "empty or null in message context");
      }
      m_From = from;
      m_To = to;
      m_Cc = cc;
      m_Subject = subject;
      m_Body = body;
      m_Url = url;
      m_MailDomain = mailDomain;
      m_SmtpHost = smtpHost;
      m_SmtpUserName = smtpUserName;
      m_SmtpPassword = smtpPassword;
      m_SmtpIsTLSEnabled = smtpIsTLSEnabled;
      m_SmtpPortNumber = smtpPortNumber;
      m_SmtpSSLPortNumber = smtpSSLPortNumber;
      m_SmtpBounceAddr = smtpBounceAddr;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getFrom() {
      return m_From;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getTo() {
      return m_To;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getCc() {
      return m_Cc;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSubject() {
      return m_Subject;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getBody() {
      return m_Body;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getURL() {
      return m_Url;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getMailDomain() {
      return m_MailDomain;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSmtpHost() {
      return m_SmtpHost;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getUserName() {
      return m_SmtpUserName;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getPassword() {
      return m_SmtpPassword;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getIsTLSEnabled() {
      return m_SmtpIsTLSEnabled;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getPortNumber() {
      return m_SmtpPortNumber;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSSLPortNumber() {
      return m_SmtpSSLPortNumber;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getBounceAddr() {
      return m_SmtpBounceAddr;
   }

   /**
    * Name of the user the mail notification is issued on behalf of.
    */
   private String m_From = null;

   /**
    * Names of the users or roles the mail notification is required to reach.
    */
   private String m_To = null;

   /**
    * CC list users a copy of the mail notification is to be sent.
    */
   private String m_Cc = null;

   /**
    * Subject of the mail notification.
    */
   private String m_Subject = null;

   /**
    * The Body of the mail message.
    */
   private String m_Body = null;

   /**
    * Url of the content item to include in the mail notification.
    */
   private String m_Url = null;

   /**
    * Mail Domain of the sender or recipients.
    */
   private String m_MailDomain = null;

   /**
    * SMTP host name for the mail plugin
    */
   private String m_SmtpHost = null;

   /**
    * SMTP user name for the mail plugin
    */
   private String m_SmtpUserName = null;

   /**
    * SMTP pass word for the mail plugin
    */
   private String m_SmtpPassword = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String m_SmtpIsTLSEnabled = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String m_SmtpPortNumber = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String m_SmtpSSLPortNumber = null;

   /**
    * State of smtp bounce address.
    */
   private String m_SmtpBounceAddr = null;
}
