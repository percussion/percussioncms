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

package com.percussion.design.catalog;

import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.error.IPSException;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.log.PSLogHandler;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSValidateSession;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSCatalogRequestHandler abstract class contains some useful
 * routines most catalog request handlers can use. As such, catalog request
 * handlers should extend this class. They must still implement the
 * IPSCatalogRequestHandler interface directly.
 * 
 * @see       IPSCatalogRequestHandler
 * @see       com.percussion.server.IPSRequestHandler
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSCatalogRequestHandler implements IPSRequestHandler,
   IPSValidateSession
{
   /* ************ IPSRequestHandler Interface Implementation ************ */
   
   /**
    * Process a data related catalog request. This uses the input context
    * information and data. The results are written to the specified output
    * stream using the appropriate XML document format.
    * 
    * @param   request    the request object containing all context
    *                  data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      try {
         /* Verify the user has logged in as a designer or
          * admin on the server
          */
         try {
            com.percussion.server.PSServer.checkAccessLevel(
               request, PSAclEntry.SACE_ACCESS_DESIGN);
         } catch (Exception e) {
            // don't catch this one, it will generate the access error
            com.percussion.server.PSServer.checkAccessLevel(
               request, PSAclEntry.SACE_ADMINISTER_SERVER);
         }

         /* check the XML document type to determine the type of catalog
          * request we must perform
          */
         Document reqDoc = request.getInputDocument();
         if (reqDoc == null) {
            createErrorResponse(
               request, new PSIllegalArgumentException(
                  IPSCatalogErrors.REQ_DOC_MISSING_GENERIC));
            return;
         }

         Element root = reqDoc.getDocumentElement();
         if (root == null) {
            createErrorResponse(
               request, new PSIllegalArgumentException(
                  IPSCatalogErrors.REQ_DOC_ROOT_MISSING_GENERIC));
            return;
         }
         
         /* locate the catalog handler from our hash table */
         IPSCatalogRequestHandler rh = 
            (IPSCatalogRequestHandler)m_catalogHandlers.get(
            root.getTagName());
         if (rh == null) {
            Object[] args = { root.getTagName() };
            createErrorResponse(
               request, new PSIllegalArgumentException(
                  IPSCatalogErrors.NO_REQ_HANDLER_FOUND, args));
            return;
         }
         
         /* perform the appropriate catalog request */
         rh.processRequest(request);
      }
      catch (Throwable t)
      {
         createErrorResponse(request, t);
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public abstract void shutdown();
   
   /**
    * Create the error response for this request. This will return the
    * result to the request and log the conditon.
    *
    * @param   request      the request object
    *
    * @param   t            the exception that was thrown during processing
    */
   protected static void createErrorResponse(PSRequest request, Throwable t)
   {
      /* log all designer error conditions!!
       *
       * 1. log basic and detailed user activity so we have all the debug
       *    info we may need.
       *
       * 2. log the actual error.
       */
      PSLogHandler lh = com.percussion.server.PSServer.getLogHandler();
      if (lh != null) {
         lh.logBasicUserActivity(request, true);
         lh.logDetailedUserActivity(request, true);

         // we may want to send this through the PSErrorHandler some day so
         // notification, etc. can be sent. For now, we will just log it

         // get the request category from the CGI var
         String reqCategory
            = request.getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE, "");
         if (reqCategory.startsWith("design-catalog-")) {
            //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
            reqCategory = reqCategory.substring("design-catalog-".length());
         }
            
         // get the request type from the input XML doc
         String reqType = "";
         Document reqDoc = request.getInputDocument();
         if (reqDoc != null) {
            Element root = reqDoc.getDocumentElement();
            if (root != null)
               reqType = root.getNodeName();
         }

         // if this a PSException, get the thrown error info
         int errorCode;
         Object[] args;
         if (t instanceof IPSException) {
            IPSException pse = (IPSException)t;
            errorCode = pse.getErrorCode();
            args = pse.getErrorArguments();
         }
         else {
            errorCode = IPSCatalogErrors.CATALOG_EXCEPTION;
            args = new Object[] { t.toString() };
         }

         /* use the error handler for the logging and to perform
          * the notification of any administrators of the error
          */
         com.percussion.error.PSCatalogRequestError err
            = new com.percussion.error.PSCatalogRequestError(
            request.getUserSessionId(), reqCategory, reqType,
            errorCode, args);
         lh.write(err);
      }

      /* and report it to the user */
      Document respDoc = PSErrorHandler.fillErrorResponse(t);
      sendXmlData(request, respDoc);
   }

   /**
    * Send the specified XML document as the response for this request.
    *
    * @param   request      the request object
    *
    * @param   doc         the response doc to send
    */
   protected static void sendXmlData(PSRequest request, Document doc)
   {
      try {
         PSResponse resp = request.getResponse();
         if (resp == null) {
            return;   // nothing to do without a response
         }

         resp.setContent(doc);
      } catch (Throwable t) {
         /* log this */
         Object[] args = { request.getUserSessionId(),
                  com.percussion.error.PSException.getStackTraceAsString(t) };
         com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
               true, "CatalogRequestHandler"));
      }
   }

   /**
    * Add a handler to the hash table. The handler is stored for each
    * request type it supports.
    *
    * @param   rh      the request handler
    */
   protected void addHandler(IPSCatalogRequestHandler rh)
   {
      String[] reqTypes = rh.getSupportedRequestTypes();
      
      for (int i = 0; i < reqTypes.length; i++)
         m_catalogHandlers.put(reqTypes[i], rh);
   }


   /**
    * We keep a hash of XML document types and their appropriate catalog
    * handler for fast execution of the request
    */
   protected java.util.Hashtable      m_catalogHandlers = null;
}
