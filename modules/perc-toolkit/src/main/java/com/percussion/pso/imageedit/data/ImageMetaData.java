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

import java.io.Serializable;
public class ImageMetaData implements Serializable
{
   
   private static final long serialVersionUID = -13542359L;

   private String mimeType; 
   private String ext; 
   private String filename; 
   
   private long size = 0L;
   private int width = 0;
   private int height = 0;
   
   /**
    * Default constructor. 
    */
   public ImageMetaData()
   {
      super(); 
   }
   
   /**
    * Copy constructor. 
    * @param o other 
    */
   public ImageMetaData(ImageMetaData o)
   {
      this.filename = o.getFilename();
      this.ext = o.getExt();
      this.mimeType = o.getMimeType();
      this.size = o.getSize();
      this.height = o.getHeight(); 
      this.width = o.getWidth();
   }
   /**
    * @return the mimeType
    */
   public String getMimeType()
   {
      return mimeType;
   }
   /**
    * @param mimeType the mimeType to set
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }
   /**
    * @return the ext
    */
   public String getExt()
   {
      return ext;
   }
   /**
    * @param ext the ext to set
    */
   public void setExt(String ext)
   {
      this.ext = ext;
   }
   /**
    * @return the filename
    */
   public String getFilename()
   {
      return filename;
   }
   /**
    * @param filename the filename to set
    */
   public void setFilename(String filename)
   {
      this.filename = filename;
   }

   /**
    * @return the size
    */
   public long getSize()
   {
      return size;
   }
   /**
    * @param size the size to set
    */
   public void setSize(long size)
   {
      this.size = size;
   }
   /**
    * @return the width
    */
   public int getWidth()
   {
      return width;
   }
   /**
    * @param width the width to set
    */
   public void setWidth(int width)
   {
      this.width = width;
   }
   /**
    * @return the height
    */
   public int getHeight()
   {
      return height;
   }
   /**
    * @param height the height to set
    */
   public void setHeight(int height)
   {
      this.height = height;
   }
   
   public String toString() 
   {
      return new ToStringBuilder(this)
      .append("filename", filename)
      .append("MIME type", mimeType)
      .append("size", size)
      .append("height", height)
      .append("width", width)
      .toString();
   }

}
