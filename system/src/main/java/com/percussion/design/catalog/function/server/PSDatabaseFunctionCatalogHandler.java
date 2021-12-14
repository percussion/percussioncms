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

package com.percussion.design.catalog.function.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.design.catalog.PSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.server.PSRequest;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * This class implements cataloging of database functions installed on the
 * server.
 * <p>
 * Database functions catalog requests are sent to the server
 * using the PSXDatabaseFunctionCatalog XML document. Its definition
 * is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXDatabaseFunctionCatalog (driver)&gt;
 *  &lt;!ELEMENT driver (#PCDATA)&gt;
 *
 * <pre><code>
 *
 * The PSXDatabaseFunctionCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXDatabaseFunctionCatalogResults (PSXDatabaseFunctionDef*)&gt;
 *
 * <pre><code>
 *
 * See "sys_DatabaseFunctionDefs.dtd" for the DTD of the
 * "PSXDatabaseFunctionDef" element.
 */

public class PSDatabaseFunctionCatalogHandler
   extends PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Get the XML document types supported by this handler.
    *
    * @return the supported request type(s), never <code>null</code> or empty
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { NODE_NAME };
   }


   /**
    * Process the catalog request. This uses the XML document sent as the
    * input data. The results are sent using <code>PSResponse</code> object in
    * the supplied request object.
    *
    * @param request the request object containing all context data associated
    * with the request, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFuncMgr</code> is
    * <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null" );

      Document doc = request.getInputDocument();
      Element root = null;
      if ((doc == null) || ((root = doc.getDocumentElement()) == null))
      {
         Object[] args = { REQ_CATEGORY_VALUE, REQ_TYPE_VALUE, NODE_NAME };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      // verify this is the appropriate request type
      if (!NODE_NAME.equals(root.getTagName()))
      {
         Object[] args = { NODE_NAME, root.getTagName() };
         createErrorResponse(request,
            new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      Element el = walker.getNextElement(EL_DATASOURCE, firstFlags);
      String datasource = PSXmlTreeWalker.getElementData(el);

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element retRoot = PSXmlDocumentBuilder.createRoot(
         retDoc, (NODE_NAME + "Results"));

      // get driver from the datasource
      String driver;
      IPSConnectionInfo connInfo = new PSConnectionInfo(datasource);
      try
      {
         driver = PSConnectionHelper.getConnectionDetail(connInfo).getDriver();
      }
      catch (Exception e)
      {
         createErrorResponse(request, e);
         return;
      }
      
      PSDatabaseFunctionManager dbFuncMgr =
         PSDatabaseFunctionManager.getInstance();
      Iterator it = dbFuncMgr.getDatabaseFunctionsDef(
         PSDatabaseFunctionManager.FUNCTION_TYPE_SYSTEM |
            PSDatabaseFunctionManager.FUNCTION_TYPE_USER,
         driver);
      while (it.hasNext())
      {
         PSDatabaseFunctionDef dbFuncDef = (PSDatabaseFunctionDef)it.next();
         retRoot.appendChild(dbFuncDef.toXml(retDoc));
      }

      // and send the result to the caller
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   public static final String REQ_CATEGORY_VALUE = "function";
   public static final String REQ_TYPE_VALUE = "DatabaseFunction";

   public static final String NODE_NAME = "PSXDatabaseFunctionCatalog";
   public static final String EL_DATASOURCE = "datasource";

}

