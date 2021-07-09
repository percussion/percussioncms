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

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSTransformErrorListener;
import com.percussion.data.PSUriResolver;
import com.percussion.data.PSUrlRequestExtractor;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSStylesheet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSStopwatch;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An action set is a series of internal requests against a single content
 * editor resource (determined dynamically from parameters in the request),
 * followed by a redirect URL.  Each internal request is represented by a
 * <code>PSAction</code> object and the redirect URL is represented by a
 * <code>PSUrlRequest</code>.
 * <p>
 * Each action's URL is evaluted in the context of the action that came before
 * it, to allow parameters generated in one action to be passed to the next
 * (such as obtaining the content id from a newly inserted content item).
 * <p>
 * If all actions are successful, the redirect URL will be returned to the
 * requestor.  Otherwise, an optionally-transformed XML document representing
 * the results of the run will be returned to the requestor.
 */
public class PSActionSet
{
   /**
    * Creates a newly created <code>PSActionSet</code> object, from
    * an XML representation described in <code>sys_StoredActions.dtd</code>.
    * The {@link #init(IPSExtensionManager) init} method must be called before
    * using the getter methods.
    *
    * @param sourceNode XML element to construct this object from, not
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   public PSActionSet(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException( "sourceNode cannot be null" );

      fromXml( sourceNode );
   }


   /**
    * Prepares internal data structures for runtime processing.  Creates
    * an extractor for the redirect URL and prepares each child action.
    *
    * @param extMgr responsible for resolving exit references, not
    * <code>null</code>.
    *
    * @throws PSException if there is a problem preparing one of the actions.
    */
   public void init(IPSExtensionManager extMgr) throws PSException
   {
      if (extMgr == null)
         throw new IllegalArgumentException(
            "Extension manager may not be null" );

      try
      {
         m_extractor = new PSUrlRequestExtractor( m_redirect );
      } catch (IllegalArgumentException e)
      {
         // cannot happen as m_redirect is never null
         throw new RuntimeException( "bug: url extractor threw exception" );
      }

      // prep each child action
      for (Iterator iter = m_actions.iterator(); iter.hasNext();)
      {
         PSAction action = (PSAction) iter.next();
         action.init( extMgr );
      }
   }


   /**
    * Initializes this object from its XML representation, which is defined
    * in <code>sys_StoredActions.dtd</code> and reproduced here for convenience:
    * <code><pre>
    * &lt;!ELEMENT ActionSet (Action+, PSXUrlRequest, PSXStylesheet?)>
    * &lt;!ATTLIST ActionSet
    *    name CDATA #REQUIRED
    * >
    * </pre></code>
    *
    * @param sourceNode XML element to construct this object from, assumed not
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   private void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      // validate the root element
      String localName = sourceNode.getNodeName();
      if (!XML_NODE_NAME.equals( localName ))
      {
         Object[] args = {XML_NODE_NAME, localName};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args );
      }

      // process the name attribute (required)
      m_name = sourceNode.getAttribute( "name" );
      if (m_name == null || m_name.trim().length() == 0)
      {
         Object[] args = {XML_NODE_NAME, "name", m_name};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
      }

      int searchChildren = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int searchSiblings = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      Element elem;
      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      // process all the action children (one or more)
      elem = tree.getNextElement( searchChildren );
      if (elem == null ||
         !elem.getNodeName().equals( PSAction.XML_NODE_NAME ))
      {
         Object[] args = {XML_NODE_NAME, PSAction.XML_NODE_NAME,
                          (elem == null ? "null" : elem.getNodeName())};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );
      }
      else
      {
         // make sure each action name is unique by using a set
         Set actionNames = new HashSet();

         while (elem != null)
         {
            PSAction action = new PSAction( elem );
            String actionName = action.getName();

            // make sure each action name is unique
            if (actionNames.contains( actionName ))
            {
               Object[] args = { actionName, m_name };
               throw new PSUnknownNodeTypeException(
                  IPSServerErrors.ACTION_DUPLICATE_NAME, args );
            }

            actionNames.add( actionName );
            m_actions.add( action );
            elem = tree.getNextElement( PSAction.XML_NODE_NAME,
               searchSiblings );
         }
      }
      // process the redirect URL (required)
      elem = tree.getNextElement( searchSiblings );
      if (elem == null ||
         !elem.getNodeName().equals( PSUrlRequest.XML_NODE_NAME ))
      {
         Object[] args = {XML_NODE_NAME, PSUrlRequest.XML_NODE_NAME,
                          (elem == null ? "null" : elem.getNodeName())};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );
      }
      else
         m_redirect = new PSUrlRequest( elem, null, null );

      // process the stylesheet (optional)
      elem = tree.getNextElement( PSStylesheet.XML_NODE_NAME, searchSiblings );
      if (elem != null)
         m_stylesheet = new PSStylesheet( elem, null, null );

   }


   /**
    * Gets the name of this action set, which should be unique across all
    * action sets.
    *
    * @return the name of this action set, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Gets the list of actions defined for this set.
    *
    * @return iterator of <code>PSAction</code> objects; never
    * <code>null</code> or empty.
    */
   public Iterator getActions()
   {
      return m_actions.iterator();
   }


