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
package com.percussion.services.assembly.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.html.PSHtmlParsingException;
import com.percussion.html.PSHtmlUtils;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.nio.charset.StandardCharsets;

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

   private static final Logger log = LogManager.getLogger(PSInlineLinkProcessor.class);

   PSInlineLinkContentHandler contentHandler = new PSInlineLinkContentHandler();


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
            log.warn("Problem processing inline links", e);
            StringBuilder message = new StringBuilder();
            message
                  .append("<div class='perc-assembly-error'>");
            message.append(PSI18nUtils
                  .getString("psx_assembly@Error processing inline link"));
            message.append(" ");
            message.append(e.getLocalizedMessage());
            message.append("<h2 class='perc-assembly-orginal-content'>Original content:</h2>");
            message.append(originalValue);
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
    */
   private String processInlineLinks(String body)
   {
      try {
         //Don't bother trying to parse if the string doesn't contain html / xml
         if(SecureStringUtils.isHTML(body) || SecureStringUtils.isXML(body)) {
            Document htmlDoc = PSHtmlUtils.createHTMLDocument(body, StandardCharsets.UTF_8,true,null);
            return contentHandler.processDocument(htmlDoc,this);
         }
      }catch (PSHtmlParsingException e){
         log.error("Error parsing content for inline links in Content Type field. Error: {}. The offending source code was: {}",
                 PSExceptionUtils.getMessageForLog(e), body);
      }
      return body;
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
