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
package com.percussion.pso.imageedit.services.cache;
import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;
public interface ImageCacheManager
{
   /**
    * Adds an image to the cache.
    * @param data the image data
    * @return the image key.  Use this key to retrieve the image or its metadata. 
    */
   public String addImage(ImageData data);
   
   /**
    * Gets an image from the cache.  The image includes the binary data. 
    * @param imageKey the image key
    * @return the image data, will be <code>null</code> if the image is not in the cache. 
    */
   public ImageData getImage(String imageKey);
   
   /**
    * Gets an image metadata from the cache.  The metadata does not include the binary data. 
    * @param imageKey the image key
    * @return the image metadata, will be <code>null</code> if the image is not in the cache. 
    */
   public ImageMetaData getImageMetaData(String imageKey); 
   
   /**
    * Removes an image from the cache. Images should be removed when the application has finished with them. 
    * The cache configuration controls whether unused images get flushed over time. 
    * 
    * @param imageKey the image key.
    */
   public void removeImage(String imageKey);
   
   /**
    * Tests if an image is in the cache. 
    * @param imageKey the image key
    * @return <code>true</code> if the image is in the cache. 
    */
   public boolean hasImage(String imageKey); 
}
