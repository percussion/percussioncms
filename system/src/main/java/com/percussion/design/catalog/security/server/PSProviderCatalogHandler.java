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
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * The PSProviderCatalogHandler class implements cataloging of security
 * providers. This request type is used to locate the security providers
 * available for authentication and ACL membership on the E2 server.
 * <p>
 * Security provider catalog requests are sent to the server using the
 * PSXSecurityProviderCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityProviderCatalog    EMPTY&gt;
 * </pre>
 *
 * The PSXSecurityProviderCatalogResults XML document is sent as the
 * response. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityProviderCatalogResults (Provider*)&gt;
 *
 *    &lt;!ELEMENT Provider                   (name, fullname, description, ConnectionProperties?)&gt;
 *
 *    &lt;!--
 *       the name by which the provider should be referenced. This is
 *        the name to use wherever a security provider type is required.
 *     --&gt;
 *    &lt;!ELEMENT name                          (#PCDATA)&gt;
 *
 *    &lt;!--
 *       the provider's full name. This is a more presentable form of
 *        the name which can be used in lists, etc.
 *     --&gt;
 *    &lt;!ELEMENT fullname                      (#PCDATA)&gt;
 *
 *    &lt;!-- a brief description of the provider.
 *     --&gt;
 *    &lt;!ELEMENT description                   (#PCDATA)&gt;
 *
 *    &lt;!-- the connection properties required for login.
 *     --&gt;
 *    &lt;!ELEMENT ConnectionProperties          (ConnectionProperty*)&gt;
 *
 *    &lt;!-- a connection property which is required for login.
 *     --&gt;
 *    &lt;!ELEMENT ConnectionProperty            (name, description)&gt;
 *
 *    &lt;!-- the name of the connection property.
 *     --&gt;
 *    &lt;!ELEMENT name                          (#PCDATA)&gt;
 *
 *    &lt;!-- the description of the connection property.
 *     --&gt;
 *    &lt;!ELEMENT description                (#PCDATA)&gt;
 * </pre>
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSProviderCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements com.percussion.design.catalog.IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSProviderCatalogHandler()
   {
      super();
   }


   // ********  IPSCatalogRequestHandler Interface Implementation ********

   /**
    * Get the request type(s) (XML document types) supported by this
    * handler.
    *
    * @return   the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { ms_RequestDTD };
   }


   // ************ IPSRequestHandler Interface Implementation ************

   /**
    * Processes the catalog request. This uses the XML document sent as the
    * input data. The results are written to the specified output
    * stream using the appropriate XML document format.
    *
    * @param request The request object containing all context
    * data associated with the request.
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
      root = PSXmlDocumentBuilder.createRoot(retDoc, (ms_RequestDTD + "Results"));
      
      /* some day, convert this to use JDK 1.2 package info instead of
       * hardcoded classes
       */
      com.percussion.security.IPSSecurityProviderMetaData meta;

      int providerType = PSSecurityProvider.SP_TYPE_BETABLE;
      if ( PSSecurityProvider.isSupportedType( providerType ))
      {
         meta = new com.percussion.security.PSBackEndTableProviderMetaData();
         addProviderDefinition(
            doc, root, meta.getName(), providerType, meta.getFullName(),
            meta.getDescription(), meta.getConnectionProperties());
      }

      providerType = PSSecurityProvider.SP_TYPE_DIRCONN;
      if ( PSSecurityProvider.isSupportedType( providerType ))
      {
         meta = new com.percussion.security.PSDirectoryConnProviderMetaData();
         addProviderDefinition(
            doc, root, meta.getName(), providerType, meta.getFullName(),
            meta.getDescription(), meta.getConnectionProperties());
      }

      providerType = PSSecurityProvider.SP_TYPE_WEB_SERVER;
      if ( PSSecurityProvider.isSupportedType( providerType ))
      {
         meta = new com.percussion.security.PSWebServerProviderMetaData();
         addProviderDefinition(
            doc, root, meta.getName(), providerType, meta.getFullName(),
            meta.getDescription(), meta.getConnectionProperties());
      }

      providerType = PSSecurityProvider.SP_TYPE_RXINTERNAL;
      if(PSSecurityProvider.isSupportedType( providerType ))
      {
         meta = new com.percussion.security.PSWebServerProviderMetaData();
            addProviderDefinition(
            doc, root, PSSecurityProvider.XML_FLAG_SP_INTERNAL, providerType,
            ms_provFullName, ms_rxInternalDesc, null);
      }   

      // and send the result to the caller
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      // nothing to do here
   }


   /**
    * Adds a provider definition to the XML document. Private utility
    * method.
    *
    * @param doc The document to which we add the definition. Must not
    * be <CODE>null</CODE>.
    *
    * @param root The element under which we add the definition. Must
    * not be <CODE>null</CODE>.
    *
    * @param name The name of the provider. Must not be <CODE>null</CODE>.
    *
    * @param type The integer representation of the type of provider.
    *
    * @param fullName The full (descriptive) name of the provider. Must
    * not be <CODE>null</CODE>.
    *
    * @param description The description of the provider. Must not be
    * <CODE>null</CODE>.
    *
    * @param connProps The connection properties. Each property will
    * be added. Must not be <CODE>null</CODE>.
    */
   private void addProviderDefinition(
      Document doc, Element root,
      String name, int type, String fullName, String description,
      Properties connProps)
   {
      Element node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Provider");
      PSXmlDocumentBuilder.addElement(doc, node, "name", name);
      PSXmlDocumentBuilder.addElement(doc, node, "type", Integer.toString(type));
      PSXmlDocumentBuilder.addElement(doc, node, "fullName", fullName);
      PSXmlDocumentBuilder.addElement(doc, node, "description", description);

      if (connProps != null)
      {
         Element propListNode = PSXmlDocumentBuilder.addEmptyElement(
            doc, node, "ConnectionProperties");

         Iterator iterator = connProps.entrySet().iterator();
         while (iterator.hasNext())
         {
            Map.Entry entry = (Map.Entry)iterator.next();
            Element propNode = PSXmlDocumentBuilder.addEmptyElement(
               doc, propListNode, "ConnectionProperty");
            PSXmlDocumentBuilder.addElement(
               doc, propNode, "name", (String)entry.getKey());
            PSXmlDocumentBuilder.addElement(
               doc, propNode, "description", (String)entry.getValue());
         }
      }
   }


   private static final String   ms_RequestCategory   = "security";
   private static final String   ms_RequestType         = "Provider";
   private static final String   ms_RequestDTD         = "PSXSecurityProviderCatalog";
   private static final String   ms_rxInternalDesc =
      "A special provider used to allow the Rhythmyx server to access resources";
   public static final String   ms_provFullName =
      "Rhythmyx Internal Security Provider";
}

