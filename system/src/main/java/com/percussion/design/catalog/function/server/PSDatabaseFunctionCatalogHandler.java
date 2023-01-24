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

