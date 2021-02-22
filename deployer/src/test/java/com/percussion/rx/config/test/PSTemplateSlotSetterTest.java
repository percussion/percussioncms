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
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.config.impl.PSTemplateSlotSetter;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link PSTemplateSlotSetter}
 *
 * @author YuBingChen
 */
@SuppressWarnings("unchecked")
@Category(IntegrationTest.class)
public class PSTemplateSlotSetterTest extends PSConfigurationTest // TestCase
{
   public void testSlotProperties() throws Exception
   {
      IPSDesignModel model = getSlotModel();
      IPSTemplateSlot slot = (IPSTemplateSlot) model.loadModifiable(SLOT_NAME);
      
      // get the initial association
      Collection<PSPair<IPSGuid, IPSGuid>> origAssoc = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      origAssoc.addAll(slot.getSlotAssociations());
      
      // create the setter
      PSTemplateSlotSetter setter = new PSTemplateSlotSetter();

      // set Content Type setter
      Map<String, Object> props = new HashMap<String, Object>();

      String MY_LABEL = "My SLOT LABEL";
      String MY_DESC = "My SLOT DESCRIPTION";

      props.put("label", MY_LABEL);
      props.put("description", MY_DESC);

      // add an extra/invalid parameter, which will be ignored (with warning)
      // when applying the properties by the setter.
      Map<String, String> testParams = new HashMap<String, String>();
      testParams.putAll(ms_testParams);
      testParams.put("junk", "4");

      // add in invalid ContentType/Template association, which will be
      // ignored and log warning when applying the association
      List<PSPair> testAssociation = new ArrayList<PSPair>();
      testAssociation.addAll(ms_testAssociation);
      // add the invalid association "rffEvent" <-> "rffSnLink"
      testAssociation.add(new PSPair("rffEvent", "rffSnLink"));
      
      props.put(PSTemplateSlotSetter.FINDER_PARAMS, testParams);
      props.put(PSTemplateSlotSetter.SLOT_ASSOCIATION, testAssociation);

      setter.setProperties(props);

      // create association
      List<IPSAssociationSet> aSets = getEmptyAssociation();

      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(slot, ObjectState.BOTH, aSets);

      assertTrue(MY_LABEL.equals(slot.getLabel()));
      assertTrue(MY_DESC.equals(slot.getDescription()));
      assertTrue(slot.getFinderArguments().size() == ms_testParams.size());
      assertTrue(ms_testParams.equals(slot.getFinderArguments()));
      
      model.save(slot, aSets);

      // validate the association
      // the new association should have the original pair and a new pair 
      Collection<PSPair<IPSGuid, IPSGuid>> newAssoc = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      newAssoc.addAll(slot.getSlotAssociations());
      assertTrue(!newAssoc.equals(origAssoc));

      assertTrue(ms_testAssociation.size() != newAssoc.size());
      assertTrue(testAssociation.size() == newAssoc.size());
      
      //\/\/\/\/\/\/\/\
      // cleanup
      slot = (IPSTemplateSlot) model.loadModifiable(SLOT_NAME);
      setSlotToDefaultProperties(props);
      aSets = getEmptyAssociation();
      // set PREVIOUS property contains the to be removed association
      Map<String, Object> prevProps = new HashMap<String, Object>();
      prevProps.put(PSTemplateSlotSetter.SLOT_ASSOCIATION, ms_testAssociation);
      setter.setPrevProperties(prevProps);

      h.process(slot, ObjectState.BOTH, aSets);
      model.save(slot, aSets);

      assertTrue(slot.getSlotAssociations().equals(origAssoc));
   }

