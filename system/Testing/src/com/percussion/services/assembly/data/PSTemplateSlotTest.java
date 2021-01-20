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
package com.percussion.services.assembly.data;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test basic CRUD on a template slot
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSTemplateSlotTest
{
   /**
    * Test slot name
    */
   private static final String USER_SLOT_1 = "user_slot_1";

   public PSTemplateSlotTest() {
      super();
   }

   /**
    * Test to and from xml methods for round trip accuracy
    * @throws Exception
    */
   @Test
   public void testSerialization() throws Exception
   {
      PSTemplateSlot slot = new PSTemplateSlot();
      slot.setFinderName("test1");
      slot.setDescription("My test slot");
      slot.setSystemSlot(true);
      slot.setGUID(new PSGuid(PSTypeEnum.SLOT, 123));
      slot.setName(USER_SLOT_1);
      slot.setLabel(USER_SLOT_1);
      String xml = slot.toXML();
      PSTemplateSlot copy = new PSTemplateSlot();
      copy.fromXML(xml);
      assertEquals(slot, copy);
   }

   /**
    * @throws Exception
    */
   @Test
   public void testSerializationWithParams() throws Exception
   {
      PSTemplateSlot slot = new PSTemplateSlot();
      setupSlot(slot);
      slot.setGUID(new PSGuid(PSTypeEnum.SLOT, 123));
      String xml = slot.toXML();
      PSTemplateSlot copy = new PSTemplateSlot();
      copy.fromXML(xml);
      assertEquals(slot, copy);
   }

   /**
    * @param slot
    */
   @SuppressWarnings("unchecked")
   private void setupSlot(IPSTemplateSlot slot)
   {
      slot.setFinderName("test1");
      slot.setDescription("My test slot");
      slot.setSystemSlot(true);
      slot.setName(USER_SLOT_1);
      slot.setLabel(USER_SLOT_1);
      Map<String,String> args = new HashMap<String,String>();
      args.put("a", "one");
      args.put("b", "two");
      slot.setFinderArguments(args);
      Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      slotAssociations.add(new PSPair(new PSGuid(PSTypeEnum.NODEDEF, 1000),
            new PSGuid(PSTypeEnum.TEMPLATE, 1001)));
      slotAssociations.add(new PSPair(new PSGuid(PSTypeEnum.NODEDEF, 1002),
            new PSGuid(PSTypeEnum.TEMPLATE, 1003)));
      slot.setSlotAssociations(slotAssociations);
   }

   /**
    * @throws Exception
    */
   public void testCRUD() throws Exception
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = assembly.createSlot();
      setupSlot(slot);
      assembly.saveSlot(slot);

      // load non-cached slot instances
      slot = assembly.loadSlotModifiable(slot.getGUID());
      IPSTemplateSlot nonCachedSlot = assembly.loadSlotModifiable(slot
            .getGUID());
      assertTrue(slot != nonCachedSlot);

      // load cached slot instances
      slot = assembly.loadSlot(slot.getGUID());
      IPSTemplateSlot cachedSlot = assembly.loadSlot(slot.getGUID());
      assertTrue(slot == cachedSlot);
      cachedSlot = assembly.findSlot(slot.getGUID());
      assertTrue(slot == cachedSlot);
      
      IPSTemplateSlot second = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(slot, second);
      assertEquals(slot.hashCode(), second.hashCode());
      Map<String,String> args = slot.getFinderArguments();
      args.put("c", "three");
      second.setFinderArguments(args);
      assertNotSame(slot, second);
      assembly.saveSlot(second);
      slot = assembly.findSlotByName(USER_SLOT_1);
      args = slot.getFinderArguments();
      args.remove("a");
      second.setFinderArguments(args);
      assembly.saveSlot(slot);
      
      slot = assembly.findSlot(slot.getGUID());
      assertNotNull(slot);

      assembly.deleteSlot(slot.getGUID());

      slot = assembly.findSlot(slot.getGUID());
      assertNull(slot);
}

   /**
    * @throws Exception
    */
   @Test
   public void testLoadExisting() throws Exception
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = assembly.findSlotByName("rffAutoIndex");
      String xml = slot.toXML();
      IPSTemplateSlot compare = assembly.createSlot();
      compare.fromXML(xml);
      assertEquals(slot, compare);
   }

   /**
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testFindArgumentChanges() throws Exception
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = assembly.createSlot();
      setupSlot(slot);
      assertEquals(2, slot.getFinderArguments().size());
      assembly.saveSlot(slot);
      
      // add a new finder argument      
      slot.addFinderArgument("f1", "f1_value");
      assembly.saveSlot(slot);
      // verify the added one
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(3, slot.getFinderArguments().size());

      // remove the added finder argument
      slot.removeFinderArgument("f1");
      assembly.saveSlot(slot);
      // verify the removed finder argument
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(2, slot.getFinderArguments().size());
      
      // replace the finder arguments
      slot.removeFinderArgument("a");
      slot.removeFinderArgument("b");
      slot.addFinderArgument("a", "a new value");
      slot.addFinderArgument("b", "b new value");
      assembly.saveSlot(slot);
      // verify the replaced finder argument
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(2, slot.getFinderArguments().size());      
      
      // empty the finder argument
      slot.removeFinderArgument("a");
      slot.removeFinderArgument("b");
      assertEquals(0, slot.getFinderArguments().size());
      assembly.saveSlot(slot);
      // verify the empty finder argument
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(0, slot.getFinderArguments().size());
      
      //\/\/\/\/\/\/\/\/\/\/\/\/
      // test setFinderArgument
      //\/\/\/\/\/\/\/\/\/\/\/\/
      
      // add 2 finder arguments
      Map<String,String> args = new HashMap<String,String>();
      args.put("a", "a_value");
      args.put("b", "b_value");
      slot.setFinderArguments(args);
      assembly.saveSlot(slot);
      // verify the empty finder argument
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(2, slot.getFinderArguments().size());

      // add 1 more finder argument
      args = slot.getFinderArguments();
      args.put("c", "c_value");
      slot.setFinderArguments(args);
      assembly.saveSlot(slot);
      // verify the empty finder argument
      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(3, slot.getFinderArguments().size());
      
      // empty the arguments
      slot.setFinderArguments(null);
      assembly.saveSlot(slot);
      // verify the empty finder argument
      slot = assembly.loadSlot(slot.getGUID());
      assertEquals(0, slot.getFinderArguments().size());      
      
      // cleanup test data
      assembly.deleteSlot(slot.getGUID());
   }
   
   /**
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testAssociationChanges() throws Exception
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = assembly.createSlot();
      setupSlot(slot);
      assembly.saveSlot(slot);
      
      // Reload, and readd the existing, then save
      slot = assembly.loadSlotModifiable(slot.getGUID());
      Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      slotAssociations.add(new PSPair(new PSGuid(PSTypeEnum.NODEDEF, 1000),
            new PSGuid(PSTypeEnum.TEMPLATE, 1001)));
      slotAssociations.add(new PSPair(new PSGuid(PSTypeEnum.NODEDEF, 1002),
            new PSGuid(PSTypeEnum.TEMPLATE, 1003)));
      slot.setSlotAssociations(slotAssociations);
      assembly.saveSlot(slot);

      slot = assembly.loadSlot(slot.getGUID());
      assertEquals(2, slot.getSlotAssociations().size());

      // Now do the same with a disjoint set
      slot = assembly.loadSlotModifiable(slot.getGUID());
      slotAssociations = new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      slotAssociations.add(new PSPair(new PSGuid(PSTypeEnum.NODEDEF, 1002),
            new PSGuid(PSTypeEnum.TEMPLATE, 1003)));
      slot.setSlotAssociations(slotAssociations);
      // Verify count
      assertEquals(1, slot.getSlotAssociations().size());
      // Verify content
      PSPair<IPSGuid, IPSGuid> a1 = slot.getSlotAssociations().iterator().next();
      assertEquals(1002, a1.getFirst().longValue());
      assertEquals(1003, a1.getSecond().longValue());
      assembly.saveSlot(slot);  

      slot = assembly.loadSlotModifiable(slot.getGUID());
      assertEquals(1, slot.getSlotAssociations().size());

      // Empty set, verify size
      slot.setSlotAssociations(new ArrayList());
      assertEquals(0, slot.getSlotAssociations().size());
      assembly.saveSlot(slot);
      
      slot = assembly.loadSlot(slot.getGUID());
      assertEquals(0, slot.getSlotAssociations().size());
   }

}
