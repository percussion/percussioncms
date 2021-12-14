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
import com.percussion.security.IPSDirectoryCataloger;
import com.percussion.security.IPSSubjectCataloger;
import com.percussion.server.PSRequest;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Server-side implementation of 
 * {@link com.percussion.design.catalog.security.PSCatalogerCatalogHandler}. See
 * that class for more details.
 */
public class PSCatalogerCatalogHandler extends PSCatalogRequestHandler
   implements
      IPSCatalogRequestHandler
{

   /**
    * Get the request type(s) (XML document types) supported by this handler.
    * 
    * @return the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] {ms_RequestDTD};
   }

   /**
    * Process a data related catalog request. This uses the input context
    * information and data. The results are written to the specified output
    * stream using the appropriate XML document format.
    * 
    * @param request the request object containing all context data associated
    * with the request
    */
   public void processRequest(PSRequest request)
   {
      Document doc = request.getInputDocument();
      Element root = null;
      if (   (doc == null) ||
         ((root = doc.getDocumentElement()) == null) )
      {
         Object[] args = { ms_RequestCategory, ms_RequestType, ms_RequestDTD };
         createErrorResponse(
            request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      // verify this is the appropriate request type
      if (!ms_RequestDTD.equals(root.getTagName()))
      {
         Object[] args = { ms_RequestDTD, root.getTagName() };
         createErrorResponse(
            request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(retDoc, ms_RequestDTD + "Results");
      
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      for (IPSDirectoryCataloger dirCat : roleMgr.getDirectoryCatalogers())
      {
         if (roleMgr.isDefaultCataloger(dirCat))
            continue;
         addCatalogerDefinition(doc, root, dirCat.getName(), 
            dirCat.getCatalogerType(), dirCat.getName() + "/" + 
            dirCat.getCatalogerDisplayType(), "");
      }

      for (IPSSubjectCataloger subCat : roleMgr.getSubjectCatalogers())
      {
         addCatalogerDefinition(doc, root, subCat.getName(), 
            IPSRoleMgr.SUBJECT_CATALOGER_TYPE, subCat.getName() + "/" + 
            "Subject Cataloger", subCat.getDescription());         
      }

      // and send the result to the caller
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {

   }
   
   /**
    * Adds a cataloger definition to the XML document. Private utility
    * method.
    *
    * @param doc The document to which we add the definition. Must not
    * be <CODE>null</CODE>.
    *
    * @param root The element under which we add the definition. Must
    * not be <CODE>null</CODE>.
    *
    * @param name The name of the cataloger. Must not be <CODE>null</CODE>.
    *
    * @param type The string representing the type of cataloger.
    *
    * @param fullName The full (descriptive) name of the cataloger. Must
    * not be <CODE>null</CODE>.
    *
    * @param description The description of the cataloger. Must not be
    * <CODE>null</CODE>.
    */
   private void addCatalogerDefinition(
      Document doc, Element root,
      String name, String type, String fullName, String description)
   {
      //catalogerName, catalogerType, fullName, description, objectType+
      Element node = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "cataloger");
      PSXmlDocumentBuilder.addElement(doc, node, "catalogerName", name);
      PSXmlDocumentBuilder.addElement(doc, node, "catalogerType", type);
      PSXmlDocumentBuilder.addElement(doc, node, "fullName", fullName);
      PSXmlDocumentBuilder.addElement(doc, node, "description", description);
   }

   private static final String ms_RequestCategory = "security";

   private static final String ms_RequestType = "Provider";

   private static final String ms_RequestDTD = "PSXSecurityCatalogerCatalog";   
}
