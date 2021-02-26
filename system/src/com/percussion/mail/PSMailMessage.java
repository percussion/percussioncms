/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.mail;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCharSets;
import com.percussion.utils.date.PSDateFormatter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;


/**
 * The PSMailMessage class is used to create message which can be sent to
 * one or more users through the mail providers E2 mail providers. At this
 * time, only SMTP mail is supported.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSMailMessage implements IPSMailErrors
{
   /**
    * Construct an empty mail message. At least one name to send to must
    * be added.
    */
   public PSMailMessage()
   {
      super();

      // default from we use is e2. We'll try to make it e2@<local domain>
      // which is a bit better. If we can't get the local domain, we'll
      // leave the default of E2 and hope they call setFrom to override it
      try {
         String host = java.net.InetAddress.getLocalHost().getHostName();
         int pos = host.indexOf('.');
         if (pos != -1)
            m_from = "e2@" + host.substring(pos+1);
      } catch (java.net.UnknownHostException e) {
         m_from = "e2";
      }
   }

   /**
    * Add a user to the list of users to send to. This name will be added
    * to the <code>To</code> header, unless <code>setToHeaderString</code>
    * is called
    *
    * @param      name   the name of the user to send the message to, in
    *                     <code>user@domain</code> format
    *
    * @exception   PSIllegalArgumentException
    *                     if name is null, empty or is not in 
    *                     <code>user@domain</code> format
    *
    * @see         #setToHeaderString
    */
   public void addSendTo(String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateEmailAddress(name);
      if (ex != null)
         throw ex;

      m_sendTo.add(name);
   }

   /**
    * Add a user to the list of users to copy. This name will be added
    * to the <code>cc</code> header, unless <code>setToHeaderString</code>
    * is called
    *
    * @param      name   the name of the user to copy on the message, in
    *                     <code>user@domain</code> format
    *
    * @exception   PSIllegalArgumentException
    *                     if name is null, empty or is not in 
    *                     <code>user@domain</code> format
    *
    * @see         #setToHeaderString
    */
   public void addCopyTo(String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateEmailAddress(name);
      if (ex != null)
         throw ex;

      m_copyTo.add(name);
   }

   /**
    * Add a user to the list of users to blind copy. If a user is
    * blind copied, their name will not appear in the message text.
    *
    * @param      name   the name of the user to copy on the message, in
    *                     <code>user@domain</code> format
    *
    * @exception   PSIllegalArgumentException
    *                     if name is null, empty or is not in 
    *                     <code>user@domain</code> format
    */
   public void addBlindCopyTo(String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateEmailAddress(name);
      if (ex != null)
         throw ex;

      m_blindCopyTo.add(name);
   }

   /**
    * Validates an email address according to RFC 822 (single address)
    *
    * The syntax of an email address is:
    * <PRE>
    * addr-spec   =  local-part "@" domain        ; global address
    *
    * local-part  =  word *("." word)             ; uninterpreted
    *                                             ; case-preserved
    *
    * domain      =  sub-domain *("." sub-domain)
    *
    * sub-domain  =  domain-ref / domain-literal
    *
    * domain-ref  =  atom                         ; symbolic reference
    *
    * word        =  atom / quoted-string
    *
    * atom        =  1*<any CHAR except specials, SPACE and CTLs>
    *
    * </PRE>
    * @author   chadloder
    * 
    * @version 1.4 1999/06/17
    * 
    * @param   name
    * 
    * @return   PSIllegalArgumentException
    */
   private static PSIllegalArgumentException validateEmailAddress(String name)
   {
      if (name == null || name.length() == 0)
      {
         return new PSIllegalArgumentException(MAIL_ADDRESS_EMPTY);
      }

      int atPos = name.indexOf('@');
      if (atPos < 1 || atPos >= name.length() - 1)
      {
         return new PSIllegalArgumentException(MAIL_ADDRESS_INVALID, name);
      }

      String localPart = name.substring(0, atPos);
      String domain = name.substring(atPos + 1);

      if (localPart.length() == 0 || domain.length() == 0)
      {
         return new PSIllegalArgumentException(MAIL_ADDRESS_INVALID, name);
      }

      return null;
   }

   /**
    * Override the text displayed instead of the <code>To</code> and
    * <code>cc</code> headers of the message.
    * <P>
    * The default behavior is to display all names added through
    * <code>addSendTo</code> in the <code>To</code> header and all names
    * added through <code>addCopyTo</code> in the <code>cc</code> header,
    * and all names added through <code>addBlindCopyTo</code> in the
    * <code>bcc</code> header.
    * <P>
    * This should be used to display a text message, such as
    * "All Employees" instead of each particular name.
    *
    * @param      displayText      the text to use
    *
    * @exception   PSIllegalArgumentException
    *                              if displayText is null or empty
    */
   public void setToHeaderString(String displayText)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateToHeaderString(displayText);
      if (ex != null)
         throw ex;
      m_toHeaderForDisplay = displayText;
   }

   private static PSIllegalArgumentException validateToHeaderString(String toHeader)
   {
      if (toHeader == null || toHeader.length() == 0)
      {
         return new PSIllegalArgumentException(MAIL_CUSTOM_TO_HEADER_EMPTY);
      }

      if (toHeader.indexOf('\n')   > 0
         || toHeader.indexOf('\f') > 0
         || toHeader.indexOf('\r') > 0
         || toHeader.indexOf('\t') > 0)
      {
         return new PSIllegalArgumentException(MAIL_CUSTOM_TO_HEADER_INVALID,
            toHeader);
      }

      return null;
   }

   /**
    * Get the originator to use for the message.
    *
    * @return      the originator to use
    */
   public String getFrom()
   {
      return m_from;
   }

   /**
    * Set the originator to use for the message.
    *
    * @param      from      the originator to use
    *
    * @exception   PSIllegalArgumentException
    *                     if from is null, empty or is not in 
    *                     <code>user@domain</code> format
    */
   public void setFrom(String from)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateEmailAddress(from);
      if (ex != null)
         throw ex;
         
      m_from = from;
   }

   /**
    * Set the text to use as the subject line for the message.
    *
    * @param      subject      the subject line (may be null)
    */
   public void setSubject(String subject)
   {
      m_subject = subject;
   }

   /**
    * Set the the date to use as when the message was created. The date
    * and time this object was constructed is used by default.
    *
    * @param      date                           the date to use
    *
    * @exception   PSIllegalArgumentException      if date is null
    */
   public void setCreationDate(java.util.Date date)
      throws PSIllegalArgumentException
   {
      if (date != null)
         m_creationDate = date;
   }

   /**
    * Set the specified stream as the source of the body.
    *
    * @param      in      the stream to use
    */
   public void setBody(java.io.InputStream in)
      throws java.io.IOException
   {
      m_bodyIn = in;
   }

   /**
    * The text to append to the message. In a subclass PSQueuedNotification,
    * the appendBodyText method is added into carriage return and newline
    * for each appended text. This will make each newly appended text display
    * from a new line.
    *
    * <em>Note: </em>If an input stream has been previously defined using
    * setBody, its contents will be read into the buffer used to store
    * the message's text body. This may prove inefficient (high memory
    * usage). Use only setBody whenever possible.
    *
    * @param      text      the text to append
    */
   public void appendBodyText(String text)
      throws java.io.IOException
   {
      if (m_bodyIn != null)
      {
         char[] buf = new char[2048];
         InputStreamReader rdr = new InputStreamReader(m_bodyIn, m_charEncoding);

         while (rdr.ready())
         {
            int len = rdr.read(buf, 0, buf.length);
            if (len < 0)
               break;
            appendWrappedBodyText(buf, 0, len);
         }

         m_bodyIn = null;
      }

      final char[] chars = text.toCharArray();
      appendWrappedBodyText(chars, 0, chars.length);
   }


   private void appendWrappedBodyText(
      final char[] chars, final int offset, final int len)
   {
      m_bodyBuf.ensureCapacity(m_bodyBuf.length() + (len - offset));

      char c;

      // the start of the latest run of normal characters
      int startNormal = offset;
      
      int i = offset;
      while (true)
      {
         if (i == len)
         {
            if (startNormal != i)
            {
               writeWithBreaks(chars, startNormal, i - startNormal); // flush run and stop
               
               // when we exit the loop, this makes the m_afterLastBreak calculation work out
               i = m_afterLastBreak;
               startNormal = 0;
            }
            break;
         }
         c = chars[i];
         switch (c)
         {
            case '\r' :
               if (startNormal != i)
                  writeWithBreaks(chars, startNormal, i - startNormal); // flush previous run
   
               m_bodyBuf.append("\r\n");
               m_afterLastBreak = 0;
               startNormal = i + 1;

               if (i < (len-1)) // see if there is a \n after the \r and if so, skip it
               {
                  if (chars[i+1] == '\n')
                  {
                     i++; // bump past the \n only when it follows a \r
                     startNormal++;
                  }
               }
               break;
            case '\n' :
               if (startNormal != i)
                  writeWithBreaks(chars, startNormal, i - startNormal);

               m_bodyBuf.append("\r\n");
               m_afterLastBreak = 0;
               startNormal = i + 1;
               break;
            default:
               // do nothing...this char becomes part of the normal run
         }
         i++;
      }

      m_afterLastBreak = i - startNormal;
   }

   private final void writeWithBreaks(char[] chars, int off, int len)
   {
      while (len + m_afterLastBreak > MAX_LINE_LEN)
      {
         // we need to insert a break
         int write = MAX_LINE_LEN - m_afterLastBreak;
         m_bodyBuf.append(chars, off, write);
         m_bodyBuf.append("\r\n");
         len -= write;
         off += write;
         m_afterLastBreak = 0;
      }
      m_bodyBuf.append(chars, off, len);
      m_afterLastBreak += len;
   }

   public String getBodyText()
   {
      return m_bodyBuf.toString();
   }

   /**
    * Remove the appended message after the message has been sent.
    */
   public void resetBodyText()
   {
      m_bodyBuf.setLength(0);
      m_afterLastBreak = 0;
   }

   /**
    * Get the list of recipients for this message. This can be used by
    * the mail provider when sending the message to determine each user
    * names the message must be sent to.
    *
    * @return      the names to receive this message
    */
   public String[] getRecipients()
   {
      // go through the to, cc and bcc headers for this and create the
      int toSize = m_sendTo.size();
      int ccSize = m_copyTo.size();
      int bccSize = m_blindCopyTo.size();

      String[] recipients = new String[toSize + ccSize + bccSize];
      int onEntry = 0;   // which array entry are we on

      for (int i = 0; i < toSize; i++)
         recipients[onEntry++] = (String)m_sendTo.get(i);

      for (int i = 0; i < ccSize; i++)
         recipients[onEntry++] = (String)m_copyTo.get(i);

      for (int i = 0; i < bccSize; i++)
         recipients[onEntry++] = (String)m_blindCopyTo.get(i);

      return recipients;
   }

   /**
    * Write this message to the specified stream (writer). This uses
    * the message format defined in
    * <A HREF="http://www.cis.ohio-state.edu/htbin/rfc/rfc822.html">RFC 822</A>
    * for sending text messages.
    *
    * @param      writer               the stream (writer) to write to
    *
    * @exception   java.io.IOException   if an I/O error occurs
    */
   public void write(Writer writer)
      throws java.io.IOException
   {
      // 1. send the Date header
      writer.write("Date: ");
      writer.write(PSDateFormatter.formatDateForHttp(m_creationDate));
      writer.write("\r\n");

      // 2. send the From header
      writer.write("From: ");
      writer.write(m_from);
      writer.write("\r\n");

      // 3. send the Subject header
      if (m_subject != null) {
         writer.write("Subject: ");
         writer.write(m_subject);
         writer.write("\r\n");
      }

      // are we overriding the To header or sending the to/cc names?
      if (m_toHeaderForDisplay != null) {
         writer.write("To: ");
         writer.write(m_toHeaderForDisplay);
         writer.write("\r\n");
      }
      else {
         // now go back through the to, cc and bcc lists building the to
         // string
         boolean addedUsers = false;

         int size = (m_sendTo == null) ? 0 : m_sendTo.size();
         for (int i = 0; i < size; i++) {
            // if this is not the first recipient, add a comma/space
            if (addedUsers)
               writer.write(", ");
            else {
               addedUsers = true;
               writer.write("To: ");
            }

            writer.write((String)m_sendTo.get(i));
         }
         if (size > 0)
            writer.write("\r\n");
      
         size = (m_copyTo == null) ? 0 : m_copyTo.size();
         for (int i = 0; i < size; i++) {
            // if this is not the first recipient, add a comma/space
            if (addedUsers)
               writer.write(", ");
            else {
               addedUsers = true;
               writer.write("CC: ");
            }

            writer.write((String)m_copyTo.get(i));
         }
         if (size > 0)
            writer.write("\r\n");

         // bcc is never included in the displayed list, so skip them!
         // however, if no To or CC names exit, we must place a blank BCC line
         if (!addedUsers)
            writer.write("BCC:");
      }

      // set our MIME stuff
      sendMimeHeaders(writer);

      // now comes the body which is preceeded by a blank line
      if (m_bodyIn != null) {
         writer.write("\r\n");

         char[] buf = new char[2048];
         InputStreamReader rdr = new InputStreamReader(m_bodyIn, m_charEncoding);

         int n;
         while ( (n = rdr.read(buf, 0, buf.length)) != -1)
         {   // todo: this really needs to deal with 998 char line limit
            writer.write(buf, 0, n);
         }
      }
      else if (m_bodyBuf.length() > 0) {
         writer.write("\r\n");
         writer.write(m_bodyBuf.toString());
      }

      // we're done, send it all through now
      writer.flush();
   }

   /**
    * Sets the character encoding that will be used to send this
    * message.
    *
    * @author   chadloder
    * 
    * @version 1.7 1999/07/30
    * 
    * 
    * @param   enc
    * 
    */
   public void setCharEncoding(String enc)
   {
      m_charEncoding = PSCharSets.getJavaName(enc);
   }

   /**
    * Gets the character encoding that will be used to send this
    * message.
    *
    * @author   chadloder
    * 
    * @version 1.7 1999/07/30
    * 
    * 
    */
   public String getCharEncoding()
   {
      return m_charEncoding;
   }

   /**
    * Sends our standard MIME headers for content type, encoding, disposition,
    * and MIME version. The encoding is (and must be) the same encoding we
    * created the writer with.
    * <p>
    * CAUTION: When we support attachments, be sure to modify the Content-Disposition
    * header from inline to whatever else is appropriate.
    *
    * @author   chadloder
    * 
    * @version 1.8 1999/07/30
    * 
    * @param   out
    * 
    * @throws   java.io.IOException
    * 
    */
   private void sendMimeHeaders(Writer out)
      throws java.io.IOException
   {
      out.write("MIME-Version: 1.0\r\n");
      out.write("Content-Type: TEXT/PLAIN; charset="
         + PSCharSets.getStdName(m_charEncoding) + "\r\n");
      // out.write("Content-Disposition: inline\r\n");
   }

   private static final int MAX_LINE_LEN = 998;

   private int            m_afterLastBreak = 0;
   private String         m_from = "E2";                     // who's sending this?
   private String         m_toHeaderForDisplay = null;      // overrides the To header
   private ArrayList      m_sendTo = new ArrayList();      // names in To
   private ArrayList      m_copyTo = new ArrayList();      // names in cc
   private ArrayList      m_blindCopyTo = new ArrayList();   // names in bcc
   private Date         m_creationDate = new Date();      // the date of the message
   private String         m_subject = null;                  // subject of the message
   private String         m_charEncoding = PSCharSets.rxJavaEnc();

   // the body can be in one of two formats, a string or an input stream
   private StringBuffer   m_bodyBuf = new StringBuffer();
   private InputStream   m_bodyIn = null;
}

