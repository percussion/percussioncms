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

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

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
