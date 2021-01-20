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
 * This interface defines methods to access the mail message related data. The
 * sendMessage method of the IPSMailProgram interface gets this read only object
 * so that the plugin for custom mail program can access the data and send the
 * message.
 */
public interface IPSMailMessageContext extends Serializable
{
   /**
    * Returns the name of the user or role that is sending the workflow
    * notification.
    *
    * @return sender's name as String, may be <code>null</code> or
    * <code>empty</code>.
    *
    */
   String getFrom();

   /**
    * Returns the comma separated list of all users or roles that need to
    * receive the message as per the workflow definition.
    *
    * @return comma separated list of user or role names as String, Shall never
    * be <code>null</code> or <code>empty</code>.
    *
    */
   String getTo();

   /**
    * Returns the comma separated list of all users (roles) that need to receive
    * a copy of the the message. Meant for future use.
    *
    * @return comma separated list of user or role names as String, can be
    * <code>null</code> or <code>empty</code>.
    *
    */
   String getCc();

   /**
    * Returns the subject of the mail message as defined in the workflow
    * definition.
    *
    * @return subject of mail message as String, can be <code>null</code>
    * or <code>empty</code>.
    */
   String getSubject();

   /**
    * Returns the body of the mail message as defined in the workflow
    * definition.
    *
    * @return Body of the message as String, can be <code>null</code> or
    * <code>empty</code>.
    */
   String getBody();

   /**
    * Returns the URL to access the current content item in the workflow defined
    * in the workflow definition. This is normally appended to the message body.
    *
    * @return URL of the current content item as String, can be
    * <code>null</code> or <code>empty</code>.
    */
   String getURL();

   /**
    * Returns the mail domain of the user as registered in the workflow
    * properties file for the variable MAIL_DOMAIN
    * (with the syntax @percussion.com). The sender or recipients' names
    * normally need to appended with the domain name while sending mail
    * notification.
    *
    * @return mail domain, can be <code>null</code> or <code>empty</code>.
    */
   String getMailDomain();

   /**
    * Returns the SMTP host name as registered in the workflow
    * properties file for the variable SMTP_HOST (in the syntax
    * ne.mediaone.net).
    *
    * @return smtp HOST NAME, can be <code>null</code> or <code>empty</code>.
    */
   String getSmtpHost();

   /**
    * Returns the user name used to authenticate
    * to the SMTP server.
    * @return smtp user name, can be <code>null</code> or <code>empty</code>.
    */
   String getUserName();

   /**
    * Returns the password used to authenticate
    * to the SMTP server.
    * @return smtp password, can be <code>null</code> or <code>empty</code>.
    */
   String getPassword();

   /**
    * Returns whether or not TLS is enabled on
    * the SMTP server.
    * @return "true" if TLS is enabled on the SMTP server.  Can be
    * <code>null</code> or <code>empty</code>.
    */
   String getIsTLSEnabled();

   /**
    * Returns the port number of the SMTP server.
    * Typically defaults to 587.
    * @return the port number used to connect to the SMTP server.  Can be
    * <code>null</code> or <code>empty</code>.
    */
   String getPortNumber();

   /**
    * Gets the SSL port number used for SMTP.
    * @return the SSL port number.  Can be
    * <code>null</code> or <code>empty</code>.
    */
   String getSSLPortNumber();

   /**
    * Gets the email bounce address used for SMTP.
    * @return the email bounce address as a String.  Can be
    * <code>null</code> or <code>empty</code>.
    */
   String getBounceAddr();

}
