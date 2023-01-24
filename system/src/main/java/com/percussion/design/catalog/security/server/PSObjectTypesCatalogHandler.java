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

package com.percussion.design.catalog.security.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.design.catalog.PSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.IPSSecurityProviderMetaData;
import com.percussion.server.PSRequest;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Server-side implementation of 
 * {@link com.percussion.design.catalog.security.PSObjectTypesCatalogHandler}. 
 * See that class for more details.
 */
public class PSObjectTypesCatalogHandler
   extends PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSObjectTypesCatalogHandler()
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

      String catalogerName = tree.getElementData("catalogerName");
      String catalogerType = tree.getElementData("catalogerType");

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(retDoc, (ms_RequestDTD + "Results"));

      if (catalogerName != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "catalogerName", 
            catalogerName);

      if (catalogerType != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "catalogerName", 
            catalogerType);

      try 
      {
         // this will throw if non-existant cataloger
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         boolean supportsGroups = roleMgr.supportsGroups(catalogerName, 
            catalogerType);
         
         // all catalogers support users
         Element node;
         node = PSXmlDocumentBuilder.addEmptyElement(retDoc, root, 
            "ObjectType");
         node.setAttribute("type", 
            IPSSecurityProviderMetaData.OBJECT_TYPE_USER);

         if (supportsGroups)
         {
            node = PSXmlDocumentBuilder.addEmptyElement(retDoc, root, 
               "ObjectType");
            node.setAttribute("type", 
               IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP);
         }
         
         /* and send the result to the caller */
         sendXmlData(request, retDoc);
      } 
      catch (Exception e) 
      {
         createErrorResponse(request, e);
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
   private static final String   ms_RequestType         = "ObjectTypes";
   private static final String   ms_RequestDTD         = "PSXSecurityObjectTypesCatalog";
}

