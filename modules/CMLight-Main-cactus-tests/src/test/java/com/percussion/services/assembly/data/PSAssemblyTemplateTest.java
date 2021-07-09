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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.assembly.data;

import com.percussion.cms.objectstore.server.PSContentTypeVariantsMgr;
import com.percussion.services.assembly.*;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.utils.types.PSPair;
import junit.framework.JUnit4TestAdapter;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static com.percussion.extension.IPSExtension.LEGACY_ASSEMBLER;

/**
 * Test the assembly template object for correct behavior
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSAssemblyTemplateTest extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSAssemblyTemplateTest.class);

   /**
    * Name of created template
    */
   private static final String MYTESTVARIANT = "mytestvariant";

   /**
    * Name of created template
    */
   private static final String MYTESTVARIANT0 = MYTESTVARIANT + "_0";

   /**
    * Count used in generating template names
    */
   private static int ms_count = 0;

   /**
    * Name of created slot
    */
   private static final String TEST_SLOT = "TestSlot";

   /**
    * Setup test binding information for test template
    * @param var
    */
   private void setupBindingData(IPSAssemblyTemplate var)
   {
      List<PSTemplateBinding> bindings = new ArrayList<>();
      bindings.add(new PSTemplateBinding(1, "x", "y * z"));
      bindings.add(new PSTemplateBinding(2, "w", "x  / 3"));
      var.setBindings(bindings);
   }

   /**
    * Setup test template
    * @param var
    */
   private void setupTemplateData(IPSAssemblyTemplate var)
   {
      String name = MYTESTVARIANT + "_" + ms_count++;

      var.setActiveAssemblyType(IPSAssemblyTemplate.AAType.NonHtml);
      var.setAssembler(LEGACY_ASSEMBLER);
      var.setAssemblyUrl("myassemblyurl");
      var.setDescription("Test template");
      var.setLocationPrefix("prefix");
      var.setLocationSuffix("suffix");
      var.setName(name);
      var.setLabel(name);
      var.setTemplateType(IPSAssemblyTemplate.TemplateType.Shared);
      var.setOutputFormat(IPSAssemblyTemplate.OutputFormat.Page);
      var.setPublishWhen(IPSAssemblyTemplate.PublishWhen.Always);
      var.setGlobalTemplateUsage(IPSAssemblyTemplate.GlobalTemplateUsage.Defined);
      var.setStyleSheetPath("My template");

      setupSlotData(var, 2);
   }

   /**
    * Setup test slot
    * @param var
    * @param count
    */
   private void setupSlotData(IPSAssemblyTemplate var, int count)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      List<IPSTemplateSlot> slots = service.findSlotsByName("%");
      for (int i = 0; i < slots.size() && i < count; i++)
      {
         var.addSlot(slots.get(i));
      }
   }
   
   @Override
   protected void setUp() 
   {
      try
      {
         cleanup();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   protected void tearDown()
   {
      setUp();
   }
   
   
   /**
    * @throws Exception
    */
   public static void cleanup() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      // Clear out old test data

      Set<IPSAssemblyTemplate> templates = service.findAllTemplates();
      for (IPSAssemblyTemplate t : templates)
      {
         if (t.getName().startsWith(MYTESTVARIANT) 
               || t.getName().startsWith("____test1____"))
         {
            try
            {
               service.deleteTemplate(t.getGUID());
            }
            catch (Exception e)
            {
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
            }
         }
      }

      while (true)
      {
         IPSTemplateSlot slot;
         try
         {
            slot = service.findSlotByName(TEST_SLOT);
            service.deleteSlot(slot.getGUID());
         }
         catch (Exception e)
         {
            break;
         }
      }
      
      ms_count = 0; // Reset
   }

   /**
    * MSM Specific test and relies on FF to be installed. This test does read 
    * a template, modify the slots and produce a new template as an 
    * xml representation. This xml representation is tested for accuracy.
    * @throws Exception
    */
   @Test
   public void testModifySlotsInTemplate() throws Exception
   {
      IPSAssemblyService svc = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyTemplate t = svc.loadTemplate(new PSGuid(
            PSTypeEnum.TEMPLATE, 505), true);
      // Extract the slot guids from the template
      String tmpStr = t.toXML();
      // save the binding's version
      // Save bindings versions
      Map<Long, Integer> bver = new HashMap<Long, Integer>();
      for (IPSTemplateBinding binding : t.getBindings())
      {
         PSTemplateBinding rbinding = (PSTemplateBinding) binding;
         bver.put(rbinding.getBindingId(), rbinding.getVersion());
      }

      
      
      Set<IPSGuid> slots = PSAssemblyTemplate.getSlotIdsFromTemplate(tmpStr);
      // clean up the slots
      for (IPSGuid guid : slots)
      {
        IPSTemplateSlot ts = svc.loadSlot(guid);
        t.removeSlot(ts);
      }
      // make new slot guids from the existing FF implementation
      Set<IPSGuid> newSlots = new HashSet<IPSGuid>();
      List<String> slotNames = new ArrayList<String>();
      slotNames.add("rffAutoCalendarEvents");     //520
      slotNames.add("rffAutoPressReleases2004");  //522
      slotNames.add("rffAutoPressReleases2005");  //523
      List<IPSTemplateSlot> slotList = svc.findSlotsByNames(slotNames);
      for (IPSTemplateSlot s : slotList)
         newSlots.add(s.getGUID());

      // replace the slots with the new ones
      String newTmpStr = PSAssemblyTemplate.replaceSlotIdsFromTemplate(tmpStr,
            newSlots);
      
      // and extract them from the modified template string
      Set<IPSGuid> modifiedSlots = PSAssemblyTemplate.getSlotIdsFromTemplate(newTmpStr);
      // make sure the modified templates were really added to the serialization
      assertTrue(CollectionUtils.isEqualCollection(newSlots, modifiedSlots));
      
      //deserialize on the existing template with the modified Template Str
      ((PSAssemblyTemplate) t).setVersion(null);
      t.fromXML(newTmpStr);
      Set<IPSTemplateSlot> s1 = t.getSlots();
      for (IPSTemplateSlot s : s1)
      {
         if ( !modifiedSlots.contains(s.getGUID()) )
         {
            assert(false);
         }
      }
      assert(true);
   } 

   /**
    * Text for below test
    */
   private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, "
         + "consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
         + "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
         + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. "
         + "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
         + "dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat "
         + "non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

   /**
    * Create, save and load a large template with the database. Written to deal
    * with an oracle problem with large templates. The object created is 64k of
    * lorum ipsum text
    * 
    * @throws Exception
    */
   @Test
   public void testLargeTemplate() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      IPSAssemblyTemplate template = service.createTemplate();
      template.setName("test_big_template_source");

      StringBuilder text = new StringBuilder(65000);
      while (text.length() < 65000)
      {
         text.append(LOREM_IPSUM);
         text.append(' ');
      }
      template.setTemplate(text.toString());

      // Save
      service.saveTemplate(template);

      // Restore
      IPSAssemblyTemplate restore = service.loadTemplate(template.getGUID(),
            true);

      assertNotNull(restore);
      assertEquals(template, restore);

      // Remove created
      service.deleteTemplate(template.getGUID());
   }

   /**
    * Test the various finder methods
    * 
    * @throws Exception
    */
   @Test
   public void testFinders() throws Exception
   {
      final IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      FutureTask<IPSAssemblyTemplate> loadedById = 
         new FutureTask<IPSAssemblyTemplate>(new Callable<IPSAssemblyTemplate>()
               {

                  public IPSAssemblyTemplate call() throws Exception
                  {
                     return service.loadUnmodifiableTemplate("526");
                  }});
      loadedById.run();
      IPSAssemblyTemplate template = service
            .findTemplateByName("rffSnLink");
      assertNotNull(template);
      assertTrue(template == loadedById.get());
      
      List<IPSTemplateSlot> slots = service.findSlotsByName("rffHome%");
      assertNotNull(slots);
      assertTrue(slots.size() > 1);
      
      List<String> nlist = new ArrayList<String>();
      nlist.add("rffAutoCalendarEvents");
      nlist.add("rffAutoPressReleases2007");
      slots = service.findSlotsByNames(nlist);
      assertNotNull(slots);
      assertEquals(2, slots.size());
      
      Collection<IPSAssemblyTemplate> templates;

      // find all FF snippets
      String templateName = "rffSn%";
      templates = service.findTemplates(templateName, null, null, null, null,
            null, null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);
      int count = templates.size();

      // Check that finder is not case sensitive
      templates = service.findTemplates("RFFSN%", null, null, null, null, null, null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);
      int icount = templates.size();
      assertEquals(count,icount);
      
      // find all FF snippets of the Generic contenttype
      String contentType = "rffgeneric";
      templates = service.findTemplates(templateName, contentType, null, null,
            null, null, null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);
      count = templates.size();
      
      // find all FF snippets of the Generic contenttype and output format page
      Set<IPSAssemblyTemplate.OutputFormat> outputFormats = new HashSet<IPSAssemblyTemplate.OutputFormat>();
      outputFormats.add(IPSAssemblyTemplate.OutputFormat.Page);
      templates = service.findTemplates(templateName, contentType,
            outputFormats, null, null, null, null);
      assertNotNull(templates);
      assertTrue(templates.size() == 0);

      // find all FF snippets of the Generic contenttype and output format
      // page and snippet
      outputFormats.add(IPSAssemblyTemplate.OutputFormat.Snippet);
      templates = service.findTemplates(templateName, contentType,
            outputFormats, null, null, null, null);
      assertNotNull(templates);
      assertTrue(templates.size() == count);

      // Find legacy templates (commented out for now)
      /*
      templates = service.findTemplates(null, null, null, null, null, true,
            null);
      assertNotNull(templates);
      assertTrue(templates.size() == 0);
      */

      // Find non-legacy templates
      templates = service.findTemplates(null, null, null, null, null, false,
            null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);

      // Find non-legacy global templates
      templates = service.findTemplates(null, null, null, null, true, false,
            null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);

      // Find non-legacy non-global templates
      templates = service.findTemplates(null, null, null, null, false, false,
            null);
      assertNotNull(templates);
      assertTrue(templates.size() > 0);

      templates = service.findAllGlobalTemplates();
      assertNotNull(templates);
      assertTrue(templates.size() > 0);

      templates = service.findAllTemplates();
      assertNotNull(templates);
      assertTrue(templates.size() > 0);

      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(new PSGuid(PSTypeEnum.SLOT, 103));
      ids.add(new PSGuid(PSTypeEnum.SLOT, 104));
      ids.add(new PSGuid(PSTypeEnum.SLOT, 105));

      slots = service.loadSlots(ids);
   }

   /**
    * Test requesting one template where more than one matches the name
    * 
    * @throws Exception
    */
   @Test
   public void testDuplicateName() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      String name = "____test1____";

      List<IPSAssemblyTemplate> templates = service.findTemplates(name, null,
            null, null, false, false, null);
      
      for(IPSAssemblyTemplate t : templates)
      {
         service.deleteTemplate(t.getGUID());
      }

      IPSAssemblyTemplate x = service.createTemplate();
      IPSAssemblyTemplate y = service.createTemplate();

      x.setName(name);
      x.setLabel(name);

      y.setName(name);
      y.setLabel(name);

      service.saveTemplate(x);
      service.saveTemplate(y);

      try
      {
         service.findTemplateByName(name);
         assertFalse("No exception where one expected", true);
      }
      catch (PSAssemblyException e)
      {
         // OK
      }
      catch (Exception e)
      {
         assertFalse("Wrong exception found", true);
      }

   }

   /**
    * @throws Exception
    */
   @Test
   public void testTemplateCreation() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate var = service.loadTemplate(new PSGuid(
            PSTypeEnum.TEMPLATE, 516), true);
      PSStopwatch watch1 = new PSStopwatch();
      PSStopwatch watch2 = new PSStopwatch();
      watch1.start();
      var = service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 517), true);
      watch1.stop();
      watch2.start();
      IPSAssemblyTemplate second = service.loadTemplate(new PSGuid(
            PSTypeEnum.TEMPLATE, 517), true);
      watch2.stop();
      System.out.println("First load " + watch1);
      System.out.println("Second load " + watch2);
      // Create a new template and persist
      var = service.createTemplate();
      var.setActiveAssemblyType(IPSAssemblyTemplate.AAType.Normal);
      var.setAssembler("velocity");
      setupTemplateData(var);
      setupBindingData(var);
      // Persist to database
      service.saveTemplate(var);

      // Reload it
      second = service.loadTemplate(var.getGUID(), true);
      assertEquals(var, second);
   }

   /**
    * @throws Exception
    */
   @Test
   public void testBindingModification() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      IPSAssemblyTemplate var = service.createTemplate();
      setupTemplateData(var);
      // Persist to database
      service.saveTemplate(var);

      // Reload and modify, then save
      var = service.loadTemplate(var.getGUID(), true);
      setupBindingData(var);
      service.saveTemplate(var);

      // Reload, modify again, then save
      var = service.loadTemplate(var.getGUID(), true);
      var.setBindings(new ArrayList<>());
      setupBindingData(var);
      service.saveTemplate(var);
   }


   /**
    * @throws Exception
    */
   @Test
   public void fixme_testTemplateSerialization() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      IPSAssemblyTemplate var = service.findTemplateByName(MYTESTVARIANT0);
      String ser = var.toXML();

      IPSAssemblyTemplate blank = new PSAssemblyTemplate();
      try
      {
         blank.fromXML(ser);
      }
      catch (Exception e)
      {
         System.out.println("Error occurred during de-serialization");
      }
      assertEquals(var, blank);
      assertEquals(var.hashCode(), blank.hashCode());
   }

   /**
    * @throws Exception
    */
   @Test
   public void fixme_testSlotSerialization() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate var = service.findTemplateByName(MYTESTVARIANT0);
      IPSTemplateSlot s = var.getSlots().iterator().next();
      // Test slot serialization
      String ser = s.toXML();
      IPSTemplateSlot newslot = service.createSlot();
      newslot.fromXML(ser);
      assertEquals(s, newslot);
   }

   /**
    * @throws Exception
    */
   @Test
   public void fixme_testModifySlotAssociations() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate cachedTemplate = service
            .findTemplateByName(MYTESTVARIANT0);
      IPSAssemblyTemplate nonCacheTemplate = service.loadTemplate(
            cachedTemplate.getGUID(), true);
      // Modify associations and save again
      IPSTemplateSlot s = nonCacheTemplate.getSlots().iterator().next();
      List<PSPair<IPSGuid, IPSGuid>> news = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      s.setSlotAssociations(news);
      service.saveTemplate(nonCacheTemplate);
   }

   /**
    * A test to deserialize a template and save it to the system that does not
    * have this template.
    * 
    * @throws Exception
    * 
    */
   @Test
   public void fixme_testDeSerializeAndLoadTemplate() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate cachedTemplate = service
            .findTemplateByName(MYTESTVARIANT0);
      IPSAssemblyTemplate var = service.loadTemplate(cachedTemplate.getGUID(),
            true);

      String ser = var.toXML();
      Integer ver = ((PSAssemblyTemplate) var).getVersion();
      IPSAssemblyTemplate blank = new PSAssemblyTemplate();
      blank.fromXML(ser);

      // Save bindings versions
      Map<Long, Integer> bver = new HashMap<Long, Integer>();
      for (IPSTemplateBinding binding : var.getBindings())
      {
         PSTemplateBinding rbinding = (PSTemplateBinding) binding;
         bver.put(rbinding.getBindingId(), rbinding.getVersion());
      }

      // deserialize on loaded template
      ((PSAssemblyTemplate) var).setVersion(null);
      var.fromXML(ser);
      ((PSAssemblyTemplate) var).setVersion(null);
      ((PSAssemblyTemplate) var).setVersion(ver);
      for (IPSTemplateBinding binding : var.getBindings())
      {
         PSTemplateBinding rbinding = (PSTemplateBinding) binding;
         rbinding.setVersion(bver.get(rbinding.getBindingId()));
      }

      // Restore binding versions

      // modify something and update
      var.setAssembler("unknownAssembler");
      service.saveTemplate(var);
   }

   /**
    * @throws Exception
    */
   @Test
   public void fixme_testRemoveTemplate() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate var = service.findTemplateByName(MYTESTVARIANT0);
      var = service.findTemplate(var.getGUID());
      assertNotNull(var);
      // Delete it
      service.deleteTemplate(var.getGUID());

      var = service.findTemplate(var.getGUID());
      assertNull(var);
}

   /**
    * @throws Exception
    */
   @Test
   public void testFindByContentType() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      List<IPSAssemblyTemplate> templates = service
            .findTemplatesByContentType(new PSGuid(PSTypeEnum.NODEDEF, 311));
      assertTrue(templates.size() > 0);
      
      IPSAssemblyTemplate template = templates.get(0);
      IPSAssemblyTemplate template2 = (IPSAssemblyTemplate)template.clone();
      assertTrue(template.equals(template2));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testTemplateCheckPerformanceMetrics() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      // Things have been exercised. Let's get ahold of the session factory,
      // enable statistics and clear the 2nd level cache
      SessionFactory fact = (SessionFactory) PSAssemblyServiceLocator
            .getBean("sys_sessionFactory");
      fact.getCache().evictEntityRegion(PSAssemblyTemplate.class);
      fact.getCache().evictEntityRegion(PSTemplateBinding.class);
      fact.getStatistics().setStatisticsEnabled(true);

      // Now load several templates
      PSStopwatch watch = new PSStopwatch();
      watch.start();
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 501), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 502), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 503), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 504), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 505), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 506), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 507), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 508), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 509), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 510), false);
      watch.stop();
      System.out.println("Loading 10 fresh: " + watch);
      System.out.println("2nd level cache stats: "
            + fact.getStatistics().getSecondLevelCacheStatistics("object"));
      watch.start();
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 501), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 502), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 503), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 504), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 505), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 506), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 507), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 508), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 509), false);
      service.loadTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 510), false);
      watch.stop();
      System.out.println("Loading 10 from cache: " + watch);
      System.out.println("2nd level cache stats: "
            + fact.getStatistics().getSecondLevelCacheStatistics("object"));
   }

   /**
    * 
    */
   @Test
   public void testRemoveTestSlots()
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      while (true)
      {
         try
         {
            IPSTemplateSlot slot = service.findSlotByName(TEST_SLOT);
            service.deleteSlot(slot.getGUID());
         }
         catch (PSAssemblyException e)
         {
            break;
         }
      }
   }

   /**
    * @throws Exception
    */
   @SuppressWarnings(
   {"deprecation", "unchecked"})
   @Test
   public void testContentTypeVariantsMgr() throws Exception
   {
      com.percussion.cms.objectstore.PSContentTypeVariantSet set = PSContentTypeVariantsMgr
            .getAllContentTypeVariants(null);
      assertTrue(set.size() > 0);
      com.percussion.cms.objectstore.PSContentTypeVariant var = set
            .getContentVariantById(505);
      com.percussion.cms.objectstore.PSVariantSlotTypeSet slots = var
            .getVariantSlots();
      assertNotNull(slots.getLocator());
      Iterator<com.percussion.cms.objectstore.PSVariantSlotType> iter = slots
            .iterator();
      while (iter.hasNext())
      {
         com.percussion.cms.objectstore.PSVariantSlotType slot = iter.next();
         assertNotNull(slot.getLocator());
      }
   }

   /**
    * @throws Exception
    */
   @Test
   public void testFindBySlot() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = service.loadSlot(new PSGuid(PSTypeEnum.SLOT, 510));
      List<IPSAssemblyTemplate> templates = service.findTemplatesBySlot(slot);
      assertTrue(templates.size() > 0);
   }

   /**
    * @throws Exception
    */
   @Test
   public void testGetTemplatesByType() throws Exception
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      assertNotNull(service.findTemplateByNameAndType("rffPgEiGeneric",
            new PSGuid(PSTypeEnum.NODEDEF, 310)));
      try
      {
         // No template for this case
         service.findTemplateByNameAndType("NavImageLink", new PSGuid(
               PSTypeEnum.NODEDEF, 323));
      }
      catch (Exception e)
      {
         // Normal case
      }
   }
   
   /**
    * Test is variant case for template
    */
   @Test
   public void testIsVariant()
   {
      // Doug: A null value for the assembly plugin is fine for variants.
      final PSAssemblyTemplate template = new PSAssemblyTemplate();
      assertTrue(template.isVariant());
      template.setAssembler("assembler");
      assertEquals("assembler", template.getAssembler());
      assertFalse(template.isVariant());
   }
   
   /**
    * Required for JUnit4 tests to be run with Ant 1.6.5.
    * 
    * @return Adapter object which wraps existing class as JUnit4.
    */
   public static junit.framework.Test suite()
   {
      return new JUnit4TestAdapter(PSAssemblyTemplateTest.class);
   }
}
