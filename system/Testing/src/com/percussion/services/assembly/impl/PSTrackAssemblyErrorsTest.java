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
package com.percussion.services.assembly.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

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
   public void test10NonAssemblyCase() throws IOException {
      PSTrackAssemblyError.addProblem("Problem");
      PSAssemblyWorkItem test = createItem();
      PSTrackAssemblyError.handleItem(test);
      checkResult(RESULT, "text/xhtml;charset=utf8", test);
   }
   
   /**
    * Test single problem case.
    */
   @Test
   public void test20OneAssemblyCase() throws IOException {
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
   public void test30TwoAssemblyCase() throws IOException {
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

   /**
    * Create a dummy item with a known result
    * @return item
    */
   private PSAssemblyWorkItem createItem() throws IOException {
      PSAssemblyWorkItem work = new PSAssemblyWorkItem();
      work.setResultData(RESULT.getBytes(StandardCharsets.UTF_8));
      work.setMimeType("text/xhtml;charset=utf8");
      return work;
   }
}
