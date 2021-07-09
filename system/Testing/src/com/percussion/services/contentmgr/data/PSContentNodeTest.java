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
package com.percussion.services.contentmgr.data;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

/**
 * Test certain methods on content node
 * 
 * @author dougrand
 */
public class PSContentNodeTest extends TestCase
{
   public void testGetNodeMethods() throws RepositoryException
   {
      PSContentNode parent = new PSContentNode(null, "parent", null, null,
            null, null);
      parent.addNode("foo");
      parent.addNode("foo");
      parent.addNode("foo");
      parent.addNode("bar");
      parent.addNode("bar");
      parent.addNode("bar");
      parent.addNode("bletch");
      parent.addNode("bletch");
      parent.addNode("bletch");
      parent.setChildrenLoaded(true); // Cheat so we don't try to hit the db
      
      NodeIterator ni = parent.getNodes();
      assertEquals(0, ni.getPosition());
      assertEquals(9, ni.getSize());
      assertEquals(0, ni.getPosition());
      assertTrue(ni.hasNext());
      assertTrue(ni.next() instanceof Node);
      
      ni = parent.getNodes("foo");
      assertEquals(0, ni.getPosition());
      assertEquals(3, ni.getSize());
      assertEquals(0, ni.getPosition());
      
      int i = 0;
      while(ni.hasNext())
      {
         Node n = ni.nextNode();
         assertEquals("foo", n.getName());
         i++;
      }
      assertEquals(3, i);
      
      ni = parent.getNodes("b*");
      assertEquals(0, ni.getPosition());
      assertEquals(6, ni.getSize());
      assertEquals(0, ni.getPosition());
      
      i = 0;
      while(ni.hasNext())
      {
         Node n = ni.nextNode();
         assertTrue(n.getName().startsWith("b"));
         i++;
      }
      assertEquals(6, i);     
      
      Node n = parent.getNode("bar");
      assertNotNull(n);   
      assertEquals("bar", n.getName());
      n = parent.getNode("bar[1]");
      assertNotNull(n);
      assertEquals("bar", n.getName());
   }
}
