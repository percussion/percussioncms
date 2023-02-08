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

import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

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
