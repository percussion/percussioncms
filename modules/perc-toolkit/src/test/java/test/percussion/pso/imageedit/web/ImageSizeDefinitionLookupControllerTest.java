/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.web;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;

import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.web.ImageSizeDefinitionLookupController;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

public class ImageSizeDefinitionLookupControllerTest
{
   private static Log log = LogFactory.getLog(ImageSizeDefinitionLookupControllerTest.class);
   
   private ImageSizeDefinitionLookupController cut; 
   
   private Mockery context; 
   private ImageSizeDefinitionManager defmgr; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(); 
      cut = new ImageSizeDefinitionLookupController(); 
      defmgr = context.mock(ImageSizeDefinitionManager.class); 
      cut.setDefmgr(defmgr);
      cut.setResultKey("result"); 
   }
   @Test
   public final void testHandleRequestInternalHttpServletRequestHttpServletResponse()
   {
       MockHttpServletRequest request = new MockHttpServletRequest(); 
       request.setMethod("GET"); 
       MockHttpServletResponse response = new MockHttpServletResponse();
       
       final ImageSizeDefinition sizea = new ImageSizeDefinition(){{
          setCode("a");
          setLabel("Size A"); 
       }};
       final ImageSizeDefinition sizeb = new ImageSizeDefinition(){{
          setCode("b");
          setLabel("Size B"); 
       }};
       
       try
      {
         context.checking(new Expectations(){{
             one(defmgr).getAllImageSizes();
             will(returnValue(Arrays.asList(new ImageSizeDefinition[]{sizea,sizeb}))); 
          }});
          
          ModelAndView mav = cut.handleRequest(request, response);
          
          Document result = (Document) mav.getModel().get("result"); 
          assertNotNull(result);
          log.info("Document is " + PSXmlDocumentBuilder.toString(result));
          
          PSXmlTreeWalker walker = new PSXmlTreeWalker(result.getDocumentElement());
          walker.getNextElement("PSXEntry", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
          log.info("current element " + walker.getCurrentNodeName());
          walker.getNextElement("PSXDisplayText", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
          String display = walker.getElementData(); 
          assertEquals("Size A", display); 
          walker.getNextElement("Value", PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS); 
          String value = walker.getElementData();
          assertEquals("a", value); 
          
      } catch (Exception ex)
      {         
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception Caught"); 
      } 
   }
}