   /**
    * Gets the number of actions defined for this set.
    *
    * @return the number of actions defined for this set.
    */
   public int getNumberofActions()
   {
      return m_actions.size();
   }


   /**
    * Gets the URL that should be returned to the browser (as a 302 redirect)
    * when the set has successfully completed all actions.
    *
    * @param execData The environment used to evaluate any replacement objects
    * contained in the redirect URL (such as HTML parameters), not
    * <code>null</code>.
    *
    * @return The string representation of the redirect URL with any replacement
    * values resolved using the specified execution data.  Will be
    * <code>null</code> if the the replacement value specified by the URL
    * request is not found.
    *
    * @throws IllegalStateException if the {@link #init(IPSExtensionManager)
    * init} method has not been called.
    */
   public String getRedirectUrl(PSExecutionData execData)
      throws PSDataExtractionException
   {
      if (execData == null)
         throw new IllegalArgumentException( "data cannot be null" );

      if (m_extractor == null)
         throw new IllegalStateException(
            "Must call init method before calling this method" );

      Object value = m_extractor.extract( execData );
      if (value != null)
         return value.toString();
      else
         return null;
   }


   /**
    * Iterates through each action, making an internal request to the URL
    * supplied by the action when resolved with the specified content editor
    * URL and the request context from the previous action.  If an action is
    * successful, any associated exits are run.  If all actions are
    * successful, this action set's redirect URL is resolved and returned to
    * the requestor.
    * <p>
    * In the case of an error, an XML status report is generated, optionally
    * transformed, and returned to the requestor.
    *
    * @throws IOException if an error occurs while sending the response
    * @throws PSException if an error occurs while transforming the status
    * report
    */
   public void processRequest(String ceUrl, IPSRequestHandler handler,
      PSRequest originalReq) throws IOException, PSException
   {
      if (ceUrl == null || ceUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "Content editor URL may not be null or empty." );

      if (handler == null)
         throw new IllegalArgumentException( "Request handler may not be null" );

      if (originalReq == null)
         throw new IllegalArgumentException( "Request may not be null" );

      PSActionSetResult actionSetResult = new PSActionSetResult( this, ceUrl );
      Iterator actions = m_actions.iterator();

      // if there is a content id but no revision id specified in the request,
      // add one
      if (originalReq.getParameter( IPSHtmlParameters.SYS_CONTENTID ) != null
         && originalReq.getParameter( IPSHtmlParameters.SYS_REVISION ) == null)
         originalReq.setParameter( IPSHtmlParameters.SYS_REVISION, "1" );

      PSRequest actionContext = originalReq;

      Exception error = null;
      String itemValidationError = null;
      while (actions.hasNext() && error == null && itemValidationError == null)
      {
         PSAction action = (PSAction) actions.next();
         Document resultDoc = null; // assigned if action is a query
         PSExecutionData execData = null;
         try
         {
            /* Build an execution data from the context of the last request
               to resolve any replacement values in the action URLs.  The data
               is released at the end of this try block. */
            execData = new PSExecutionData( null, handler, actionContext );
            String actionUrl = action.getUrl( ceUrl, execData );

            PSInternalRequest ir = PSServer.getInternalRequest( actionUrl,
               originalReq, null, true );
            if (ir != null)
            {
               /* Set the context so the next action will resolve replacement
                  values using the results of this action.  Set before
                  executing the request, so it will not be skipped if the action
                  generates an ignored exception. */
               actionContext = ir.getRequest();

               resultDoc = null;
               /* Determine if this request is for an update or query, as
                  calling getResultDoc against an update handler is currently
                  unsupported behavior. */
               IPSInternalRequestHandler irh = ir.getInternalRequestHandler();
               if (irh instanceof PSContentEditorHandler)
               {
                  // this should always be true, as action sets only hit CEs
                  PSContentEditorHandler ceHndlr = (PSContentEditorHandler) irh;
                  if (ceHndlr.isUpdateRequest( ir.getRequest() ))
                     ir.performUpdate();
                  else
                     resultDoc = ir.getResultDoc(); // grab XML for use by exits
               }
               else
               {
                  /* Shouldn't get here, as all actions make requests of
                     content editors and all content editor requests are handled
                     by a PSContentEditorHandler.  This error will be thrown
                     if someone has changed the behavior of content editors. */
                  throw new IllegalStateException(
                     "Unknown request handler type (" + irh.getClass() +
                     ") instead of expected PSContentEditorHandler" );
               }
            }
            else
            {
               // didn't find it -- this exception should not be ignored
               error = new PSActionSetException(
                  IPSServerErrors.ACTION_SET_ACTION_NOT_FOUND, new Object[]{
                     action.getName(), actionUrl} );
            }
         } 
         catch (Exception e)
         {
            error = e;
         } 
         finally
         {
            if (execData != null) execData.release();
         }

         itemValidationError = actionContext.getParameter(
            IPSHtmlParameters.SYS_VALIDATION_ERROR);
         if (itemValidationError != null && 
            itemValidationError.trim().length() == 0)
            itemValidationError = null;
         
         if (error == null && itemValidationError == null)
         {
            // completed with no errors, process this action's extensions
            // resultDoc will be null if update command, not null if query
            Iterator iter = action.getExtensionRunners();
            try
            {
               /* Build a new execution data from the context of the just
                  completed action resolve any replacement values in the exits.
                */
               execData = new PSExecutionData( null, handler, actionContext );
               while (iter.hasNext())
               {
                  PSExtensionRunner runner = (PSExtensionRunner) iter.next();
                  resultDoc = runner.processResultDoc( execData, resultDoc );
               }

               // we aren't tracking statistics currently so pass null
               actionSetResult.setSuccess( action.getName(), null );

            } 
            catch (Exception e)
            {
               // trap any exception from exits, including runtime exceptions
               error = e;
               // fall-through to if clause below which will report the error
            } 
            finally
            {
               if (execData != null) execData.release();
            }
         }

         /* An error could have been generated while running exits, so use
            another "if" here instead of "else".  Having a value assigned to
            the error varible will cause the loop to abort and will be set as
            the response at the end of this method.
          */
         if (error != null || itemValidationError != null)
         {
            actionSetResult.setFailed( action.getName(), error );

            // *don't* abort the loop if we are ignoring this action's errors
            if (action.ignoreError())
            {
               // certain errors must not be ignored
               if (error instanceof PSException)
               {
                  int errorCode = ((PSException)error).getErrorCode();
                  switch (errorCode)
                  {
                     case IPSServerErrors.ACTION_SET_ACTION_NOT_FOUND:
                     case IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION:
                     case IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION:
                     case IPSSecurityErrors.SESS_NOT_AUTHORIZED:
                     case IPSServerErrors.SQL_PROBLEM:
                        break;  // don't skip these errors

                     default:
                        error = null;
                  }
               }
               else
                  error = null;
            }
         }

      } // end of the action while loop

      PSExecutionData execData = null;
      try
      {
         /* Build an execution data from the context of the last completed
            action to resolve any replacement values in the redirect or
            error stylesheet URLs. */
         execData = new PSExecutionData( null, handler, actionContext );

         PSResponse response = originalReq.getResponse();
         if (error == null && itemValidationError == null)
         {
            // completed with no errors, redirect
            try
            {

               Object redirectUrl = m_extractor.extract( execData );
               if (redirectUrl != null)
                  response.sendRedirect( redirectUrl.toString(), originalReq );
               else
               {
                  // in the rare case that redirect URL resolves to null
                  // return the results as XML
                  response.setContent( actionSetResult.toXml() );
               }
            } 
            catch (PSDataExtractionException e)
            {
               error = e;
               // fall-through to if clause below which will report the error
            }
         }

         // an error could have been generated while trying to perform the
         // success redirect so use another "if" here instead of "else"
         if (error != null || itemValidationError != null)
         {
            if (error != null)
            {
               // errors -- maybe style and return
               if (originalReq.getRequestPageType() == PSRequest.PAGE_TYPE_HTML
                  && m_stylesheet != null)
               {
                  transformAndSetResponse( actionSetResult, execData, originalReq );
               }
               else
               {
                  // no stylesheet or not HTML request means return XML
                  response.setContent( actionSetResult.toXml() );
               }
               response.setStatus( IPSHttpErrors.HTTP_INTERNAL_SERVER_ERROR );
            }
            else
            {
               response = actionContext.getResponse();
            }
         }
      } 
      finally
      {
         if (execData != null) execData.release();
      }
   }


