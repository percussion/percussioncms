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

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Handle the input and rendering for the progress bar
 */
public class PSUIProgressBar extends UIOutput
{

   /**
    * The percentage of the progress bar. 
    */
   private int m_percent;

   /**
    * @return the value of "percent" attribute. Default to <code>-1</code>
    *    if any error occurs.
    */
   private int getPercent()
   {
      String v = (String) getAttributes().get("percent");
      int percent = -1;
      try
      {
         percent = Integer.parseInt(v);
      }
      catch (Exception e)
      {
         // ignore error input
      }
      
      return percent;
   }
   
   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered())
         return;
      
      m_percent = getPercent();

      if (m_percent < 0 || m_percent > 100)
      {
         setRendered(false);
         return; // do nothing if not in expected range (0 - 100)
      }
      
      ResponseWriter writer = context.getResponseWriter();
      writer.startElement("div", this);
      writer.writeAttribute("style", "width: 90%; border: 1px solid black; margin-bottom: 8px; margin-left: 5px; margin-right: 5px; padding: 0", null);
      writer.startElement("div", this);
      String v = "width: " + m_percent + "%; height: 8px;  background-color: blue";
      writer.writeAttribute("style", v, null);
   }

   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (! isRendered())
         return; // do nothing if not rendering for some reason

      ResponseWriter writer = context.getResponseWriter();
      writer.endElement("div");
      writer.endElement("div");
   }
}
