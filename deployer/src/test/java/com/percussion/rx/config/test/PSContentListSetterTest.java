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

import com.percussion.rx.config.impl.PSContentListSetter;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSContentListSetterTest extends PSConfigurationTest
{
   public void testNegative() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG,
               LOCAL_BAD_CFG);
         assertTrue(false);
      }
      catch (Exception e)
      {
         assertTrue(true);
      }

   }
   
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      validateLocalContentList();
      
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
      validateDefaultContentList();
   }

   private void validateLocalContentList() throws Exception
   {
      IPSContentList cList = getContentList();

      assertTrue(cList.getExpander().equals("Java/global/percussion/system/sys_ListTemplateExpander"));
      assertTrue(cList.getExpanderParams().get("template").equals("MyTemplate"));
      
      assertTrue(cList.getGenerator().equals("Java/global/percussion/system/sys_SearchGenerator"));
      String sql = cList.getGeneratorParams().get("query");
      assertTrue(sql.equals("select rx:sys_contentid from rx:rffimage where jcr:path like '//Sites%'"));
      
      assertTrue(cList.getUrl().indexOf("ftp") > 0);

      // filter
      IPSFilterService srv = PSFilterServiceLocator.getFilterService();      
      IPSItemFilter filter = srv.loadFilter(cList.getFilterId());
      assertTrue(filter.getName().equals("preview"));
   }
   
   public void testAddPropertyDefs() throws Exception
   {
      IPSContentList cList = getContentList();
      
      PSContentListSetter setter = new PSContentListSetter();
      
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      props.put(PSContentListSetter.DELIVERY_TYPE, "${perc.prefix.deliveryType}");
      props.put(PSContentListSetter.FILTER, "${perc.prefix.filter}");
      props.put(PSContentListSetter.EXPANDER_PARAMS, "${perc.prefix.exp_params}");
      props.put(PSContentListSetter.GEN_PARAMS, "${perc.prefix.gen_params}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(cList, defs);
      
      assertTrue("Expect 4 defs", defs.size() == 4);
      String value = (String) defs.get("perc.prefix.filter");
      assertTrue("Expect public", value.equals("public"));
      value = (String) defs.get("perc.prefix.deliveryType");
      assertTrue("Expect filesystem", value.equals("filesystem"));
   }
   
   private IPSContentList getContentList() throws Exception
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.CONTENT_LIST);
      return (IPSContentList) model.load(CONTENT_LIST);
   }

   private void validateDefaultContentList() throws Exception
   {
      IPSContentList cList = getContentList();
      assertTrue(cList.getExpander().equals("Java/global/percussion/system/sys_SiteTemplateExpander"));
      
      assertTrue(cList.getGenerator().equals("Java/global/percussion/system/sys_SearchGenerator"));
      String sql = cList.getGeneratorParams().get("query");
      assertTrue(sql.equals("select rx:sys_contentid, rx:sys_folderid from rx:rfffile,rx:rffimage,rx:percnavimage where jcr:path like '//Sites/CorporateInvestments%'"));
      
      assertTrue(cList.getUrl().indexOf("filesystem") > 0);

      // filter
      IPSFilterService srv = PSFilterServiceLocator.getFilterService();      
      IPSItemFilter filter = srv.loadFilter(cList.getFilterId());
      assertTrue(filter.getName().equals("public"));
   }
   
   private static final String CONTENT_LIST = "rffCiFullBinary";
   
   public static final String PKG_NAME = "PSContentListSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_ConfigDefs.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_LocalConfigs.xml";

   public static final String LOCAL_BAD_CFG = PKG_NAME + "_UnknownDeliveryType.xml";
   
   public static final String DEFAULT_CFG = PKG_NAME + "_DefaultConfigs.xml";

}
