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

package com.percussion.widgets.image.data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;

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
      if(ext == null || StringUtils.isEmpty(ext))
         throw new IllegalArgumentException("Extension is required");

      this.ext = ext.replace(".","");
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
