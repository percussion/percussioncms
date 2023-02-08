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
package com.percussion.servlets.utils;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Get the component urls for a given page. Does an internal request, followed
 * by extraction from the document.
 * 
 * @author dougrand
 * 
 */
public class PSComponentUrls
{

   private static final Logger log = LogManager.getLogger(PSComponentUrls.class);

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
      Map<String, String> requestparams = new HashMap<>();
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
         log.error("Couldn't get the component info Error: {}", e1.getMessage());
         log.debug(e1.getMessage(), e1);
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
