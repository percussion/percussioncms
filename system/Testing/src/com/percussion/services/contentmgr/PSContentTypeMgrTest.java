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
package com.percussion.services.contentmgr;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test basic node definition methods, CRUD operations
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSContentTypeMgrTest
{
   private static IPSContentMgr ms_mgr = PSContentMgrLocator.getContentMgr();
   
   static long start_count = System.currentTimeMillis() & 0x0FFFL;
   
   /**
    * Create a node definition object and setup basic relationships to templates. 
    * @return the initialized object, but not a persisted object
    */
   public IPSNodeDefinition createND()
   {
      IPSNodeDefinition rval = ms_mgr.createNodeDefinition();
      
      rval.setName("test_x_" + start_count++);
      rval.setDescription("test nodedef");
      rval.setLabel("test_node_definition");
      rval.setObjectType(1);
      
      rval.addVariantGuid(new PSGuid(PSTypeEnum.TEMPLATE, 501));
      rval.addVariantGuid(new PSGuid(PSTypeEnum.TEMPLATE, 502));
      return rval;
   }
   
   /**
    * Really a cleanup for any leftover test data
    */
   @Test
   public void testDeleteNodeDefs() 
   {
      try
      {
         List<IPSNodeDefinition> defs = ms_mgr.findNodeDefinitionsByName("test_x_%");
         ms_mgr.deleteNodeDefinitions(defs);
      }
      catch(Exception e)
      {
         // OK, no existing test defs
      }
   }
   
   /**
    * Test adding node definitions to the db
    * 
    * @throws Exception
    */
   @Test
   public void testAddNodedef() throws Exception
   {
      List<IPSNodeDefinition> defs = new ArrayList<IPSNodeDefinition>();
      defs.add(createND());
      defs.add(createND());
      defs.add(createND());
      ms_mgr.saveNodeDefinitions(defs);
   }
   
   /**
    * Perform modifications on one of the node definitions saved in the previous
    * test. Persist to exercise the machinery.
    * 
    * @throws Exception
    */
   @Test
   public void testModifyNodeDefinition() throws Exception
   {
      List<IPSNodeDefinition> defs = ms_mgr.findNodeDefinitionsByName("test_x_%");
      
      assertTrue(defs.size() > 0);
      
      // Grab the first, remove a node def and persist
      IPSNodeDefinition def = defs.get(0);
      
      def.removeVariantGuid(new PSGuid(PSTypeEnum.TEMPLATE, 503));
      
      List<IPSNodeDefinition> sdefs = new ArrayList<IPSNodeDefinition>();
      sdefs.add(def);
      
      ms_mgr.saveNodeDefinitions(sdefs);
      
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(def.getGUID());
      
      // Regrab the specific def and check the count
      defs = ms_mgr.loadNodeDefinitions(ids);
      
      assertTrue(defs.size() == 1);
      
      def = defs.get(0);
      
      assertEquals(def.getVariantGuids().size(), 2);
   }
   
   /**
    * Cleanup for any leftover test data
    */
   @Test
   public void testDeleteNodeDefs2() 
   {
      try
      {
         List<IPSNodeDefinition> defs = ms_mgr.findNodeDefinitionsByName("test_x_%");
         ms_mgr.deleteNodeDefinitions(defs);
      }
      catch(Exception e)
      {
         // OK, no existing test defs
      }
   }
}
