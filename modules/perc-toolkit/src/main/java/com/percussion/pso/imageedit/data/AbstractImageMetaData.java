/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.data;
public class AbstractImageMetaData
{
   private String imageKey;
   private ImageMetaData metaData;

   public AbstractImageMetaData()
   {
      super();
   }

   /**
    * @return the imageKey
    */
   public String getImageKey()
   {
      return imageKey;
   }

   /**
    * @param imageKey the imageKey to set
    */
   public void setImageKey(String imageKey)
   {
      this.imageKey = imageKey;
   }

   /**
    * @return the metaData
    */
   public ImageMetaData getMetaData()
   {
      return metaData;
   }

   /**
    * @param metaData the metaData to set
    */
   public void setMetaData(ImageMetaData metaData)
   {
      this.metaData = metaData;
   }
}
