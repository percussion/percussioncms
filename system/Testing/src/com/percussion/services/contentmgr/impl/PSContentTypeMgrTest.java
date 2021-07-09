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
package com.percussion.services.contentmgr.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.testing.IPSCustomJunitTest;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test type manager methods
 * 
 * @author dougrand
 */
//TODO:  Duplicate Test?
@Category(IntegrationTest.class)
public class PSContentTypeMgrTest
      implements IPSCustomJunitTest
{
   static IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
   
   /**
    * deserialize a node and delete,add a template association and save
    * this is what msm does for a node def
    */
   @Test
   public void testDeserializeAndLoadNode() throws Exception
   {   
      // Normalize some data for testing purposes
      String xml = 
         "<node-definition id=\"1\">" +
         "<auto-created>false</auto-created>" +
         "<description>vTest Node</description>" +
         "<hide-from-menu>false</hide-from-menu>" +
         "<id>911</id>" +
         "<internal-name>rffGeneric</internal-name>" +
         "<label>Generic</label>" +
         "<mandatory>false</mandatory>" +
         "<name>rx:rffGeneric</name>" +
         "<new-request>../psx_cerffGeneric/rffGeneric.html</new-request>" +
         "<object-type>1</object-type>" +
         "<protected>false</protected>" +
         "<query-request>../psx_cerffGeneric/rffGeneric.html</query-request>" +
         "<raw-content-type>911</raw-content-type>" +
         "<template-ids>" +
            "<template-id>0-4-504</template-id>" +
            "<template-id>0-4-502</template-id>" +
            "<template-id>0-4-543</template-id>" +
            "<template-id>0-4-501</template-id>" +
            "<template-id>0-4-505</template-id>" +
            "<template-id>0-4-503</template-id>" +
            "<template-id>0-4-537</template-id>" +
         "</template-ids>" +
         "<update-request/>" +
         "</node-definition>";
      
      // Create an empty and reconstitute
      IPSNodeDefinition newdef = mgr.createNodeDefinition();
      newdef.fromXML(xml);
      
      newdef.removeVariantGuid(new PSGuid("0-4-501"));
      newdef.addVariantGuid(new PSGuid("0-4-525"));
      
      List<IPSNodeDefinition> defs = new ArrayList<IPSNodeDefinition>();
      defs.add(newdef);
      try
      {
         mgr.saveNodeDefinitions(defs);
      }
      catch ( Exception e)
      {
         System.out.println("Exception occurred: "+e.getLocalizedMessage());
      }
   }

   @Test
   public void testSerialization() throws Exception
   {
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(new PSGuid(PSTypeEnum.NODEDEF, 311));
      List<IPSNodeDefinition> defs = mgr.loadNodeDefinitions(ids);
      
      // Serialize to and from xml
      for(IPSNodeDefinition def : defs)
      {  
         // Normalize some data for testing purposes
         def.setName(def.getName());
         String xml = def.toXML();
         
         // Create an empty and reconstitute
         IPSNodeDefinition newdef = mgr.createNodeDefinition();
         newdef.fromXML(xml);
         assertEquals(newdef, def);
      }
   }

   @Test
   public void testLoadOneDefinition() throws Exception
   {
      
      try
      {
         mgr.findNodeDefinitionByName("XYZ");
         assertFalse("Failed to throw exception", true);
      }
      catch(NoSuchNodeTypeException e)
      {
         // Correct
      }
      
      try
      {
         mgr.findNodeDefinitionByName("%G%");
         assertFalse("Failed to throw exception", true);
      }
      catch(RepositoryException e)
      {
         // Correct
      }
      
      assertNotNull(mgr.findNodeDefinitionByName("rx:rffBrief"));
   }

   @Test
   public void testFindMultipleDefs() throws Exception
   {
      List<IPSNodeDefinition> defs = mgr.findNodeDefinitionsByName("rffGener%");
      assertTrue(defs != null && defs.size() == 2);

      defs = mgr.findNodeDefinitionsByName("rffGeneric");
      assertTrue(defs != null && defs.size() == 1);
      IPSNodeDefinition def = defs.get(0);
      Set<IPSGuid> vars = def.getVariantGuids();
      assertTrue(vars.contains(new PSGuid(PSTypeEnum.TEMPLATE, 502)));
      assertFalse(vars.contains(new PSGuid(PSTypeEnum.TEMPLATE, 506)));
      assertEquals(new PSGuid(PSTypeEnum.NODEDEF, 311), def.getGUID());
      assertEquals("A generic HTML page; includes local text plus Snippets of other Content Items.",
            def.getDescription());      
   }

   @Test
   public void testFindAllDefs() throws Exception
   {
      List<IPSNodeDefinition> defs = mgr.findAllItemNodeDefinitions();
      assertTrue(defs != null && defs.size() > 2);
      checkForDupes(defs);
   }

   @Test
   public void testLoadByIds() throws Exception
   {    
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(new PSGuid(PSTypeEnum.NODEDEF, 308));
      ids.add(new PSGuid(PSTypeEnum.NODEDEF, 309));
      ids.add(new PSGuid(PSTypeEnum.NODEDEF, 310));
      List<IPSNodeDefinition> defs = mgr.loadNodeDefinitions(ids);
      assertEquals(3, defs.size());
   }

   @Test
   public void testFindByVariantID() throws Exception
   {
      List<IPSNodeDefinition> defs = 
         mgr.findNodeDefinitionsByTemplate(new PSGuid(PSTypeEnum.TEMPLATE, 504));
      assertTrue(defs.size() > 0);
      checkForDupes(defs);
      System.out.println("Count: " + defs.size());
   }

   @Test
   public void testCreateSaveAndDelete() throws Exception
   {
      List<IPSNodeDefinition> defs = new ArrayList<IPSNodeDefinition>();
      IPSNodeDefinition def = mgr.createNodeDefinition();
      def.setDescription("Test content type");
      def.setName("rx:TestType1");
      def.setObjectType(1);
      def.addVariantGuid(new PSGuid(PSTypeEnum.TEMPLATE, 502));
      defs.add(def);
      mgr.saveNodeDefinitions(defs);
      // Reload and check
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(def.getGUID());
      List<IPSNodeDefinition> newdefs = mgr.loadNodeDefinitions(ids);
      assertEquals(1, newdefs.size());
      IPSNodeDefinition reloaded = newdefs.get(0);
      assertEquals("rx:TestType1", reloaded.getName());
      assertEquals("Test content type", reloaded.getDescription());
      assertEquals(1, reloaded.getVariantGuids().size());
      // Delete 
      mgr.deleteNodeDefinitions(defs);
   }
   
   /**
    * Verifies that every entry in the supplied list only occurs once within the
    * list (using the object's <code>equals</code> method.
    *  
    * @param defs Assumed not <code>null</code>.
    */
   private void checkForDupes(List<IPSNodeDefinition> defs)
   {
      for (IPSNodeDefinition def : defs)
      {
         int count = 0;
         for (IPSNodeDefinition d : defs)
         {
            if (def.equals(d))
               count++;
         }
         assertTrue("Found duplicate defs in the results.", count == 1);
      }
   }

}
