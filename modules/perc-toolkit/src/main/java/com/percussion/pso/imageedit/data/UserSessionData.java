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

public class UserSessionData
{

	MasterImageMetaData mimd = null;
	String[] pages = null; 
	
	double scaleFactor = 1.0;
	
	boolean dirty = false; 
	
	SimpleImageMetaData displayImage = null; 
	//Map<String, Dimension> resizeDetails = new HashMap<String, Dimension>();
	
	public MasterImageMetaData getMimd()
	{
		return mimd;
	}
	
	public void setMimd(MasterImageMetaData mimd)
	{
		this.mimd = mimd;
	}
	
	public String[] getPages()
	{
		return pages;
	}
	
	public void setPages(String[] pages)
	{
		this.pages = pages;
	}

   /**
    * @return the scaleFactor
    */
   public double getScaleFactor()
   {
      return scaleFactor;
   }

   /**
    * @param scaleFactor the scaleFactor to set
    */
   public void setScaleFactor(double scaleFactor)
   {
      this.scaleFactor = scaleFactor;
   }

   /**
    * @return the displayImage
    */
   public SimpleImageMetaData getDisplayImage()
   {
      return displayImage;
   }

   /**
    * @param displayImage the displayImage to set
    */
   public void setDisplayImage(SimpleImageMetaData displayImage)
   {
      this.displayImage = displayImage;
   }

   public boolean isDirty()
   {
      return dirty;
   }

   public void setDirty(boolean dirty)
   {
      this.dirty = dirty;
   }
	
   
}
