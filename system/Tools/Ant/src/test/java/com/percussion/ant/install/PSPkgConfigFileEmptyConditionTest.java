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
package com.percussion.ant.install;

import com.percussion.utils.testing.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class PSPkgConfigFileEmptyConditionTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   /**
    * Constant for the non-empty package configuration file location.
    */   
   private static final String TEST_CFG_FILE_NONEMPTY =
      "/com/percussion/ant/install/perc.SystemObjects_defaultConfig.xml";
   
   /**
    * Constant for the empty package configuration file location.
    */
   private static final String TEST_CFG_FILE_EMPTY =
      "/com/percussion/ant/install/perc.SystemObjects_defaultConfig_Empty.xml";

   @Test
   public void testEval() throws IOException
   {
      Path root = temporaryFolder.getRoot().toPath();

      PSPkgConfigFileEmptyCondition p = new PSPkgConfigFileEmptyCondition();
      p.setRootDir(root.toAbsolutePath().toString());

      InputStream is = PSPkgConfigFileEmptyConditionTest.class.getResourceAsStream(TEST_CFG_FILE_EMPTY);
      Files.copy(is,root.resolve("perc.SystemObjects_defaultConfig_Empty.xml"));

      is =  PSPkgConfigFileEmptyConditionTest.class.getResourceAsStream(TEST_CFG_FILE_NONEMPTY);
      Files.copy(is,root.resolve("perc.SystemObjects_defaultConfig.xml"));


      // test non-empty file
      p.setRelativeFilePath(("perc.SystemObjects_defaultConfig.xml"));
      assertFalse(p.eval());


      // test empty file
      p.setRelativeFilePath("perc.SystemObjects_defaultConfig_Empty.xml");
      assertTrue(p.eval());
   }
   
}
