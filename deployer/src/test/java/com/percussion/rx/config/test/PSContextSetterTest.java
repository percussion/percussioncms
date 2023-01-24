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

import com.percussion.rx.config.impl.PSConfigUtils;
import com.percussion.rx.config.impl.PSContextSetter;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSContextSetterTest extends PSConfigurationTest
{
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG_2);      

      // validate the configuration
      String schemeName = getDefaultScheme("Site_Folder_Assembly");
      assertTrue(schemeName == null);
            

      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);      

      // validate the configuration
      schemeName = getDefaultScheme("Site_Folder_Assembly");
      assertTrue(schemeName.equals("CI_Home"));
            
      //\/\/\/\/\/\
      // Cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
      
      // validate
      schemeName = getDefaultScheme("Site_Folder_Assembly");
      assertTrue(schemeName.equals("Generic"));
   }

   public void testAddPropertyDefs() throws Exception
   {
      IPSDesignModel ctxModel = PSConfigUtils.getContextModel();
      IPSPublishingContext ctx = (IPSPublishingContext) ctxModel
            .load("Site_Folder_Assembly");
      
      PSContextSetter setter = new PSContextSetter();
      
      Map<String, Object> props = new HashMap<String, Object>();
      Map<String, Object> defs = new HashMap<String, Object>();
      props.put(PSContextSetter.DEFAULT_SCHEME, "${perc.prefix.defaultScheme}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(ctx, defs);
      
      assertTrue("Expect 1 def", defs.size() == 1);
      assertTrue("Expect \"Generic\"", defs.get("perc.prefix.defaultScheme")
            .equals("Generic"));
   }
   
   /**
    * Gets the default Location Scheme name from the specified context name.
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @return the default Location Scheme name.
    */
   private String getDefaultScheme(String ctxName) throws PSNotFoundException {
      IPSDesignModel ctxModel = PSConfigUtils.getContextModel();
      IPSPublishingContext ctx = (IPSPublishingContext) ctxModel.load(ctxName);
      IPSGuid schemeId = ctx.getDefaultSchemeId();
      if (schemeId == null)
         return null;
      
      PSLocationSchemeModel schemeModel = PSConfigUtils.getSchemeModel();
      IPSLocationScheme scheme = (IPSLocationScheme) schemeModel.load(schemeId);
      
      assertTrue(scheme != null);
      return scheme.getName();      
   }
   
   public static final String PKG_NAME = "PSContextSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String LOCAL_CFG_2 = PKG_NAME + "_2_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";


}
