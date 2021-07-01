/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
