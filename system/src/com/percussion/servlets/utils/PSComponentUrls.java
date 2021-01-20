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
package com.percussion.servlets.utils;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Get the component urls for a given page. Does an internal request, followed
 * by extraction from the document.
 * 
 * @author dougrand
 * 
 */
public class PSComponentUrls
{
   static Log ms_log = LogFactory.getLog(PSComponentUrls.class);

   PSExtractComponentUrls m_extractor = null;

   /**
    * Ctor
    * 
    * @param request the servlet request, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public PSComponentUrls(HttpServletRequest request) {
      if (request == null)
      {
         throw new IllegalArgumentException("request may not be null");
      }
      Map<String, String> requestparams = new HashMap<String, String>();
      for (Map.Entry<String, String[]> e : ((Map<String, String[]>) request
            .getParameterMap()).entrySet())
      {
         if (e.getValue().length == 0)
            continue;
         requestparams.put(e.getKey(), e.getValue()[0]);
      }

      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSInternalRequest ireq = PSServer.getInternalRequest(
            "sys_ComponentSupport/componentsupport.xml", req, requestparams,
            false, null);
      try
      {
         Document doc = ireq.getResultDoc();
         m_extractor = new PSExtractComponentUrls(doc);

      }
      catch (PSInternalRequestCallException e1)
      {
         ms_log.error("Couldn't get the component info", e1);
      }
   }

   /**
    * Extract the named component url
    * 
    * @param name the name, never <code>null</code> or empty
    * @return the url or <code>null</code> if not found
    */
   public String getComponentUrl(String name)
   {
      String url = m_extractor.getComponentUrl(name);
      // Strip the absolute part of the url off
      int slashsys = url.indexOf("/sys");
      if (slashsys > 0)
         return url.substring(slashsys);
      else
         return url;
   }
}
