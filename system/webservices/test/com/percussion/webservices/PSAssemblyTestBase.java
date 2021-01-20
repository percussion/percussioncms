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
package com.percussion.webservices;

import com.percussion.extension.IPSExtension;
import com.percussion.webservices.assembly.AssemblySOAPStub;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.assembly.data.TemplateType;
import com.percussion.webservices.assemblydesign.AssemblyDesignSOAPStub;
import com.percussion.webservices.assemblydesign.DeleteAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.DeleteSlotsRequest;
import com.percussion.webservices.assemblydesign.FindAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.FindSlotsRequest;
import com.percussion.webservices.assemblydesign.LoadAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.LoadSlotsRequest;
import com.percussion.webservices.assemblydesign.SaveAssemblyTemplatesRequest;
import com.percussion.webservices.assemblydesign.SaveSlotsRequest;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.rhythmyx.AssemblyLocator;
import com.percussion.webservices.rhythmyxdesign.AssemblyDesignLocator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.assertNotNull;

/**
 * Implements utilities used by all assembly test cases.
 */
public class PSAssemblyTestBase extends PSTestBase
{
   /**
    * Create a new binding for the assembly SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   protected AssemblySOAPStub getBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         AssemblyLocator locator = new AssemblyLocator();
         locator.setassemblySOAPEndpointAddress(getEndpoint("assemblySOAP"));

         AssemblySOAPStub binding = (AssemblySOAPStub) locator
            .getassemblySOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(600000);
         else
            binding.setTimeout(timeout);

         return binding;
      }
      catch (ServiceException e)
      {
         if (e.getLinkedCause() != null)
            e.getLinkedCause().printStackTrace();

         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + e);
      }
   }

   /**
    * Create a new binding for the assembly design SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   public static AssemblyDesignSOAPStub getDesignBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         AssemblyDesignLocator locator = new AssemblyDesignLocator();
         locator
            .setassemblyDesignSOAPEndpointAddress(getEndpoint("assemblyDesignSOAP"));

         AssemblyDesignSOAPStub binding = (AssemblyDesignSOAPStub) locator
            .getassemblyDesignSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);

         return binding;
      }
      catch (ServiceException e)
      {
         if (e.getLinkedCause() != null)
            e.getLinkedCause().printStackTrace();

         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + e);
      }
   }

   @BeforeClass
   public void setUp() throws Exception
   {

      deleteTestSlots(m_session);
      deleteTestTemplates(m_session);

      try
      {
         createTestSlots();
      }
      catch (Exception e)
      {
         // ignore, tests will fail
      }
      try
      {
         createTestTemplates();
      }
      catch (Exception e)
      {
         // ignore, tests will fail
      }
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   @After
   protected static void tearDown() throws Exception
   {
      deleteTestSlots(m_session);
      deleteTestTemplates(m_session);

      PSTestBase.tearDown();
   }

   /**
    * Creates all slots used for testing.
    * 
    * @throws Exception for any error.
    */
   protected void createTestSlots() throws Exception
   {
      m_eigerSlot = createSlot(ms_slotNames[0], true, m_session);
      m_jungfrauSlot = createSlot(ms_slotNames[1], true, m_session);
   }

   /**
    * Creates all templates used for testing.
    * 
    * @throws Exception for any error.
    */
   protected void createTestTemplates() throws Exception
   {
      m_eigerTemplate = createTemplate(ms_templateNames[0], "assembler",
         OutputFormatType.snippet, TemplateType.local, m_session);
      m_jungfrauTemplate = createTemplate(ms_templateNames[1],
         IPSExtension.LEGACY_ASSEMBLER, OutputFormatType.global,
         TemplateType.shared, m_session);
   }

   /**
    * Create a new slot for the supplied name.
    * 
    * @param name the slot name, may be <code>null</code> or empty.
    * @param save <code>true</code> to save the new slot to the repository,
    *    <code>false</code> to return the created slot unsaved.
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @return the new created slot, never <code>null</code>.
    * @throws Exception for any error creating the new slot.
    */
   public PSTemplateSlot createSlot(String name, boolean save, String session)
      throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      PSTemplateSlot[] slots = binding.createSlots(new String[] { name });

      if (save)
      {
         SaveSlotsRequest saveRequest = new SaveSlotsRequest();
         saveRequest.setPSTemplateSlot(slots);
         binding.saveSlots(saveRequest);
      }

