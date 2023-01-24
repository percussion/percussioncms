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

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.services.cache.ImageCacheManagerLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@org.springframework.stereotype.Controller
public class BinaryImageController extends AbstractController
      implements
         Controller
{
   private static final Logger log = LogManager.getLogger(BinaryImageController.class);
   
   private ImageUrlBuilder urlBuilder;  
   
   private ImageCacheManager cacheMgr = null; 
   
   public BinaryImageController()
   {
      
   }
   
   private void initServices()
   {
      if(cacheMgr == null)
      {
         cacheMgr = ImageCacheManagerLocator.getImageCacheManager();
      }
   }
   
   @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      String emsg; 
      initServices();
      String uri = request.getRequestURI();
      log.debug("uri is {}", uri);
      String imageKey = urlBuilder.extractKey(uri); 
      if(StringUtils.isBlank(imageKey))
      {
         emsg = "Image Key was null"; 
         log.error(emsg); 
         response.sendError(HttpServletResponse.SC_BAD_REQUEST, emsg ); // the url must be bad.  
         return null; 
      }
      log.debug("Image key is {}", imageKey);
      if(!cacheMgr.hasImage(imageKey))
      {
         emsg = "The image was not found"; 
         log.info(emsg);
         response.sendError(404,emsg);
         return null; 
      }
      ImageData data = cacheMgr.getImage(imageKey); 
      
      response.setContentType(data.getMimeType()); 
      int sz = Long.valueOf(data.getSize()).intValue(); 
      if(sz > 0)
      {
         log.debug("image size is {}", sz);
         response.setContentLength(sz);
         response.setStatus(HttpServletResponse.SC_OK);
         ServletOutputStream ostream = response.getOutputStream();
         ostream.write(data.getBinary()); 
         ostream.flush();
         response.flushBuffer();
         return null; 
      }
      emsg = "Image is empty"; 
      log.info(emsg); 
      response.sendError(HttpServletResponse.SC_NO_CONTENT); 
      response.flushBuffer();
      return null;
   }

   /**
    * @return the urlBuilder
    */
   public ImageUrlBuilder getUrlBuilder()
   {
      return urlBuilder;
   }

   /**
    * @param urlBuilder the urlBuilder to set
    */
   public void setUrlBuilder(ImageUrlBuilder urlBuilder)
   {
      this.urlBuilder = urlBuilder;
   }

   /**
    * @param cacheMgr the cacheMgr to set
    */
   public void setCacheMgr(ImageCacheManager cacheMgr)
   {
      this.cacheMgr = cacheMgr;
   }
}
