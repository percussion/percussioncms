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
