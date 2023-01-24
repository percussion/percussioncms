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

package com.percussion.ant.packagetool;

import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class PSPackageBuildToolHelperTest  
{
   @Test
   public void getDestinationDirectoryTest()
   {
       String rootDir = System.getProperty("java.io.tmpdir").concat(File.separator).concat("dest");
       String zipFileWithPath = System.getProperty("java.io.tmpdir").concat(File.separator).concat("perc.gadget.activity8.ppkg");
   
       String expectedDestinationDir = rootDir.concat(File.separator).concat("perc.gadget.activity8");
       String actualDestinationDir = PSPackageBuildToolHelper.getDestinationDirectoryPath(zipFileWithPath, rootDir);
   
       assertEquals(expectedDestinationDir, actualDestinationDir);
    }   
}
