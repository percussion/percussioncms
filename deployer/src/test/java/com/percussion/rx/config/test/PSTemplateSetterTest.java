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
import com.percussion.rx.config.impl.PSSimplePropertySetter;
import com.percussion.rx.config.impl.PSTemplateSetter;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.impl.PSAssociationSet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link PSTemplateSetter}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSTemplateSetterTest extends PSConfigurationTest
{
   
   private IPSAssemblyTemplate getTemplate(String name) throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      return service.findTemplateByName(name);
   }
   
   /**
    * Test properties and association setters
    * 
    * @throws Exception if an error occurs.
    */
   public void testPropertiesAssociation() throws Exception
   {
      IPSAssemblyTemplate template = getTemplate("rffPgCiGeneric");

      // create the setter
      PSTemplateSetter setter = new PSTemplateSetter();

      // init setter properties
      Map<String, Object> ct_pmap = new HashMap<String, Object>();

      String MY_LABEL = "My LABEL";

      ct_pmap.put("label", MY_LABEL);

      // association properties
      List<String> assocSlots = new ArrayList<String>();
      assocSlots.add("rffList");
      assocSlots.add("rffSidebar");
      ct_pmap.put(PSTemplateSetter.SLOTS, assocSlots);

      setter.setProperties(ct_pmap);

      // create association
      List<IPSAssociationSet> aSets = new ArrayList<IPSAssociationSet>();
      PSAssociationSet aset = new PSAssociationSet(
            IPSAssociationSet.AssociationType.TEMPLATE_SLOT);
      aSets.add(aset);

      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(template, ObjectState.BOTH, aSets);

      assertTrue(template.getLabel().equals(MY_LABEL));

      assertTrue(aSets.get(0).getAssociations().size() == 2);
   }

   /**
    * Tests {@link PSTemplateSetter#addPropertyDefs(Object, Map)}
    * 
    * @throws Exception if an error occurs
    */
   @SuppressWarnings("unchecked")
   public void testAddPropertyDefs() throws Exception
   {
      PSTemplateSetter setter = new PSTemplateSetter();
      IPSAssemblyTemplate template = getTemplate("rffPgCiGeneric");
      
      Map<String, Object> defs = new HashMap<String, Object>();
      // init setter properties
      Map<String, Object> pmap = new HashMap<String, Object>();
      pmap.put("label", "${perc.prefix.mylabel}");
      pmap.put(PSTemplateSetter.GLOBAL_TEMPLATE, "${perc.prefix.globalTemplate}");
      pmap.put(PSTemplateSetter.SLOTS, "${perc.prefix.slots}");
      pmap.put(PSTemplateSetter.BINDING_SET, "${perc.prefix.bindingSet}");
      pmap.put(PSTemplateSetter.BINDINGS, "${perc.prefix.bindings}");
      setter.setProperties(pmap);
      setter.addPropertyDefs(template, defs);
      
      // validate "defs"
      assertTrue("Expect 5 elements", defs.size() == 5);
      List slots = (List) defs.get("perc.prefix.slots");
      assertTrue("Expect 2 slots", slots.size() == 2);
      List bindingSet = (List) defs.get("perc.prefix.bindingSet");
      assertTrue("Expect 1 bindingSet", bindingSet.size() == 1);
      Map bindings = (Map) defs.get("perc.prefix.bindings");
      assertTrue("Expect 2 bindings", bindings.size() == 2);
      List seq = (List)bindings.get(PSTemplateSetter.BINDING_SEQ);
      assertTrue("Expect 1 sequence", seq.size() == 1);
      String gTemplate = (String) defs.get("perc.prefix.globalTemplate");
      assertTrue("Expect rffGtCorporateInvestmentsCommon", gTemplate
            .equals("rffGtCorporateInvestmentsCommon"));
      
      // NULL global template
      defs.clear();
      template.setGlobalTemplate(null);
      setter.addPropertyDefs(template, defs);
      gTemplate = (String) defs.get("perc.prefix.globalTemplate");
      assertTrue("Expect NULL", gTemplate == null);
      
      // test "fix-me" property definitions
      List slotsProps = new ArrayList<String>();
      slotsProps.add("${perc.prefix.slotValue1}");
      slotsProps.add("${perc.prefix.slotValue2}");
      slotsProps.add("${perc.prefix.slotValue3} AND ${perc.prefix.slotValue4}");
      pmap.put(PSTemplateSetter.SLOTS, slotsProps);
      
      defs.clear();
      setter.addPropertyDefs(template, defs);
      assertTrue("Expect 8 elements", defs.size() == 8);
      String fixme = (String) defs.get("perc.prefix.slotValue1");
      assertTrue("Expect null", fixme==null);
      fixme = (String) defs.get("perc.prefix.slotValue4");
      assertTrue("Expect fixme", fixme.equals(PSSimplePropertySetter.FIX_ME));
      
      // 
      // a MAP property value contains a ${place-holder} 
      // and the map entry does exist in the source map.
      defs.clear();
      pmap.clear();
      Map<String, Object> bindingMap = new HashMap<String, Object>();
      bindingMap.put("$rxs_navbase", "${perc.prefix.rxs_navbase}");
      pmap.put(PSTemplateSetter.BINDINGS, bindingMap);

      setter.setProperties(pmap);
      setter.addPropertyDefs(template, defs);
      
      assertTrue("Expecting 1 defs", defs.size() == 1);
      String value = (String)defs.get("perc.prefix.rxs_navbase");
      assertTrue("Expect \"$sys.variables.rxs_navbase\"", value
            .equals("$sys.variables.rxs_navbase"));
   }
   
   /**
    * Test properties and association setters
    * 
    * @throws Exception if an error occurs.
    */
   public void testBindingsSet() throws Exception
   {
      IPSAssemblyTemplate template = getTemplate("rffPgCiGeneric");

      // create the setter
      PSTemplateSetter setter = new PSTemplateSetter();

      // set Content Type setter
      Map<String, Object> ct_pmap = new HashMap<String, Object>();

      //\/\/\/\/\/\/\/\
      // set properties
      List<PSPair<String, String>> bindingSet = new ArrayList<PSPair<String, String>>();
      PSPair<String, String> p = new PSPair<String, String>("$rxs_navbase",
            "$sys.variables.rxs_navbase");
      bindingSet.add(p);

      ct_pmap.put(PSTemplateSetter.BINDING_SET, bindingSet);

      setter.setProperties(ct_pmap);

      // clear/reset the bindings
      template.getBindings().clear();
      
      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(template, ObjectState.BOTH, null);

      assertTrue(template.getBindings().size() == 1);

      //\/\/\/\/
      // Cleanup
      //\/\/\/\/
      resetBindings(template);
   }

   /**
    * Reset the binding of the given template.
    * @param template the template, assumed not <code>null</code>.
    */
   private void resetBindings(IPSAssemblyTemplate template)
   {
      PSTemplateBinding binding = new PSTemplateBinding(1, "$rxs_navbase",
            "$sys.variables.rxs_navbase");
      template.getBindings().clear();
      template.addBinding(binding);
   }
   
   /**
    * Test properties and association setters
    * 
    * @throws Exception if an error occurs.
    */
   public void fix_testBindings() throws Exception
   {
      IPSAssemblyTemplate template = getTemplate("rffPgCiGeneric");

      // create the setter
      PSTemplateSetter setter = new PSTemplateSetter();

      // set Content Type setter
      Map<String, Object> ct_pmap = new HashMap<String, Object>();

      //\/\/\/\/\/\/\/\
      // set properties
      Map<String, Object> bindings = new HashMap<String, Object>();
      bindings.put("$b1", "$b1_var");

      ct_pmap.put(PSTemplateSetter.BINDINGS, bindings);

      setter.setProperties(ct_pmap);

      // clear/reset the bindings
      //template.getBindings().clear();
      
      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(template, ObjectState.BOTH, null);

      assertTrue(template.getBindings().size() == 2);
      assertTrue(template.getBindings().get(1).getVariable().equals("$b1"));

      //\/\/\/\/\/\/\/\
      // reorder
      //\/\/\/\/\/\/\/\
      List<String> seq = new ArrayList<String>();
      seq.add("$b1");
      seq.add("$rxs_navbase");
      bindings.put(PSTemplateSetter.BINDING_SEQ, seq);
      bindings.put("$b2", "$b2_var");

      h.process(template, ObjectState.BOTH, null);

      assertTrue(template.getBindings().size() == 3);
      assertTrue(template.getBindings().get(0).getVariable().equals("$b1"));
      assertTrue(template.getBindings().get(2).getVariable().equals("$b2"));

      // just reorder existing ones
      seq.clear();
      seq.add("$b1");
      seq.add("$b2");
      seq.add("$rxs_navbase");

      h.process(template, ObjectState.BOTH, null);

      assertTrue(template.getBindings().size() == 3);
      assertTrue(template.getBindings().get(0).getVariable().equals("$b1"));
      assertTrue(template.getBindings().get(1).getVariable().equals("$b2"));

      //\/\/\/\/
      // Cleanup
      //\/\/\/\/
      resetBindings(template);
   }
   
   /**
    * Tests the "bindingSet" property.
    * 
    * @throws Exception
    */
   public void testConfigFiles() throws Exception
   {
      validateTestConfigFiles(IMPL_CFG);
   }

   /**
    * Tests the "bindings" property.
    * 
    * @throws Exception
    */
   public void testConfigFiles_2() throws Exception
   {
      validateTestConfigFiles(IMPL_2_CFG);
   }
   
   public void testValidation() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(
               PKG_NAME, IMPL_CFG, LOCAL_CFG);

         // validation should do nothing here
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2", IMPL_CFG,
               LOCAL_CFG);
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
      
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
   }
   
   public void testDoNothing() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(
               PKG_NAME, IMPL_CFG, DEFAULT_CFG, DEFAULT_CFG, true, true);
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
   }
   
   private void validateTestConfigFiles(String implCfg) throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, implCfg, LOCAL_CFG);
      
      // validate the result
      IPSAssemblyTemplate template = getTemplate("rffPgCiGeneric");

      assertTrue(template.getLabel().equals("My-Label"));
      assertTrue(template.getGlobalTemplateUsage().equals(GlobalTemplateUsage.None));
      assertTrue(template.getPublishWhen().equals(PublishWhen.Never));
      
      assertTrue(template.getMimeType().equals("text/xml"));
      assertTrue(template.getCharset().equals("UTF-16"));
      assertTrue(template.getLocationPrefix().equals("TestPrefix"));
      assertTrue(template.getLocationSuffix().equals("TestSuffix"));
      
      List<PSTemplateBinding> bindings = template.getBindings();
      assertTrue(bindings.size() == 3);
      assertTrue(bindings.get(0).getVariable().equals("$rxs_navbase_1"));
      assertTrue(bindings.get(1).getVariable().equals("$rxs_navbase"));
      assertTrue(bindings.get(2).getVariable().equals("$rxs_navbase_2"));
      
      // \/\/\/\/\/\/\/\
      // cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);

      // validate the default result
      template = getTemplate("rffPgCiGeneric");
      assertTrue(template.getLabel().equals("P - CI Generic"));
      assertTrue(template.getGlobalTemplateUsage().equals(GlobalTemplateUsage.Default));
      assertTrue(template.getPublishWhen().equals(PublishWhen.Default));

      assertTrue(template.getMimeType().equals("text/html"));
      assertTrue(template.getCharset().equals("UTF-8"));
      assertTrue(template.getLocationPrefix() == null);
      assertTrue(template.getLocationSuffix() == null);

      bindings = template.getBindings();
      assertTrue(bindings.size() == 1);
      assertTrue(bindings.get(0).getVariable().equals("$rxs_navbase"));
   }

   public static final String PKG_NAME = "PSTemplateSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String IMPL_2_CFG = PKG_NAME + "_2_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

}
