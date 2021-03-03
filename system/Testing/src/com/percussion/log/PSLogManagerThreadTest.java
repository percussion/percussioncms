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

package com.percussion.log;

import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.testing.PSRequestHandlerTestSuite;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class PSLogManagerThreadTest extends PSConfigHelperTestCase
   implements IPSServerBasedJunitTest
{
   /*
    * This is the necessary request handler to run tests using
    * this class
      <RequestHandlerDef handlerName="JUnitTestHandler"
        className="com.percussion.testing.PSJunitRequestHandler" >
        <RequestRoots>
           <RequestRoot baseName="sys_junitTestHandler">
              <RequestType>POST</RequestType>
              <RequestType>GET</RequestType>
           </RequestRoot>
        </RequestRoots>
      </RequestHandlerDef>
   */

   public PSLogManagerThreadTest(String name)
   {
      super(name);
   }


   /**
    * The loadable handler will call this method once before any test method.
    *
    * @param req The request that was passed to the loadable handler.
    *            Never <code>null</code>;
    */
   @Override
   public void oneTimeSetUp(Object req) {

   }

   /* (non-Javadoc)
    * @see com.percussion.testing.IPSServerBasedJunitTest#oneTimeTearDown()
    */
   public void oneTimeTearDown() {
      // TODO Auto-generated method stub

   }

   /**
    * A private class for use in hammer() -- is a thread that periodically
    * sleeps for a random millisecond interval between 0 and waitLimit,
    * then writes a message to the log manager and then sleeps again, and
    * so on...until it gets interrupted.
    */
   private class LogTestingThread extends Thread {
      public LogTestingThread(long runForMs, long waitLimitMs, ThreadGroup group, String name)
      {
         super(group, name);
         m_waitLimitMs = waitLimitMs;
         m_runForMs = runForMs;
         m_rand = new SecureRandom();
      }

      public void run() {
         flushPrint("*** \tInitializing new log testing thread.");
         long startTime = System.currentTimeMillis();
         long endTime = startTime + m_runForMs;
         try
         {
            while (System.currentTimeMillis() < endTime)
            {
               long sleepFor = m_rand.nextLong() % m_waitLimitMs;
               if (sleepFor < 0)
                  sleepFor = -sleepFor;

               // flushPrint("*** \tTestThread " + Thread.currentThread().toString()
               //    + " sleeping for " + sleepFor + "ms");

               Thread.sleep(sleepFor);

               try
               {
                  PSLogManager.write(
                     new PSLogApplicationStart(0, m_messageData));
                     flushPrint("*** \tTestThread " + Thread.currentThread().toString()
                        + " queued one message.");
               }
               catch (IllegalStateException e)
               {
                  // if someone closed the log manager on us, then this
                  // is an expected response, but if that is not the case,
                  // then this is not a good error
                  if (PSLogManager.isOpen())
                     throw e;

                  flushPrint("*** \tTestThread " +
                     Thread.currentThread().toString() +
                     " aborted write because the log manager has closed.");
               }
               m_numSuccessfullyWritten++;
            }
         }
         catch (InterruptedException e) {
            flushPrint("*** \tLog testing thread cleaning up.");
         }
      }
      private long m_waitLimitMs;
      private long m_runForMs;
      SecureRandom m_rand;
   }


   /**
    * Hammers an open log manager from multiple threads
    *
    */
   private long hammer(int numThreads, int msecHammerFor) throws Exception
   {
      m_numSuccessfullyWritten = 0;
      java.util.Vector loggers = new java.util.Vector();
      ThreadGroup threadGroup = new ThreadGroup(
         "Hammer Threads");
      try
      {
         for (int i = 0; i < numThreads; i++)
         {
            loggers.add(new LogTestingThread(msecHammerFor, msecHammerFor/10, threadGroup, "" + i));
            ((LogTestingThread)loggers.elementAt(i)).start();
         }

         for (int i = 0; i < numThreads; i++)
         {
            ((Thread)loggers.elementAt(i)).join();
         }

         // Thread.sleep(msecHammerFor); // replaced by join
      }
      finally
      {
         threadGroup.interrupt();
      }
      return m_numSuccessfullyWritten;
   }

   /**
    * Initialize with valid file properties and then spawn a bunch
    * of threads that all write to the log manager at random
    * intervals.
    */
   public void testInitWithFilePropsAndHammer() throws Exception
   {
      flushPrint("testInitWithFilePropsAndHammer");
      try
      {
         PSLogManager.init(m_validFileProps);
         hammer(30, 120000);
      }
      finally
      {
         PSLogManager.close();
      }
   }


   /**
    * Initialize with valid DBMS properties and then spawn a bunch
    * of threads that all write to the log manager at random
    * intervals.
    */
   public void testInitWithDBMSPropsAndHammer() throws Exception
   {
      flushPrint("testInitWithDBMSPropsAndHammer");
      try
      {
         PSLogManager.init(m_validDBMSProps);
         hammer(30, 60000);
      }
      finally
      {
         PSLogManager.close();
      }
   }

   private class DataTestFilter extends PSOutputLogReaderFilter
   {
      public DataTestFilter(PSLogManagerThreadTest parent,
         long numEntriesToExpect, java.io.OutputStream out, int outputFormat)
      {
         this(parent, numEntriesToExpect, out, outputFormat, null, null, null);
      }

      public DataTestFilter(PSLogManagerThreadTest parent,
         long numEntriesToExpect,
         java.io.OutputStream out, int outputFormat,
         java.util.Date startTime,
         java.util.Date endTime,
         int[] applicationIds)
      {
         super(out, outputFormat, startTime, endTime, applicationIds);
         m_numEntriesToExpect = numEntriesToExpect;
         m_testCase = parent;
      }


      public void processMessage(PSLogEntry msg, boolean filterWasApplied)
      {
         if (msg == null)
         {
            assertEquals(m_numEntriesToExpect, m_logEntryNum);
            return;
         }

         flushPrint("*** \tTesting log entry #" + m_logEntryNum++);
         flushPrint(msg.toString());
         PSLogSubMessage[] subs = msg.getSubMessages();
         for (int i = 0; i < subs.length; i++) {
            m_testCase.assertData(m_logEntryNum, m_messageData, subs[i].getText());
         }
      }

      private long m_numEntriesToExpect = -1;
      private long m_logEntryNum = 0;
      private PSLogManagerThreadTest m_testCase;
   }

   public void assertData(long logNum, String expected, String actual)
   {
       assertEquals("Log entry #" + logNum, expected, actual);
   }

   /**
    * Initialize with valid  properties and then hammer.
    * Then use the log reader to read messages and make sure we only get
    * the ones back that we wrote, and that the text is equal to what we
    * put in.
    * Test that we get back *every* message we successfully wrote in
    * the test period (by having hammer() return the number of succesfully
    * written messages and then putting a test in
    * DataTestFilter.processMessage() that compares logEntryNum with the
    * return value from hammer() when it gets a null (final) entry.
    */
   public void testInitWithPropsAndHammerThenRead() throws Exception
   {
      flushPrint("testInitWithPropsAndHammerThenRead");
      try
      {
         PSLogManager.init(m_validDBMSProps);
         PSLogManager.write(new PSLogApplicationStart(0, "Excluded before"));
         Thread.sleep(1000);
         java.util.Date startTime = new java.util.Date();
         long numWritten = hammer(30, 30000);
         java.util.Date endTime = new java.util.Date();
         Thread.sleep(1000);
         PSLogManager.write(new PSLogApplicationStart(0, "Excluded after"));
         PSLogManager.read(new DataTestFilter(
            this, numWritten, System.err, PSOutputLogReaderFilter.OUTPUT_RAW, startTime, endTime, null)
         );
      }
      catch (Exception e)
      {
         flushPrint("*** \tUnit Test: Caught exception " + e);
         throw e;
      }
      finally
      {
         PSLogManager.close();
      }
   }

   /**
    * Initialize with valid file properties and then hammer.
    * Then use the log reader to read messages and make sure we only get
    * the ones back that we wrote, and that the text is equal to what we
    * put in.
    * Test that we get back *every* message we successfully wrote in
    * the test period (by having hammer() return the number of succesfully
    * written messages and then putting a test in
    * DataTestFilter.processMessage() that compares logEntryNum with the
    * return value from hammer() when it gets a null (final) entry.
    */
   public void testInitWithFilePropsAndHammerThenRead() throws Exception
   {
      flushPrint("testInitWithFilePropsAndHammerThenRead");
      try
      {
         PSLogManager.init(m_validFileProps);
         PSLogManager.write(new PSLogApplicationStart(0, "Excluded before"));
         Thread.sleep(1000);
         java.util.Date startTime = new java.util.Date();
         long numWritten = hammer(30, 30000);
         java.util.Date endTime = new java.util.Date();
         Thread.sleep(1000);
         PSLogManager.write(new PSLogApplicationStart(0, "Excluded after"));
         PSLogManager.read(new DataTestFilter(
            this, numWritten, System.err, PSOutputLogReaderFilter.OUTPUT_RAW, startTime, endTime, null)
         );
      }
      catch (Exception e)
      {
         flushPrint("*** \tUnit Test: Caught exception " + e);
         e.printStackTrace();
         throw e;
      }
      finally
      {
         PSLogManager.close();
      }
   }

   /**
    * Set up the test case variables
    * @throws Exception
    */
   public void setUp() throws Exception
   {
      try {
         m_validDBMSProps  = getConnectionProps(CONN_TYPE_SQL);
      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
      m_validDBMSProps.setProperty("logTo", "DBMS");

      m_validFileProps  = new Properties(ms_defaultFileProps);
   }


   private static void flushPrint(String msg)
   {
      System.err.println(msg);
      System.err.flush();
   }

   private Properties m_validFileProps = null;
   private Properties m_validDBMSProps = null;

   private final String m_messageData =
      "To restore order and repose to an empire so great and so " +
      "distracted as ours is, merely in the attempt, an undertaking " +
      "that would ennoble the flights of the highest genius, " +
      "and obtain pardon for the efforts of the meanest understanding. " +
      "Struggling a good while with these thoughts, by degrees I felt " +
      "myself more firm. I derived, at length, some confidence from what " +
      "in other circumstances usually produces timidity. I grew less " +
      "anxious, even from the idea of my own insignificance. For, " +
      "judging of what you are_by what you ought to be, I persuaded " +
      "myself that you would not reject a reasonable proposition because " +
      "it had nothing but its reason to recommend it. " +
      "The proposition is peace. Not peace through the medium of war; not " +
      "peace to be hunted through the labyrinth of intricate and endless " +
      "negotiations; not peace to arise out of universal discord, fomented " +
      "from principle, in all parts of the empire; not peace to depend on " +
      "the juridical determination of perplexing questions, or the precise " +
      "marking the shadowy boundaries of a complex government. It is simple " +
      "peace, sought in its natural course and in its ordinary haunts.";

   // the number of succesfully written log entries, according to hammer()
   protected static long m_numSuccessfullyWritten = 0;

   private static Properties ms_defaultProps = null;
   private static Properties ms_defaultFileProps = null;

   static
   {
      ms_defaultFileProps = new Properties();
      ms_defaultFileProps.setProperty("logTo", "FILE");
      try {
         File f =  File.createTempFile("test", ".log");

         ms_defaultFileProps.setProperty("logUrl", "file:///" + f.getAbsolutePath());

      } catch (IOException e) {
         e.printStackTrace();
      }


   }



}
