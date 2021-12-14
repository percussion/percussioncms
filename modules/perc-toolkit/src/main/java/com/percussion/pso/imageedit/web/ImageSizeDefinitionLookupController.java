/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.web;

import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManagerLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 
 *
 * @author DavidBenua
 *
 */
@org.springframework.stereotype.Controller
public class ImageSizeDefinitionLookupController
      extends
         ParameterizableViewController implements Controller
{

   private static final Logger log = LogManager.getLogger(ImageSizeDefinitionLookupController.class);
   
   /**
    * Key for result passed to view.  Defaults to "result" 
    */
   private String resultKey = "result"; 
   
   
   private ImageSizeDefinitionManager defmgr = null; 
   
   
   /**
    * 
    */
   public ImageSizeDefinitionLookupController()
   {
      
   }

   private void initServices()
   {
      if(defmgr == null)
      {
         defmgr = ImageSizeDefinitionManagerLocator.getImageSizeDefinitionManager(); 
      }
   }
  
   /**
    * @see ParameterizableViewController#handleRequestInternal(HttpServletRequest, HttpServletResponse)
    */
   @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {      
      initServices();
      ModelAndView mav = super.handleRequestInternal(request, response);
      
      Document resultDoc = PSXmlDocumentBuilder.createXmlDocument(); 
      Element root = PSXmlDocumentBuilder.createRoot(resultDoc, "sys_Lookup");
      
      List<ImageSizeDefinition> defs = defmgr.getAllImageSizes(); 
      for(ImageSizeDefinition size : defs)
      {
         log.debug("Adding size {}", size.getCode());
         Element entry = PSXmlDocumentBuilder.addEmptyElement(resultDoc, root, "PSXEntry");
         PSXmlDocumentBuilder.addElement(resultDoc, entry, "PSXDisplayText", size.getLabel());
         PSXmlDocumentBuilder.addElement(resultDoc, entry, "Value", size.getCode());         
      }
      
      mav.addObject(resultKey, resultDoc);
      return mav; 
   }


   /**
    * Gets the result key. 
    * @return the resultKey
    */
   public String getResultKey()
   {
      return resultKey;
   }


   /**
    * Sets the result key.
    * @param resultKey the resultKey to set
    */
   public void setResultKey(String resultKey)
   {
      this.resultKey = resultKey;
   }

   /**
    * Sets the ImageSizeDefinitionManager for unit test. 
    * @param defmgr the defmgr to set
    */
   public void setDefmgr(ImageSizeDefinitionManager defmgr)
   {
      this.defmgr = defmgr;
   }
}
