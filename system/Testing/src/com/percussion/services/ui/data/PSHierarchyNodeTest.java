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

