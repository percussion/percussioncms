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
package com.percussion.services.assembly.impl.nav;

import com.percussion.cms.PSCmsException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSProxyNode;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 *  Test managed nav support code
 */
@Category(IntegrationTest.class)
public class PSNavUnitTest extends ServletTestCase
{
   /**
    * @throws Exception
    */
   public void testBasicNode() throws Exception
   {
      PSStopwatch sw = new PSStopwatch();
      PSThreadRequestUtils.initServerThreadRequest();
      
      IPSAssemblyItem item = creteWorkItem(309, 487);
      
      PSNavHelper helper = new PSNavHelper(item);
      sw.start();
      
     
      Node navon = helper.findNavNode(item);
      System.err.println("Loading proxies for parent axis: " + sw);
      Node self = navon;
      
      // Check image children
      NodeIterator images = self.getNodes("nav:image");
      System.err.println("Loading children: " + sw);
      assertTrue(images.getSize() > 0);
      
      // Check submenu children of parent
      NodeIterator submenu = navon.getParent().getNodes("nav:submenu");
      assertTrue(submenu.getSize() > 0);
      System.err.println("Loaded parent submenus: " + sw);
      
      assertNotNull(navon);

      // Test each navon in the parent axis
      int count = 0;
      while (navon != null)
      {
         PSNavAxisEnum axis = PSNavAxisEnum.ANCESTOR;
         if (count == 0)
         {
            axis = PSNavAxisEnum.SELF;
         }
         else if (count == 1)
         {
            axis = PSNavAxisEnum.PARENT;
         }
         count++;
         checkNavon(navon, axis);
         navon = navon.getParent();
      }
      
      sw.stop();
      System.err.println("Whole test: " + sw);
   }

   /**
     *
    */
   public void testGetAncestors() throws PSAssemblyException, RepositoryException, PSCmsException, PSFilterException {
      PSThreadRequestUtils.initServerThreadRequest();
      
      IPSAssemblyItem item = creteWorkItem(309, 487);
      
      PSNavHelper helper = new PSNavHelper(item);
      Node navon = helper.findNavNode(item);

      IPSProxyNode pnode = (IPSProxyNode) navon;
      List<Node> ancestors = pnode.getAncestors();
      assertTrue(ancestors.size() == 1);
      
      IPSProxyNode rootNode = (IPSProxyNode) pnode.getRoot();
      
      IPSGuid rootId = ((IPSProxyNode)ancestors.get(0)).getGuid();
      assertTrue(rootNode.getGuid().equals(rootId));
      
      IPSGuid item487_Id = pnode.getGuid();

      // test navon with 2 parents
      item = creteWorkItem(316, 376);
      helper = new PSNavHelper(item);
      navon = helper.findNavNode(item);
      pnode = (IPSProxyNode) navon;
      ancestors = pnode.getAncestors();
      assertTrue(ancestors.size() == 2);
      
      IPSGuid id = ((IPSProxyNode) ancestors.get(0)).getGuid();
      assertTrue(rootId.equals(id));
      
      id = ((IPSProxyNode) ancestors.get(1)).getGuid();
      assertTrue(item487_Id.equals(id));
   }
   
   /**
    * Creates a work item with the specified item and folder in the 
    * site (id=301), preview context and revision (3).
    * 
    * @param folderId the ID of the parent folder of the item.
    * @param itemId the content ID of the item.
    * 
    * @return the created work item, never <code>null</code>.
    * 
    * @throws PSAssemblyException
    */
   private IPSAssemblyItem creteWorkItem(int folderId, int itemId)
         throws PSAssemblyException
   {
      IPSAssemblyItem item = new PSAssemblyWorkItem();
      item.setParameterValue(IPSHtmlParameters.SYS_ITEMFILTER, "public");
      item.setParameterValue(IPSHtmlParameters.SYS_SITEID, "301");
      item.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, "1");
      item.setParameterValue(IPSHtmlParameters.SYS_CONTENTID, "" +itemId);
      item.setParameterValue(IPSHtmlParameters.SYS_REVISION, "3");
      item.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, "" + folderId);
      item.normalize();
      
      return item;
   }
   
   /**
    * @param navon
    * @param axis
    * @throws Exception
    */
   private void checkNavon(Node navon, PSNavAxisEnum axis)
         throws Exception
   {
      String axis_val = navon.getProperty("nav:axis").getString();
      PSNavAxisEnum axis_enum = PSNavAxisEnum.valueOf(axis_val);
      assertEquals(axis, axis_enum);

      Node image = navon.getProperty("nav:selectedImage").getNode();
      assertNotNull(image);
   }
}
