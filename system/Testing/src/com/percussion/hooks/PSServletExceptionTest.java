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

package com.percussion.hooks;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test class for testing <code>PSServletException</code>
 */
public class PSServletExceptionTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSServletExceptionTest(String name)
   {
      super(name);
   }

   /**
    * Tests all Xml functions for PSContentSelectorDef class
    *
    * @throws Exception if there are any errors.
    */
   public void testAll() throws Exception
   {
      PSServletException e = new PSServletException(50155);
      String errorMsg = e.getLocalizedMessage();
      assertTrue(errorMsg.length() >  String.valueOf(50155).length() + 2);
      
      e = new PSServletException(100);
      errorMsg = e.getLocalizedMessage();
      assertTrue(errorMsg.length() > String.valueOf(100).length() + 2);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSServletExceptionTest("testAll"));
      return suite;
   }

}
