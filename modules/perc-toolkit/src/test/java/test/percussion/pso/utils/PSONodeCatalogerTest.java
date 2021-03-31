/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.utils PSONodeCatalogerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;

public class PSONodeCatalogerTest
{

   private static Log log = LogFactory.getLog(PSONodeCatalogerTest.class); 
   
   Mockery context; 
   PSONodeCataloger cut; 
   IPSContentMgr cmgr; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(); 
      cut = new PSONodeCataloger();
      cmgr = context.mock(IPSContentMgr.class); 
      cut.setCmgr(cmgr);
      
   }
   @Test
   public final void testGetContentTypeNames()
   {
      log.info("Getting content type names");
      
      final IPSNodeDefinition t1 = context.mock(IPSNodeDefinition.class,"t1");
      final IPSNodeDefinition t2 = context.mock(IPSNodeDefinition.class,"t2");
      final List<IPSNodeDefinition> nodes = Arrays.<IPSNodeDefinition>asList(new IPSNodeDefinition[]{t1,t2});
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).findAllItemNodeDefinitions(); 
               will(returnValue(nodes)); 
               allowing(t1).getName(); 
               will(returnValue("type1"));
               allowing(t2).getName(); 
               will(returnValue("type2"));
         }});
         
         List<String> names = cut.getContentTypeNames();
         assertNotNull(names);
         assertEquals(2,names.size()); 
         assertEquals("type1", names.get(0));
         assertEquals("type2", names.get(1));
         context.assertIsSatisfied(); 
         
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
   
   @Test
   public final void testGetContentTypeNamesWithField()
   {
      log.info("Getting content type names with field");
      final Sequence ctypes = context.sequence("ctypes"); 
      final IPSNodeDefinition nd1 = context.mock(IPSNodeDefinition.class,"nd1");
      final IPSNodeDefinition nd2 = context.mock(IPSNodeDefinition.class,"nd2");
      final List<IPSNodeDefinition> nodes = Arrays.asList(new IPSNodeDefinition[]{nd1,nd2});

      final NodeType t1 = context.mock(NodeType.class,"t1");
      final NodeType t2 = context.mock(NodeType.class,"t2");
      final PropertyDefinition p1 = context.mock(PropertyDefinition.class,"p1");
      final PropertyDefinition p2 = context.mock(PropertyDefinition.class,"p2");
      final PropertyDefinition p3 = context.mock(PropertyDefinition.class,"p3");
      
      final PropertyDefinition[] t1p = new PropertyDefinition[]{p1,p2,p3};
      final PropertyDefinition[] t2p = new PropertyDefinition[]{p1,p3};
      
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).findAllItemNodeDefinitions(); 
               will(returnValue(nodes));
               one(nd1).getDeclaringNodeType();
               will(returnValue(t1));
               allowing(nd1).getName();
               will(returnValue("rx:node1"));
               allowing(nd2).getName();
               will(returnValue("rx:node2"));               
               one(nd2).getDeclaringNodeType();
               will(returnValue(t2)); 
               allowing(t1).getName(); 
               will(returnValue("rx:type1"));
               allowing(t2).getName(); 
               will(returnValue("rx:type2"));
               one(t1).getDeclaredPropertyDefinitions();
               will(returnValue(t1p));
               one(t2).getDeclaredPropertyDefinitions();
               will(returnValue(t2p));
               allowing(p1).getName();
               will(returnValue("rx:prop1"));
               allowing(p2).getName();
               will(returnValue("rx:prop2"));
               allowing(p3).getName();
               will(returnValue("rx:prop3"));
               
         }});
         
         List<String> names = cut.getContentTypeNamesWithField("prop2"); 
         assertNotNull(names);
         assertEquals(1,names.size()); 
         assertEquals("rx:type1", names.get(0));
         context.assertIsSatisfied();
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
   @Test
   public final void testGetFieldNamesForContentType()
   {
      log.info("Getting content type names with field");
  
      final NodeTypeIterator nodes = context.mock(NodeTypeIterator.class);
      final IPSNodeDefinition nodeDef = context.mock(IPSNodeDefinition.class); 
      final NodeType t1 = context.mock(NodeType.class); 
      final PropertyDefinition p1 = context.mock(PropertyDefinition.class,"p1");
      final PropertyDefinition p2 = context.mock(PropertyDefinition.class,"p2");
      final PropertyDefinition p3 = context.mock(PropertyDefinition.class,"p3");
      
      final PropertyDefinition[] t1p = new PropertyDefinition[]{p1,p2,p3};
      
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).findNodeDefinitionByName("rx:type1"); 
               will(returnValue(nodeDef));
               one(nodeDef).getDeclaringNodeType();
               will(returnValue(t1)); 
               allowing(t1).getName(); 
               will(returnValue("rx:type1"));
               one(t1).getDeclaredPropertyDefinitions();
               will(returnValue(t1p));
               allowing(p1).getName();
               will(returnValue("rx:prop1"));
               allowing(p2).getName();
               will(returnValue("rx:prop2"));
               allowing(p3).getName();
               will(returnValue("rx:prop3"));
               
         }});
         
         List<String> names = cut.getFieldNamesForContentType("type1"); 
         assertNotNull(names);
         assertEquals(3,names.size()); 
         assertEquals("rx:prop1", names.get(0));
         context.assertIsSatisfied();
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
}
