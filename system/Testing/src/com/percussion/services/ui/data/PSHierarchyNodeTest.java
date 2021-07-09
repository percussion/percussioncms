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
package com.percussion.services.ui.data;

import com.percussion.services.ui.IPSUiService;
import com.percussion.services.ui.PSUiServiceLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PSHierarchyNode} class.
 */
@Category(IntegrationTest.class)
public class PSHierarchyNodeTest
{
   /**
    * Tests the CRUD functionality for hierarchy nodes. 
    */
   @Test
   public void testCRUD() throws Exception
   {
      try
      {
         IPSUiService service = PSUiServiceLocator.getUiService();
         
         // create and save eiger folder
         PSHierarchyNode eiger = service.createHierarchyNode("eiger", null, 
            PSHierarchyNode.NodeType.FOLDER);
         eiger.addProperty("kanton", "wallis");
         service.saveHierarchyNode(eiger);
         List<PSHierarchyNode> nodes = service.findHierarchyNodes(
            "eiger", null);
         assertTrue(nodes.size() == 1);
         assertTrue(nodes.get(0).getProperty("kanton").equals(
            "wallis"));
         
         // update a eiger property
         eiger.addProperty("kanton", "berner oberland");
         service.saveHierarchyNode(eiger);
         nodes = service.findHierarchyNodes("eiger", null);
         assertTrue(nodes.size() == 1);
         assertTrue(nodes.get(0).getProperty("kanton").equals(
            "berner oberland"));
         
         // create and save folder child eigernordwand
         PSHierarchyNode eigerNordwand = service.createHierarchyNode(
            "eigernordwand", eiger.getGUID(), 
            PSHierarchyNode.NodeType.PLACEHOLDER);
         service.saveHierarchyNode(eigerNordwand);
         nodes = service.findHierarchyNodes("eigernordwand", null);
         assertTrue(nodes.size() == 1);
         
         // add new property to eigernordwand
         eigerNordwand.addProperty("grade", "difficult");
         service.saveHierarchyNode(eigerNordwand);
         nodes = service.findHierarchyNodes("eigernordwand", null);
         assertTrue(nodes.size() == 1);
         assertTrue(nodes.get(0).getProperty("grade").equals(
            "difficult"));
         
         // remove property from eigernordwand
         eigerNordwand.removeProperty("grade");
         service.saveHierarchyNode(eigerNordwand);
         nodes = service.findHierarchyNodes("eigernordwand", null);
         assertTrue(nodes.size() == 1);
         assertTrue(nodes.get(0).getProperty("grade") == null);
         
         // create and save jungfrau folder
         PSHierarchyNode jungfrau = service.createHierarchyNode("jungfrau", 
            null, PSHierarchyNode.NodeType.FOLDER);
         jungfrau.addProperty("kanton", "berner oberland");
         service.saveHierarchyNode(jungfrau);
         nodes = service.findHierarchyNodes("jungfrau", null);
         assertTrue(nodes.size() == 1);
         assertTrue(nodes.get(0).getProperty("kanton").equals(
            "berner oberland"));
         
         // move child eigernordwand from eiger to jungfrau
         List<IPSGuid> moveIds = new ArrayList<IPSGuid>();
         moveIds.add(eigerNordwand.getGUID());
         service.moveChildren(eiger.getGUID(), jungfrau.getGUID(), moveIds);
         assertTrue(service.findHierarchyNodes(
            "eigernordwand", eiger.getGUID(), null).isEmpty());
         assertTrue(!service.findHierarchyNodes(
            "eigernordwand", jungfrau.getGUID(), null).isEmpty());
         
         // remove child eigernordwand from jungfrau
         service.removeChildren(jungfrau.getGUID(), moveIds);
         assertTrue(service.findHierarchyNodes(
            "eigernordwand", jungfrau.getGUID(), null).isEmpty());
         
         // add jungfrau to eiger
         jungfrau.setParentId(eiger.getGUID());
         service.saveHierarchyNode(jungfrau);
         
         // remove parent with all children
         service.deleteHierarchyNode(eiger.getGUID());
         nodes = service.findHierarchyNodes("eiger", null);
         assertTrue(nodes.size() == 0);
         nodes = service.findHierarchyNodes("jungfrau", null);
         assertTrue(nodes.size() == 0);
      }
      catch (Exception e)
      {
         throw e;
      }
   }
}

