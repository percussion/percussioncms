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

package com.percussion.widgets.image.web.impl;
      
      import com.percussion.widgets.image.data.CachedImageMetaData;
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import javax.servlet.http.HttpServletRequest;
      import javax.servlet.http.HttpServletResponse;
      import net.sf.json.JSON;
      import net.sf.json.JSONSerializer;
      import org.apache.commons.lang.StringUtils;
      import org.apache.logging.log4j.Logger;
      import org.apache.logging.log4j.LogManager;
      import org.springframework.web.servlet.ModelAndView;
      import org.springframework.web.servlet.mvc.Controller;
      import org.springframework.web.servlet.mvc.ParameterizableViewController;
      
      public class ImageRequestController extends ParameterizableViewController
        implements Controller
      {
    	  private static final Logger log = LogManager.getLogger(ImageRequestController.class);
    	  private String modelObjectName = "results";
    	  private ImageCacheManager imageCacheManager = null;
      
        protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
          throws Exception
        {
        	ModelAndView mav = super.handleRequestInternal(request, response);
      
        	String imageKey = request.getParameter("imageKey");
        	if (StringUtils.isBlank(imageKey))
          {
        		String emsg = "Image Key was null";
        		log.error(emsg);
        		response.sendError(400, emsg);
        		return null;
          }
        	log.debug("Image key is {}", imageKey);
        	if (!this.imageCacheManager.hasImage(imageKey))
          {
        		String emsg = "The image was not found";
        		log.info(emsg);
        		response.sendError(404, emsg);
        		return null;
          }
        	ImageData data = this.imageCacheManager.getImage(imageKey);
        	CachedImageMetaData cimd = new CachedImageMetaData(data, imageKey);
        	JSON json = JSONSerializer.toJSON(cimd);
        	mav.addObject(this.modelObjectName, json);
      
        	return mav;
        }
      
        public String getModelObjectName()
        {
        	return this.modelObjectName;
        }
      
        public void setModelObjectName(String modelObjectName)
        {
        	this.modelObjectName = modelObjectName;
        }
      
        public ImageCacheManager getImageCacheManager()
        {
        	return this.imageCacheManager;
        }
      
        public void setImageCacheManager(ImageCacheManager imageCacheManager)
        {
        	this.imageCacheManager = imageCacheManager;
        }
      }
