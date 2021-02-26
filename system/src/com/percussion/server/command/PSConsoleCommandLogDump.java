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

package com.percussion.server.command;

import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.mail.PSMailMessage;
import com.percussion.mail.PSMailProvider;
import com.percussion.mail.PSMailSendException;
import com.percussion.mail.PSSmtpMailProvider;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Properties;


/**
 * The PSConsoleCommandLogDump class implements processing of the
 * "log dump" console command.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSConsoleCommandLogDump extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param      cmdArgs      the argument string to use when executing
    *                           this command
    *
    */
   public PSConsoleCommandLogDump(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);

      java.util.Date since = null;
      java.util.Date until = null;
      java.util.List queryTypes = new java.util.ArrayList();
      java.util.List applications = new java.util.ArrayList();

      /* this may contain the following tokens (they're all optional):
       *
       * o  since 'YYYYMMDD HH:MM:SS'
       *   o  until 'YYYYMMDD HH:MM:SS'
       * o  type [1 = errors, ...]
       * o  server
       * o  application [name]
       * o  mailto [e-mail address]
       */
      if ((cmdArgs != null) && (cmdArgs.length() > 0)) {
         int tokStart = 0;
         int tokEnd = cmdArgs.indexOf(' ');
         while (tokEnd != -1) {
            // first get the token which is the command name
            String tokCmd = cmdArgs.substring(tokStart, tokEnd).toLowerCase();

            // skip past any white space to get to the command value
            for (; tokEnd < cmdArgs.length(); tokEnd++) {
               if (!Character.isWhitespace(cmdArgs.charAt(tokEnd)))
                  break;
            }

            // only the server token requires no args
            String tokValue = "";
            if (!"server".equals(tokCmd)) {
               // if the command value is quoted, search for the end quote
               char findTok = cmdArgs.charAt(tokEnd);
               if ((findTok != '\'') && (findTok != '"'))
                  findTok = ' ';
               else if (tokEnd < cmdArgs.length())
                  tokEnd++;

               tokStart = tokEnd;
               tokEnd = cmdArgs.indexOf(findTok, tokStart);
               if (tokEnd == -1) {
                  // is this the last token in the string?
                  if (findTok != ' ') {
                     Object[] args = { ms_cmdName, cmdArgs };
                     throw new PSIllegalArgumentException(
                        IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
                  }
                  else
                     tokEnd = cmdArgs.length();
               }

               // now get the command value
               if (tokEnd == cmdArgs.length())
                  tokValue = cmdArgs.substring(tokStart);
               else
                  tokValue = cmdArgs.substring(tokStart, tokEnd);

               // skip past the quote or double quote char, if that's
               // what we searched on
               if ((findTok != ' ') && (tokEnd < cmdArgs.length()))
                  tokEnd++;
                  
               // skip past any white space to get to the command value
               for (; tokEnd < cmdArgs.length(); tokEnd++) {
                  if (!Character.isWhitespace(cmdArgs.charAt(tokEnd)))
                     break;
               }
            }

            // now let's see what we've got
            if ("since".equals(tokCmd)) {
               since = parseDate(tokCmd, tokValue);
            }
            else if ("until".equals(tokCmd)) {
               until = parseDate(tokCmd, tokValue);
            }
            else if ("type".equals(tokCmd)) {
               try {
                  queryTypes.add(new Integer(tokValue));
               } catch (NumberFormatException e) {
                  Object[] args = { ms_cmdName, tokCmd + " " + tokValue };
                  throw new PSIllegalArgumentException(
                     IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
               }
            }
            else if ("server".equals(tokCmd)) {
               applications.add(new Integer(0));
            }
            else if ("application".equals(tokCmd)) {
               // locate the application id for the given app name
               
               // requiring id now
               applications.add(new Integer(tokValue));
            }
            else if ("mailto".equals(tokCmd)) {
               m_recipients.add(tokValue);
            }
            else {
               Object[] args = { ms_cmdName, tokCmd };
               throw new PSIllegalArgumentException(
                  IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
            }

            if (tokEnd == cmdArgs.length())
               tokEnd = -1;
            else {
               tokStart = tokEnd;
               tokEnd = cmdArgs.indexOf(' ', tokStart);
               if (tokEnd == -1)
                  tokEnd = cmdArgs.length();
            }
         }
      }

      if (m_recipients.size() > 0)
      {
         m_filter = createMailFilter(
            since, until, queryTypes, applications);
      }
      else
      {
         m_filter = new PSXmlLogDumpFilter(
            since, until, queryTypes, applications);
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText)&gt;
    *
    *      &lt;--
    *         the command that was executed
    *      --&gt;
    *      &lt;ELEMENT command                     (#PCDATA)&gt;
    *
    *      &lt;--
    *         the result code for the command execution
    *      --&gt;
    *      &lt;ELEMENT resultCode                  (#PCDATA)&gt;
    *
    *      &lt;--
    *         the message text associated with the result code
    *      --&gt;
    *      &lt;ELEMENT resultText                  (#PCDATA)&gt;
    * </CODE></PRE>
    *   
    * @param      request                     the requestor object
    *
    * @return                                 the result document
    *
    * @exception   PSConsoleCommandException   if an error occurs during
    *                                          execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      Document logDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         logDoc, "PSXConsoleCommandResults");

      try
      {
         // prepare for log reading
         m_filter.init(logDoc, root);

         /* get the log based upon the requested info */
         com.percussion.log.PSLogManager.read(m_filter);

         // done with log reading
         m_filter.term();

         PSXmlDocumentBuilder.addElement(logDoc, root, "command", ms_cmdName);
         PSXmlDocumentBuilder.addElement(logDoc, root, "resultCode", "0");
         PSXmlDocumentBuilder.addElement(logDoc, root, "resultText", "");
      } catch (OutOfMemoryError e) {
         Object[] args = { "command", ms_cmdName, "size too large" };
         throw new PSConsoleCommandException(IPSServerErrors.LOG_SIZE_TOO_BIG, args);
      }

      return logDoc;
   }


   private PSLogDumpFilter createMailFilter(
      java.util.Date since,
      java.util.Date until,
      java.util.List queryTypes,
      java.util.List applications)
      throws PSIllegalArgumentException
   {
      PSServerConfiguration conf = PSServer.getServerConfiguration();
      if (conf == null)
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_SRVCONFIG_REQD_FOR_MAILTO);
      }

      PSNotifier notif = conf.getNotifier();
      if (notif == null)
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_NOTIFIER_REQD_FOR_MAILTO);
      }

      String host = notif.getServer();
      if ((host == null) || (host.length() == 0))
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_NOTIFIER_HOST_REQD_FOR_MAILTO);
      }

      String from = notif.getFrom();
      if ((from == null) || (from.length() == 0))
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_NOTIFIER_FROM_REQD_FOR_MAILTO);
      }

      Properties props = new Properties();
      props.setProperty(
         PSSmtpMailProvider.PROPERTY_HOST, host);
      PSMailProvider prov = new PSSmtpMailProvider(props);
      
      PSMailMessage msg = new PSMailMessage();
      msg.setFrom(from);
      msg.setSubject(ms_cmdName);

      int size = m_recipients.size();
      for (int i = 0; i < size; i++)
      {
         msg.addSendTo(m_recipients.get(i).toString());
      }

      try {
         return new PSMailLogDumpFilter(
            since, until, queryTypes, applications, prov, msg);
      }
      catch (IOException e)
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_EXEC_EXCEPTION,
            new Object[] { ms_cmdName,   e.toString() } );            
      }
   }

   private static java.util.Date parseDate(
      String tokenName, String dateToParse)
      throws PSIllegalArgumentException
   {
      try {
         return FastDateFormat.getInstance("yyyyMMdd HH:mm:ss").parse(dateToParse);
      } catch (Exception e) {
         Object[] args = { ms_cmdName, tokenName + " '" + dateToParse + "'" };
         throw new PSIllegalArgumentException(
                  IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
      }
   }

   private static String formatDate(java.util.Date date)
   {
      return FastDateFormat.getInstance("yyyyMMdd HH:mm:ss").format(date);
   }

   /** the people we email */
   private java.util.List      m_recipients   = new java.util.ArrayList();

   /** the filter which places the messages into the message/xml doc */
   private PSLogDumpFilter      m_filter         = null;


   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "log dump";


   abstract class PSLogDumpFilter
      implements com.percussion.log.IPSLogReaderFilter
   {
      protected PSLogDumpFilter(
         java.util.Date since,
         java.util.Date until,
         java.util.List queryTypes,
         java.util.List applications)
      {
         m_since = since;
         m_until = until;

         int size = queryTypes.size();
         if (size == 0)
            m_queryTypes = null;
         else {
            m_queryTypes = new int[size];
            for (int i = 0; i < size; i++)
               m_queryTypes[i] = ((Integer)queryTypes.get(i)).intValue();
         }

         size = applications.size();
         if (size == 0)
            m_appIds = null;
         else {
            m_appIds = new int[size];
            for (int i = 0; i < size; i++)
               m_appIds[i] = ((Integer)applications.get(i)).intValue();
         }
      }

      public int[] getApplicationIds()
      {
         return m_appIds;
      }

      public java.util.Date getStartTime()
      {
         return m_since;
      }

      public java.util.Date getEndTime()
      {
         return m_until;
      }

      public java.util.Date getNextStartTime()
      {
         // not really correct, but we don't need it
         return m_until;
      }

      public int[] getEntryTypes()
      {
         return m_queryTypes;
      }

      public abstract void processMessage(
         com.percussion.log.PSLogEntry msg,
         boolean filterWasApplied);

      abstract void init(Document doc, Element root)
         throws PSConsoleCommandException;

      abstract void term()
         throws PSConsoleCommandException;


      protected java.util.Date      m_since         = null;
      protected java.util.Date      m_until         = null;
      protected int[]               m_queryTypes   = null;
      protected int[]               m_appIds         = null;
      protected Document             m_doc;
      protected Element               m_root;
   } // end private inner class PSLogDumpFilter


   class PSMailLogDumpFilter extends PSLogDumpFilter
   {
      PSMailLogDumpFilter(java.util.Date since,
         java.util.Date until,
         java.util.List queryTypes,
         java.util.List applications,
         PSMailProvider prov,
         PSMailMessage msg)
         throws java.io.IOException
      {
         super(since, until, queryTypes, applications);

         m_msgFile = new com.percussion.util.PSPurgableTempFile(
            "psx", ".msg", null);
         m_msgStream = 
            new java.io.OutputStreamWriter(
            new java.io.FileOutputStream( m_msgFile ) );

         m_provider = prov;
         m_msg = msg;
      }

      public void processMessage(com.percussion.log.PSLogEntry msg,
                                 boolean filterWasApplied)
      {
         if (msg == null)   // signifies they're done processing
            return;

         // if (!filterWasApplied) TODO: enable this
         {
            try {
               m_msgStream.write(msg.toXMLString());
            } catch (java.io.IOException e) {
               // this can only happen if an InputStream was previously
               // used, since we're not this cannot happen
            }
         }
      }

      void init(Document doc, Element root)
      {
         m_doc = doc;
         m_root = root;

         try {
            m_msgStream.write("<PSXLog");

            if (m_since != null)
            {
               m_msgStream.write(" since=\"");
               m_msgStream.write(formatDate(m_since));
               m_msgStream.write("\"");
            }

            if (m_until != null)
            {
               m_msgStream.write(" until=\"");
               m_msgStream.write(formatDate(m_until));
               m_msgStream.write("\"");
            }

            int size = (m_queryTypes == null) ? 0 : m_queryTypes.length;
            if (size != 0)
            {
               m_msgStream.write(" types=\"");
               for (int i = 0; i < size; i++)
               {
                  if (i > 0)
                     m_msgStream.write(",");
                  m_msgStream.write(String.valueOf(m_queryTypes[i]));
               }
               m_msgStream.write("\"");
            }

            size = (m_appIds == null) ? 0 : m_appIds.length;
            if (size != 0)
            {
               m_msgStream.write(" appids=\"");
               for (int i = 0; i < size; i++)
               {
                  if (i > 0)
                     m_msgStream.write(",");
                  m_msgStream.write(String.valueOf(m_appIds[i]));
               }
               m_msgStream.write("\"");
            }

            m_msgStream.write(">\r\n");
         } catch (java.io.IOException e) {
            // this can only happen if an InputStream was previously
            // used, since we're not this cannot happen
         }
      }

      void term()
         throws PSConsoleCommandException
      {
         java.io.InputStream fIn = null;

         try {
            // close the XML text stream
            m_msgStream.write("</PSXLog>");
            m_msgStream.close();
            m_msgStream = null;

            fIn = new java.io.FileInputStream(m_msgFile);
            m_msg.setBody(fIn);
            m_provider.send(m_msg);
         }
         catch (IOException e)
         {
            throw new PSConsoleCommandException(
               IPSServerErrors.RCONSOLE_EXEC_EXCEPTION,
               new Object[] { ms_cmdName,   e.toString() } );            
         }
         catch (PSMailSendException e)
         {
            throw new PSConsoleCommandException(
               IPSServerErrors.RCONSOLE_EXEC_EXCEPTION,
               new Object[] { ms_cmdName,   e.toString() } );
         }
         finally
         {
            if (m_msgStream != null)
            {
               try { m_msgStream.close(); } catch (Exception e) { }
            }

            if (fIn != null)
            {
               try { fIn.close(); } catch (Exception e) { }
            }

            // we're done with the file, delete it!
            m_msgFile.delete();
         }
      }

      private PSMailProvider m_provider;
      private PSMailMessage m_msg;
      private java.io.File m_msgFile;
      private java.io.Writer m_msgStream;
   } // end private inner class PSMailLogDumpFilter

   class PSXmlLogDumpFilter extends PSLogDumpFilter
   {
      PSXmlLogDumpFilter(java.util.Date since,
         java.util.Date until,
         java.util.List queryTypes,
         java.util.List applications)
      {
         super(since, until, queryTypes, applications);
      }

      public void processMessage(com.percussion.log.PSLogEntry msg,
                                 boolean filterWasApplied)
      {
         if (msg == null)   // signifies they're done processing
            return;

         // if (!filterWasApplied) TODO: enable this
         {
            m_root.appendChild(msg.toXml(m_doc));
         }
      }

      void init(Document doc, Element root)
      {
         m_doc = doc;
         m_root = PSXmlDocumentBuilder.addEmptyElement(m_doc, root, "PSXLog");
      }

      void term()
      {   // nothing to do here, we've already written to the doc
      }

      private PSMailProvider m_provider;
      private PSMailMessage m_msg;
   } // end private inner class PSMailLogDumpFilter
}
