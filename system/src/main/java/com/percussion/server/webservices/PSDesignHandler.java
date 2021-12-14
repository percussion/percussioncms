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

package com.percussion.server.webservices;

import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class is used to handle all content data related operations for 
 * webservices. These operations are specified in the "Design" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSDesign}.
 */

public class PSDesignHandler extends PSWebServicesBaseHandler
{

   /**
    * This operation is used to retrieve a blank copy of content type definition
    * which can be used to create a new content type Object.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void contentTypeAction(PSRequest request, Document parent)
      throws PSException
   {
      String contentType = null;

      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element el = PSXMLDomUtil.getFirstElementChild(root);

      if (el != null)
      {
         String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
         if (name.equals(EL_CONTENTKEY))
         {
            int id = PSXMLDomUtil.checkAttributeInt(el, ATTR_CONTENTID, true);
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID, "" + id);

            // will throw an exception if the content item is not found
            contentType = "" + lookupContentTypeId(request);
         }
         else if (name.equals(EL_CONTENTTYPENAMEID))
         {
            contentType = PSXMLDomUtil.getElementData(el);
         }
      }

      if (el == null || contentType == null)
      {
         String[] args = 
         {
            "contentType",
            root.getNodeName(),
            "<" + EL_CONTENTKEY + ">, <" + EL_CONTENTTYPENAMEID + ">"
         };
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_MISSING_ELEMENT, args );
      }

      PSItemDefinition itemDef = getItemDefinition(request, contentType);

      Element respEl = parent.getDocumentElement();
      String xmlData = PSXmlDocumentBuilder.toString(itemDef.toXml(parent));
      PSXmlDocumentBuilder.addElement(parent, respEl, EL_XMLDATA, xmlData);
   }

   /**
    * This operation is used to retrieve the list of content types available. 
    * This list will also contain the url location of where to obtain the editor.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void contentTypeListAction(PSRequest request, Document parent)
      throws PSException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      Collection itemDefSummaryList =
         mgr.getSummaries(request.getSecurityToken());

      Element root = parent.getDocumentElement();

      Iterator iter = itemDefSummaryList.iterator();
      while (iter.hasNext())
      {
         PSItemDefSummary itemDefSummary = (PSItemDefSummary)iter.next();

         Element el =
            PSXmlDocumentBuilder.addElement(
               parent,
               root,
               EL_CONTENTTYPE,
               itemDefSummary.getDescription());
         el.setAttribute(ATTR_ID, "" + itemDefSummary.getTypeId());
         el.setAttribute(ATTR_NAME, itemDefSummary.getName());
         el.setAttribute(ATTR_EDITOR_URL, itemDefSummary.getEditorUrl());
      }
   }

   /**
    * This operation is used to retrieve the list of variants available to
    * the specified content type or the content item, in which case we find
    * the content type and the retrieve the list.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void variantListAction(PSRequest request, Document parent)
      throws PSException
   {
      long typeId = -1;

      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element el = PSXMLDomUtil.getFirstElementChild(root);

      if (el != null)
      {
         String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
         if (name.equals(EL_CONTENTKEY))
         {
            int ctid = PSXMLDomUtil.checkAttributeInt(el, ATTR_CONTENTID, true);
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID, "" + ctid);
            int rvid = PSXMLDomUtil.checkAttributeInt(el, ATTR_REVISION, false);
            if (rvid != -1)
               request.setParameter(IPSHtmlParameters.SYS_REVISION, "" + rvid);


            // will throw an exception if the content item is not found
            typeId = lookupContentTypeId(request);
         }
         else if (name.equals(EL_CONTENTTYPENAMEID))
         {
            String contentType = PSXMLDomUtil.getElementData(el);
            try
            {
               typeId = Long.parseLong(contentType);
            }
            catch (NumberFormatException e)
            {
               PSItemDefManager mgr = PSItemDefManager.getInstance();
               typeId = mgr.contentTypeNameToId(contentType);
            }
         }
      }

      if (el == null || typeId == -1)
      {
         String[] args = 
         {
            "variantList",
            root.getNodeName(),
            "<" + EL_CONTENTKEY + ">, <" + EL_CONTENTTYPENAMEID + ">"
         };
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_MISSING_ELEMENT, args );
      }

      request.setParameter(IPSHtmlParameters.SYS_CONTENTTYPEID, "" + typeId);

      String path = WEB_SERVICES_APP + "/" + WS_VARIANTLIST;
      processInternalRequest(request, path, parent);
   }

   /**
    * action string constants
    */
   private static final String WS_VARIANTLIST = "variantList";

   /**
    * Constants for XML elements/attributes defined in the 
    * schema <code>sys_DesignParameters.xsd</code>
    */
   private static final String EL_CONTENTTYPENAMEID = "ContentTypeNameId";
   private static final String ATTR_ID = "id";
   private static final String ATTR_EDITOR_URL = "editorUrl";
   private static final String ATTR_NAME = "name";
}
