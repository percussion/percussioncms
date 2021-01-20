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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.spring;

import static com.percussion.util.PSResourceUtils.getResourcePath;
import static org.junit.Assert.*;

import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.UnitTest;
import com.percussion.utils.tools.PSBaseXmlConfigTest;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;


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

