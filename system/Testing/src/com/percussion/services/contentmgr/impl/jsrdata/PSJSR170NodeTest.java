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
package com.percussion.services.contentmgr.impl.jsrdata;


import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.utils.jsr170.PSProperty;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSJSR170NodeTest extends TestCase
{
   Node m_root;
   
   public PSJSR170NodeTest(String name) {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSJSR170NodeTest.class);
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSContentNode aaa, bbb, current;
      
      m_root = new PSContentNode(null, "root", null, null, null, null);
      aaa = (PSContentNode) m_root.addNode("aaa");
      aaa.addProperty(new PSProperty("rx:dog", aaa, "Fido"));
      aaa.addProperty(new PSProperty("rx:cat", aaa, "Pippin"));
      aaa.addProperty(new PSProperty("rx:canary", aaa, "Tweetie"));
      current = (PSContentNode) aaa.addNode("aa_1");
      aaa.addProperty(new PSProperty("rx:house", current, Boolean.TRUE));
      bbb = (PSContentNode) m_root.addNode("bbb");
      bbb.addNode("ccc");
      bbb.addNode("ccc");
      fixupChildStatus(m_root);
   }
   
   private void fixupChildStatus(Node n) throws RepositoryException
   {
      PSContentNode cur = (PSContentNode) n;
      cur.setChildrenLoaded(true);
      NodeIterator niter = cur.getNodes();
      while(niter.hasNext())
      {
         fixupChildStatus(niter.nextNode());
      }
      
   }

   public void testNodeAndPropertyAccess() throws Exception
   {
      String prop = m_root.getProperty("aaa/dog").getString();
      assertEquals("Fido", prop);
      assertTrue(m_root.hasNodes());
      assertTrue(m_root.hasNode("bbb"));
      Node ccc = m_root.getNode("bbb/ccc[0]");
      Node ccc2 = m_root.getNode("bbb/ccc[1]");
      assertNotSame(ccc, ccc2);
      assertEquals("Pippin", m_root.getProperty("aaa/cat").getString());
      assertEquals("Tweetie", m_root.getProperty("aaa/canary").getString());
      assertTrue(m_root.getProperty("aaa/house").getBoolean());
   }
   
   public void testPropIterator() throws Exception
   {
      Node aaa = m_root.getNode("aaa");
      PropertyIterator iter = aaa.getProperties();
      assertEquals(4, iter.getSize());
      Property val = iter.nextProperty();
      assertEquals(PSProperty.class, val.getClass());
      assertEquals(1, iter.getPosition());
      iter = aaa.getProperties("rx:ca*");
      assertEquals(2, iter.getSize());
   }
   
   public void testNodeIterator() throws Exception
   {
      Node bbb = m_root.getNode("bbb");
      NodeIterator iter = bbb.getNodes("ccc");
      assertEquals(2, iter.getSize());
      assertEquals("ccc", iter.nextNode().getName());
      assertEquals(1, iter.getPosition());
   }
}
