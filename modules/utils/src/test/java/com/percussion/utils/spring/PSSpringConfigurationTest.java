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
package com.percussion.utils.spring;

import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.UnitTest;
import com.percussion.utils.tools.PSBaseXmlConfigTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Test case for the {@link PSSpringConfiguration} class.
 */
@Category(UnitTest.class)
public class PSSpringConfigurationTest extends PSBaseXmlConfigTest
{
   /**
    * Constant for the file location of the server-beans.xml file, relative to
    * the project root.
    */
   public static final String TEST_BEANS_FILE = "/com/percussion/design/objectstore/legacy/test-beans.xml";

   /**
    * Tests all functionality
    * @throws Exception
    */
   @Test
   public void testAll() throws Exception
   {
      // create copy of bean config to work with
      File srcConfig = getTempXmlFile();
      File tmpConfig1 = getTempXmlFile();
      
      // copy source to that file and to test file
      File srcFile = PSResourceUtils.getFile(PSSpringConfigurationTest.class,
              TEST_BEANS_FILE,
              null);
      copyXmlFile(srcFile, srcConfig);
      copyXmlFile(srcFile, tmpConfig1);
      compareXmlDocs(srcConfig, tmpConfig1);
      
      // round trip the config and compare
      PSSpringConfiguration springConfig;
      springConfig = new PSSpringConfiguration(tmpConfig1);
      springConfig.save();
      compareXmlDocs(srcConfig, tmpConfig1);
     
      // now add a bean
      PSTestBeanConfig test = new PSTestBeanConfig(); 
      springConfig.setBean(test);
      IPSBeanConfig test2 = (IPSBeanConfig)springConfig.getBean(
         test.getBeanName());
      assertTrue(test2 instanceof PSTestBeanConfig);
      
      // test saving and reloading the bean
      springConfig.save();
      springConfig = new PSSpringConfiguration(tmpConfig1);
      test2 = (IPSBeanConfig)springConfig.getBean(test.getBeanName());
      assertTrue(test2 instanceof PSTestBeanConfig);
      
      // test remove
      assertFalse(springConfig.removeBean("foo"));
      assertTrue(springConfig.removeBean(test2.getBeanName()));
      assertNull(springConfig.getBeanXml(test2.getBeanName()));
      springConfig.save();
      compareXmlDocs(srcConfig, tmpConfig1);
      
      // clean up tmp files
      deleteTmpFiles();
   }

   @Override
   protected String getFilePrefix()
   {
      return "beans-";
   }
}

