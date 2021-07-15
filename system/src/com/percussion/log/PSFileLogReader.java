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

import com.percussion.util.PSDateFormatISO8601;
import com.percussion.util.PSRandomAccessInputStream;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParsePosition;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * The PSFileLogReader class implements reading log entries from
 * a file.
 *
 * @see        PSFileLogWriter
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSFileLogReader implements IPSLogReader
{
   /**
    * Construct a file log reader. This is given package access with the
    * intent that only the PSLogManager object will instantiate it.
    *
    * @param      file        the file to read the log entries from
    *
    * @exception  IOException if file contains an invalid path or the
    *                         file cannot be opened for reading
    *
    * @exception   SAXException   if the log file is invalid
    */
   PSFileLogReader(RandomAccessFile file)
      throws IOException, SAXException
   {
      super();
      com.percussion.server.PSConsole.printMsg(
         "LogReader", "Opening log file.");

      m_file = file;
      m_file.seek(0);
      m_fileIn = new PSRandomAccessInputStream(m_file);
      m_xmlDoc = PSXmlDocumentBuilder.createXmlDocument(m_fileIn, false);
      m_isOpen = true;
   }

   /* **************  IPSLogReader Interface Implementation ************** */
      
   /**
    * Close the log reader. This should only be called when it is no longer
    * needed. Any subsequent use of this object will throw an exception. It
    * is not an error to call close() on a closed log reader.
    */
   public synchronized void close()
   {
      m_isOpen = false;
      m_file = null;
      m_fileIn = null;
      m_xmlDoc = null;
   }

   /**
    *   Use to query whether the reader is open or not.
    *
    *   @return true if the log reader is open, false if the log reader is not
    *   open.
    */
   public synchronized boolean isOpen()
   {
      return m_isOpen;
   }

   /**
    * Read log messages using the specified filter.
    *
    * @param      filter                  the log message filter
    *
    * @exception  IllegalStateException   if close has already been called
    *                                     on this reader
    */
   public synchronized void read(IPSLogReaderFilter filter)
   {
      read(filter, true);
   }

   /**
    * Read log messages using the specified filter.
    *
    * @param      filter                  the log message filter
    *
    * @exception  IllegalStateException   if close has already been called
    *                                     on this reader
    */
   synchronized void read(IPSLogReaderFilter filter, boolean reparse)
      throws java.lang.IllegalStateException
   {
      if (!isOpen())
         throw new IllegalStateException("cannot read from a closed log reader.");

      if (reparse)
      {
         try
         {
            m_file.seek(0);
            // build an up-to-date XML document from the logfile
            m_xmlDoc = PSXmlDocumentBuilder.createXmlDocument(m_fileIn, false);
         }
         catch (Exception e)
         {
            com.percussion.server.PSConsole.printMsg("LogReader",
               "Could not read log file", new String[] { e.toString() });
         }
      }

      // get the criteria from the filter
      PSDateFormatISO8601 fmt = new PSDateFormatISO8601();
      java.util.Date startTime = filter.getStartTime();
      java.util.Date endTime = filter.getEndTime();
      int[] appIds = filter.getApplicationIds();
      // iterate through the file and process the messages which match
      PSXmlTreeWalker walker = new PSXmlTreeWalker(m_xmlDoc);
      
      // this is allocated only once but cleared after each use
      java.util.ArrayList subMessages = new java.util.ArrayList();

      final int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      final int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for(Element msg = walker.getNextElement("PSXLogMessage", firstFlags);
         msg != null;
         msg = walker.getNextElement("PSXLogMessage", nextFlags))
      {
         String msgTypeString = walker.getElementData("@type", false);
         int msgType;
         try
         {
            msgType = Integer.parseInt(msgTypeString, 10);
         }
         catch (NumberFormatException e)
         {
            continue;   // skip this invalid entry
         }

         boolean appIdMatches = false;

         String time = walker.getElementData("time", false);
         if (time == null)
            continue;   // skip this invalid entry
         time = time.trim();
         java.util.Date msgTime = fmt.parse(time, new ParsePosition(0));
         if (msgTime == null)
            continue;   // skip this invalid entry

         if ((startTime == null || msgTime.compareTo(startTime) >= 0)
            && (endTime == null || msgTime.compareTo(endTime) <= 0))
         {
            // get the applicationId element data and parse it
            String appIdString = walker.getElementData("applicationId", false);
            if (appIdString == null)
               continue;   // skip this invalid entry
            int appId;
            try
            {
               appId = Integer.parseInt(appIdString, 10);
            }
            catch (NumberFormatException e)
            {
               continue;   // skip this invalid entry
            }

            if (appIds == null)
            {
               // the filter says "match any app id"
               appIdMatches = true;
            }
            else if (appIdString != null)
            {
               // see if the filter allows this app id
               for (int i = 0; !appIdMatches && i < appIds.length; i++)
               {
                  if (appIds[i] == appId)
                  {
                     appIdMatches = true;
                  }
               }
            }
            // finally, if this message matches, then get all its
            // submessages and build a PSLogEntry to pass through
            // the filter
            if (appIdMatches)
            {
               Node logMsgNode = walker.getCurrent();

               for (Element subMsg = walker.getNextElement("data", firstFlags);
                  subMsg != null;
                  subMsg = walker.getNextElement("data", nextFlags))
               {
                  String subMsgTypeString = subMsg.getAttribute("type");
                  if (subMsgTypeString == null)
                     continue;
                  int subMsgType;
                  try
                  {
                     subMsgType = Integer.parseInt(subMsgTypeString, 10);
                  }
                  catch (NumberFormatException e)
                  {
                     continue;   // skip this invalid entry
                  }

                  subMessages.add(new PSLogSubMessage(
                     subMsgType,
                     walker.getElementData((Element)walker.getCurrent())
                     ));
               }
               PSLogSubMessage[] subMsgArray
                  = new PSLogSubMessage[subMessages.size()];
               subMessages.toArray(subMsgArray);
               PSLogEntry logInfo = new PSLogEntry(msgType, appId,
                  msgTime, subMsgArray);
               subMessages.clear();

               // let the filter process this now
               filter.processMessage(logInfo, true);

               walker.setCurrent(logMsgNode);
            }
         }
      }
   }

   /**
    * Make sure we close everything when we get garbage collected
    */
   protected void finalize() throws Throwable
   {
      close();
      super.finalize();
   }

   /**
    *    Member variable is true if and only if the reader is open.
    *
    *    @see #close
    *    @see #isOpen
    */
   private boolean m_isOpen = false;

   /**
    *    The file input stream of the logfile
    */
   private RandomAccessFile m_file;
   private PSRandomAccessInputStream m_fileIn;

   /**
    *    The XML document corresponding to this log reader
    */
   private Document m_xmlDoc;
}

