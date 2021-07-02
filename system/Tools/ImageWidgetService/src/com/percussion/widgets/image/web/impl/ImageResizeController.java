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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.widgets.image.web.impl;

      import com.percussion.widgets.image.data.CachedImageMetaData;
import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.services.ImageCacheManager;
import com.percussion.widgets.image.services.ImageResizeManager;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
      import org.springframework.web.bind.annotation.PostMapping;
      import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

      @Controller
      @RequestMapping("/imageWidget/resizeImage.do")
      public class ImageResizeController
      {
        private static final Logger log = LogManager.getLogger(ImageResizeController.class);
        private String viewName;
        private String modelObjectName;
        private ImageCacheManager imageCacheManager;
        private ImageResizeManager imageResizeManager;

        @PostMapping()
        public ModelAndView handle(@ModelAttribute("results") ResizeImageBean bean, BindingResult result)
        {
          ModelAndView mav = new ModelAndView("imageWidgetJSONView");
          try
          {

          CachedImageMetaData cimd = resizeImage(bean);
          JSON json = JSONSerializer.toJSON(cimd);
          mav.addObject(getModelObjectName(), json);
          }
          catch (Exception ex)
          {
          String emsg = "Unexpected exception " + ex.getLocalizedMessage();
          log.error(emsg, ex);
          JSON json = new JSONObject().accumulate("error", emsg);
          mav.addObject(getModelObjectName(), json);
          }

        return mav;
        }
      
        protected CachedImageMetaData resizeImage(ResizeImageBean bean)
          throws Exception
        {
        Dimension size = null;
        Rectangle cropBox = null;
        int rotate = 0;
      
        Validate.notEmpty(bean.getImageKey(), "You must supply an image key");
      
        if ((bean.getWidth() != 0) || (bean.getHeight() != 0))
          {
         size = new Dimension(bean.getWidth(), bean.getHeight());
         log.debug("new image size is " + size);
          }
      
       if ((bean.getX() != 0) && (bean.getY() != 0) && (bean.getDeltaX() != 0) && (bean.getDeltaY() != 0))
          {
         cropBox = new Rectangle(bean.getX(), bean.getY(), bean.getDeltaX(), bean.getDeltaY());
         log.debug("new image crop box is " + cropBox);
          }
       if (bean.getRotate() != 0)
          {
         rotate = bean.getRotate();
         log.debug("rotate is " + rotate);
          }
       ImageData iData = this.imageCacheManager.getImage(bean.getImageKey());
       Validate.notNull(iData, "Image to be resized was not found");
       InputStream is = new ByteArrayInputStream(iData.getBinary());
       ImageData rData = this.imageResizeManager.generateImage(is, cropBox, size, rotate);
       String key = this.imageCacheManager.addImage(rData);
       return new CachedImageMetaData(rData, key);
        }
      
        public String getViewName()
        {
        	return "imageWidgetJSONView";
        }
      
        public void setViewName(String viewName)
        {
        	this.viewName = viewName;
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
      
        public ImageResizeManager getImageResizeManager()
        {
        	return this.imageResizeManager;
        }
      
        public void setImageResizeManager(ImageResizeManager imageResizeManager)
        {
        	this.imageResizeManager = imageResizeManager;
        }
      }
