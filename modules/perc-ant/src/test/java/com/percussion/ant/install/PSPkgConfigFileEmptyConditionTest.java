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
package com.percussion.ant.install;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.utils.testing.UnitTest;
import org.junit.Before;
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

   @Before
   public void setup(){
      PSSecureXMLUtils.setupJAXPDefaults();
   }

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
