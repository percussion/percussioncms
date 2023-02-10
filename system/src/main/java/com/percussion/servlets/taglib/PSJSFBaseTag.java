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
