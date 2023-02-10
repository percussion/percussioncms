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
package com.percussion.servlets.taglib;

import javax.faces.component.UIComponent;

/**
 * The tag that implements the progress bar.
 */
public class PSProgressBarTag extends PSJSFBaseTag
{
   /**
    * It holds the value of "percent" attribute.
    */
   private String m_percent;

   @Override
   public String getComponentType()
   {
      return "com.percussion.jsf.ProgressBar";
   }
   
   /* (non-Javadoc)
    * @see com.percussion.servlets.taglib.PSJSFBaseTag#setProperties(javax.faces.component.UIComponent)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void setProperties(UIComponent comp)
   {
      super.setProperties(comp);
      setValueBinding(comp, "percent", m_percent);
   }

   /**
    * @return the value of "percent" attribute, may be <code>null</code> or 
    * empty.
    */
   public String getPercent()
   {
      return m_percent;
   }
   
   /**
    * Set the value of "percent" attribute
    * 
    * @param percent the value of percent, may be <code>null</code> or empty.
    */
   public void setPercent(String percent)
   {
      m_percent = percent;  
   }
}
