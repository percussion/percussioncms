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

package com.percussion.design.catalog.exit.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.design.catalog.PSCatalogRequestHandler;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionDefFactory;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * This class implements cataloging of extension handlers
 * installed on the server.
 * <p>
 * Extension handler catalog requests are sent to the server
 * using the PSXExtensionHandlerCatalog XML document. Its definition
 * is as follows:
 * <pre><code>
 *
 *    &lt;!ELEMENT PSXExtensionHandlerCatalog EMPTY&gt;
 *
 * <pre><code>
 *
 * The PSXExtensionHandlerCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * <pre><code>
 *
 *    &lt;!ELEMENT PSXExtensionHandlerCatalogResults   (ExtensionHandlerDef*)&gt;
 *
 * <pre><code>
 * Where 'Factory' is the classname of the IPSExtensionDefFactory used to
 * serialize this def.
 */
public class PSExtensionHandlerCatalogHandler
   extends PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler. This is used primarily
    * by the cataloger.
    *
    * @param mgr An initialized extension manager used to perform cataloging.
    * Must not be <code>null</code>;
    *
    * @throws IllegalArgumentException if mgr is <code>null</code>.
    */
   public PSExtensionHandlerCatalogHandler( IPSExtensionManager mgr )
   {
      super();

      if ( null == mgr )
         throw new IllegalArgumentException( "extension mgr can't be null" );
      m_extensionMgr = mgr;
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
      if (   (doc == null) || ((root = doc.getDocumentElement()) == null) ) {
         Object[] args = { ms_requestCategory, ms_requestType, ms_requestDTD };
         createErrorResponse(request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      /* verify this is the appropriate request type */
      if (!ms_requestDTD.equals(root.getTagName())) {
         Object[] args = { ms_requestDTD, root.getTagName() };
         createErrorResponse(request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      Document   retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc, (ms_requestDTD + "Results"));

      Iterator handlerRefs = m_extensionMgr.getExtensionHandlerNames();

      IPSExtensionDef def = null;
      try
      {
         IPSExtensionDefFactory factory =  new PSExtensionDefFactory();
         // go through each defined extension handler and write it to the doc
         while ( handlerRefs.hasNext())
         {
            def = m_extensionMgr.getExtensionDef(
               (PSExtensionRef) handlerRefs.next());
            factory.toXml( root, def );
         }
      }
      catch ( PSExtensionException e )
      {
         createErrorResponse( request, e );
         return;
      }
      catch ( PSNotFoundException e )
      {
         createErrorResponse( request, e );
         return;
      }

      /* and send the result to the caller */
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {   // nothing to do
   }


   /**
    * A valid extension mgr used for cataloging. Initialized during construction.
    * Never <code>null</code> after inited.
    */
   private IPSExtensionManager m_extensionMgr;

   private static final String   ms_requestCategory   = "exit";
   private static final String   ms_requestType         = "ExtensionHandler";
   private static final String   ms_requestDTD         = "PSXExtensionHandlerCatalog";
}