   /**
    * Tests {@link PSTemplateSlotSetter#addPropertyDefs(Object, Map)}.
    * 
    * @throws Exception if an error occurs.
    */
   public void testAddPropertyDefs() throws Exception
   {
      IPSTemplateSlot slot = getSlot();
      PSTemplateSlotSetter setter = new PSTemplateSlotSetter();

      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      // properties are constants, no property definition generated
      props.put("label", "Events slot");
      props.put("description", "Slot populated by the auto index query");
      
      setter.setProperties(props);
      setter.addPropertyDefs(slot, defs);
      
      assertTrue("Expecting 0 defs", defs.size() == 0);

      // property values contains ${place-holder}
      props.clear();
      defs.clear();
      props.put("label", "${perc.prefix.label}");
      props.put(PSTemplateSlotSetter.FINDER_PARAMS, "${perc.prefix.params}");
      props.put(PSTemplateSlotSetter.SLOT_ASSOCIATION, "${perc.prefix.assocs}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(slot, defs);
      
      // validates "defs"
      assertTrue("Expecting 3 defs", defs.size() == 3);
      Map params = (Map) defs.get("perc.prefix.params");
      assertTrue("Expecting 4 params", params.size() == 4);
      assertTrue("type = sql", params.get("type").equals("sql"));
      List<PSPair<String, String>> assocs = (List<PSPair<String, String>>) defs
            .get("perc.prefix.assocs");
      assertTrue("Expecting 1 assoc", assocs.size() == 1);
      assertTrue("Expect rffEvent", assocs.get(0).getFirst().equals("rffEvent"));
      assertTrue("Expect rffSnTitleLink", assocs.get(0).getSecond().equals("rffSnTitleLink"));
      
      // a MAP property value contains a ${place-holder} 
      // and the map entry does exist in the source map.
      defs.clear();
      props.clear();
      Map<String, Object> paramsMap = new HashMap<String, Object>();
      paramsMap.put("type", "${perc.prefix.params_type}");
      props.put(PSTemplateSlotSetter.FINDER_PARAMS, paramsMap);

      setter.addPropertyDefs(slot, defs);
      
      assertTrue("Expecting 1 defs", defs.size() == 1);
      assertTrue("Expect sql", defs.get("perc.prefix.params_type").equals("sql"));
   }
   
   public void testConfigFiles_WithPrevProperties() throws Exception
   {
      // get the initial association
      IPSTemplateSlot slot = getSlot();      
      Collection<PSPair<IPSGuid, IPSGuid>> origAssoc = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      origAssoc.addAll(slot.getSlotAssociations());
      
      // apply "previous" configure file
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      slot = getSlot();
      assertTrue(slot.getFinderArguments().size() == ms_testParams.size());
      assertTrue(ms_testParams.equals(slot.getFinderArguments()));
      assertTrue(!origAssoc.equals(slot.getSlotAssociations()));
      
      // \/\/\/\/\/\/\/\
      // cleanup, add the default association and remove the previous association
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG,
            LOCAL_CFG);
      slot = getSlot();
      assertTrue(origAssoc.equals(slot.getSlotAssociations()));
   }

   public void testConfigFiles_UnProcess() throws Exception
   {
      // get the initial association
      IPSTemplateSlot slot = getSlot();      
      Collection<PSPair<IPSGuid, IPSGuid>> origAssoc = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      origAssoc.addAll(slot.getSlotAssociations());
      
      PSConfigFilesFactoryTest factory = null;
      try
      {
         // apply "local" configure file
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(PKG_NAME,
               IMPL_CFG, LOCAL_CFG);
         slot = getSlot();
         assertTrue(slot.getFinderArguments().size() == ms_testParams.size());
         assertTrue(ms_testParams.equals(slot.getFinderArguments()));
         assertTrue(!origAssoc.equals(slot.getSlotAssociations()));

         // Testing validation failure
         try
         {
            PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2", IMPL_CFG,
                  LOCAL_CFG);
            fail("Should have failed here, due to validation failure");
         }
         catch (Exception e)
         {
         }
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
      
      // de-apply "local" configure file
      PSConfigFilesFactoryTest.deApplyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      slot = getSlot();
      assertTrue(origAssoc.equals(slot.getSlotAssociations()));
   }

   
   private IPSTemplateSlot getSlot() throws PSNotFoundException {
      IPSDesignModel model = getSlotModel();
      IPSTemplateSlot slot = (IPSTemplateSlot) model.loadModifiable(SLOT_NAME);
      return slot;
   }

   private IPSDesignModel getSlotModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.SLOT);
   }
   

   private List<IPSAssociationSet> getEmptyAssociation()
   {
      return getSlotModel().getAssociationSets();
   }
   
   private void setSlotToDefaultProperties(Map<String, Object> props)
   {
      props.clear();
      
      props.put("label", "Events slot");
      props.put("description", "Slot populated by the auto index query");
      props.put(PSTemplateSlotSetter.FINDER_PARAMS, ms_defaultParams);
      props.put(PSTemplateSlotSetter.SLOT_ASSOCIATION, ms_defaultAssociation);
   }

   private static final String SLOT_NAME = "rffEvents";

   /**
    * The tested/temporary finder parameters
    */
   private static Map<String, String> ms_testParams = new HashMap<String, String>();

   /**
    * The tested/temporary association
    */
   private static List<PSPair> ms_testAssociation = new ArrayList<PSPair>();

   /**
    * The default association
    */
   private static List<PSPair> ms_defaultAssociation = new ArrayList<PSPair>();

   /**
    * The default finder parameters
    */
   private static Map<String, String> ms_defaultParams = new HashMap<String, String>();
   static
   {
      // set the default properties.
      ms_defaultParams.put("query", "select rx:sys_contentid, rx:sys_contentstartdate  from rx:rffcalendar  where jcr:path like :sitepath order by rx:sys_contentstartdate  asc");
      ms_defaultParams.put("type", "sql");
      ms_defaultParams.put("template", "rffSnTitleCalloutLink");
      ms_defaultParams.put("max_results", "4");
      ms_defaultParams.put("sys_lang", "");

      ms_defaultAssociation.add(new PSPair("rffEvent", "rffSnTitleLink"));
      
      // set to the test/temporary properties
      //    ms_testParams.put("query", "");
      //    ms_testParams.put("type", "");
      //    ms_testParams.put("sys_lang", "");
      ms_testParams.put("template", "UnknownTemplate");
      ms_testParams.put("max_results", "987");
      
      ms_testAssociation.add(new PSPair("rffBrief", "rffSnCallout"));
   }
   
   public static final String PKG_NAME = "PSTemplateSlotSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";
   
   
}
