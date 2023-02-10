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
