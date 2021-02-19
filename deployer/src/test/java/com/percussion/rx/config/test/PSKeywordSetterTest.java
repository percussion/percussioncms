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
