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

import com.percussion.security.PSNotificationEmailAddress;

import java.io.Serializable;
import java.util.List;

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
      this.from = from;
      this.to = to;
      this.cc = cc;
      this.subject = subject;
      this.body = body;
      this.url = url;
      this.mailDomain = mailDomain;
      this.smtpHost = smtpHost;
      this.smtpUserName = smtpUserName;
      this.smtpPassword = smtpPassword;
      this.smtpIsTLSEnabled = smtpIsTLSEnabled;
      this.smtpPortNumber = smtpPortNumber;
      this.smtpSSLPortNumber = smtpSSLPortNumber;
      this.smtpBounceAddr = smtpBounceAddr;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getFrom() {
      return from;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getTo() {
      return to;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getCc() {
      return cc;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSubject() {
      return subject;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getBody() {
      return body;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getURL() {
      return url;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getMailDomain() {
      return mailDomain;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSmtpHost() {
      return smtpHost;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getUserName() {
      return smtpUserName;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getPassword() {
      return smtpPassword;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getIsTLSEnabled() {
      return smtpIsTLSEnabled;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getPortNumber() {
      return smtpPortNumber;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getSSLPortNumber() {
      return smtpSSLPortNumber;
   }

   /*
    * Implementation of the method in the interface
    */
   @Override
   public String getBounceAddr() {
      return smtpBounceAddr;
   }

   /**
    * Gets the source list of notification email addresses, used in debug / logging.  Source email list
    * should include information on the source role / subject that the email was pulled from.
    *
    * @return may be empty.  the list of source to email addresses for this message.
    */
   @Override
   public List<PSNotificationEmailAddress> getSourceToList() {
      return sourceToList;
   }

   /**
    * Gets the source list of notification cc email addresses, used in debug / logging.  source email list
    * should include information on the source role / subject that the email was pulled from.
    *
    * @return may be empty.  the list of source cc email addresses for this message.
    */
   @Override
   public List<PSNotificationEmailAddress> getSourceCCList() {
      return sourceCCList;
   }

   /**
    * Sets the source list of to email addresses.
    *
    * @param sourceTo the list of to email addresses
    */
   @Override
   public void setSourceToList(List<PSNotificationEmailAddress> sourceTo) {
      sourceToList = sourceTo;
   }

   /**
    * Sets the list of cc source email addresses
    *
    * @param sourceCC the list of cc addresses
    */
   @Override
   public void setSourceCCList(List<PSNotificationEmailAddress> sourceCC) {
      sourceCCList = sourceCC;
   }

   /**
    * Name of the user the mail notification is issued on behalf of.
    */
   private String from = null;

   /**
    * Names of the users or roles the mail notification is required to reach.
    */
   private String to = null;

   /**
    * CC list users a copy of the mail notification is to be sent.
    */
   private String cc = null;

   /**
    * Subject of the mail notification.
    */
   private String subject = null;

   /**
    * The Body of the mail message.
    */
   private String body = null;

   /**
    * Url of the content item to include in the mail notification.
    */
   private String url = null;

   /**
    * Mail Domain of the sender or recipients.
    */
   private String mailDomain = null;

   /**
    * SMTP host name for the mail plugin
    */
   private String smtpHost = null;

   /**
    * SMTP user name for the mail plugin
    */
   private String smtpUserName = null;

   /**
    * SMTP pass word for the mail plugin
    */
   private String smtpPassword = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String smtpIsTLSEnabled = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String smtpPortNumber = null;

   /**
    * State of whether TLS is enabled on the SMTP server or not.
    */
   private String smtpSSLPortNumber = null;

   /**
    * State of smtp bounce address.
    */
   private String smtpBounceAddr = null;

   private List<PSNotificationEmailAddress> sourceToList;

   private List<PSNotificationEmailAddress> sourceCCList;


}