   /**
    * Transforms the action set result XML using the stylesheet specified
    * when this object was constructed.  The response object is set with the
    * output of the transformation.  Any errors that occur are printed
    * to the response.
    * <p>
    * This method may only be called when a transformation stylesheet has
    * been defined for this action set.
    *
    * @param actionSetResult contains the results to be transformed; assumed
    * not <code>null</code>.
    * @param execData contains the context to resolve the stylesheet URL;
    * assumed not <code>null</code>.
    * @param response the request's response object that will be assigned
    * the output from the transformation; asssumed not <code>null</code>.
    *
    * @throws IllegalStateException if called when no stylesheet has been
    * defined for this action set.
    * @throws IOException if there is a problem obtaining the stylesheet
    * @throws PSException if an error occurs while transforming
    */
   private void transformAndSetResponse(PSActionSetResult actionSetResult,
                                        PSExecutionData execData,
                                        PSRequest request)
      throws PSException, IOException
   {
      if (m_stylesheet == null)
         throw new IllegalStateException(
            "cannot transform when no stylesheet has been defined" );

      PSResponse response = request.getResponse();
      PSCachedStylesheet styleCached = null;
      try
      {
         StringWriter errorWriter = new StringWriter();
         // record transformation errors so they can be added to the response
         PSTransformErrorListener errorListener =
            new PSTransformErrorListener( new PrintWriter( errorWriter ) );

         // resolve the stylesheet URL
         // TODO: implement caching of stylesheets
         PSUrlRequestExtractor styleUrlExtractor =
            new PSUrlRequestExtractor( m_stylesheet.getRequest() );
         Object styleUrlObj = styleUrlExtractor.extract( execData );
         // Not sure how to cause this error but it is syntatically possible
         if (styleUrlObj == null)
            throw new PSActionSetException(
               IPSServerErrors.ACTION_SET_INVALID_STYLESHEET,
               new Object[]{m_stylesheet} );

         String styleUrl = styleUrlObj.toString();
         styleCached = new PSCachedStylesheet( new URL( styleUrl ) );

         Transformer nt = styleCached.getStylesheetTemplate().newTransformer();
         nt.setErrorListener( errorListener );
         nt.setURIResolver( new PSUriResolver() );

         Source src = new DOMSource( (Node) actionSetResult.toXml() );
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         Result res = new StreamResult( bout );

         Logger l = LogManager.getLogger(this.getClass());
         PSStopwatch watch = null;
         if (l.isDebugEnabled())
         {
            watch = new PSStopwatch();
            watch.start();
         }
         
         nt.transform( src, res );
         
         if (watch != null)
         {
            watch.stop();
            l.debug("Transforming document took " + watch.toString());
         }  

         ByteArrayInputStream in =
            new ByteArrayInputStream( bout.toByteArray() );

         // stylesheet could have set any encoding, don't specify one
         String contentHeader =
            PSBaseHttpUtils.constructContentTypeHeader(
               IPSMimeContentTypes.MIME_TYPE_TEXT_HTML, null );
         response.setContent( in, bout.size(), contentHeader, false );
      } catch (TransformerException e)
      {
         // add the details of the error to the message
         StringBuffer errorMsg = new StringBuffer( e.toString() );
         errorMsg.append( "\r\n" );
         try
         {
            errorMsg.append( PSXslStyleSheetMerger.getErrorListenerMessage(
               (PSTransformErrorListener) styleCached.getErrorListener() ) );
         } catch (IOException e1)
         {
            // if there is an error while including the context, ignore it
         }
         throw new PSConversionException( IPSDataErrors.XML_CONV_EXCEPTION,
            new Object[]{request.getUserSessionId(), errorMsg.toString()} );
      } catch (SAXException e)
      {
         throw new PSActionSetException( IPSServerErrors.XML_PARSER_SAX_ERROR,
            new Object[]{e.getLocalizedMessage()} );
      }
   }


   /** Name of the root element in this class' XML representation */
   public static final String XML_NODE_NAME = "ActionSet";

   /**
    * Name of this action set, used in the results.  Never <code>null</code> or
    * empty after construction.
    */
   private String m_name;

   /**
    * List of <code>PSAction</code> objects, populated by the <code>fromXml
    * </code> method.  Never <code>null</code> or empty.
    */
   private List m_actions = new ArrayList();

   /**
    * The redirect URL, returned to browser after successfully processing all
    * actions.  Never <code>null</code> or modified after construction.
    */
   private PSUrlRequest m_redirect;

   /**
    * The extractor for <code>m_redirect</code>, used for efficient runtime
    * resolution of the replacement value.  Assigned in the <code>init</code>
    * method and, never <code>null</code> or modified after that.
    */
   private IPSDataExtractor m_extractor = null;

   /**
    * Optional stylesheet used to transform the result document in the case of
    * any error.  May be <code>null</code>.
    */
   private PSStylesheet m_stylesheet = null;
}
