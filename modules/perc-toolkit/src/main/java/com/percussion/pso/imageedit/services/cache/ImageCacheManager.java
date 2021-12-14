/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