      return slots[0];
   }

   /**
    * Lock all slots for the supplied ids.
    * 
    * @param ids the ids of the slots to lock, not <code>null</code> or
    *    empty.
    * @param session the session for which to lock the slots, not
    *    <code>null</code> or empty.
    * @return the locked slots, never <code>null</code> or empty.
    * @throws Exception for any error locking the slots.
    */
   public PSTemplateSlot[] lockSlots(long[] ids, String session)
      throws Exception
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      LoadSlotsRequest loadRequest = new LoadSlotsRequest();
      loadRequest.setId(ids);
      loadRequest.setLock(true);
      loadRequest.setOverrideLock(true);
      PSTemplateSlot[] slots = binding.loadSlots(loadRequest);

      return slots;
   }

   /**
    * Delete the supplied slot.
    * 
    * @param slot the slot to be deleted, may be <code>null</code>.
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @throws Exception for any error deleting the supplied slot.
    */
   protected void deleteSlot(PSTemplateSlot slot, String session)
      throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (slot != null)
      {
         AssemblyDesignSOAPStub binding = getDesignBinding(null);

         PSTestUtils.setSessionHeader(binding, session);

         long[] ids = new long[1];
         ids[0] = slot.getId();

         // lock slots
         lockSlots(ids, session);

         // now delete it
         DeleteSlotsRequest request = new DeleteSlotsRequest();
         request.setId(ids);
         binding.deleteSlots(request);
      }
   }

   /**
    * Create a new template for the supplied name.
    * 
    * @param name the template name, may be <code>null</code> or empty.
    * @param assembler the assember, may be <code>null</code> or empty.
    * @param outputFormat the template output format, not <code>null</code>.
    * @param type the template type, may be <code>null</code>.
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @return the new created template, never <code>null</code>.
    * @throws Exception for any error creating the new template.
    */
   public PSAssemblyTemplate createTemplate(String name, String assembler,
      OutputFormatType outputFormat, TemplateType type, String session)
      throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (StringUtils.isBlank(assembler))
         assembler = IPSExtension.LEGACY_ASSEMBLER;

      if (type == null)
         type = TemplateType.shared;

      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      PSAssemblyTemplate[] templates = binding
         .createAssemblyTemplates(new String[] { name });

      PSAssemblyTemplate template = templates[0];
      template.setLabel("label");
      template.setAssemblyUrl("assemblyUrl");
      template.setAssembler(assembler);
      template.setDescription("description");
      template.setOutputFormat(outputFormat);
      template.setTemplateType(type);

      SaveAssemblyTemplatesRequest saveRequest = new SaveAssemblyTemplatesRequest();
      saveRequest.setPSAssemblyTemplate(templates);
      binding.saveAssemblyTemplates(saveRequest);

      return templates[0];
   }

   /**
    * Lock all templates for the supplied ids.
    * 
    * @param ids the ids of the templates to lock, not <code>null</code> or
    *    empty.
    * @param session the session for which to lock the templates, not
    *    <code>null</code> or empty.
    * @return the locked templates, never <code>null</code> or empty.
    * @throws Exception for any error locking the templates.
    */
   protected PSAssemblyTemplate[] lockTemplates(long[] ids, String session)
      throws Exception
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      LoadAssemblyTemplatesRequest loadRequest = new LoadAssemblyTemplatesRequest();
      loadRequest.setId(ids);
      loadRequest.setLock(true);
      PSAssemblyTemplate[] templates = binding
         .loadAssemblyTemplates(loadRequest);

      return templates;
   }

   /**
    * Delete the supplied template.
    * 
    * @param template the template to be deleted, may be <code>null</code>.
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @throws Exception for any error deleting the supplied template.
    */
   public void deleteTemplate(PSAssemblyTemplate template, String session)
      throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (template != null)
      {
         AssemblyDesignSOAPStub binding = getDesignBinding(null);

         PSTestUtils.setSessionHeader(binding, session);

         long[] ids = new long[1];
         ids[0] = template.getId();

         // lock templates
         lockTemplates(ids, session);

         // now delete it
         DeleteAssemblyTemplatesRequest request = new DeleteAssemblyTemplatesRequest();
         request.setId(ids);
         binding.deleteAssemblyTemplates(request);
      }
   }

   /**
    * Delete all test slots.
    * 
    * @param session the session used to perform the deletes, not 
    *    <code>null</code> or empty.
    * @throws Exception for any error.
    */
   public static void deleteTestSlots(String session) throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      List<PSObjectSummary> objects = new ArrayList<PSObjectSummary>();

      FindSlotsRequest findRequest = new FindSlotsRequest();
      for (String name : ms_slotNames)
      {
         findRequest.setName(name);
         PSObjectSummary[] summaries = binding.findSlots(findRequest);
         if (summaries.length > 0)
            objects.add(summaries[0]);
      }

      if (objects.size() > 0)
      {
         long[] ids = new long[objects.size()];
         for (int i = 0; i < objects.size(); i++)
            ids[i] = objects.get(i).getId();

         DeleteSlotsRequest deleteRequest = new DeleteSlotsRequest();
         deleteRequest.setId(ids);
         binding.deleteSlots(deleteRequest);
      }
   }

   /**
    * Delete all test templates.
    * 
    * @param session the session used to perform the deletes, not 
    *    <code>null</code> or empty.
    * @throws Exception for any error.
    */
   public static void deleteTestTemplates(String session) throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      List<PSObjectSummary> objects = new ArrayList<PSObjectSummary>();

      FindAssemblyTemplatesRequest findRequest = new FindAssemblyTemplatesRequest();
      for (String name : ms_templateNames)
      {
         findRequest.setName(name);
         PSObjectSummary[] summaries = binding
            .findAssemblyTemplates(findRequest);
         if (summaries.length > 0)
            objects.add(summaries[0]);
      }

      if (objects.size() > 0)
      {
         long[] ids = new long[objects.size()];
         for (int i = 0; i < objects.size(); i++)
            ids[i] = objects.get(i).getId();

         DeleteAssemblyTemplatesRequest deleteRequest = new DeleteAssemblyTemplatesRequest();
         deleteRequest.setId(ids);
         binding.deleteAssemblyTemplates(deleteRequest);
      }
   }

   // test slots
   protected static final String[] ms_slotNames = { "eiger", "jungfrau" };

   protected PSTemplateSlot m_eigerSlot = null;

   protected PSTemplateSlot m_jungfrauSlot = null;

   // test templates
   protected static final String[] ms_templateNames = { "eiger", "jungfrau" };

   protected PSAssemblyTemplate m_eigerTemplate = null;

   protected PSAssemblyTemplate m_jungfrauTemplate = null;
}
