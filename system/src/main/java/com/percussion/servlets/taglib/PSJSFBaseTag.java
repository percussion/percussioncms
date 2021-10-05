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


import org.apache.commons.lang.StringUtils;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


/**
 * Base class for our JSF tags, provides basic implementations inspired by
 * the core jsf book.
 * 
 * @author dougrand
 *
 */
public abstract class PSJSFBaseTag extends UIComponentTag
{
   /**
    * The label for the component.
    */
   private String m_label;

   @Override
   public String getRendererType()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   @Override
   protected void setProperties(UIComponent comp)
   {
      super.setProperties(comp);
      setValueBinding(comp, "label", m_label);
   }

   /**
    * Process the value binding, and set the property on the component.
    * @param comp the component
    * @param name the name of the property, never <code>null</code> or empty
    * @param value the value, may be <code>null</code> or empty
    */
   @SuppressWarnings("unchecked")
   protected void setValueBinding(UIComponent comp, String name, String value)
   {
      if (comp == null)
      {
         throw new IllegalArgumentException("comp may not be null");
      }
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException(
               "name may not be null or empty");
      }
      if (StringUtils.isBlank(value))
      {
         return;
      }
      if (!isValueReference(value))
      {
         comp.getAttributes().put(name, value);
      }     
      else
      {
         FacesContext ctx = FacesContext.getCurrentInstance();
         Application app = ctx.getApplication();
         ValueBinding vb = app.createValueBinding(value);
         comp.setValueBinding(name, vb);
      }
   }

   /**
    * Create a method binding.
    * 
    * @param comp the component, never <code>null</code>
    * @param name the name of the property, never <code>null</code> or empty.
    * @param value the value, may be <code>null</code> or empty.
    * @param params the parameter classes used by the called method, 
    *   or <code>null</code> 
    */
   @SuppressWarnings("unchecked")
   protected void setMethodBinding(UIComponent comp, String name, String value, Class[] params)
   {
      if (comp == null)
      {
         throw new IllegalArgumentException("comp may not be null");
      }
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException(
               "name may not be null or empty");
      }
      if (StringUtils.isBlank(value))
      {
         return;
      }
      FacesContext ctx = FacesContext.getCurrentInstance();
      Application app = ctx.getApplication();
      MethodBinding mb = app.createMethodBinding(value, params);
      comp.getAttributes().put(name, mb);
   }

   /**
    * @return the label
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * @param label the label to set
    */
   public void setLabel(String label)
   {
      m_label = label;
   }
   
}
