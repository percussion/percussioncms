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

/**
 * A Simple image with height and width.
 *
 * @author DavidBenua
 *
 */
public class SimpleImageMetaData extends AbstractImageMetaData
{

   /**
    * width in pixels.
    */
   private int width = 0;
   /**
    * height in pixels.
    */
   private int height = 0;

   /**
    * Default constructor.
    */
   public SimpleImageMetaData()
   {
      
   }
   
   /**
    * Copy constructor.
    * @param data metadata to copy from. 
    */
   public SimpleImageMetaData(AbstractImageMetaData data)
   {
      this.setImageKey(data.getImageKey());
      this.setMetaData(data.getMetaData());
      this.height = data.getMetaData().getHeight();
      this.width = data.getMetaData().getWidth(); 
   }
   /**
    * Gets the width.
    * @return the width.
    */
   public int getWidth()
   {
    	return width;
    }

   /**
    * Sets the width.
    * @param width the width to set. 
    */
   public void setWidth(int width)
   {
    	this.width = width;
    }

   /**
    * Gets the height.
    * @return the height.
    */
   public int getHeight()
   {
    	return height;
    }

   /**
    * Sets the height. 
    * @param height the height to set.  
    */
   public void setHeight(int height)
   {
    	this.height = height;
    }
   
   
}
