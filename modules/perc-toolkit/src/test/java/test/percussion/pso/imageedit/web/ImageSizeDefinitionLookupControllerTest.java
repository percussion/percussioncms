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
package test.percussion.pso.imageedit.web;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   private static final Logger log = LogManager.getLogger(ImageSizeDefinitionLookupControllerTest.class);
   
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
