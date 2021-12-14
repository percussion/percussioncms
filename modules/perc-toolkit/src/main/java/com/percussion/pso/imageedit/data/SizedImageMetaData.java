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

/**
 * A Sized Image. 
 *
 * @author DavidBenua
 *
 */
public class SizedImageMetaData extends SimpleImageMetaData
{
    private ImageSizeDefinition sizeDefinition;

	private int x = 0;
	private int y = 0;
	private Boolean constraint = true;

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

/**
    * @return the sizeDefinition
    */
   public ImageSizeDefinition getSizeDefinition()
   {
      return sizeDefinition;
   }

   /**
    * @param sizeDefinition the sizeDefinition to set
    */
   public void setSizeDefinition(ImageSizeDefinition sizeDefinition)
   {
      this.sizeDefinition = sizeDefinition;
   }
    
   public String toString() 
   {
      return ToStringBuilder.reflectionToString(this);
   }

public Boolean isConstraint()
{
	return constraint;
}

public void setConstraint(Boolean constraint)
{
	this.constraint = constraint;
}

   
}
