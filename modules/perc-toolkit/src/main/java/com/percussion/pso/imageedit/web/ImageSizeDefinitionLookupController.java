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
