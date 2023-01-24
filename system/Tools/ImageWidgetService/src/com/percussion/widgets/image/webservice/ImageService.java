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

package com.percussion.widgets.image.webservice;
      
      import com.percussion.widgets.image.data.CachedImageMetaData;
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import com.percussion.widgets.image.services.ImageResizeManager;
      import java.awt.Dimension;
      import java.awt.Rectangle;
      import java.io.ByteArrayInputStream;
      import java.io.InputStream;
      import org.apache.commons.lang.Validate;
      import org.apache.logging.log4j.Logger;
      import org.apache.logging.log4j.LogManager;
      
      public class ImageService
      {
      private static final Logger log = LogManager.getLogger(ImageService.class);
        private ImageResizeManager resizeManager;
        private ImageCacheManager cacheManager;
      
        public CachedImageMetaData getImageMetadata(String imageKey)
        {
        return this.cacheManager.getImageMetaData(imageKey);
        }
      
        public CachedImageMetaData resizeImage(ResizeImageRequest request)
        {
        Dimension size = null;
        Rectangle cropBox = null;
        int rotate = 0;
      
        Validate.notEmpty(request.getImageKey(), "You must supply an image key");
      
        if ((request.getWidth() != 0) || (request.getHeight() != 0))
          {
          size = new Dimension(request.getWidth(), request.getHeight());
          }
      
        if ((request.getX() != 0) && (request.getY() != 0) && (request.getDeltaX() != 0) && (request.getDeltaY() != 0))
          {
          cropBox = new Rectangle(request.getX(), request.getY(), request.getDeltaX(), request.getDeltaY());
          }

          ImageData iData = this.cacheManager.getImage(request.getImageKey());
          Validate.notNull(iData, "Image to be resized was not found");
          try(InputStream is = new ByteArrayInputStream(iData.getBinary())){
              ImageData rData = this.resizeManager.generateImage(is, cropBox, size, rotate);
              String key = this.cacheManager.addImage(rData);
                return new CachedImageMetaData(rData, key);
          }
          catch (Exception ex) {
             log.error("Unexpected Exception " + ex, ex);
             throw new RuntimeException(ex.getLocalizedMessage(), ex);
          }
        }
      
        public ImageResizeManager getResizeManager()
        {
        	return this.resizeManager;
        }
      
        public void setResizeManager(ImageResizeManager resizeManager)
        {
        	this.resizeManager = resizeManager;
        }
      
        public ImageCacheManager getCacheManager()
        {
        	return this.cacheManager;
        }
      
        public void setCacheManager(ImageCacheManager cacheManager)
        {
        	this.cacheManager = cacheManager;
        }
      }
