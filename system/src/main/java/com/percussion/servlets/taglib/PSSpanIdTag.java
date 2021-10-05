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
 * The span id tag allows the creation of elements in a JSF tree that have
 * calculate ids, which is useful for updating said elements using Ajax
 * 
 * @author dougrand
 *
 */
public class PSSpanIdTag extends PSJSFBaseTag
{
   /**
    * The id
    */
   String m_definedid;
   
   /**
    * A CSS style to be applied
    */
   String m_inlineStyle;
   
   @Override
   public String getComponentType()
   {
      return "com.percussion.jsf.SpanId";
   }

   /* (non-Javadoc)
    * @see com.percussion.servlets.taglib.PSJSFBaseTag#setProperties(javax.faces.component.UIComponent)
    */
   @Override
   protected void setProperties(UIComponent comp)
   {
      super.setProperties(comp);
      setValueBinding(comp, "definedid", m_definedid);
      setValueBinding(comp, "inlinestyle", m_inlineStyle);
   }
   
   /**
    * @return the id
    */
   public String getDefinedid()
   {
      return m_definedid;
   }

   /**
    * @param definedid the id to set
    */
   public void setDefinedid(String definedid)
   {
      m_definedid = definedid;
   }
   
   /**
    * @return the inlineStyle
    */
   public String getInlineStyle()
   {
      return m_inlineStyle;
   }

   /**
    * @param inlineStyle the inlineStyle to set
    */
   public void setInlineStyle(String inlineStyle)
   {
      m_inlineStyle = inlineStyle;
   }
}
