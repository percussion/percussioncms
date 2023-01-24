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
package test.percussion.pso.imageedit.services;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.ImageSizeTemplateExpander;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSNodeIterator;
import com.percussion.utils.jsr170.PSProperty;

public class ImageSizeTemplateExpanderTest
{
   private static final Logger log = LogManager.getLogger(ImageSizeTemplateExpanderTest.class);
   
   Mockery context;
   TestableImageSizeTemplateExpander cut;
   ImageSizeDefinitionManager isdm; 
   IPSAssemblyService asm; 
  
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new TestableImageSizeTemplateExpander();
      
      isdm = context.mock(ImageSizeDefinitionManager.class, "isdm");
      cut.setIsdm(isdm);
      asm = context.mock(IPSAssemblyService.class, "asm");
      cut.setAsm(asm);
      
   }
   @Test
   public final void testFindTemplates()
   {    
      final IPSNode contentNode = context.mock(IPSNode.class);
      Map<String,String> parameters = new HashMap<String, String>();
      
      try
      {
//         final Node childNode = context.mock(Node.class, "childnode"); 
//         MultiMap childMap = new MultiValueMap(){{
//            put("nodename", childNode);
//         }};
//         
//         context.checking(new Expectations(){{
//            allowing(childNode).getDepth();
//            will(returnValue(0));
//         }});
//         
//         final Property sizeProperty = new PSProperty("sizecode", childNode, "sizeA");
//        
//         final NodeIterator nodes = new PSNodeIterator(childMap,null);
//         
//         final ImageSizeDefinition sizeA = new ImageSizeDefinition(){{
//            setCode("sizeA");
//            setBinaryTemplate("binarySizeA"); 
//         }};
//         
//         final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class, "templateA");
//         final IPSGuid templateGuid = context.mock(IPSGuid.class, "templateGuid");
//         
//         context.checking(new Expectations(){{
//            one(isdm).getSizedImageNodeName();
//            will(returnValue("nodename"));
//            one(isdm).getSizedImagePropertyName();
//            will(returnValue("sizecode")); 
//            one(contentNode).getNodes("nodename");
//            will(returnValue(nodes)); 
//            one(childNode).getProperty("sizecode");
//            will(returnValue(sizeProperty));
//            
//            one(isdm).getImageSize("sizeA");
//            will(returnValue(sizeA));
//            
//            one(asm).findTemplateByName("binarySizeA");
//            will(returnValue(template));
//            
//            one(template).getGUID(); 
//            will(returnValue(templateGuid));
//            
//          
//            
//         }});
//         
//         List<IPSGuid> results = cut.findTemplates(null, null, null, 1, null, contentNode, parameters);
//         assertNotNull(results); 
//         assertEquals(1,results.size()); 
//         assertEquals(templateGuid, results.get(0));
//         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
   }
   
   private class TestableImageSizeTemplateExpander extends ImageSizeTemplateExpander
   {

      /**
       * @see ImageSizeTemplateExpander#findTemplates(IPSGuid, IPSGuid, IPSGuid, int, PSComponentSummary, Node, Map)
       */
      @Override
      public List<IPSGuid> findTemplates(IPSGuid itemGuid,
            IPSGuid folderGuid, IPSGuid siteGuid, int context,
            PSComponentSummary summary, Node contentNode,
            Map<String, String> parameters)
      {
         return super.findTemplates(itemGuid, folderGuid, siteGuid, context, summary,
               contentNode, parameters);
      }

      /**
       * @see ImageSizeTemplateExpander#setAsm(IPSAssemblyService)
       */
      @Override
      public void setAsm(IPSAssemblyService asm)
      {
         super.setAsm(asm);
      }

      /**
       * @see ImageSizeTemplateExpander#setIsdm(ImageSizeDefinitionManager)
       */
      @Override
      public void setIsdm(ImageSizeDefinitionManager isdm)
      {
         super.setIsdm(isdm);
      }
      
   }
}
