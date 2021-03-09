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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.servlets.taglib;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag renders the navigation for a ui page. 
 * 
 * @author dougrand
 */
public class PSPageSidenavTag extends PSPageBaseTag
{
   /**
    * The name of the component
    */
   protected String m_component;
   
   /**
    * The name of the page
    */
   protected String m_page;

   @Override
   public int doStartTag() throws JspException
   {
      JspWriter out = m_context.getOut();
      try
      {
         Map<String,String> extra = new HashMap<>();
         extra.put("sys_pagename",m_page);
         out.print(getUrlContent(m_component, extra));
      }
      catch (Exception e)
      {
         throw new JspException("Problem while rendering sidenav", e);
      }
      return 0;
   }

   @Override
   public int doEndTag() throws JspException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * @return Returns the component.
    */
   public String getComponent()
   {
      return m_component;
   }

   /**
    * @param component The component to set.
    */
   public void setComponent(String component)
   {
      m_component = component;
   }
   
   /**
    * @return Returns the page.
    */
   public String getPage()
   {
      return m_page;
   }

   /**
    * @param page The page to set.
    */
   public void setPage(String page)
   {
      m_page = page;
   }  

}
