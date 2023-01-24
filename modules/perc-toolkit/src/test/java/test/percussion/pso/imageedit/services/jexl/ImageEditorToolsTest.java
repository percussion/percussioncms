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
package test.percussion.pso.imageedit.services.jexl;

import static org.junit.Assert.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.jexl.ImageEditorTools;

public class ImageEditorToolsTest
{
   private static final Logger log = LogManager.getLogger(ImageEditorToolsTest.class);
   
   Mockery context;
   ImageEditorTools cut;
   ImageSizeDefinitionManager isdm; 
   Node parent; 
   NodeIterator nodes;
   Node nodea;
   Node nodeb;
   Property propa;
   Property propb; 
   Sequence nodeSeq; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(); 
      cut = new ImageEditorTools(); 
      isdm = context.mock(ImageSizeDefinitionManager.class);
      cut.setIsdm(isdm);
      context.checking(new Expectations(){{
         one(isdm).getSizedImageNodeName();
         will(returnValue("child1"));
         one(isdm).getSizedImagePropertyName();
         will(returnValue("foo")); 
      }});
      parent = context.mock(Node.class,"parent");
      nodes = context.mock(NodeIterator.class,"nodes");
      nodeSeq = context.sequence("nodes");
      nodea = context.mock(Node.class,"nodea");
      nodeb = context.mock(Node.class, "nodeb");
      propa = context.mock(Property.class, "propa");
      propb = context.mock(Property.class, "propb");
   }
   
   @Test
   public final void testGetSizedNodeA()
   {
      try
      {
         setNodeExpectations();
         context.checking(new Expectations(){{
            one(parent).getNodes("child1");
            will(returnValue(nodes));
            one(nodes).hasNext(); inSequence(nodeSeq); 
            will(returnValue(true));
            one(nodes).nextNode(); inSequence(nodeSeq);
            will(returnValue(nodea));            
         }});   
         Node result = (Node) cut.getSizedNode(parent, "a"); 
         assertEquals(nodea, result); 
         context.assertIsSatisfied();
         
      } catch (RepositoryException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception"); 
      }
      
   }
   
   @Test
   public final void testGetSizedNodeB()
   {
      try
      {
         setNodeExpectations();
         context.checking(new Expectations(){{
            one(parent).getNodes("child1");
            will(returnValue(nodes));
            one(nodes).hasNext(); inSequence(nodeSeq); 
            will(returnValue(true));
            one(nodes).nextNode(); inSequence(nodeSeq);
            will(returnValue(nodea));
            one(nodes).hasNext();inSequence(nodeSeq);
            will(returnValue(true));
            one(nodes).nextNode();inSequence(nodeSeq);
            will(returnValue(nodeb));            
         }});   
         Node result = (Node) cut.getSizedNode(parent, "b"); 
         assertEquals(nodeb, result); 
         context.assertIsSatisfied();
         
      } catch (RepositoryException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception"); 
      }
      
   }
   
   @Test
   public final void testHasSizedNodeC()
   {
      try
      {
         setNodeExpectations();
         context.checking(new Expectations(){{
            one(parent).getNodes("child1");
            will(returnValue(nodes));
            one(nodes).hasNext(); inSequence(nodeSeq); 
            will(returnValue(true));
            one(nodes).nextNode(); inSequence(nodeSeq);
            will(returnValue(nodea));
            one(nodes).hasNext();inSequence(nodeSeq);
            will(returnValue(true));
            one(nodes).nextNode();inSequence(nodeSeq);
            will(returnValue(nodeb));
            one(nodes).hasNext(); inSequence(nodeSeq);
            will(returnValue(false));
         }});   
         boolean result = cut.hasSizedNode(parent, "c"); 
         assertFalse(result);
         context.assertIsSatisfied();
         
      } catch (RepositoryException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception"); 
      }
      
   }
   private void setNodeExpectations() throws RepositoryException
   {
      context.checking(new Expectations(){{
        allowing(nodea).getProperty("foo");
        will(returnValue(propa));
        allowing(nodeb).getProperty("foo");
        will(returnValue(propb));
        allowing(propa).getString();
        will(returnValue("a"));
        allowing(propb).getString();
        will(returnValue("b")); 
      }});
   }
   
}
