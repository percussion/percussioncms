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

import java.io.IOException;

import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;

/**
 * Handle the input and rendering for the menu item
 * 
 * @author dougrand
 */
public class PSUIMenuItem extends HtmlCommandLink
{

   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      ResponseWriter writer = context.getResponseWriter();
      writer.startElement("li", this);
      String href = (String) getAttributes().get("url") ;
      if (href != null)
      {
         String title = (String) getAttributes().get("title") ;
         writer.startElement("a", this);
         writer.writeAttribute("href", href, null);
         if (StringUtils.isNotBlank(title))
         {
            writer.writeAttribute("title", title, null);
         }
      }
      else if (getAction() != null)
      {
         super.encodeBegin(context);
      }
   }

   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      ResponseWriter writer = context.getResponseWriter();
      String href = (String) getAttributes().get("url") ;
      if (href != null)
      {
         writer.endElement("a");
      }
      else
      {
         super.encodeEnd(context);
      }
      writer.endElement("li");
   }
}
