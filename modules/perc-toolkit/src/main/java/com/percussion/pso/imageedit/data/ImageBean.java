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
