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
