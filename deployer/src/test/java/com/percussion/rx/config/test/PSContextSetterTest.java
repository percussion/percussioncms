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

import com.percussion.rx.config.impl.PSConfigUtils;
import com.percussion.rx.config.impl.PSContextSetter;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
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
   private String getDefaultScheme(String ctxName)
   {
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
