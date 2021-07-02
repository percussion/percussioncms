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

package com.percussion.widgets.image.services.impl;
      
      import com.percussion.widgets.image.data.CachedImageMetaData;
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.data.ImageMetaData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import net.sf.ehcache.Cache;
      import net.sf.ehcache.Element;
      import org.apache.commons.lang.StringUtils;
      import org.apache.logging.log4j.Logger;
      import org.apache.logging.log4j.LogManager;
      
      public class ImageCacheManagerImpl
        implements ImageCacheManager
      {
    	  private static final Logger log = LogManager.getLogger(ImageCacheManagerImpl.class);
        private long counter;
        private Cache cache;
      
        public ImageCacheManagerImpl()
        {
        	this.counter = 1L;
        }
      
        public String addImage(ImageData data)
        {
		  String imageKey = generateKey(data);
		  	this.log.debug("new image key is " + imageKey);
		Element element = new Element(imageKey, data);
		this.cache.put(element);
		  
			return imageKey;
    }
      
        public ImageData getImage(String imageKey)
        {
        Element elem = this.cache.get(imageKey);
        if (elem == null)
          {
          return null;
          }
        return (ImageData)elem.getValue();
        }
      
        public CachedImageMetaData getImageMetaData(String imageKey)
        {
        ImageData data = getImage(imageKey);
  		 if (data != null)
          {
	  		return new CachedImageMetaData(data, imageKey);
          }
  			return null;
        }
      
        public boolean hasImage(String imageKey) {
        	return this.cache.isKeyInCache(imageKey);
        }
      
        public void removeImage(String imageKey)
        {
        	this.cache.remove(imageKey);
        }
      
        protected String generateKey(ImageMetaData data)
        {
        	long value = data.getSize() + data.getHeight() * 2;
        	String fname = data.getFilename();
  			if (StringUtils.isNotBlank(fname))
          {
	  			value -= fname.hashCode();
          }
          else
          {
        	  value -= "abc.xyz".hashCode();
          }
  			value = (value << 12) + this.counter++;
  			return Long.toHexString(value);
        }
      
        public Cache getCache()
        {
      return this.cache;
        }
      
        public void setCache(Cache cache)
        {
      this.cache = cache;
        }
      }
