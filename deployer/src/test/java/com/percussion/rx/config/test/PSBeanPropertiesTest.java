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
package com.percussion.rx.config.test;

import com.percussion.rx.config.IPSBeanProperties;
import com.percussion.rx.config.PSBeanPropertiesLocator;
import com.percussion.rx.config.impl.PSBeanPropertiesSetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.config.impl.spring.IPSBeanPropertiesInternal;
import com.percussion.rx.config.impl.spring.PSBeanProperties;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link PSBeanProperties} and {@link PSBeanPropertiesSetter}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSBeanPropertiesTest extends PSConfigurationTest
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      cleanup();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      cleanup();
   }

   /**
    * Test the Bean Properties manager
    * @throws Exception
    */
   public void testBeanProperties() throws Exception
   {
      IPSBeanPropertiesInternal p = (IPSBeanPropertiesInternal) PSBeanPropertiesLocator
            .getBeanProperties();
      PSConfigNormalizerTest n = new PSConfigNormalizerTest();
      
      p.save(n.loadLocalConfigFile(true));
      
      // validate the saved properties
      n.validateLocalConfig(p.getProperties(), true);
   }

   /**
    * Tests the Bean Properties Setter
    * @throws Exception
    */
   public void testBeanPropertiesSetter() throws Exception
   {
      // setup the setter
      PSConfigNormalizerTest n = new PSConfigNormalizerTest();
      PSBeanPropertiesSetter setter = new PSBeanPropertiesSetter();
      setter.setProperties(n.loadLocalConfigFile(true));
      
      // apply properties
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(null, null, null);
      
      // validate the result
      IPSBeanPropertiesInternal p = (IPSBeanPropertiesInternal) PSBeanPropertiesLocator
            .getBeanProperties();
      n.validateLocalConfig(p.getProperties(), true);
   }
   
   public void testBeanPropertiesFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);

      // validate the result
      IPSBeanProperties p = PSBeanPropertiesLocator.getBeanProperties();

      assertTrue(p.getString("psx.Sample.stringKey").equals("String Value"));
      assertTrue(p.getString("psx.Sample.intKey").equals("9992"));
      
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("key1", "value1");
      params.put("key2", "value2");
      params.put("key3", "value3");
      assertTrue(p.getMap("psx.Sample.mapKey").equals(params));
      
      List<String> list = new ArrayList<String>();
      list.add("element 1");
      list.add("element 2");
      list.add("element 3");
      assertTrue(p.getList("psx.Sample.listKey").equals(list));
   }
   
   public void testAddPropertyDefs() throws Exception
   {
      PSBeanPropertiesSetter setter = new PSBeanPropertiesSetter();
      
      Map<String, Object> defs= new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      props.put("key1", "value1");
      
      setter.setProperties(props);
      setter.addPropertyDefs(null, defs);
      
      assertTrue("Expect 0 def", defs.size() == 0);

      // test more than one property "defs"
      defs.clear();
      props.clear();
      props.put("key1", "${perc.prefix.value1}");
      props.put("key2", "${perc.prefix.value2}");
      props.put("key3", "${perc.prefix.value3}");
      props.put("key4", "${perc.prefix.value4}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(null, defs);
      
      assertTrue("Expect 4 def", defs.size() == 4);
      assertTrue("Expect fixme", defs.get("perc.prefix.value4")==null);
   }
   
   /**
    * Clean up the Bean Properties repository
    */
   private void cleanup()
   {
      IPSBeanPropertiesInternal p = (IPSBeanPropertiesInternal) PSBeanPropertiesLocator
            .getBeanProperties();
      p.getProperties().clear();
      p.save(new HashMap<String, Object>());
      
      assertTrue(p.getProperties().isEmpty());
   }
   
   public static final String PKG_NAME = "PSBeanPropertiesTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";
   
}
