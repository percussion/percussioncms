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
      import org.apache.commons.logging.Log;
      import org.apache.commons.logging.LogFactory;
      
      public class ImageService
      {
      private static Log log = LogFactory.getLog(ImageService.class);
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
