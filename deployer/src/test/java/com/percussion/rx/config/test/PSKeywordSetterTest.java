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

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.impl.PSKeywordSetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSKeywordSetterTest extends PSConfigurationTest
{
   @SuppressWarnings("unchecked")
   public void testKeywordSetter() throws PSNotFoundException {
      IPSDesignModelFactory dm = PSDesignModelFactoryLocator.getDesignModelFactory();
      IPSDesignModel model = dm.getDesignModel(PSTypeEnum.KEYWORD_DEF);
      PSKeyword kw = getKeyword();
      String origLabel = kw.getLabel();
      List<PSKeywordChoice> origChs = kw.getChoices();
      
      //Create the setter
      PSKeywordSetter kws = new PSKeywordSetter();
      
      String label = "CarColors";
      List<PSPair<String, String>> choices = new ArrayList<PSPair<String,String>>();
      choices.add(new PSPair("Blue","1"));
      choices.add(new PSPair("Red","2"));
      choices.add(new PSPair("Green","3"));
      choices.add(new PSPair("Orange","4"));
      Map<String, Object> props = new HashMap<String, Object>();
      props.put("label", label);
      props.put("choicePairs", choices);
      kws.setProperties(props);
      PSObjectConfigHandler cfgh = getConfigHandler(kws);
      cfgh.process(kw, ObjectState.BOTH, null);
      model.save(kw);
      kw = getKeyword();
      assertTrue(kw.getLabel().equals(label));
      assertTrue(kw.getChoices().size() == choices.size());
      assertTrue(kw.getChoices().get(0).getLabel().equals("Blue"));
      assertTrue(kw.getChoices().get(3).getLabel().equals("Orange"));
      
      //Reset the data
      kw.setLabel(origLabel);
      kw.setChoices(origChs);
      model.save(kw);
      
   }
   
   @SuppressWarnings("unchecked")
   public void testAddPropertyDefs() throws Exception
   {
      PSKeyword kw = getKeyword();
      //Create the setter
      PSKeywordSetter kws = new PSKeywordSetter();

      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      props.put("label", "${perc.prefix.label}");
      props.put(PSKeywordSetter.CHOICES_PAIRS, "${perc.prefix.choices}");
      kws.setProperties(props);

      kws.addPropertyDefs(kw, defs);
      
      assertTrue("Expect 2 defs", defs.size() == 2);
      assertTrue("Expect Body_Markup", defs.get("perc.prefix.label").equals("Body_Markup"));
      List<PSPair<String, String>> choices = (List<PSPair<String, String>>)defs.get("perc.prefix.choices");
      assertTrue("Expect 2 choices", choices.size() == 2);
      assertTrue("Expect Text", choices.get(1).getFirst().equals("Text"));
      assertTrue("Expect 1", choices.get(1).getSecond().equals("1"));
   }
   
   private PSKeyword getKeyword() throws PSNotFoundException {
      IPSDesignModelFactory dm = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dm.getDesignModel(PSTypeEnum.KEYWORD_DEF);
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      return (PSKeyword) model.load(gmgr.makeGuid(2, PSTypeEnum.KEYWORD_DEF));
   }
   
   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);

      //\/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
   }
   
   public static final String PKG_NAME = "PSKeywordSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";
   
}
