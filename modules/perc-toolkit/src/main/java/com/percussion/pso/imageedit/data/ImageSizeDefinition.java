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
   
   public String toString() 
   {
      return ToStringBuilder.reflectionToString(this);
   }

}
