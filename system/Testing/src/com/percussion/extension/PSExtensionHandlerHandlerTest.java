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
