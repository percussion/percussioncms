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

import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.error.PSException;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class to handle the web services HTTP requests from the clients. Standard
 * clients are:
 * <br>
 * <ul>
 * <li>Web Services client </li>
 * <li>Command line</li>
 * <li>Site Migration</li>
 * </ul>
 * <br>
 *
 * This loadable handler's basic functionality is to take a request, parse the
 * action and execute the proper internal request based on that action. It uses
 * the input document for all parameters for each action. The input document is
 * defined within the specific schema based on the different actions.
 * <p>
 * Loosely implements the Singleton pattern in that a single instance is
 * created by the server, and other classes should use {@link #getInstance()}
 * to obtain a reference.
 */

public class PSWebServicesRequestHandler
   extends PSWebServicesBaseHandler
   implements IPSLoadableRequestHandler
{
   /**
    * Parameterless ctor used by server to construct this loadable handler.
    * Should not be used otherwise, as a single instance of this class should
    * exist and a reference to it be held by the server.  All other classes
    * should use {@link #getInstance()}.
    */
   public PSWebServicesRequestHandler()
   {
      ms_wsHandler = this;
   }

   /**
    * Get this instance of the webservice handler
    *
    * @return The single instance of the webservice handler running on the
    * server, or <code>null</code> if one has not been created.
    */
   public static PSWebServicesRequestHandler getInstance()
   {
      return ms_wsHandler;
   }

   /* ************ IPSLoadableRequestHandler Interface Implementation ********/

   /**
    * Server thread calls this method during initialization. Any initialization
    * required to be done based on the data from the config file shall be done
    * here.
    *
    * @param requestRoots @see IPSLoadableRequestHandler
    * @param cfgFileIn  @see IPSLoadableRequestHandler
    * @throws PSServerException  @see IPSLoadableRequestHandler
    */
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      if (requestRoots == null || requestRoots.size() == 0)
         throw new IllegalArgumentException("must provide at least one request root");

      // validate that requestRoots contains only Strings
      for (Iterator iter = requestRoots.iterator(); iter.hasNext();)
      {
         if (!(iter.next() instanceof String))
            throw new IllegalArgumentException("request roots collection may only contain String objects");
      }
      ms_requestRoots = requestRoots;

      PSConsole.printMsg(HANDLER, "Web Services request handler initialized");
   }

   /* ************ IPSLoadableRequestHandler Interface Implementation ****** */

   /**
    * Process a request using the input context information and data.
    * The results will be written to the specified output stream.
    * <p>
    * The following steps are performed to handle the request:
    * <ol>
    * <li>validate the request</li>
    * <li>see if requested action can be performed</li>
    * <li>return the response</li>
    * </ol>
    *
    * @param request - the request object containing all context
    * data associated with the request, not <code>null</code>
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      Document responseDoc = PSXmlDocumentBuilder.createXmlDocument();

      PSResponse response;
      try
      {
         // make sure we have a valid session
         // this should throw an exception if not logged in
         makeInternalRequest(request, WEB_SERVICES_APP + "/" + WS_AUTHENTICATE);

         Document inputDoc = request.getInputDocument();
         if (inputDoc == null)
         {
            // no parameters input doc, just create an empty one
            // check for input document parameter
            String inputDocParam = request.getParameter(INPUT_DOCUMENT, null);
            if (inputDocParam != null && inputDocParam.trim().length() > 0)
            {
               inputDoc =
                  PSXmlDocumentBuilder.createXmlDocument(
                     new StringReader(inputDocParam),
                     false);
            }
            else
            {
               inputDoc = PSXmlDocumentBuilder.createXmlDocument();
               PSXmlDocumentBuilder.createRoot(inputDoc, "NoParameters");
            }
            request.setInputDocument(inputDoc);
         }

         String port = request.getParameter(WSDL_PORT);
         request.removeParameter(WSDL_PORT);

         if (port == null || port.trim().length() == 0)
         {
            String[] args = 
            {
               WSDL_PORT
            };
            throw new PSException(
               IPSWebServicesErrors.WEB_SERVICE_MISSING_PARAMETER, args);
         }

         String action = request.getParameter(WS_ACTION);
         request.removeParameter(WS_ACTION);

         if (action == null || action.trim().length() == 0)
         {
            String[] args = 
            {
               WS_ACTION
            };
            throw new PSException(
               IPSWebServicesErrors.WEB_SERVICE_MISSING_PARAMETER, args);
         }

         Element elRoot = inputDoc.getDocumentElement();
         String rootName = PSXMLDomUtil.getUnqualifiedNodeName(elRoot);
         String responseRoot =
            PSStringOperation.singleReplace(rootName, "Request", "Response");

         // based on the request build a response document with the proper
         // root to be filled in by the various calls to the specific handler
         PSXmlDocumentBuilder.createRoot(responseDoc, responseRoot);

         // construct the name to the proper handler based on the port name
         String name =
            "com.percussion.server.webservices.PS" + port + "Handler";

         IPSPortActionHandler portActionHandler =
            (IPSPortActionHandler)Class.forName(name).newInstance();

         portActionHandler.processAction(port, action, request, responseDoc);
      }
      catch (PSException e)
      {
         int code = e.getErrorCode();

         // check to see if this exception is really an authentication error
         if (code == IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION
            || code
               == IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION
            || code == IPSSecurityErrors.SESS_NOT_AUTHORIZED)
         {
            // Using empty "Basic realm" to be consistent with other handlers
            response = request.getResponse();
            response.setResponseHeader(
               PSResponse.RHDR_WWW_AUTH,
               "Basic realm=\"\"");
            response.setStatus(IPSHttpErrors.HTTP_UNAUTHORIZED);
         }
         else
         {
            addResultResponseXml("failure", code, e.getMessage(), responseDoc);
            request.getResponse().setIsErrorResponse(true);
         }
      }
      catch (Throwable e)
      {
         StringWriter callStack = new StringWriter();
         PrintWriter p = new PrintWriter(callStack);
         e.printStackTrace(p);

         addResultResponseXml("failure", -1, callStack.toString(), responseDoc);
         request.getResponse().setIsErrorResponse(true);
      }
      
      response = request.getResponse(); // must be called after authenticated
      response.setContent(responseDoc);
   }

   /*********** IPSLoadableRequestHandler Interface Implementation ***********/
   /**
    * Shutdown the request handler, freeing any associated resources. This is
    * called by server during server shut down.
    */
   public void shutdown()
   {
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }

   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      return ms_requestRoots.iterator();
   }

   /**
    * Singleton instance of the webservice handler.  Not <code>null</code> after
    * call to ctor by the server.
    */
   private static PSWebServicesRequestHandler ms_wsHandler = null;

   /**
    * Storage for the request roots, initialized in init() call, never
    * <code>null</code> or empty after that. A list of String objects.
    */
   private static Collection ms_requestRoots = null;

   /**
    * Name of the subsystem used to dump messages to server console.
    */
   private static final String HANDLER = "WebServices";

   /**
    * Various WSDL port to categarise the requested actions (or operations).
    */
   private static final String WSDL_PORT = "wsdlPort";

   /**
    * Authenticate file name, used to force authentication
    */
   private static final String WS_AUTHENTICATE = "authenticate.xml";

   /**
    * Used when being called direct to set the input document
    */
   private static final String INPUT_DOCUMENT = "inputDocument";

   /**
    * action string constants
    */
   private static final String WS_ACTION = "action";
}
