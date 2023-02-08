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

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("MasterImageMetaData{");
      sb.append("sysTitle='").append(sysTitle).append('\'');
      sb.append(", displayTitle='").append(displayTitle).append('\'');
      sb.append(", description='").append(description).append('\'');
      sb.append(", alt='").append(alt).append('\'');
      sb.append(", sizedImages=").append(sizedImages);
      sb.append('}');
      return sb.toString();
   }
}
