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

package com.percussion.log;

import com.percussion.server.PSConsole;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSRandomAccessInputStream;
import com.percussion.util.PSRandomAccessOutputStream;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The PSFileLogWriter class implements logging to a file.
 * <p>
 * The log will be stored    on the E2    server's file    system. Log entries will
 * be    stored using an XML file format. The DTD for the log file    is as
 * follows:
 * <pre>
 *
 *       &lt;!DOCTYPE PSXLog [
 *          &lt;!ELEMENT PSXLog (PSXLogMessage*)&gt;
 *          &lt;!ELEMENT PSXLogMessage (time, applicationId, data*)&gt;
 *          &lt;!ATTLIST PSXLogMessage type CDATA #REQUIRED&gt;
 *          &lt;!ELEMENT time (#PCDATA)&gt;
 *       &lt;!--
 *           the time    this log message was reported. ISO 8601 format is    used
 *           (YYYYMMDDTHHMMSS). For instance,    19981231T000000 represents
 *           midnight, December 31, 1998.
 *        --&gt;
 *          &lt;!ELEMENT applicationId (#PCDATA)&gt;
 *          &lt;!ELEMENT data (#PCDATA)&gt;
 *          &lt;!ATTLIST data type CDATA #IMPLIED&gt;
 *       [&gt;
 *
 * </pre>
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSFileLogWriter implements IPSLogWriter
{
   /**
    *    Construct a file log writer. This is given package access with the
    *    intent that only the PSLogManager object will instantiate it.
    *
    * @param   file       the file to write the log entries to
    *
    * @exception   IOException if file contains an invalid path or the
    *    file cannot be opened for both reading and writing
    */
   PSFileLogWriter(RandomAccessFile file)
      throws IOException
   {
      super();
      com.percussion.server.PSConsole.printMsg(
         "LogWriter", "Opening log file.");
      try
      {
         m_file = file;
         m_fileIn = new PSRandomAccessInputStream(m_file);
         init();
         m_isOpen = true;
      }
      catch (IOException e)
      {
         close();
         throw e;
      }
      if (!m_file.getFD().valid())
         throw new IOException("File descriptor is not valid");
   }

   /**
    * Initializes a file log writer and sets up the writer in the right
    * position
    *
    * @throws IOException if file is empty
    */
   private synchronized void init() throws IOException
   {
      // if a zero-length file, then set it up

      if (m_file.length() == 0)
      {
          final String header = (BEGINLOGTAG + ENDFILETAG);
          final byte[] bytes = header.getBytes(PSCharSets.rxJavaEnc());
          m_file.write(bytes);
      }
      else
      {
         // We may need to convert "<PSXLog/>" into "<PSXLog></PSXLog>"

         // Go to the end of the log file, count back number of positions,
         // equal to the length of "/>"
         long pos = m_file.length()-3;
         seek(pos);

         // seek backwards for the last '/' and if it is found then add
         // ENDFILETAG (<PSXLog/>
         char c = (char)m_file.readByte();
         if(c=='/')
         {
             seek(pos);
             m_file.writeBytes(">" + ENDFILETAG);
         }
      }

      // seek backwards for the last '<', assume it's the start of the
      // log end tag </PSXLog>, and append from that position
      char c;
      long i = 0;
      for (i = m_file.length() - 1; i >= 0; i--)
      {
         seek(i);
         c = (char)m_file.readByte();
         if (c == '<')
            break;
      }
      if (i <= 0)
         throw new IOException("invalid log file format");
      else
         m_appendPos = i;

      seek(m_appendPos);
   }

   //   **************   IPSLogWriter Interface Implementation   **************

   /**
    *    Close the log writer. This should only be called when it is no longer
    *    needed. Any subsequent attempt to write through this object will throw
    *    an exception.
    */
   public synchronized void close()
   {
      m_isOpen = false;
      m_fileIn = null;
      m_file = null;
   }

   /**
    *    Write the log message.
    *
    * @param   msg the log message to be written
    * @exception   IllegalStateException if close has already been called
    *    on this reader
    */
   public synchronized void write(PSLogInformation msg)
      throws java.lang.IllegalStateException
   {
      // assert (m_appendPos >= 0)
      // assert (m_appendPos == m_file.getFilePointer());
      if (!isOpen())
         throw new IllegalStateException("cannot write to a closed log writer");

      String xmlString = "\n" + msg.toXMLString() + ENDFILETAG;
      try
      {
         if (m_file.length() == 0)
         {
            init(); // someone must have truncated the log file because it got corrupted
         }
         seek(m_appendPos);
         m_file.write(xmlString.getBytes(PSCharSets.rxJavaEnc()));
         m_file.getFD().sync();
      }
      catch (java.io.IOException e)
      {
         com.percussion.server.PSConsole.printMsg(
            "LogWriter", "Caught exception while writing to log file. ",
            new String[] { e.toString() });
         close();
      }
      m_appendPos += xmlString.length() - ENDFILETAG.length();
   }

   /**
    *    Use to query whether the writer is open or not.
    *
    * @return   true if the log writer is open, false if the log writer is not
    *    open.
    */
   public synchronized boolean isOpen()
   {
      if (m_isOpen)
      {
         try
         {
            if (m_file.getFD().valid())
               return true; // early out
         }
         catch (IOException e)
         {
            PSConsole.printMsg("Log", e);
            // fall through
         }
         com.percussion.server.PSConsole.printMsg(
            "LogWriter", "The log file has unexpectedly been closed.");
         close();
      }

      return m_isOpen;
   }

   /**
    * Attemps to reopen the writer if it is closed.
    * @return   true if the log writer was already open or if it was
    * succesfully reopend, false otherwise.
    */
   public synchronized boolean open()
   {
      return false;
   }

   /**
    * Remove all entries in the log created on or before the given date
    * and time.
    *
    * @param   allBefore    all entries with a time up to and including this
    * date will be truncated.
    */
   public synchronized void truncateLog(java.util.Date allBefore)
   {
      if (allBefore == null)
         return;      // do not truncate

      long startTruncate  = allBefore.getTime();

      com.percussion.server.PSConsole.printMsg(
         "FileLogWriter",
         "Truncating log entries prior to " +
         allBefore);

      com.percussion.util.PSDateFormatISO8601 fmt =
         new com.percussion.util.PSDateFormatISO8601();

      if (!isOpen())
         throw new IllegalStateException("cannot access a closed log writer.");

      try
      {
         seek(0);

         // build an up-to-date XML document from the logfile
         org.w3c.dom.Document xmlDoc;
         xmlDoc = PSXmlDocumentBuilder.createXmlDocument(m_fileIn, false);

         Element root = xmlDoc.getDocumentElement();
         PSXmlTreeWalker walker = new PSXmlTreeWalker(xmlDoc);

         final int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         final int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         // loop over all log message elements and remove those whose
         // time is within the truncation range
         Element msg = walker.getNextElement("PSXLogMessage", firstFlags);
         while (   msg != null )
         {
            String time = walker.getElementData("time", false);
            if (time == null)
               continue;   // skip this invalid entry
            time = time.trim();
            java.util.Date msgTime = fmt.parse(time, new java.text.ParsePosition(0));

            if (msgTime == null)
               continue;   // skip this invalid entry

            // fix for bug #Rx-99-10-0193 - get next before remove cur.
            // we are going to remove the msg element from the tree, so we
            // need to get its next element before removing it
            Element cur = msg; // save current for removal

            // get next element (could be null)
            msg = walker.getNextElement("PSXLogMessage", nextFlags);

            if (msgTime.getTime() <= startTruncate)
            {
               // need to truncate -- remove node & surrounding whitespace

               // now remove all text nodes between the current and the next
               // we can be sure that these are whitespace
               Node n = cur.getNextSibling();

               // loop: while the next sibling != next element
               // This also works in the case where msg is null
               while (n != null && n != msg)
               {
                  Node next = n.getNextSibling(); // get next before remove
                  if (n.getNodeType() == Node.TEXT_NODE)
                  {
                     root.removeChild(n);
                  }
                  n = next;
               }

               root.removeChild(cur);   // DOMException
            }

         }

         // now re-write the document
         m_file.setLength(0);
         seek(0);

         walker.write(new PSRandomAccessOutputStream(m_file));
         init(); // reset the append position to the appropriate spot
      }
      catch (java.io.IOException e1)
      {
         com.percussion.server.PSConsole.printMsg("PSFileLogWriter",
            "Caught IOException while truncating", new String[] { e1.toString() });
         close();
      }
      catch (org.xml.sax.SAXException saxErr)
      {
         String prefix = "rxlog";    // at least three characters
         String suffix = ".xml";
         try
         {
            java.io.File tempFile = java.io.File.createTempFile(prefix, suffix,
               new File(".").getCanonicalFile());

            com.percussion.server.PSConsole.printMsg("PSFileLogWriter",
               "Error parsing logfile. Will clear logfile after copying it to " + tempFile.toString(),
               new String[] { saxErr.toString() });

            seek(0);

            // copy from m_file to tempFile
            byte[] data = new byte[8192];

            java.io.FileOutputStream fOut= new java.io.FileOutputStream(tempFile);

            int total, curRead;
            for (total = 0; total < m_file.length(); total += curRead)
            {
               curRead = m_file.read(data);
               if (curRead == -1)
                  break;
               fOut.write(data, 0, curRead);
            }

            m_file.setLength(0);
            init();
            fOut.flush();
            fOut.close();
         }
         catch(java.io.IOException ioErr)
         {
            com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
               "Caught IOException", new String[] { ioErr.toString() });
            close();
         }
         catch(IllegalArgumentException illErr)
         {
            com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
               "Caught IllegalArgumentException", new String[] { illErr.toString() });
            close();
         }
         catch (SecurityException secuErr)
         {
            com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
               "Caught SecurityException", new String[] { secuErr.toString() });
            close();
         }
         catch (NullPointerException nulErr)
         {
            com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
               "Caught NullPointerException", new String[] { nulErr.toString() });
            close();
         }

         /*
        String errorText = "";
        if (saxErr instanceof org.xml.sax.SAXParseException)
        {
        org.xml.sax.SAXParseException se = (org.xml.sax.SAXParseException)saxErr;

        StringBuffer errorMsg = new StringBuffer();

        errorMsg.append(
        PSXmlContentParser.getSaxExceptionContextMessage(se));

        errorText = errorMsg.toString();
        }
        else if (saxErr.getMessage() != null)
        errorText = saxErr.getMessage();
        else
        errorText = saxErr.toString();

        com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
        "Caught SAXException", new String[] { errorText });
        close();
        */
      }
      catch (org.w3c.dom.DOMException e3)
      {
         com.percussion.server.PSConsole.printMsg("PSFileLogWriter, truncate method",
            "Caught DOMException", new String[] { e3.toString() });
         close();
      }
   }

   private void seek(long offset) throws IOException
   {
      m_file.seek(offset);
   }

   /**
    *    Member variable is true if and only if the writer is open.
    *
    * @see   #close
    * @see   #isOpen
    */
   private boolean m_isOpen = false;

   /**
    *    The file we write to. It remains open for reading and writing while the
    *    log file writer is open.
    */
   private RandomAccessFile m_file;
   private PSRandomAccessInputStream m_fileIn;

   /**
    *    The string that begins the XML log file
    *    Annex F of ISO 10646-1:1993 and § 2.4 of Unicode 2.0 recommend that UTF-16
    *    texts start with the no-op character U+FEFF ZERO WIDTH NO-BREAK SPACE as
    *    byte-order mark (BOM) to recognize byte-swapped UTF-16 text from haphazard
    *    programs on little-endian Intel or DEC machines from its =FF=FE signature
    *    (U+FFFE , or \uFEFF is guaranteed to be no Unicode character).
    */
   private static final String BEGINLOGTAG   = "<PSXLog>\n";

   /**
    *    The XML DTD (document type definition) for the logfile
    */
   private static final String LOGFILEDTD = "<?xml version=\"1.0\" encoding=\""
      + PSCharSets.rxStdEnc() + "\"?>\n" +
      "<!DOCTYPE PSXLog [\n" +
      "\t<!ELEMENT PSXLog (PSXLogMessage*)>\n" +
      "\t<!ELEMENT PSXLogMessage (time, applicationId, data*)>\n" +
      "\t<!ATTLIST PSXLogMessage type CDATA #REQUIRED>\n" +
      "\t<!ELEMENT time (#PCDATA)>\n" +
      "\t<!ELEMENT applicationId (#PCDATA)>\n" +
      "\t<!ELEMENT data (#PCDATA)>\n" +
      "\t<!ATTLIST data type CDATA #IMPLIED>\n" +
      "]>";

   /**
    *    The string that ends the XML log file
    */
   private static final String ENDFILETAG    = "</PSXLog>";

   /**
    *    We keep track of the append position and update it each time we
    *    write a message.
    */
   private long m_appendPos = 0;

   /**
    * Total minutes in a single day.
    */
   private static final int ms_minutesInOneDay      = 1440;     // 24 * 60

   private static final long MILLIS_IN_MINUTE      = 60000L;   // 1000 * 60
}
