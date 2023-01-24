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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageSizeDefinition
{
   private String code;  
   private String label; 
   private int height; 
   private int width;
   private String snippetTemplate; 
   private String binaryTemplate;
   /**
    * @return the code
    */
   public String getCode()
   {
      return code;
   }
   /**
    * @param code the code to set
    */
   public void setCode(String code)
   {
      this.code = code;
   }
   /**
    * @return the label
    */
   public String getLabel()
   {
      return label;
   }
   /**
    * @param label the label to set
    */
   public void setLabel(String label)
   {
      this.label = label;
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
    * @return the snippetTemplate
    */
   public String getSnippetTemplate()
   {
      return snippetTemplate;
   }
   /**
    * @param snippetTemplate the snippetTemplate to set
    */
   public void setSnippetTemplate(String snippetTemplate)
   {
      this.snippetTemplate = snippetTemplate;
   }
   /**
    * @return the binaryTemplate
    */
   public String getBinaryTemplate()
   {
      return binaryTemplate;
   }
   /**
    * @param binaryTemplate the binaryTemplate to set
    */
   public void setBinaryTemplate(String binaryTemplate)
   {
      this.binaryTemplate = binaryTemplate;
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("ImageSizeDefinition{");
      sb.append("code='").append(code).append('\'');
      sb.append(", label='").append(label).append('\'');
      sb.append(", height=").append(height);
      sb.append(", width=").append(width);
      sb.append(", snippetTemplate='").append(snippetTemplate).append('\'');
      sb.append(", binaryTemplate='").append(binaryTemplate).append('\'');
      sb.append('}');
      return sb.toString();
   }
}
