/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.widgets.image.data;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ImageMetaData implements Serializable
{
   private static final long serialVersionUID = -13542359L;

   private String mimeType;

   private String ext;

   private String filename;

   private long size = 0L;

   private int width = 0;

   private int height = 0;
   
   private int thumbWidth = 0;

   public ImageMetaData()
   {
   }

   public ImageMetaData(ImageMetaData o)
   {
      this.filename = o.getFilename();
      this.ext = o.getExt();
      this.mimeType = o.getMimeType();
      this.size = o.getSize();
      this.height = o.getHeight();
      this.width = o.getWidth();
      this.thumbWidth = o.getThumbWidth();
   }

   public String getMimeType()
   {
      return this.mimeType;
   }

   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }

   public String getExt()
   {
      return this.ext;
   }

   public void setExt(String ext)
   {
      this.ext = ext;
   }

   public String getFilename()
   {
      return this.filename;
   }

   public void setFilename(String filename)
   {
      this.filename = filename;
   }

   public long getSize()
   {
      return this.size;
   }

   public void setSize(long size)
   {
      this.size = size;
   }

   public int getWidth()
   {
      return this.width;
   }

   public void setWidth(int width)
   {
      this.width = width;
   }

   public int getHeight()
   {
      return this.height;
   }

   public void setHeight(int height)
   {
      this.height = height;
   }

   public int getThumbWidth()
   {
      return thumbWidth;
   }

   public void setThumbWidth(int thumbWidth)
   {
      this.thumbWidth = thumbWidth;
   }

   public String toString()
   {
      return new ToStringBuilder(this).append("filename", this.filename).append("MIME type", this.mimeType)
            .append("size", this.size).append("height", this.height).append("width", this.width).append("thumbWidth", this.thumbWidth).toString();
   }
}
