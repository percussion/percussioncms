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
