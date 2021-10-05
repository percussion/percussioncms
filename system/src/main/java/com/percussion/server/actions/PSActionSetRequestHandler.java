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

package com.percussion.server.actions;

import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSException;
import com.percussion.error.PSNonFatalError;
import com.percussion.error.PSUnknownProcessingError;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.log.PSLogError;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerLogHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements a loadable request handler for dispatching action set
 * requests.  It determines the content editor URL from request parameters,
 * then delegates to the named action set to complete the request.  Any
 * errors that occur during processing are returned in the response to the
 * requestor.
 *
 * @see PSActionSet
 */
public class PSActionSetRequestHandler implements IPSLoadableRequestHandler
{
   /**
    * Constructs a new <code>PSActionSetRequestHandler</code> object from the
    * supplied parameters.  The {@link #init(Collection, InputStream) init}
    * method must be called before this instance can process requests.
    *
    * @param objectStore currently unused by this class, but necessary for the 
    * method signature to match <code>PSServer</code>'s reflection.  May be 
    * <code>null</code>.
    *
    * @param extMgr responsible for resolving exit references, not
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if extMgr is <code>null</code>
    */
   public PSActionSetRequestHandler(IPSObjectStoreHandler objectStore,
                                    IPSExtensionManager extMgr)
   {
      if (extMgr == null)
         throw new IllegalArgumentException(
            "extension manager may not be null" );

      this.extMgr = extMgr;

      // some day loadable handlers will have their own error handlers,
      // but today they piggy-back off the server's.
      errorHandler = PSServer.getErrorHandler();
   }


   // see the IPSLoadableRequestHandler interface for method javadocs
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      if (requestRoots == null || requestRoots.isEmpty())
         throw new IllegalArgumentException(
            "must provide at least one request root" );

      // validate that requestRoots contains only Strings
      for (Object requestRoot : requestRoots) {
         if (!(requestRoot instanceof String))
            throw new IllegalArgumentException(
                    "request roots collection may only contain String objects");
      }
      this.requestRoots = requestRoots;

