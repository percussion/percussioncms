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

import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used to handle all assembly related operations for 
 * webservices. These operations are specified in the "Assembly" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSAssembly}.
 */
class PSAssemblyHandler extends PSWebServicesBaseHandler
{
   /**
    * Operation to handle creating the variant data specified by the content id
    * and revision parameters within the request. The data create will contain 
    * a base64 encoded data of the assembled variant, could be just simple HTML, 
    * but could be binary or PDF etc.. depending on the variant requested.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void createVariantAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      // get the rest of the paramters
      el = PSXMLDomUtil.getNextElementSibling(el, EL_VARIANTID);
      String variantId = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_AUTHTYPE);
      String authType = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_CONTEXT);
      String context = PSXMLDomUtil.getElementData(el);

      // set up the request to make an internal request for the variant data                  
      request.setParameter(
         IPSHtmlParameters.SYS_SESSIONID,
         request.getUserSessionId());
      request.setParameter(IPSHtmlParameters.SYS_VARIANTID, variantId);
      request.setParameter(IPSHtmlParameters.SYS_AUTHTYPE, authType);
      request.setParameter(IPSHtmlParameters.SYS_CONTEXT, context);

      Document resultDoc = processInternalRequest(request, ASSEMBLY_URL);
      if (resultDoc == null || resultDoc.getDocumentElement() == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
            "no assembly found");
      }

      String assemblyURL =
         resultDoc.getDocumentElement().getAttribute(ATTRIB_ASSEMBLY_URL);

      if (assemblyURL.length() == 0)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
            "no assembly found");
      }

      try(ByteArrayOutputStream out = processMergeResultRequest(request, assemblyURL)) {

         // build a result with the base64 encoded data
         String encodedData = PSBase64Encoder.encode(out.toString());
         Element root = parent.getDocumentElement();
         PSXmlDocumentBuilder.addElement(parent, root, EL_DATA, encodedData);
      } catch (IOException e) {
         throw new PSException(
                 IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
                 "create variant");
      }
   }

   /**
    * Assembly URL application and resource name
    */
   private static final String ASSEMBLY_URL = "sys_casSupport/AssemblyUrl";

   /**
    * Assembly URL attribute name
    */
   private static final String ATTRIB_ASSEMBLY_URL = "current";

   /**
    * Constants for XML elements/attributes defined in the 
    * schema <code>sys_AssemblyParameters.xsd</code>
    */
   private static final String EL_VARIANTID = "VariantId";
   private static final String EL_AUTHTYPE = "AuthType";
   private static final String EL_CONTEXT = "Context";
   private static final String EL_DATA = "Data";
}
