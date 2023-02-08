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
import org.apache.myfaces.shared_impl.taglib.UIComponentTagUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * The tag that implements the actual menu item for the CSS menu implementation.
 * 
 * @author dougrand
 *
 */
public class PSMenuItemTag extends PSJSFBaseTag
{
   /**
    * An optional action for the component.
    */
   private String m_action;
   
   /**
    * An optional url for the component.
    */
   private String m_url;
   
   /**
    * The shown string for the link.
    */
   private String m_value;
   
   /**
    * An optional tooltip to show for the item.
    */
   private String m_title;
   
   /**
    * Causes the JSF submittal to skip the validation phase.
    */
   private String m_immediate;
   
   /**
    * An optional to call a JavaScript when click at the component.
    */
   private String m_onclick;
   
   /**
    * Switches rendering on or off for the component.
    */
   private String m_rendered;

   @Override
   public String getComponentType()
   {
      return "com.percussion.jsf.MenuItem";
   }
   
   /* (non-Javadoc)
    * @see com.percussion.servlets.taglib.PSJSFBaseTag#setProperties(javax.faces.component.UIComponent)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void setProperties(UIComponent comp)
   {
      super.setProperties(comp);
      if (StringUtils.isNotBlank(m_action))
      {
         FacesContext ctx = FacesContext.getCurrentInstance();
         UIComponentTagUtils.setActionProperty(ctx, comp, m_action);
      }
      setValueBinding(comp, "url", m_url);
      setValueBinding(comp, "value", m_value);
      setValueBinding(comp, "title", m_title);
      setValueBinding(comp, "rendered", m_rendered);
      setValueBinding(comp, "onclick", m_onclick);

      boolean immediate = m_immediate != null && 
         ("t".equals(m_immediate.toLowerCase()) ||
         "true".equals(m_immediate.toLowerCase()));
      comp.getAttributes().put("immediate", immediate);
   }

   /**
    * Get the onclick property.
    * @return the onclick property value, may be <code>null</code> or empty.
    */
   public String getOnclick()
   {
      return m_onclick;
   }
   
   /**
    * Set the onclick attribute.
    * @param onclick the new onclick value, may be <code>null</code> or empty.
    */
   public void setOnclick(String onclick)
   {
      m_onclick = onclick;
   }
   
   /**
    * @return the action
    */
   public String getAction()
   {
      return m_action;
   }

   /**
    * @param action the action to set
    */
   public void setAction(String action)
   {
      m_action = action;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return m_url;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      m_url = url;
   }

   /**
    * @return the value
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * @param value the value to set
    */
   public void setValue(String value)
   {
      m_value = value;
   }

   /**
    * @return the title
    */
   public String getTitle()
   {
      return m_title;
   }

   /**
    * @param title the title to set
    */
   public void setTitle(String title)
   {
      m_title = title;
   }

   /**
    * @return the immediate
    */
   public String getImmediate()
   {
      return m_immediate;
   }

   /**
    * @param immediate the immediate to set
    */
   public void setImmediate(String immediate)
   {
      m_immediate = immediate;
   }

   /**
    * @return the rendered
    */
   public String getRendered()
   {
      return m_rendered;
   }

   /**
    * @param rendered the rendered to set
    */
   @Override
   public void setRendered(String rendered)
   {
      m_rendered = rendered;
   }
}