      actionSets = new HashMap<>();
      if (cfgFileIn != null)
      {
         try
         {
            // load the configuration file to construct map of action sets
            Document configDoc =
               PSXmlDocumentBuilder.createXmlDocument( cfgFileIn, false );

            Element root = configDoc.getDocumentElement();
            if (root != null)
            {
               NodeList as = root.getChildNodes();
               for (int i = 0; i < as.getLength(); i++)
               {
                  Node child = as.item( i );
                  if (child instanceof Element)
                     try
                     {
                        PSActionSet actionSet =
                           new PSActionSet( (Element) child );
                        if (this.actionSets.containsKey( actionSet.getName() ))
                           throw new PSActionSetException(
                              IPSServerErrors.ACTION_SET_DUPLICATE_NAME,
                              actionSet.getName() );
                        else
                        {
                           // ctor successful, init it
                           actionSet.init(extMgr);

                           PSConsole.printMsg( HANDLER,
                              "Initialization completed: " +
                              actionSet.getName() );

                           // only add to the map when ctor and init succeed
                           this.actionSets.put( actionSet.getName(), actionSet );
                        }
                     } catch (Exception e)
                     {
                        // failure loading or initing a set -- not fatal
                        PSConsole.printMsg( HANDLER, "An action set could not" +
                        " be loaded because of an error and will be skipped. " +
                        e.getMessage() );
                     }
               } // end of action set for loop
            }
            // else configuration file is empty -- that's ok

            PSConsole.printMsg( HANDLER,
               "Action sets request handler initialization completed." );

         } catch (RuntimeException e)
         {
            // let these propagate
            throw e;
         } catch (Exception e)
         {
            PSConsole.printMsg( HANDLER,
               "Failed to initialize action sets request handler: " +
               e.getMessage() );
         }
      }

   }


   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }


   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      return requestRoots.iterator();
   }

   /**
    * Makes an internal request to determine which content editor should be
    * used with the specified action set request.
    *
    * @param request contains the html parameters SYS_CONTENTID and/or
    * SYS_CONTENTTYPEID that will be used to determine the URL, assumed not
    * <code>null</code>.
    *
    * @return a relative URL to the registered content editor resource,
    * only <code>null</code> if the request needs to authenticate.
    *
    * @throws PSActionSetException if a content editor URL cataloging
    * application could not be found or if a URL could not be determined for
    * the provided request (because of missing params or invalid params).
    *
    * @throws PSInternalRequestCallException if an error occurs while
    * requesting the content editor URL from the cataloging application.
    *
    * @throws PSErrorException if the request is not authorized to query
    * the cataloging application, or if the request failed to be authenticated.
    */
   private String getContentEditorURL(PSRequest request) throws
      PSActionSetException, PSInternalRequestCallException, PSErrorException
   {
      // extract the ids from request parameters
      String contenttypeid =
         request.getParameter( IPSHtmlParameters.SYS_CONTENTTYPEID );
      String contentid =
         request.getParameter( IPSHtmlParameters.SYS_CONTENTID );

      // one or the other can be null, but not both
      if (contentid == null && contenttypeid == null)
         throw new PSActionSetException(
            IPSServerErrors.ACTION_SET_MISSING_REQUIRED_PARAMS,
            new Object[]{IPSHtmlParameters.SYS_CONTENTID,
                         IPSHtmlParameters.SYS_CONTENTTYPEID} );

      Map params = new HashMap( 2 );
      params.put( IPSHtmlParameters.SYS_CONTENTTYPEID, contenttypeid );
      params.put( IPSHtmlParameters.SYS_CONTENTID, contentid );

      // request must be authenticated to access the cataloger resource
      PSInternalRequest ir = PSServer.getInternalRequest(
              catalogerAppRsrcName, request, params, false );
      if (ir != null)
      {
         try
         {
            // make the internal request and extract the URL from the result XML
            Document result = ir.getResultDoc();
            PSXmlTreeWalker tree = new PSXmlTreeWalker( result );
            String url = tree.getElementData( "new" );
            if (url != null && url.trim().length() != 0)
            {
               return url;
            }
            else
            {
               // unexpected error: the result does not contain URL
               throw new PSActionSetException(
                  IPSServerErrors.ACTION_SET_COULD_NOT_DETERMINE_CE,
                  new Object[] {IPSHtmlParameters.SYS_CONTENTID,
                                contentid,
                                IPSHtmlParameters.SYS_CONTENTTYPEID,
                                contenttypeid} );
            }
         } catch (PSInternalRequestCallException e)
         {
            // check to see if this exception is really an authentication error
            if (e.getErrorCode() ==
               IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION ||
               e.getErrorCode() ==
               IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION)
            {
               PSLogError err = new PSNonFatalError(
                  IPSServerErrors.NO_AUTHORIZATION, new String[]{
                     request.getUserSession().getRealAuthenticatedUserEntry()});
               throw new PSErrorException( err );
            }
            else
            {
               throw e;
            }
         }
      }
      else
      {
         // unexpected error: the cataloging resource was not found
         throw new PSActionSetException( 
            IPSServerErrors.ACTION_SET_MISSING_CATALOGER,
                 catalogerAppRsrcName);
      }
   }


   /**
    * Performs the requested action set against a content editor determined
    * by the parameters in the request.  Will respond to the requestor either
    * with a redirect URL (if all actions are successful), the action set
    * result document (if an action failed), or the text of an error message.
    *
    * @param request the request to process, not <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>request</code> is <code>null
    * </code>
    * @throws IllegalStateException if the {@link #init} method has never been 
    * called on this instance.
    * 
    * @see PSActionSet#processRequest
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      if (actionSets == null)
         throw new IllegalStateException(
            "Must call the init method before processing requests" );
          
      final PSResponse response = request.getResponse();
      try
      {
         // determine which action set is requested
         String setName = request.getRequestPage( false );
         if (!actionSets.containsKey( setName ))
            PSServerLogHandler.handleRequestHandlerNotFound( request );
         else
         {
            // determine which content editor to apply the action set to
            String ceUrl = getContentEditorURL( request );
               // do the work
               PSActionSet actionSet = actionSets.get( setName );
               actionSet.processRequest( ceUrl, this, request );
         }
      } catch (PSErrorException e)
      {
         PSLogError err = e.getLogError();
         errorHandler.reportError( response, err );
      } catch (PSException e)
      {
         // wrap the exception in a PSLogError so it can be returned
         PSLogError err = new PSUnknownProcessingError( 0,
            request.getUserSessionId(), e.getErrorCode(),
            e.getErrorArguments() );
         errorHandler.reportError( response, err );
      } catch (Exception e)
      {
         // wrap the exception in a PSLogError so it can be returned
         PSLogError err = new PSUnknownProcessingError( 0,
            request.getUserSessionId(), IPSServerErrors.EXCEPTION_NOT_CAUGHT,
            e.toString() );
         errorHandler.reportError( response, err );
      }
   }


   // see interface for method javadoc
   public void shutdown()
   {
      // nothing to do
   }


   /**
    * Name of the subsystem used to dump messages to server console.
    */
   private static final String HANDLER = "ActionSets";

   /**
    * Defines the number of times the user can attempt to re-login when bad
    * credentials are supplied. Basically, the browser will pop up the login
    * dialog each time we return a 401. After this many attempts, we will
    * return a 500, which will require the user to open a new browser session
    * before a new login can be attempted. The count of failed logins is kept
    * in the user session.
    */
   public static final int MAX_LOGIN_ATTEMPTS = 3;
   
   /**
    * Name of the internal resource used to catalog content editor URLs.
    */ 
   private static final String catalogerAppRsrcName =
      "sys_psxContentEditorCataloger/getUrl.xml";

   /**
    * Storage for the request roots, initialized in the <code>init</code> 
    * method, never <code>null</code> or empty after. Contains
    * <code>String</code> objects.
    */
   private Collection<String> requestRoots = null;

   /**
    * Reference to the server's extension manager. Used to process the actions'
    * extensions.  Never <code>null</code> after construction.
    */
   private final IPSExtensionManager extMgr;

   /**
    * Maps a action set name (a <code>String</code>) to its object 
    * representation (a <code>PSActionSet</code>). Initialized in the 
    * <code>init</code> method and never <code>null</code> after that.
    */
   private Map<String,PSActionSet> actionSets = null;

   /**
    * The error handler for this request handler.  Never <code>null</code>
    * after construction.
    */
   private PSErrorHandler errorHandler;
}
