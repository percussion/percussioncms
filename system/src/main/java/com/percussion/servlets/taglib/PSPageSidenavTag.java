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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.HashMap;
import java.util.Map;

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
