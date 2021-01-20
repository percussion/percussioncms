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

package com.percussion.webservices.rhythmyx;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSAssemblyTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.assembly.AssemblySOAPStub;
import com.percussion.webservices.assembly.LoadAssemblyTemplatesRequest;
import com.percussion.webservices.assembly.LoadSlotsRequest;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.rmi.RemoteException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AssemblyTestCase extends PSAssemblyTestBase
{
   @Test
   public void test1assemblySOAPLoadSlots() throws Exception
   {
      AssemblySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadSlotsRequest request = null;
         PSTemplateSlot[] slots = null;

         // try to load all slots without rhythmyx session
         try
         {
            request = new LoadSlotsRequest();
            request.setName(null);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load all slots with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadSlotsRequest();
            request.setName(null);
            binding.loadSlots(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load all slots
         request = new LoadSlotsRequest();
         request.setName(null);
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length > 0);

         int count = slots.length;

         request = new LoadSlotsRequest();
         request.setName(" ");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == count);

         request = new LoadSlotsRequest();
         request.setName("*");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == count);

         // try to load a non-existing slot
         request = new LoadSlotsRequest();
         request.setName("someslot");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 0);

         // load test slots
         request = new LoadSlotsRequest();
         request.setName("eiger*");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 1);

         request = new LoadSlotsRequest();
         request.setName("EIGER");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 1);

         request = new LoadSlotsRequest();
         request.setName("*frau");
         slots = binding.loadSlots(request);
         assertTrue(slots != null && slots.length == 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   @Test
   public void test2assemblySOAPLoadAssemblyTemplates() throws Exception
   {
      AssemblySOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         LoadAssemblyTemplatesRequest request = null;
         PSAssemblyTemplate[] templates = null;

         // try to load all templates without rhythmyx session
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setName(null);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load all templates with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadAssemblyTemplatesRequest();
            request.setName(null);
            binding.loadAssemblyTemplates(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load all templates
         request = new LoadAssemblyTemplatesRequest();
         request.setName(null);
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length > 0);

         int count = templates.length;

         request = new LoadAssemblyTemplatesRequest();
         request.setName(" ");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == count);

         request = new LoadAssemblyTemplatesRequest();
         request.setName("*");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == count);

         // try to load a non-existing template
         request = new LoadAssemblyTemplatesRequest();
         request.setName("sometemplate");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 0);

         // load test templates
         request = new LoadAssemblyTemplatesRequest();
         request.setName("eiger");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         request = new LoadAssemblyTemplatesRequest();
         request.setName("EIGER");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         request = new LoadAssemblyTemplatesRequest();
         request.setName("*frau");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);

         // load template with 1 (or more) site reference
         request = new LoadAssemblyTemplatesRequest();
         //want a difference in case of the name to test case-insensitivity
         request.setName("rffpgeigeneric");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);
         assertTrue(templates[0].getSites().length > 0);

         // load template with 2 (or more) site references
         request = new LoadAssemblyTemplatesRequest();
         request.setName("rffPgCalendarMonth");
         templates = binding.loadAssemblyTemplates(request);
         assertTrue(templates != null && templates.length == 1);
         assertTrue(templates[0].getSites().length > 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }
}
