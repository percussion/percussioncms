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
package com.percussion.process;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test the process package. These tests work off of a loaded process
 * file for the retrievalware processes. This test may require updates
 * as that file, rw_processes.xml, changes. Note that these are simply 
 * preliminary tests at this time to test positive functionality. At
 * some point it would be useful to extend these tests to negative test
 * cases.
 */
@Deprecated
public class PSProcessTest
{
   /**
    * The manager, initialized in {@link #setUp} and never <code>null</code>
    * or modified afterward.
    */
   PSProcessManager m_mgr = null;
   
   /**
    * values initialized in {@link #setUp}, never modified afterward.
    */
   Map m_vars = new HashMap();

   @Test
   @Ignore
   public void testSindex() throws Exception
   {
      IPSProcess sindex = m_mgr.getProcess("sindex_create");
      PSProcessDef def = sindex.getProcessDef();

      
      String executable = def.getExecutable(m_vars);
      String[] args = def.getCommandParams(m_vars);
      assertEquals(executable, "sindex");
      assertEquals(args.length, 6);
      assertEquals("-cfg", args[0]);
      assertEquals("C:\\rware70\\rx\\config\\rware.cfg", args[1]);
      assertEquals("-library", args[2]);
      assertEquals("ce299", args[3]);
      assertEquals("-new", args[4]);
      assertEquals("-create", args[5]);
   }
   
   /**
    * Try running a process. Note that you must have "ls" on your path for
    * this test to run.
    * 
    * @throws Exception
    */
   @Test
   @Ignore
   public void testLsRun() throws Exception
   {
      IPSProcess sindex = m_mgr.getProcess("dirlisting");
      PSProcessAction action = sindex.start(m_vars);
      int count = 0;
      
      while(action.getStatus() == PSProcessStatus.PROCESS_NOT_STARTED)
      {
         synchronized(this)
         {
            wait(100);
         }
         count++;
         
         if (count > 1000)
         {
            throw new Exception("Process failed to start");
         }
      }
      
      count = 0;
      while(action.getStatus() != PSProcessStatus.PROCESS_FINISHED)
      {
         synchronized(this)
         {
            wait(1000);
         }
         count++;
         
         if (count > 100)
         {
            throw new Exception("Process failed to complete");
         }
      } 
      
      String output = action.getStdOutText();
      assertTrue(output.length() > 0);
      assertTrue(action.getStdOutText().length() == 0); 
      System.out.println(output);    
   }


   /**
    * Ctor
    * @param name Name of test
    */
   public PSProcessTest()
   {
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Before
   @Ignore
   public void setUp() throws Exception
   {
      // Create a manager
      InputStream fis = new FileInputStream("UnitTestResources/com/percussion" +
         "/process/processes.xml");
      m_mgr = new PSProcessManager(fis);
      
      m_vars.put("WORKING_DIR", "C:/rware70/rx");
      m_vars.put("INSTALL_DIR", "C:/rware70");
      m_vars.put("RW_LIBRARY_NAME", "ce299");      
   }

}
