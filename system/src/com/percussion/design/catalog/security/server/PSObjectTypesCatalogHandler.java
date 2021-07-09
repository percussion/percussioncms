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

