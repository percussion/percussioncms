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
package com.percussion.design.catalog.macro.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.design.catalog.PSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class catalogs all macros defined on the server.
 * <p>
 * Macro catalog requests are sent to the server using the PSXMacroCatalog XML 
 * document. It's definition is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXMacroCatalog EMPTY&gt;
 *
 * <pre><code>
 *
 * The PSXMacroCatalogResults XML document is sent as the response. It's 
 * definition is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXMacroDefinitionSet (PSXMacroDefinition*)&gt;
 *
 * <pre><code>
 */

public class PSMacroCatalogHandler extends PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Get the XML document types supported by this handler.
    *
    * @return the supported request type(s), never <code>null</code> or empty
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { CATALOGER_NAME };
   }

   /**
    * Process the catalog request. This uses the XML document sent as the
    * input data. The results are sent using <code>PSResponse</code> object in
    * the supplied request object.
    *
    * @param request the request object containing all context data associated
    *    with the request, may not be <code>null</code>
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null" );

      Document doc = request.getInputDocument();
      Element root = null;
      if ((doc == null) || ((root = doc.getDocumentElement()) == null))
      {
         Object[] args = 
         { 
            REQ_CATEGORY_VALUE, 
            REQ_TYPE_VALUE, 
            CATALOGER_NAME 
         };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      // verify this is the appropriate request type
      if (!CATALOGER_NAME.equals(root.getTagName()))
      {
         Object[] args = { CATALOGER_NAME, root.getTagName() };
         createErrorResponse(request,
            new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element retRoot = PSXmlDocumentBuilder.createRoot(
         retDoc, (CATALOGER_NAME + "Results"));

      retRoot.appendChild(PSServer.getMacros().toXml(retDoc));

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
   
   /**
    * The cataloger name. This is the name used for the root element of the 
    * request document.
    */
   public static final String CATALOGER_NAME = "PSXMacroCatalog";

   /**
    * The request category, used to construct the cataloger class name.
    */
   public static final String REQ_CATEGORY_VALUE = "macro";
   
   /**
    * The request type, use to construct the cataloger class name.
    */
   public static final String REQ_TYPE_VALUE = "Macro";
}
