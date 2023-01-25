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
package com.percussion.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSPathUtil class
 */
public class PSPathUtilTest extends TestCase
{
   
   public PSPathUtilTest(String s)
   {
      super(s);
   }
   
   public void testIsPathUnderSiteFolderRoot()
   {
      String pathA = "//Folders/test/foo";
      String pathB = "//Sites/test/bar";
      
      assertTrue(!PSPathUtil.isPathUnderSiteFolderRoot(pathA));
      assertTrue(PSPathUtil.isPathUnderSiteFolderRoot(pathB));
   }
   
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSPathUtilTest("testIsPathUnderSiteFolderRoot"));      
      return suite;
   }
}
