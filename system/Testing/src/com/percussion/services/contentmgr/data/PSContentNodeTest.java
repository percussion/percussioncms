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
