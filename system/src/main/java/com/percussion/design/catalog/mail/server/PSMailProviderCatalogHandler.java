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

package com.percussion.design.catalog.mail.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSMailProviderCatalogHandler class implements cataloging of 
 * mail providers. This request type is used to locate the mail providers
 * available to the E2 server for sending e-mail messages.
 * <p>
 * Mail provider catalog requests are sent to the server using the
 * PSXMailProviderCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXMailProviderCatalog EMPTY&gt;
 * </pre>
 *
 * The PSXMailProviderCatalogResults XML document is sent as the response. 
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXMailProviderCatalogResults (Provider*)&gt;
 *
 *    &lt;!ELEMENT Provider   (name, fullName, description)&gt;
 *
 *    &lt;!--
 *       the name by which the mail provider should be referenced. This is
 *       the name to use wherever a mail provider is required.
 *     --&gt;
 *    &lt;!ELEMENT name           (SMTP)&gt;
 *
 *    &lt;!--
 *       the mail provider's full name. This is a more presentable form of
 *       the name which can be used in lists, etc.
 *     --&gt;
 *    &lt;!ELEMENT fullName       (#PCDATA)&gt;
 *
 *    &lt;!-- a brief description of the mail provider.
 *     --&gt;
 *    &lt;!ELEMENT description    (#PCDATA)&gt;
 *
 *    &lt;!--
 *       the properties required by this provider
 *     --&gt;
 *    &lt;!ELEMENT Properties     (Property*)&gt;
 *
 *    &lt;!-- a property which is required for access to this provider.
 *     --&gt;
 *    &lt;!ELEMENT Property       (name, description)&gt;
 *
 *    &lt;!-- the name of the provider's property.
 *     --&gt;
 *    &lt;!ELEMENT name             (#PCDATA)&gt;
 *
 *    &lt;!-- the description of the provider's property.
 *     --&gt;
 *    &lt;!ELEMENT description    (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSMailProviderCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSMailProviderCatalogHandler()
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
      return new String[] { ms_requestDTD };
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
         Object[] args = { ms_requestCategory, ms_requestType, ms_requestDTD };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      /* verify this is the appropriate request type */
      if (!ms_requestDTD.equals(root.getTagName())) {
         Object[] args = { ms_requestDTD, root.getTagName() };
         createErrorResponse(
            request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(
         retDoc, (ms_requestDTD + "Results"));

      Element node = PSXmlDocumentBuilder.addEmptyElement(
         retDoc, root, "Provider");

      // at this time, we only have the SMTP mail provider
      com.percussion.mail.PSSmtpMailProvider provider
         = new com.percussion.mail.PSSmtpMailProvider();

      PSXmlDocumentBuilder.addElement(
         retDoc, node, "name", provider.getName());

      PSXmlDocumentBuilder.addElement(
         retDoc, node, "fullName", provider.getFullName());

      PSXmlDocumentBuilder.addElement(
         retDoc, node, "description", provider.getDescription());

      node = PSXmlDocumentBuilder.addEmptyElement(
         retDoc, node, "Properties");

      java.util.Properties props = provider.getPropertyDefs();
      if (props != null) {
         java.util.Iterator iterator = props.entrySet().iterator();
         while (iterator.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            Element propNode = PSXmlDocumentBuilder.addEmptyElement(
               retDoc, node, "Property");

            // now add in the name/description
            PSXmlDocumentBuilder.addElement(
               retDoc, propNode, "name", (String)entry.getKey());
            PSXmlDocumentBuilder.addElement(
               retDoc, propNode, "description", (String)entry.getValue());
         }
      }

      /* and send the result to the caller */
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   private static final String   ms_requestCategory   = "mail";
   private static final String   ms_requestType         = "MailProvider";
   private static final String   ms_requestDTD         = "PSXMailProviderCatalog";
}

