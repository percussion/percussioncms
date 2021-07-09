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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.design.catalog.security.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.IPSSecurityProviderMetaData;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.ResultSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSServerCatalogHandler class implements cataloging of 
 * servers. This request type is used to locate the servers available
 * through a back-end driver. Not all drivers are capable of locating
 * servers.
 * <p>
 * Server catalog requests are sent to the server using the
 * PSXSecurityServerCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityServerCatalog      (providverName)&gt;
 *
 *    &lt;-- the name of the driver being queried.
 *     --&gt;
 *    &lt;!ELEMENT providerName         (#PCDATA)&gt;
 * </pre>
 *
 * The PSXSecurityServerCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityServerCatalogResults (providerName, Server*)&gt;
 *
 *    &lt;-- the name of the provider which was queried.
 *     --&gt;
 *    &lt;!ELEMENT providerName               (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Server                     (name)&gt;
 *
 *    &lt;-- the name of the server.
 *     --&gt;
 *    &lt;!ELEMENT name                        (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSServerCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements com.percussion.design.catalog.IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSServerCatalogHandler()
   {
      super();
   }


   /* ********  IPSCatalogRequestHandler Interface Implementation ******** */

   /**
    * Get the request type(s) (XML document types) supported by this
    * handler.
    * 
    * @return      the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { ms_RequestDTD };
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process the catalog request. This uses the XML document sent as the
    * input data. The results are written to the specified output
    * stream using the appropriate XML document format.
    *
    * @param   request     the request object containing all context
    *                      data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      Document doc = request.getInputDocument();
      Element root = null;
      if (   (doc == null) ||
            ((root = doc.getDocumentElement()) == null) ) {
         Object[] args = { ms_RequestCategory, ms_RequestType, ms_RequestDTD };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      /* verify this is the appropriate request type */
      if (!ms_RequestDTD.equals(root.getTagName())) {
         Object[] args = { ms_RequestDTD, root.getTagName() };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      String provider = tree.getElementData("providerName");

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc, (ms_RequestDTD + "Results"));

      if (provider != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "providerName", provider);

      ResultSet rs = null;
      try {
         IPSSecurityProviderMetaData meta
            = PSSecurityProviderPool.getProviderMetaData(provider);
         if (meta != null) {
            rs = meta.getServers();
            while (rs.next()) {
               String serverName = rs.getString(1);

               Element node = PSXmlDocumentBuilder.addEmptyElement(
                  retDoc, root, "Server");
               PSXmlDocumentBuilder.addElement(retDoc, node, "name", serverName);
            }
         }

         /* and send the result to the caller */
         sendXmlData(request, retDoc);
      } catch (java.sql.SQLException e) {
         createErrorResponse(request, e);
      } finally {
         if (rs != null) {
            try { rs.close(); }
            catch (java.sql.SQLException e) { /* ignore this */ }
         }
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   private static final String   ms_RequestCategory   = "security";
   private static final String   ms_RequestType         = "Server";
   private static final String   ms_RequestDTD         = "PSXSecurityServerCatalog";
}

