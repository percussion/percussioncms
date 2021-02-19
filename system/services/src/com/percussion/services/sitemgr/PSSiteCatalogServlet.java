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

package com.percussion.services.sitemgr;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.serialization.PSObjectSerializer;

import java.beans.IntrospectionException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Returns an xml document of all sites registered in the system.
 * 
 * @author ram
 * 
 */
public class PSSiteCatalogServlet extends javax.servlet.http.HttpServlet
{
   private static Log ms_log = LogFactory.getLog(PSSiteCatalogServlet.class);

   private static final long serialVersionUID = 1L;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    * javax.servlet.http.HttpServletResponse)
    */
   @SuppressWarnings("unchecked")
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
      catch (DOMException e)
      {
         ex = e;
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      catch (IntrospectionException e)
      {
         ex = e;
      }
      catch (ParserConfigurationException e)
      {
         ex = e;
      }
      catch (PSCatalogException | PSNotFoundException e)
      {
         ex = e;
      }
     
      if (ex != null)
      {
         response.setContentType("text/plain");
         PrintWriter w;
         try
         {
            Throwable orig = PSExceptionHelper.findRootCause(ex, true);
            ms_log.error("Site list failure", orig);
            w = response.getWriter();
            w.println(ex.getLocalizedMessage());
         }
         catch (IOException e1)
         {
            throw new RuntimeException(e1);
         }
      }
   }
}
