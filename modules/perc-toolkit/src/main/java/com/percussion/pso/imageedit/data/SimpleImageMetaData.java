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
