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

package com.percussion.mail;

import com.percussion.error.PSIllegalArgumentException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


/**
 * The PSSmtpMailProvider class implements support for sending SMTP mail.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSmtpMailProvider extends PSMailProvider
{
   /**
    * The property name for the SMTP host to use for routing messages.
    */
   public static final String   PROPERTY_HOST = "host";

   /**
    * The description text for the PROPERTY_HOST property.
    */
   public static final String   PROPERTY_DESC_HOST = "The host to use for routing SMTP mail messages.";

   /**
    * The name of this provider.
    */
   public static final String   PROVIDER_NAME = "SMTP";

   /**
    * The full name of this provider.
    */
   public static final String   PROVIDER_FULLNAME   = "Simple Mail Transfer Protocol";

   /**
    * The description of this provider.
    */
   public static final String   PROVIDER_DESCRIPTION   = "Simple Mail Transfer Protocol (SMTP) implementation for sending mail messages.";


   /**
    * Construct an SMTP mail provider.
    * <P>
    * The following properties must be specified:
    * <TABLE BORDER="1">
    *      <TR><TH>Key</TH><TH>Description</TH></TR>
    *      <TR>
    *         <TD>PSSmtpMailProvider.PROPERTY_HOST</TD>
    *         <TD>The name of the smtp mail server to route messages through</TD>
    *      </TR>
    * </TABLE>
    *
    * @param      props      the properties to use when constructing the server
    *
    * @exception   PSIllegalArgumentException
    *                        if the required properties are not specified
    */
   public PSSmtpMailProvider(java.util.Properties props)
      throws PSIllegalArgumentException
   {
      super();

      m_host = props.getProperty(PROPERTY_HOST);
      if (m_host == null) {
         throw new PSIllegalArgumentException(IPSMailErrors.HOST_NOT_VALID,
                                                "No host name");
      }
   }

   /**
    * Construct an SMTP mail provider without defining the properties
    * required for use. In this state, the object can only be
    * used for cataloging. If sending messages is required, call
    * setProperty or use the appropriate constructor.
    */
   public PSSmtpMailProvider()
   {
      super();
   }

   /**
    * Construct an SMTP mail provider.
    * <P>
    * The following properties must be specified:
    * <TABLE BORDER="1">
    *      <TR><TH>Key</TH><TH>Description</TH></TR>
    *      <TR>
    *         <TD>PSSmtpMailProvider.PROPERTY_HOST</TD>
    *         <TD>The name of the smtp mail server to route messages through</TD>
    *      </TR>
    * </TABLE>
    *
    * @param      props      the properties to use when constructing the server
    *
    * @exception   PSIllegalArgumentException
    *                        if the required properties are not specified
    */
   public void setProperties(java.util.Properties props)
      throws PSIllegalArgumentException
   {
      String host = props.getProperty(PROPERTY_HOST);
      if (host == null) {
         throw new PSIllegalArgumentException(
         IPSMailErrors.HOST_NOT_VALID, "No host name");
      }

      m_host = host;
   }

   /**
    * Get the name of this mail provider. This is the name to use
    * wherever a mail provider name is required.
    *
    * @return      the provider name
    */
   public String getName()
   {
      return PROVIDER_NAME;
   }

   /**
    * Get the full name of this mail provider. 
    * Many providers use acronyms as their name, so this is often
    * the expanded acronym.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return PROVIDER_FULLNAME;
   }

   /**
    * Get a brief the description of this mail provider.
    *
    * @return      the brief description
    */
   public String getDescription()
   {
      return PROVIDER_DESCRIPTION;
   }

   /**
    * Get the property definitions for this provider. The key is set to
    * the name of the property and the value is set to the description
    * of the property. These properties must be set to instantiate the
    * provider.
    *
    * @return      the properties required by this provider
    */
   public java.util.Properties getPropertyDefs()
   {
      java.util.Properties props = new java.util.Properties();
      props.put(PROPERTY_HOST, PROPERTY_DESC_HOST);
      return props;
   }

   /**
    * Send a mail message through this provider.
    *
    * @param      msg      the message to send
    *
    * @exception   java.io.IOException
    *                        if an I/O error occurs
    *
    * @exception   PSMailSendException
    *                        if an error occurs sending the message
    */
   public void send(PSMailMessage msg)
      throws java.io.IOException, PSMailSendException
   {
      if (m_host == null) {
         throw new PSMailSendException(
            IPSMailErrors.HOST_NOT_VALID, "The SMTP host name has not been specified.");
      }

      // validate all the inputs:

      // 1. need at least one recipient (to, cc or bcc)
      String recipients[] = msg.getRecipients();
      if (recipients.length <= 0)
      {
         throw new PSMailSendException(IPSMailErrors.MAIL_ADDRESS_EMPTY);
      }

      // 2. all names (from and recipients) must be of the form user@domain
      for (int i = 0; i < recipients.length; i++)
      {
         int pos = recipients[i].indexOf("@");
         if ( (pos > 0) && (pos < (recipients[i].length()) - 1) )
            continue;
         throw new PSMailSendException(IPSMailErrors.MAIL_ADDRESS_INVALID,
            recipients[i]);
      }

      Socket smtpConn = null;
      try {
         smtpConn = new Socket(m_host, 25);   // SMTP is on port 25
      } catch (java.net.UnknownHostException e) {
         throw new PSMailSendException(
            IPSMailErrors.HOST_NOT_VALID, "Invalid host: " + e.toString());
      } catch (java.io.IOException e) {
         throw new PSMailSendException(
            IPSMailErrors.HOST_NOT_VALID, "Invalid host: " + e.toString());
      }

      BufferedReader in = null;
      BufferedWriter out = null;
      try {
         in = new BufferedReader(new InputStreamReader(
            smtpConn.getInputStream()));
         out = new BufferedWriter(
            new OutputStreamWriter(smtpConn.getOutputStream(), msg.getCharEncoding()));
      } catch (java.io.IOException e) {
         throw new PSMailSendException(
            IPSMailErrors.MAIL_SERVER_CONNECTION_ERROR,
            "Socket failure: " + e.toString());
      }

      // the first thing the SMTP server does is send us a 220
      // telling us it's up, and the domain it's servicing
      String smtpResponse = in.readLine();
      if (!smtpResponse.startsWith("220")) {
         throw new PSMailSendException(
            IPSMailErrors.MAIL_SERVER_UP_EXCEPTION,
            "Session open failed: " + smtpResponse);
      }

      // initiate the session with the target server
      try {
         String[] fromParts = getNameParts(msg.getFrom());   // need the from domain
         sendLine(out, in, "HELO " + fromParts[1], "250");
      } catch (PSIllegalArgumentException e) {
         throw new PSMailSendException(
            e.getErrorCode(), e.getErrorArguments());
      }

      // tell SMTP it can route it or store it in a local mail box
      // we also tell it who the message is from
      sendLine(out, in, "MAIL FROM:<" + msg.getFrom() + ">", "250");

      // now set up the to, cc and bcc recpients
      String[] recpients = msg.getRecipients();
      for (int i = 0; i < recpients.length; i++) {
         // we really need to check for 250 or 251 for successful send
         // also check for 551 which tells us how to forward this
         // any other response, and we either need to figure out the
         // route to the specified user, or we must deal with it as an error
         sendLine(out, in, "RCPT TO:<" + recpients[i] + ">", "250");
      }

      // let it know we'll begin sending data (354 is means ok here)
      sendLine(out, in, "DATA", "354");

      // now send the mail message body, as defined in RFC 822
      msg.write(out);
      out.flush();   // they must have called this, but make sure

      // now send the message terminator (.) and check the response
      sendLine(out, in, "\r\n.", "250");

      // we can now close the session (which gets 221 on successful close)
      sendLine(out, in, "QUIT", "221");
   }

   /**
    * Send a line of data for which the specified response is expected.
    */
   private static void sendLine(
      BufferedWriter out, BufferedReader in,
      String outData, String expectedResponseCode)
      throws java.io.IOException, PSMailSendException
   {
      out.write(outData);
      out.write("\r\n");
      out.flush();

      // some sends don't get responses
      if (expectedResponseCode != null) {
         // now check the response
         String respLine = in.readLine();
            
         if ((respLine == null) || (!respLine.startsWith(expectedResponseCode))) {
            // this is an error, deal with it (throw an exception)
            String message = (respLine == null) ? "no response" : respLine;
            Object[] args = { message };
            int msgCode = IPSMailErrors.MAIL_SEND_UNEXPECTED_EXCEPTION;
            throw new PSMailSendException(msgCode, args);
         }
      }
   }

   /**
    * Send a line of data for which no response is expected.
    */
   private static void sendLine(
      BufferedWriter out, String outData)
      throws java.io.IOException
   {
      out.write(outData);
      out.write("\r\n");
      out.flush();
   }


   private String m_host = null;
}

