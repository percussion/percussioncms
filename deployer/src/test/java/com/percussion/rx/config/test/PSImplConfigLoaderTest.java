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
package com.percussion.rx.config.test;

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.impl.PSConfigNormalizer;
import com.percussion.rx.config.impl.PSImplConfigLoader;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Test {@link PSImplConfigLoader}
 * 
 * Note, the "Working directory" must be ${workspace_loc:Rhythmyx-Main} when run
 * this unit test.
 */
@Category(UnitTest.class)
public class PSImplConfigLoaderTest
{
   /**
    * Tests implementer's configure files contain simple property handler.
    *
    * @throws Exception if an error occurs.
    */
   @Test
   public void testSimpleImplConfig() throws Exception
   {
      File f = PSResourceUtils.getFile(PSImplConfigLoader.class, SIMPLE2_CONFIG_DEF, null);
      SimpleImplConfigTest(f.getAbsolutePath(), 2, 2);

      f = PSResourceUtils.getFile(PSImplConfigLoader.class, SIMPLE_CONFIG_DEF, null);
      SimpleImplConfigTest(f.getAbsolutePath(), 1, 3);

      // make sure we can reload the same bean file with absolute path.
      SimpleImplConfigTest(f.getAbsolutePath(), 1, 3);
   }

   /**
    * Tests loading an implementation config file that contains simple property
    * setter.
    * 
    * @param expectedHandlers number of expected handlers in the file.
    * 
    * 
    * @throws Exception if an error occurs.
    */
   private void SimpleImplConfigTest(String filePath, int expectedHandlers,
         int propCount)
      throws Exception
   {
      PSImplConfigLoader loader;
      try
      {
         loader = new PSImplConfigLoader(filePath);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
      String[] beans = loader.getAllBeanNames();
      assertTrue(beans.length == expectedHandlers);

      IPSConfigHandler bean = loader.getBean(beans[0]);
      assertTrue(bean != null);

      PSObjectConfigHandler handler = (PSObjectConfigHandler) bean;
      assertTrue(handler.getPropertySetters().size() == 1);
      
      IPSPropertySetter setter = handler.getPropertySetters().get(0);
      assertTrue(setter.getProperties().size() == propCount);

      loader.close();
   }

   /**
    * Tests loading a local config file which contains simple properties.
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void testSimpleLocalConfigTest() throws Exception
   {
      // Props to get from Map
      String KEY1 = "com.percussion.RSS.label";
      String VALUE1 = "New Label"; 
      String KEY2 = "com.percussion.RSS.description";
      String VALUE2 = "New Description"; 
      
      
      File f = PSResourceUtils.getFile(PSImplConfigLoaderTest.class, SimpleLocalConfig, null);
      FileInputStream fstream = new FileInputStream(f);
      
      PSConfigNormalizer nm = new PSConfigNormalizer();
      Map<String, Object> props = nm.getNormalizedMap(fstream);
           
      assertTrue(props.size() == 2);
      assertTrue(props.get(KEY1).equals(VALUE1));
      assertTrue(props.get(KEY2).equals(VALUE2));
   }
   
   
   /**
    * The directory for all test/source files
    */
   public static final String UNIT_TEST_DIR =
      "/com/percussion/rx/config/test/";

   /**
    * The implementer's configure file contains simple properties
    */
   public static final String SIMPLE_CONFIG_DEF = UNIT_TEST_DIR + "Simple_configDef.xml";

   public static final String SIMPLE2_CONFIG_DEF = UNIT_TEST_DIR + "Simple_2_configDef.xml";
   
   /**
    * The implementer's local configuration file contains simple properties
    */
   private static final String SimpleLocalConfig = UNIT_TEST_DIR + "SimpleLocalPropertyConfig.xml";
}
