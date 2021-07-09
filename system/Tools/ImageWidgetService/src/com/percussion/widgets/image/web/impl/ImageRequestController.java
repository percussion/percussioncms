      /*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
        	log.debug("Image key is " + imageKey);
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
