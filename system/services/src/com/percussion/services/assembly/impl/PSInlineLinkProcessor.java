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
package com.percussion.services.assembly.impl;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import com.percussion.utils.xml.PSSaxHelper;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * The inline link processor substitutes assembled templates and links for
 * elements in the body.
 * 
 * @author dougrand
 */
public class PSInlineLinkProcessor implements IPSPropertyInterceptor
{
   /**
    * The filter to use with the links
    */
   private IPSItemFilter m_itemFilter = null;

   /**
    * Work item being processed
    */
   private IPSAssemblyItem m_workItem = null;

   /**
    * Create a new inline link processor. One instance should be created for
    * each property to be processed.
    * 
    * @param filter the item filter, never <code>null</code>
    * @param workitem the work item being assembled, never <code>null</code>
    */
   public PSInlineLinkProcessor(IPSItemFilter filter,
         IPSAssemblyItem workitem) {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }
      if (workitem == null)
      {
         throw new IllegalArgumentException("workitem may not be null");
      }
      m_itemFilter = filter;
      m_workItem = workitem;
   }

   public Object translate(Object originalValue)
   {
      if (originalValue == null)
         return null;

      if (originalValue instanceof String)
      {
         if (StringUtils.isBlank((String) originalValue))
         {
            return originalValue;
         }

         try
         {
            return processInlineLinks((String) originalValue);
         }
         catch (Exception e)
         {
            PSTrackAssemblyError
               .addProblem("Problem processing inline links", e);
            PSXmlEncoder enc = new PSXmlEncoder();
            Log log = LogFactory.getLog(PSInlineLinkProcessor.class);
            log.warn("Problem processing inline links", e);
            StringBuilder message = new StringBuilder();
            message
                  .append("<div style='border: 2px solid red; background-color: #FFEEEE'>");
            message.append(PSI18nUtils
                  .getString("psx_assembly@Error processing inline link"));
            message.append(" ");
            message.append(enc.encode(e.getLocalizedMessage()));
            message.append("<h2>Original content:</h2>");
            message.append(enc.encode(originalValue));
            message.append("</div>");
            return message.toString();
         }
      }
      else
      {
         return originalValue;
      }
   }

   /**
    * Do the actual inline link processing by creating a SAX parser and using
    * the {@link PSInlineLinkContentHandler} to do the real work. The handler
    * holds the results and releases them through the
    * {@link PSInlineLinkContentHandler#toString()} method.
    * 
    * @param body the body to be processed, assumed never <code>null</code> or
    *           empty
    * @return the processed body, never <code>null</code> or empty
    * @throws ParserConfigurationException if a new parser cannot be created
    * @throws SAXException if a problem occurs during parser
    * @throws IOException should never occur
    * @throws XMLStreamException should never occur
    */
   private String processInlineLinks(String body)
         throws ParserConfigurationException, SAXException, IOException,
         XMLStreamException
   {
      return PSSaxHelper.parseWithXMLWriter(body,
            PSInlineLinkContentHandler.class, this);
   }

   /**
    * @return Returns the itemFilter.
    */
   public IPSItemFilter getItemFilter()
   {
      return m_itemFilter;
   }

   /**
    * @return Returns the workItem.
    */
   public IPSAssemblyItem getWorkItem()
   {
      return m_workItem;
   }
}
