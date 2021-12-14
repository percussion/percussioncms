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

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class MasterImageMetaData extends AbstractImageMetaData 
{
   private String sysTitle;
   private String displayTitle;
   private String description;
   private String alt; 
   private Map<String,SizedImageMetaData> sizedImages;

   public MasterImageMetaData()
   {
      super();
      sizedImages = new LinkedHashMap<String, SizedImageMetaData>();
   }
   /**
    * @return the sysTitle
    */
   public String getSysTitle()
   {
      return sysTitle;
   }

   /**
    * @param sysTitle the sysTitle to set
    */
   public void setSysTitle(String sysTitle)
   {
      this.sysTitle = sysTitle;
   }

   /**
    * @return the displayTitle
    */
   public String getDisplayTitle()
   {
      return displayTitle;
   }

   /**
    * @param displayTitle the displayTitle to set
    */
   public void setDisplayTitle(String displayTitle)
   {
      this.displayTitle = displayTitle;
   }

   /**
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the alt
    */
   public String getAlt()
   {
      return alt;
   }

   /**
    * @param alt the alt to set
    */
   public void setAlt(String alt)
   {
      this.alt = alt;
   }

   /**
    * Gets the sized images. 
    * @return the sizedImages. Never <code>null</code> may be <code>empty</code>. 
    */
   public Map<String,SizedImageMetaData> getSizedImages() 
   {
      return sizedImages;
   }

   /**
    * @param sizedImages the sizedImages to set
    */
   public void setSizedImages(Map<String,SizedImageMetaData> sizedImages)
   {
      this.sizedImages = sizedImages;
   }
   
   /**
    * @param sizedImages the sizedImages to set
    */
   public void addSizedImage(SizedImageMetaData sizedImage)
   {
      String key = sizedImage.getSizeDefinition().getCode();
      this.sizedImages.put(key, sizedImage);
   }
   
   public void clearSizedImages()
   {
      this.sizedImages = new LinkedHashMap<String, SizedImageMetaData>(); 
     
   }
   public String toString() 
   {
      return ToStringBuilder.reflectionToString(this);
   }

   
   
}
