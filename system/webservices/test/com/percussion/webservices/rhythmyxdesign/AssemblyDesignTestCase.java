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

package com.percussion.webservices.rhythmyxdesign;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSAssemblyTestBase;
import com.percussion.webservices.PSContentTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.AssemblySOAPStub;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.assembly.data.PSTemplateSlotAllowedContentContent;
import com.percussion.webservices.assembly.data.PSTemplateSlotArgumentsArgument;
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
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.content.PSContentTemplateDesc;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import org.junit.experimental.categories.Category;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AssemblyDesignTestCase extends PSAssemblyTestBase
{
   public void test1assemblyDesignSOAPCreateSlots() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // try to get create a slot without rhythmyx session
         try
         {
            createSlot(binding, "eiger");
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a slot with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            createSlot(binding, "eiger");
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create a slot with a null name
         try
         {
            createSlot(binding, null);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a slot with an empty name
         try
         {
            createSlot(binding, " ");
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a slot with a name that contains spaces
         try
         {
            createSlot(binding, "eiger nordwand");
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create slot "testcreate"
         PSTemplateSlot testcreate = createSlot(binding, "testcreate");

         assertTrue(testcreate != null);
         PSTemplateSlot[] saveSlots = new PSTemplateSlot[1];
         saveSlots[0] = testcreate;
         SaveSlotsRequest saveRequest = new SaveSlotsRequest();
         saveRequest.setPSTemplateSlot(saveSlots);
         saveRequest.setRelease(false);
         binding.saveSlots(saveRequest);

         // update and save again
         testcreate.setDescription(testcreate.getDescription() + " - changed");
         binding.saveSlots(saveRequest);

         // try to create a second slot "testcreate"
         try
         {
            createSlot(binding, "TESTcreate");
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // cleanup created slot
         deleteSlot(testcreate, session);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      catch (PSContractViolationFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   private PSTemplateSlot createSlot(AssemblyDesignSOAPStub binding, String name)
      throws Exception
   {
      PSTemplateSlot[] slots = binding.createSlots(new String[] { name });
      return slots[0];
   }

   public void test2assemblyDesignSOAPFindSlots() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindSlotsRequest request = null;
         PSObjectSummary[] slots = null;

         // try to find all slots without rhythmyx session
         try
         {
            request = new FindSlotsRequest();
            request.setName(null);
            binding.findSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all slots with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new FindSlotsRequest();
            request.setName(null);
            binding.findSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // find all slots
         request = new FindSlotsRequest();
         request.setName(null);
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length > 0);

         int count = slots.length;

         request = new FindSlotsRequest();
         request.setName(" ");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == count);

         request = new FindSlotsRequest();
         request.setName("*");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == count);

         // try to find a non-existing slot
         request = new FindSlotsRequest();
         request.setName("someslot");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 0);

         // find test slots
         request = new FindSlotsRequest();
         request.setName("eiger*");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 1);
         assertTrue(slots[0].getLocked() == null);

         request = new FindSlotsRequest();
         request.setName("EIGER");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 1);

         request = new FindSlotsRequest();
         request.setName("*frau");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 1);

         // lock test slots
         long[] lockIds = new long[2];
         lockIds[0] = m_eigerSlot.getId();
         lockIds[1] = m_jungfrauSlot.getId();
         lockSlots(lockIds, session);

         // find test slots
         request = new FindSlotsRequest();
         request.setName("eiger*");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 1);
         assertTrue(slots[0].getLocked() != null);

         // release locked objects
         PSTestUtils.releaseLocks(session, lockIds);

         // find inline slots
         request = new FindSlotsRequest();
         request.setName("Inline*");
         slots = binding.findSlots(request);
         assertTrue(slots != null && slots.length == 0);

         /*
          // find inline slots filtered for template id
          request = new FindSlotsRequest();
          request.setName("Inline*");
          PSDesignGuid templateId = new PSDesignGuid(PSTypeEnum.TEMPLATE, 324);
          request.setAssociatedTemplateId(templateId.getValue());
          slots = binding.findSlots(request);
          assertTrue(slots != null && slots.length == 2);

          // find inline slots filtered for template id
          request = new FindSlotsRequest();
          request.setName("Inline*");
          templateId = new PSDesignGuid(PSTypeEnum.TEMPLATE, 407);
          request.setAssociatedTemplateId(templateId.getValue());
          slots = binding.findSlots(request);
          assertTrue(slots != null && slots.length == 1);
          */
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void test3assemblyDesignSOAPLoadSlots() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[2];
         ids[0] = m_eigerSlot.getId();
         ids[1] = m_jungfrauSlot.getId();

         LoadSlotsRequest request = null;
         PSTemplateSlot[] slots = null;

         // try to load slots without rhythmyx session
         try
         {
            request = new LoadSlotsRequest();
            request.setId(ids);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load slots with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new LoadSlotsRequest();
            request.setId(ids);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to load slots with null ids
         try
         {
            request = new LoadSlotsRequest();
            request.setId(null);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load slots with empty ids
         try
         {
            request = new LoadSlotsRequest();
            request.setId(new long[0]);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load slots with invalid ids
         try
         {
            long[] invalidIds = new long[3];
            invalidIds[0] = m_eigerSlot.getId();
            invalidIds[1] = m_jungfrauSlot.getId();
            invalidIds[2] = m_jungfrauSlot.getId() + 10;

            request = new LoadSlotsRequest();
            request.setId(invalidIds);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, 2, PSTemplateSlot.class.getName());
         }

         // load slots read-only
         request = new LoadSlotsRequest();
         request.setId(ids);
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 2);

         // load slots read-writable
         request = new LoadSlotsRequest();
         request.setId(ids);
         request.setLock(true);
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 2);

         // load locked slots read-writable with locking session
         request = new LoadSlotsRequest();
         request.setId(ids);
         request.setLock(true);
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 2);

         // try to load locked slots read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadSlotsRequest();
            request.setId(ids);
            request.setLock(true);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSAssemblyTemplate.class.getName());
         }

         // release locked objects
         PSTestUtils.releaseLocks(session, ids);
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }
   }

   /**
    * Tests save and update slot with child data.
    * 
    * throws Exception if any error occurs.
    */
   public void testUpdateSlotsChildData() throws Exception
   {
      AssemblySOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      AssemblyDesignSOAPStub dsBinding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(dsBinding, m_session);

      cleanupSlot(binding, "testingSlot");

      // create a lot with 1 argument
      PSTemplateSlot slot = createSlot(dsBinding, "testingSlot");
      PSTemplateSlotArgumentsArgument[] args = new PSTemplateSlotArgumentsArgument[1];
      args[0] = new PSTemplateSlotArgumentsArgument();
      args[0].set_value("psx_s_link");
      args[0].setName("template");
      slot.setArguments(args);

      PSTemplateSlotAllowedContentContent[] associate = new PSTemplateSlotAllowedContentContent[1];
      associate[0] = new PSTemplateSlotAllowedContentContent(
         getContentTypeGuidLong(304), getTemplateGuidLong(327));
      slot.setAllowedContent(associate);

      saveSlot(dsBinding, slot, true);

      // validate there is 1 argument
      slot = loadSlot(dsBinding, slot.getId());
      assertTrue(slot.getArguments().length == 1);
      assertTrue(slot.getArguments()[0].getName().equals("template"));
      assertTrue(slot.getArguments()[0].get_value().equals("psx_s_link"));
      associate = slot.getAllowedContent();
      assertTrue(associate.length == 1);
      assertTrue(associate[0].getContentTypeId() == getContentTypeGuidLong(304));
      assertTrue(associate[0].getTemplateId() == getTemplateGuidLong(327));

      // modify the value of the argument
      slot.getArguments()[0].set_value("psx_s_link_modified");
      slot.getAllowedContent()[0].setTemplateId(getTemplateGuidLong(395));
      saveSlot(dsBinding, slot, true);

      // validate the saved new value of the argument
      slot = loadSlot(dsBinding, slot.getId());
      assertTrue(slot.getArguments().length == 1);
      assertTrue(slot.getArguments()[0].getName().equals("template"));
      assertTrue(slot.getArguments()[0].get_value().equals(
         "psx_s_link_modified"));
      associate = slot.getAllowedContent();
      assertTrue(associate.length == 1);
      assertTrue(associate[0].getContentTypeId() == getContentTypeGuidLong(304));
      assertTrue(associate[0].getTemplateId() == getTemplateGuidLong(395));

      // remove the argument
      slot.setArguments(null);
      slot.setAllowedContent(null);
      saveSlot(dsBinding, slot, true);

      // validate the slot without argument
      slot = loadSlot(dsBinding, slot.getId());
      assertTrue(slot.getArguments().length == 0);
      assertTrue(slot.getAllowedContent().length == 0);

      cleanupSlot(binding, "testingSlot");
   }

   private long getContentTypeGuidLong(int id)
   {
      return new PSDesignGuid(new PSGuid(PSTypeEnum.NODEDEF, id)).getValue();
   }

   private long getTemplateGuidLong(int id)
   {
      return new PSDesignGuid(new PSGuid(PSTypeEnum.TEMPLATE, id)).getValue();
   }

   /**
    * Removes the specified slot if exists, do nothing if no such slot exists.
    * 
    * @param binding the object used to communicate with the server, assumed
    *    not <code>null</code>.
    * @param slotName the name of the to be removed slot, assumed not empty.
    * 
    * @throws Exception if any error occurs.
    */
   private void cleanupSlot(AssemblySOAPStub binding, String slotName)
      throws Exception
   {
      com.percussion.webservices.assembly.LoadSlotsRequest request = null;
      PSTemplateSlot[] slots = null;

      request = new com.percussion.webservices.assembly.LoadSlotsRequest();
      request.setName(slotName);
      slots = binding.loadSlots(request);

      if (slots.length == 1)
      {
         deleteSlot(slots[0], m_session);
      }
   }

   private void saveSlot(AssemblyDesignSOAPStub dsBinding, PSTemplateSlot slot,
      boolean isReleaseLock) throws Exception
   {
      SaveSlotsRequest saveReq = new SaveSlotsRequest();
      saveReq.setPSTemplateSlot(new PSTemplateSlot[] { slot });
      saveReq.setRelease(isReleaseLock);
      dsBinding.saveSlots(saveReq);
   }

   private PSTemplateSlot loadSlot(AssemblyDesignSOAPStub dsBinding, long slotId)
      throws Exception
   {
      LoadSlotsRequest lsReq = new LoadSlotsRequest();
      lsReq.setId(new long[] { slotId });
      lsReq.setLock(true);
      lsReq.setOverrideLock(true);
      return dsBinding.loadSlots(lsReq)[0];
   }

   public void test4assemblyDesignSOAPSaveSlots() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         PSTemplateSlot[] slots = new PSTemplateSlot[2];
         slots[0] = m_eigerSlot;
         slots[1] = m_jungfrauSlot;

         SaveSlotsRequest request = null;

         // try to save slots without rhythmyx session
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(slots);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save slots with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(slots);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save slots with null slots
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(null);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save slots with empty slots
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(new PSTemplateSlot[0]);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save slots in read-only mode
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(slots);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         // lock slots
         long[] lockIds = new long[2];
         lockIds[0] = m_eigerSlot.getId();
         lockIds[1] = m_jungfrauSlot.getId();
         lockSlots(lockIds, session);

         // save locked slots, do not release
         request = new SaveSlotsRequest();
         request.setPSTemplateSlot(slots);
         request.setRelease(false);
         binding.saveSlots(request);

         // load, modify and resave slots
         LoadSlotsRequest loadRequest = new LoadSlotsRequest();
         loadRequest.setId(lockIds);
         loadRequest.setLock(true);
         slots = binding.loadSlots(loadRequest);
         for (PSTemplateSlot slot : slots)
            slot.setDescription("New description");
         request = new SaveSlotsRequest();
         request.setPSTemplateSlot(slots);
         request.setRelease(false);
         binding.saveSlots(request);

         // save locked slots and release
         request = new SaveSlotsRequest();
         request.setPSTemplateSlot(slots);
         request.setRelease(true);
         binding.saveSlots(request);

         // try to save slots in read-only mode
         try
         {
            request = new SaveSlotsRequest();
            request.setPSTemplateSlot(slots);
            binding.saveSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }
   }

   public void test5assemblyDesignSOAPDeleteSlots() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[2];
         ids[0] = m_eigerSlot.getId();
         ids[1] = m_jungfrauSlot.getId();

         DeleteSlotsRequest request = null;

         // try to delete slots without rhythmyx session
         try
         {
            request = new DeleteSlotsRequest();
            request.setId(ids);
            binding.deleteSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete slots with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new DeleteSlotsRequest();
            request.setId(ids);
            binding.deleteSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to delete slots with null ids
         try
         {
            request = new DeleteSlotsRequest();
            request.setId(null);
            binding.deleteSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete slots with empty ids
         try
         {
            request = new DeleteSlotsRequest();
            request.setId(new long[0]);
            binding.deleteSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // lock slots for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         lockSlots(ids, session2);

         // try to delete slots locked by somebody else
         try
         {
            request = new DeleteSlotsRequest();
            request.setId(ids);
            binding.deleteSlots(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            assertTrue(calls != null && calls.length == 2);
            int index = 0;
            for (PSErrorsFaultServiceCall call : calls)
            {
               PSErrorsFaultServiceCallError error = call.getError();
               assertTrue(error != null);
               if (index == 0)
                  assertTrue(error.getId() == m_eigerSlot.getId());
               else
                  assertTrue(error.getId() == m_jungfrauSlot.getId());

               index++;
            }
         }

         // release locked objects
         PSTestUtils.releaseLocks(session2, ids);

         // lock one slot for admin1, leave the other slot unlocked
         lockSlots(new long[] { m_eigerSlot.getId() }, session);

         // delete all slots
         request = new DeleteSlotsRequest();
         request.setId(ids);
         binding.deleteSlots(request);
         m_eigerSlot = null;
         m_jungfrauSlot = null;

         // delete non-existing slots
         request = new DeleteSlotsRequest();
         request.setId(ids);
         binding.deleteSlots(request);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void test6assemblyDesignSOAPCreateAssemblyTemplate() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         // try to create a template without rhythmyx session
         try
         {
            binding.createAssemblyTemplates(new String[] { "eiger" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a template with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            binding.createAssemblyTemplates(new String[] { "eiger" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create a template with a null name
         try
         {
            binding.createAssemblyTemplates(new String[] { null });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a template with an empty name
         try
         {
            binding.createAssemblyTemplates(new String[] { " " });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a template with a name that contains spaces
         try
         {
            binding.createAssemblyTemplates(new String[] { "eiger nordwand" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create template "testcreate"
         PSAssemblyTemplate testcreate = createTemplate(binding,
               "testcreate", "label");
         
         saveTemplate(binding, testcreate, false);

         // update and save again
         testcreate.setDescription(testcreate.getDescription() + " - changed");
         StringBuilder template = new StringBuilder("Line 1");
         template.append("\n");
         template.append("Line 2");
         template.append("\n");
         template.append("Line 3");
         saveTemplate(binding, testcreate, false);

         // verify that whitespace is preserved for the template element
         LoadAssemblyTemplatesRequest loadRequest = new LoadAssemblyTemplatesRequest();
         loadRequest.setId(new long[] { testcreate.getId() });
         PSAssemblyTemplate[] templates = binding
            .loadAssemblyTemplates(loadRequest);
         assertTrue(templates.length == 1);
         assertTrue(templates[0].getTemplate().equals(template.toString()));

         // try to create a second template "testcreate"
         try
         {
            binding.createAssemblyTemplates(new String[] { "TESTcreate" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // cleanup
         deleteTemplate(testcreate, m_session);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public static PSAssemblyTemplate createTemplate(
         AssemblyDesignSOAPStub binding, String name, String label)
      throws Exception
   {
      PSAssemblyTemplate testCreate = binding
            .createAssemblyTemplates(new String[] { name })[0];
      assertTrue(testCreate != null);
      testCreate.setLabel(label);
      testCreate.setAssemblyUrl("assemblyUrl");
      testCreate.setAssembler("assembler");
      testCreate.setDescription("description for " + name);

      // save created template
      assertTrue(testCreate != null);
      return testCreate;
   }

   public static void saveTemplate(AssemblyDesignSOAPStub binding,
         PSAssemblyTemplate template, boolean releaseLock) throws Exception
   {
      PSAssemblyTemplate[] saveTemplates = new PSAssemblyTemplate[1];
      saveTemplates[0] = template;
      SaveAssemblyTemplatesRequest saveRequest = new SaveAssemblyTemplatesRequest();
      saveRequest.setPSAssemblyTemplate(saveTemplates);
      saveRequest.setRelease(releaseLock);
      binding.saveAssemblyTemplates(saveRequest);
   }
   
   public void test7assemblyDesignSOAPFindAssemblyTemplates() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindAssemblyTemplatesRequest request = null;
         PSObjectSummary[] templates = null;

         // try to find all templates without rhythmyx session
         try
         {
            request = new FindAssemblyTemplatesRequest();
            request.setName(null);
            binding.findAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all templates with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new FindAssemblyTemplatesRequest();
            request.setName(null);
            binding.findAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // find all templates
         request = new FindAssemblyTemplatesRequest();
         request.setName(null);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length > 0);

         int count = templates.length;

         request = new FindAssemblyTemplatesRequest();
         request.setName(" ");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == count);

         request = new FindAssemblyTemplatesRequest();
         request.setName("*");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == count);

         // find all FF snippets
         request = new FindAssemblyTemplatesRequest();
         request.setName("rffSn*");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length > 0);
         count = templates.length;

         // find all FF snippets for the Generic content type
         request = new FindAssemblyTemplatesRequest();
         request.setName("rffSn*");
         request.setContentType("rffGeneric");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length < count);
         count = templates.length;

         // find all FF snippets for the Generic content type of
         // output format page
         OutputFormatType[] outputFormatTypes = new OutputFormatType[1];
         outputFormatTypes[0] = OutputFormatType.page;
         request = new FindAssemblyTemplatesRequest();
         request.setName("rffSn*");
         request.setContentType("rffGeneric");
         request.setOutputFormats(outputFormatTypes);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 0);

         // find all FF snippets for the Generic content type of
         // output format page or snippet
         outputFormatTypes = new OutputFormatType[2];
         outputFormatTypes[0] = OutputFormatType.page;
         outputFormatTypes[1] = OutputFormatType.snippet;
         request = new FindAssemblyTemplatesRequest();
         request.setName("rffSn*");
         request.setContentType("rffGeneric");
         request.setOutputFormats(outputFormatTypes);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == count);

         // test template type filter, get all local templates
         request = new FindAssemblyTemplatesRequest();
         request.setName("*");
         request.setTemplateType(TemplateType.local);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 44);

         // test template type filter, get all shared templates
         request = new FindAssemblyTemplatesRequest();
         request.setName("*");
         request.setTemplateType(TemplateType.shared);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 7);

         // test global filter
         request = new FindAssemblyTemplatesRequest();
         request.setName("eiger*");
         request.setGlobalFilter(true);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 0);
         request.setGlobalFilter(false);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         // test legacy filter
         request = new FindAssemblyTemplatesRequest();
         request.setName("*frau");
         request.setLegacyFilter(true);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);
         request.setLegacyFilter(false);
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 0);

         // try to find a non-existing template
         request = new FindAssemblyTemplatesRequest();
         request.setName("sometemplate");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 0);

         // find test templates
         request = new FindAssemblyTemplatesRequest();
         request.setName("eiger*");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);
         assertTrue(templates[0].getLocked() == null);

         request = new FindAssemblyTemplatesRequest();
         request.setName("EIGER");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         request = new FindAssemblyTemplatesRequest();
         request.setName("*frau");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         // lock test templates
         long[] lockIds = new long[2];
         lockIds[0] = m_eigerTemplate.getId();
         lockIds[1] = m_jungfrauTemplate.getId();
         lockTemplates(lockIds, session);

         // find test templates
         request = new FindAssemblyTemplatesRequest();
         request.setName("eiger*");
         templates = binding.findAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);
         assertTrue(templates[0].getLocked() != null);

         // release locked objects
         PSTestUtils.releaseLocks(session, lockIds);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void test8assemblyDesignSOAPLoadAssemblyTemplates() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[2];
         ids[0] = m_eigerTemplate.getId();
         ids[1] = m_jungfrauTemplate.getId();

         LoadAssemblyTemplatesRequest request = null;
         PSAssemblyTemplate[] templates = null;

         // try to load templates without rhythmyx session
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setId(ids);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load templates with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setId(ids);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to load templates with null ids
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setId(null);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load templates with empty ids
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setId(new long[0]);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load templates with invalid ids
         try
         {
            long[] invalidIds = new long[3];
            invalidIds[0] = m_eigerTemplate.getId();
            invalidIds[1] = m_jungfrauTemplate.getId();
            invalidIds[2] = 0;

            request = new LoadAssemblyTemplatesRequest();
            request.setId(invalidIds);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, 2, PSAssemblyTemplate.class.getName());
         }

         // load templates read-only
         request = new LoadAssemblyTemplatesRequest();
         request.setId(ids);
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 2);

         // load templates read-writable
         request = new LoadAssemblyTemplatesRequest();
         request.setId(ids);
         request.setLock(true);
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 2);

         // load locked templates read-writable with locking session
         request = new LoadAssemblyTemplatesRequest();
         request.setId(ids);
         request.setLock(true);
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 2);

         // try to load locked templates read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setId(ids);
            request.setLock(true);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSAssemblyTemplate.class.getName());
         }

         // release locked objects
         PSTestUtils.releaseLocks(session, ids);
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void test9assemblyDesignSOAPSaveAssemblyTemplates() throws Exception
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         PSAssemblyTemplate[] templates = new PSAssemblyTemplate[2];
         templates[0] = m_eigerTemplate;
         templates[1] = m_jungfrauTemplate;

         SaveAssemblyTemplatesRequest request = null;

         // try to save templates without rhythmyx session
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(templates);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save templates with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(templates);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save templates with null templates
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(null);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save templates with empty templates
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(new PSAssemblyTemplate[0]);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save templates in read-only mode
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(templates);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         // lock templates
         long[] lockIds = new long[2];
         lockIds[0] = m_eigerTemplate.getId();
         lockIds[1] = m_jungfrauTemplate.getId();
         lockTemplates(lockIds, session);

         // save locked templates, do not release
         request = new SaveAssemblyTemplatesRequest();
         request.setPSAssemblyTemplate(templates);
         request.setRelease(false);
         binding.saveAssemblyTemplates(request);

         // update with new site and slot associations, keep locks
         Reference site301 = new Reference(new PSDesignGuid(PSTypeEnum.SITE,
            301).getValue(), "");
         Reference site303 = new Reference(new PSDesignGuid(PSTypeEnum.SITE,
            303).getValue(), "");
         templates[0].setSites(new Reference[] { site301, site303 });
         Reference jungfrauSlot = new Reference(m_jungfrauSlot.getId(),
            m_jungfrauSlot.getName());
         Reference eigerSlot = new Reference(m_eigerSlot.getId(), m_eigerSlot
            .getName());
         templates[1].setSlots(new Reference[] { jungfrauSlot, eigerSlot });
         request = new SaveAssemblyTemplatesRequest();
         request.setPSAssemblyTemplate(templates);
         request.setRelease(false);
         binding.saveAssemblyTemplates(request);

         // verify site and slot associations
         LoadAssemblyTemplatesRequest loadRequest = new LoadAssemblyTemplatesRequest();
         loadRequest.setId(lockIds);
         templates = binding.loadAssemblyTemplates(loadRequest);
         assertTrue(templates != null && templates.length == 2);
         assertTrue(templates[0].getSites().length == 2);
         assertTrue(templates[1].getSlots().length == 2);

         // update changed site and slot associations, release locks
         templates[0].setSites(new Reference[] { site301 });
         templates[1].setSlots(new Reference[] { jungfrauSlot });
         request = new SaveAssemblyTemplatesRequest();
         request.setPSAssemblyTemplate(templates);
         request.setRelease(true);
         binding.saveAssemblyTemplates(request);

         // verify site and slot associations
         loadRequest = new LoadAssemblyTemplatesRequest();
         loadRequest.setId(lockIds);
         templates = binding.loadAssemblyTemplates(loadRequest);
         assertTrue(templates != null && templates.length == 2);
         assertTrue(templates[0].getSites().length == 1);
         assertTrue(templates[1].getSlots().length == 1);

         // try to save templates in read-only mode
         try
         {
            request = new SaveAssemblyTemplatesRequest();
            request.setPSAssemblyTemplate(templates);
            binding.saveAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }
      }
      catch (PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void test10assemblyDesignSOAPDeleteAssemblyTemplates()
         throws Exception 
   {
      AssemblyDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = new long[2];
         ids[0] = m_eigerTemplate.getId();
         ids[1] = m_jungfrauTemplate.getId();

         DeleteAssemblyTemplatesRequest request = null;

         // try to delete templates without rhythmyx session
         try
         {
            request = new DeleteAssemblyTemplatesRequest();
            request.setId(ids);
            binding.deleteAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault success)
         {}

         // try to delete templates with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "jungfrau");
         try
         {
            request = new DeleteAssemblyTemplatesRequest();
            request.setId(ids);
            binding.deleteAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault success)
         {}

         PSTestUtils.setSessionHeader(binding, session);

         // try to delete templates with null ids
         try
         {
            request = new DeleteAssemblyTemplatesRequest();
            request.setId(null);
            binding.deleteAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault success)
         {}

         // try to delete templates with empty ids
         try
         {
            request = new DeleteAssemblyTemplatesRequest();
            request.setId(new long[0]);
            binding.deleteAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault success)
         {}

         // lock templates for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         lockTemplates(ids, session2);

         // try to delete templates locked by somebody else
         try
         {
            request = new DeleteAssemblyTemplatesRequest();
            request.setId(ids);
            binding.deleteAssemblyTemplates(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            assertTrue(calls != null && calls.length == 2);
            int index = 0;
            for (PSErrorsFaultServiceCall call : calls)
            {
               PSErrorsFaultServiceCallError error = call.getError();
               assertTrue(error != null);
               if (index == 0)
                  assertTrue(error.getId() == m_eigerTemplate.getId());
               else
                  assertTrue(error.getId() == m_jungfrauTemplate.getId());

               index++;
            }
         }

         // release locked objects
         PSTestUtils.releaseLocks(session2, ids);

         // lock one template for admin1, leave the other template unlocked
         lockTemplates(new long[] { m_eigerTemplate.getId() }, session);

         //add content type association to m_eigerTemplate
         PSContentTestBase ctypeTestCase = new ContentDesignTestCase();
         
         PSObjectSummary[] ctypes = ctypeTestCase.findContentTypes(null,
               session);
         assertTrue("Must have at least 2 content types for this test", 
               ctypes.length > 1);
         long ctypeId;
         PSDesignGuid folderGuid = new PSDesignGuid(PSTypeEnum.NODEDEF, 101L);
         if (ctypes[0].getId() == folderGuid.getValue())
            //don't use folder because it's a special content type
            ctypeId = ctypes[1].getId();
         else
            ctypeId = ctypes[0].getId();
         ctypeTestCase.loadTemplateAssociations(ctypeId, session, true);
         ctypeTestCase.saveTemplateAssociations(ctypeId,
               new long[] { m_eigerTemplate.getId() }, session, true);
         
         // delete all templates
         request = new DeleteAssemblyTemplatesRequest();
         request.setId(ids);
         binding.deleteAssemblyTemplates(request);
         m_eigerTemplate = null;
         m_jungfrauTemplate = null;

         //make sure we can still load the associated template
         ctypeTestCase.loadContentTypes(new long[] {ctypeId}, session, false);
         List<PSContentTemplateDesc> assoc = 
            ctypeTestCase.loadTemplateAssociations(ctypeId, session, false);
         assertTrue(assoc.size() == 0);
         
         // delete non-existing templates
         request = new DeleteAssemblyTemplatesRequest();
         request.setId(ids);
         binding.deleteAssemblyTemplates(request);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }
}
