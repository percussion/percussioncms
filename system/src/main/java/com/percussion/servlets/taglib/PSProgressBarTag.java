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
