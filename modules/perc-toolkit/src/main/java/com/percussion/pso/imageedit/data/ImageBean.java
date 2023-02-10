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

public class ImageBean
{
	private String sysTitle;
	private String displayTitle;
	private String description;
	private String alt; 
	private String sizedImages;
	private int width;
	private int height;
	private int x;
	private int y;
	private Boolean constraint = true;
	private boolean dirty = false; 
	
	
	/**
	 * is the item dirty?
     * @return the dirty flag
     */
   public boolean isDirty()
   {
      return dirty;
   }

   /**
    * Sets the dirty flag.
    * @param dirty the dirty to set
    */
   public void setDirty(boolean dirty)
   {
      this.dirty = dirty;
   }

   public Boolean isConstraint()
	{
		return constraint;
	}

	public void setConstraint(Boolean constraint)
	{
		this.constraint = constraint;
	}

	public ImageBean()
	{
		super();
	}

	public String getSysTitle()
	{
		return sysTitle;
	}
	
	public void setSysTitle(String sysTitle)
	{
		this.sysTitle = sysTitle;
	}
	
	public String getDisplayTitle()
	{
		return displayTitle;
	}
	
	public void setDisplayTitle(String displayTitle)
	{
		this.displayTitle = displayTitle;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getAlt()
	{
		return alt;
	}
	
	public void setAlt(String alt)
	{
		this.alt = alt;
	}
	
	public String getSizedImages()
	{
		return sizedImages;
	}
	
	public void setSizedImages(String sizedImages)
	{
		this.sizedImages = sizedImages;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}
		   
   
}
