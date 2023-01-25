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

import com.percussion.data.PSStylesheetCleanupFilter;

import java.io.File;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.UnitTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * @author dougrand
 */
@Category(UnitTest.class)
public class PSNamespaceCleanupTest
{
   /**
    * Input file for configuration
    */
   static File ms_cfile = null;

   @BeforeClass
   public static void setUp() throws Exception
   {
      ms_cfile = PSResourceUtils.getFile(PSNamespaceCleanupTest.class,
              "/com/percussion/services/assembly/namespaceConfig.xml", null);

      PSStylesheetCleanupFilter.getInstance(ms_cfile);
   }

   /**
    * Input
    */
   static final String ms_input = "<?xml version='1.0'?>\n"
         + "<!-- comment --><div a='1' xmlns:goofy='http://www.goofy.org'>"
         + "<el1 b='2'><el2 c='3' xmlns:bletch='somethingelse'/></el1>"
         + "<el1 xmlns:foobar='someotheruri'/>"
         + "</div>";
   
   /**
    * Expected result
    */
   static final String ms_result = "<!-- comment --><div a=\"1\"><el1 b=\"2\"><el2 c=\"3\"/>"
         + "</el1><el1/></div>";

   /**
    * Test cleanup
    */
   @Test
   public void testNSCleanup() throws PSNotFoundException {
      PSNamespaceCleanup cleanup = new PSNamespaceCleanup(null);
      String result = (String) cleanup.translate(ms_input);
      assertEquals(ms_result, result);
   }
}
