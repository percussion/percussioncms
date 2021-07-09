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
package com.percussion.services.assembly.impl;

import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;

/**
 * Test tracker.
 * 
 * @author dougrand
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSTrackAssemblyErrorsTest extends TestCase
{

   private static final Logger log = LogManager.getLogger(PSTrackAssemblyErrorsTest.class);

   /**
    * Dummy result value
    */
   private static final String RESULT = "<span>OK, good to go</span>";
   
   /**
    * Test 1 result data.
    */
   private static final String TEST1 = 
      "<html><head><title>Error in assembly</title></head><body>" +
      "<table border=\'0\'><tr><th>Description</th><th>Exception</th></tr>\n" + 
      "<tr><td>Problem</td><td></td></tr>\n" + 
      "</table></body></html>";
   
   /**
    * Test 2 result data.
    */
   private static final String TEST2 = 
      "<html><head><title>Error in assembly</title></head><body>" +
      "<table border=\'0\'><tr><th>Description</th><th>Exception</th></tr>\n" + 
      "<tr><td>Problem 1</td><td></td></tr>\n" + 
      "<tr><td>Problem 2</td><td>java.lang.Exception: An exception</td></tr>\n" +
      "</table></body></html>";   
   
   /**
    * Encoding
    */
   private static final String UTF8 = "utf8";

   /**
    * Test that the class works properly outside of assembly. Problems are
    * ignored.
    */
   @Test
   public void test10NonAssemblyCase()
   {
      PSTrackAssemblyError.addProblem("Problem");
      PSAssemblyWorkItem test = createItem();
      PSTrackAssemblyError.handleItem(test);
      checkResult(RESULT, "text/xhtml;charset=utf8", test);
   }
   
   /**
    * Test single problem case.
    */
   @Test
   public void test20OneAssemblyCase()
   {
      PSTrackAssemblyError.init();
      PSTrackAssemblyError.addProblem("Problem");
      PSAssemblyWorkItem test = createItem();
      PSTrackAssemblyError.handleItem(test);
      checkResult(TEST1, "text/html;charset=utf8", test);
   }
   
   /**
    * Test two problem case.
    */
   @Test
   public void test30TwoAssemblyCase()
   {
      PSTrackAssemblyError.init();
      PSTrackAssemblyError.addProblem("Problem 1");
      PSTrackAssemblyError.addProblem("Problem 2", 
            new Exception("An exception"));
      PSAssemblyWorkItem test = createItem();
      PSTrackAssemblyError.handleItem(test);
      checkResult(TEST2, "text/html;charset=utf8", test);
   }   

   /**
    * Check that the result data matches.
    * @param expected
    * @param expectedMimeType
    * @param item 
    */
   private void checkResult(String expected, String expectedMimeType, PSAssemblyWorkItem item)
   {
      try
      {
         String resultString = item.toResultString();
         String resultMT = item.getMimeType();
         assertEquals(expected, resultString);
         assertEquals(expectedMimeType, resultMT);
      }
      catch (UnsupportedEncodingException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Create a dummy item with a known result
    * @return item
    */
   private PSAssemblyWorkItem createItem()
   {
      PSAssemblyWorkItem work = new PSAssemblyWorkItem();
      try
      {
         work.setResultData(RESULT.getBytes(UTF8));
         work.setMimeType("text/xhtml;charset=utf8");
      }
      catch (UnsupportedEncodingException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      return work;
   }
}
