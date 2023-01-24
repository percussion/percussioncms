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
package com.percussion.extension;

import com.percussion.utils.testing.UnitTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the PSExtensionHandlerHandler class.
 */
@Category(UnitTest.class)
public class PSExtensionHandlerHandlerTest
{
   public PSExtensionHandlerHandlerTest()
   {

   }

   @Ignore //TODO: Tis test needs a proper setup method that generates a temp set of directories and files to test.
   @Test
   public void testRecursiveCopy() throws Exception
   {
      File dest = new File("temp/JUnitTest/recCopy_" + String.valueOf((new Date()).getTime()));
      File source = new File("");
      dest.mkdirs();
      assertTrue(dest.isDirectory());

      int numCopied = PSExtensionHandlerHandler.recursiveCopy(source, dest, false);
      
      assertTrue(numCopied > 0);

      int numCopied2 = PSExtensionHandlerHandler.recursiveCopy(source, dest, false);
      assertEquals(numCopied2, 0);

      int numCopied3 = PSExtensionHandlerHandler.recursiveCopy(source, dest, true);
      assertEquals(numCopied, numCopied3);
   }


}
