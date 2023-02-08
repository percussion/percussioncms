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
package com.percussion.log;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * PSFileLogReaderWriter pair is both a log reader and a log writer based
 * on the same disk file. All of the operations are synchronized.
 */
public class PSFileLogReaderWriterPair implements IPSLogReader, IPSLogWriter
{

   public PSFileLogReaderWriterPair(File file)
      throws IOException, SAXException
   {
      m_file = file;
      m_logFile = new RandomAccessFile(m_file, "rw");
      m_writer = new PSFileLogWriter(m_logFile);
      m_reader = new PSFileLogReader(m_logFile);
      m_changed = false;
   }


   /**
    * Close both the log reader and writer.
    * This should only be called when they are no longer needed. Any
    * subsequent attempts to read or write from this object will throw
    * an exception. It is not an error to call close() on a closed log reader.
    *
    *    @see #isOpen
    */
   public synchronized void close()
   {
      m_writer.close();
      m_reader.close();

      try {
         m_logFile.close();
      } catch (IOException e) {
         com.percussion.server.PSConsole.printMsg(
            "LogReaderWriter", "Caught exception while closing the log file. ",
            new String[] { e.toString() });
      }
   }

   /**
    *    Use to query whether the log reader is open or not.
    *
    *    @return Returns true if the log reader is open, false if the log
    *    reader is not open.
    *
    *    @see #close
    */
   public synchronized boolean isOpen()
   {
      return (m_writer.isOpen() && m_reader.isOpen());
   }
   
   /**
    * Read log messages   using   the specified   filter.
    *
    * @param filter the log message filter
    *
    * @exception IllegalStateException if close has already been called
    * on this reader
    */
   public synchronized void read(IPSLogReaderFilter filter)
      throws java.lang.IllegalStateException
   {
      m_reader.read(filter, m_changed);
      m_changed = false;
   }

   /**
    * Write the log message.
    *
    * @param      msg                the log message to be written
    * @exception  IllegalStateException   if close has already been called
    *                              on this writer
    */
   public synchronized void write(PSLogInformation msg)
      throws java.lang.IllegalStateException
   {
      m_writer.write(msg);
      m_changed = true;
   }

   /**
    * Attemps to reopen the writer if it is closed.
    * @return     true if the log writer was already open or if it was
    * succesfully reopend, false otherwise.
    */
   public synchronized boolean open()
   {
      return m_writer.open();
   }

   /**
    * Remove all entries in the log created on or before the given date
    * and time.
    *
    * @param   allBefore   all entries with a time up to and including this
    * date will be truncated.
    */
   public void truncateLog(java.util.Date allBefore)
   {
      m_writer.truncateLog(allBefore);
   }

   private boolean m_changed = true;
   private PSFileLogReader m_reader;
   private PSFileLogWriter m_writer;
   private File m_file;
   private RandomAccessFile m_logFile;
}
