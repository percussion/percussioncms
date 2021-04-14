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
