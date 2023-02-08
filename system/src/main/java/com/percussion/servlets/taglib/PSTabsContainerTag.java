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

import com.percussion.rx.ui.jsf.beans.PSTopNavigation;
import com.percussion.rx.ui.jsf.beans.PSTopNavigation.Tab;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Output a set of tabs for the banner. This tag progressively evaluates through
 * the tabs in the {@link PSTopNavigation} model. Each tab have information about
 * whether it is selected and/or enabled, which is used when rendering.
 * 
 * @author dougrand
 */
public class PSTabsContainerTag extends BodyTagSupport
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * The value to be used when rendering.
    */
   PSTopNavigation m_value;

   /**
    * A string naming the variable to be bound when evaluating the tab facet.
    */
   String m_var;

   /**
    * A CSS style to be applied to the tabs containing DIV.
    */
   String m_inlineStyle;

   /**
    * Page context, set by the system, assumed never <code>null</code>.
    */
   PageContext m_context;
   
   /**
    * Buffer to build the content in. Never <code>null</code>, may be empty.
    */
   StringBuilder m_buffer = new StringBuilder();

   /**
    * @return the value
    */
   public Object getValue()
   {
      return m_value;
   }

   /**
    * @param value the value to set
    */
   public void setValue(Object value)
   {
      if (! (value instanceof PSTopNavigation))
      {
         throw new IllegalArgumentException("Value must be a PSTopNavigation");
      }
      m_value = (PSTopNavigation) value;
   }

   /**
    * @return the var
    */
   public String getVar()
   {
      return m_var;
   }

   /**
    * @param var the var to set
    */
   public void setVar(String var)
   {
      m_var = var;
   }
   

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.jsp.tagext.TagSupport#setPageContext(javax.servlet.jsp.PageContext)
    */
   @Override
   public void setPageContext(PageContext pageContext)
   {
      super.setPageContext(pageContext);
      m_context = pageContext;
   }

   /*
    * (non-Javadoc)
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
    */
   @Override
   public int doStartTag() throws JspException
   {
      JspWriter writer = m_context.getOut();
      try
      {
         writer.print("<table border='0' cellspacing='0' cellpadding='0' " +
               "class='rx-tabs-panel' width='500'><tr><td valign='bottom'>");

         writer.print("<ol id=\"rxTabPanel\">");
         m_buffer.setLength(0);
      }
      catch (IOException e)
      {
         throw new JspException(e);
      }
      if (m_value.getRowCount() == 0)
         return SKIP_BODY;
      else
         return EVAL_BODY_BUFFERED;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doInitBody()
    */
   @Override
   public void doInitBody()
   {
      m_value.setRowIndex(0);
      Tab tab = (Tab) m_value.getRowData();
      m_context.setAttribute(m_var, tab);
      writeEntry(tab);      
   }

   /**
    * Write the tab entry to the buffer.
    * 
    * @param tab the tab instance, assumed not <code>null</code>.
    * 
    */
   private void writeEntry(Tab tab)
   {
      if (!tab.getEnabled())
         return;

      m_buffer.append("<li");
      if (tab.isSelected())
      {
         m_buffer.append(" class=\"rxTabSelected\"");
      }
      m_buffer.append("><a target=\"_parent\" href=\"");
      m_buffer.append(tab.getUrl());
      m_buffer.append("\"><span>");
      m_buffer.append(tab.getLabel());
      m_buffer.append("</span></a></li>\n");

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
    */
   @Override
   public int doAfterBody()
   {
      m_value.setRowIndex(m_value.getRowIndex() + 1);

      if (m_value.isRowAvailable())
      {
         Tab tab = (Tab) m_value.getRowData();
         m_context.setAttribute(m_var, tab);
         writeEntry(tab);
         return EVAL_BODY_AGAIN;
      }
      else
      {
         return SKIP_BODY;
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
    */
   @Override
   public int doEndTag() throws JspException
   {
      JspWriter writer = m_context.getOut();
      try
      {
         writer.print(m_buffer.toString());
         writer.print("</ol></td></tr></table>");
      }
      catch (IOException e)
      {
         throw new JspException(e);
      }
      return EVAL_PAGE;
   }

}
