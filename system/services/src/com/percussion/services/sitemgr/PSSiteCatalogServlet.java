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

package com.percussion.services.sitemgr;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.beans.IntrospectionException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Returns an xml document of all sites registered in the system.
 * 
 * @author ram
 * 
 */
public class PSSiteCatalogServlet extends javax.servlet.http.HttpServlet
{
   private static final Logger ms_log = LogManager.getLogger(PSSiteCatalogServlet.class);

   private static final long serialVersionUID = 1L;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    * javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException
   {
      IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
      Exception ex = null;
      OutputStream o = null;
      ByteArrayOutputStream bos = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element rootElem = PSXmlDocumentBuilder.createRoot(doc, "Sites");
         List<IPSCatalogSummary> summaries = sm.getSummaries(PSTypeEnum.SITE);
         for (IPSCatalogSummary summary : summaries)
         {
            Element elem = PSObjectSerializer.getInstance().toXml(summary);
            rootElem.appendChild(doc.importNode(elem, true));
         }
         response.setContentType("text/xml");
         o = response.getOutputStream();
         bos = new ByteArrayOutputStream();
         PSXmlDocumentBuilder.write(doc, bos);
         response.setContentLength(bos.size());
         o.write(bos.toByteArray());
         o.flush();
      }
      catch (DOMException | PSCatalogException | PSNotFoundException | SAXException | IntrospectionException | ParserConfigurationException | IOException e)
      {
         ex = e;
      }

      if (ex != null)
      {
         PrintWriter w;
         try
         {

            ms_log.error("Site list failure.  Error: {}", PSExceptionUtils.getMessageForLog(ex));
            response.reset();
            response.sendError(500);
         }
         catch (IOException e1)
         {
            response.reset();
            response.setStatus(500);
         }
      }
   }
}
