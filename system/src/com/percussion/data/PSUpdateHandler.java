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
package com.percussion.data;

import com.percussion.cms.IPSConstants;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSBackEndUpdateProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.log.PSLogError;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSInvalidRequestTypeException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSResponse;
import com.percussion.server.PSUserSession;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;


/**
 * The PSUpdateHandler class is used internally by the E2 server to process
 * data modification statements (updates, inserts, deletes). Once the
 * PSApplicationHandler determines a request is for a data
 * modification statement, it hands it off to the Update Handler. The
 * Update Handler must then prepare the request for the given back-end
 * and prepare the resulting data for return to the requestor.
 * <p>
 * When the Update Handler is first called, it prepares for update
 * processing. This is done by building transaction sets (PSTransactionSet),
 * data modification statements (PSUpdateStatement) and XML field to
 * back-end column mappings (PSStatementColumnMapper). Once these objects
 * have been created, the Update Handler is ready to accept requests for
 * processing.
 * <P>
 * The update process is actually quite complex. A common use of XML will
 * likely be to assist in e-commerce applications. Let's take the following
 * example of an order. When we break down our XML document, we may find
 * that there are actually many "rows" of data being stored in several
 * tables. For instance:
 *
 * <PRE><CODE>
 *       &lt;Order id="999"&gt;
 *          &lt;Customer id="1"&gt;
 *             &lt;name&gt;John Doe&lt;/name&gt;
 *             &lt;Billing&gt;
 *                &lt;Address&gt;
 *                   &lt;street&gt;1 Main Street&lt;/street&gt;
 *                   &lt;city&gt;Anywhere&lt;/city&gt;
 *                   &lt;state&gt;XX&lt;/state&gt;
 *                   &lt;zip&gt;12345&lt;/zip&gt;
 *                &lt;/Address&gt;
 *                &lt;Payment method="COD"/&gt;
 *             &lt;/Billing&gt;
 *             &lt;Shipping type="FedEx-2Day"&gt;
 *                &lt;Address&gt;
 *                   &lt;street&gt;1 Main Street&lt;/street&gt;
 *                   &lt;city&gt;Anywhere&lt;/city&gt;
 *                   &lt;state&gt;XX&lt;/state&gt;
 *                   &lt;zip&gt;12345&lt;/zip&gt;
 *                &lt;/Address&gt;
 *             &lt;/Shipping&gt;
 *          &lt;/Customer&gt;
 *          &lt;Items&gt;
 *             &lt;Item id="123"&gt;
 *                &lt;quantity&gt;5&lt;/quantity&gt;
 *                &lt;price&gt;100&lt;/price&gt;
 *             &lt;/Item&gt;
 *             &lt;Item id="456"&gt;
 *                &lt;quantity&gt;1&lt;/quantity&gt;
 *                &lt;price&gt;500&lt;/price&gt;
 *             &lt;/Item&gt;
 *          &lt;/Items&gt;
 *       &lt;/Order&gt;
 * </CODE></PRE>
 *
 * <P>
 * There are actually several updates going on here. We have order info,
 * billing info, shipping info. This will clearly be stored in several
 * tables. Some info, such as the items being purchased, may require
 * multiple rows. Handling this is clearly a challenge.
 * <P>
 * To solve complex problems such as this, the E2 server uses several
 * tactics. When dealing with an order, we clearly want everything to go
 * through or nothing. To handle this all or none situation, a transaction
 * set can be created enforcing that all updates/inserts/deletes get
 * processed successfully or else all changes are reverted to their initial
 * state.
 * <P>
 * Consistency of changes is only one of the problems. The other involves
 * the mapping of the XML data to the back-end. Update pipes only allow a
 * single table to be defined. However, the table may require data from
 * multiple XML components. E2 allows mappings to be defined for an object
 * or any of its parent levels. For instance, the Item object can
 * be mapped to the items table. It can also get data from the Items
 * object or the Order object as these objects are its direct parents. It
 * cannot, however, get data from the Customer object. This should not pose
 * a problem for the majority of applications.
 *
 * @see         com.percussion.server.PSApplicationHandler
 * @see         PSUpdateStatement
 * @see         PSTransactionSet
 * @see         PSStatementColumnMapper
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSUpdateHandler extends PSDataHandler
{

   private static final Logger log = LogManager.getLogger(PSUpdateHandler.class);

   /**
    * Construct a data modification handler to manage the inserting,
    * updating and deleting for the specified data set.
    * <p>
    * The following steps are performed during construction:
    * <ol>
    * <li>build the execution plans for inserting, updating and/or deleting.
    * This is done by creating transaction sets to group sets of
    * related statements. Statements are also built, but they're
    * associated with their corresponding transaction sets. They're not
    * used directly by the handler.</li>
    * <li>go through the transaction sets in the execution plans. For each
    * PSStatement object add its column mapper to the appropriate
    * internal mapper.</li>
    * <li>if this update causes a query to be fired, prepare the request
    * generator. Otherwise, set the result set converter we'll be
    * using. (store either in m_outputGenerator)</li>
    * </ol>
    * The information used from the
    * {@link com.percussion.design.objectstore.PSDataSet PSDataSet}
    * object is:
    * <ul>
    * <li>the type of transaction support</li>
    * <li>the pipes defining the data associated with this data set</li>
    * <li>the page data tank describing the XML document being used for this
    * data set</li>
    * <li>the definition of the results being generated by this data set</li>
    * <li>the extensions which act upon the entire data set</li>
    * </ul>
    *
    * @param      app      the application containing the data set
    *
    * @param      ds         the data set containing the update pipe(s) this
    *                      object will handle
    *
    * @exception   PSInvalidRequestTypeException If ds contains no update pipes.
    */
   public PSUpdateHandler(PSApplicationHandler app, PSDataSet ds)
      throws PSInvalidRequestTypeException, PSIllegalArgumentException,
            java.sql.SQLException, PSSystemValidationException,
            PSNotFoundException, PSExtensionException
   {
      super(app, ds);

      // the transaction set is the real work horse
      m_transactionSet = new PSTransactionSet(app, ds);

      m_styleSheetEvaluators = null;   /* default to no style sheet */

      /* if this update causes a query to be fired, prepare the request
       * generator. Otherwise, set the result set converter we'll be
       * using.
       */
      if (!ds.isOutputResultPages()) {
         PSRequestLink link = ds.getOutputRequestLink();
         if (link != null) {
            try {
               m_outputGenerator = new PSRequestRedirector(app, link);
            } catch (com.percussion.design.objectstore.PSNotFoundException e) {
               throw new PSSystemValidationException(
                  e.getErrorCode(), e.getErrorArguments(),
                  app.getApplicationDefinition(), ds);
            }
         }
      }
      else {
         PSCollection pageSet = null;
         PSResultPageSet rs = ds.getOutputResultPages();
         if (rs != null)
            pageSet = rs.getResultPages();
         int size = (pageSet == null) ? 0 : pageSet.size();
         if (size != 0)
            m_styleSheetEvaluators = new PSSetStyleSheetEvaluator[size];
         for (int i = 0; i < size; i++) {
            // now build the style sheet object
            PSResultPage page = (PSResultPage)pageSet.get(i);

            try {
               m_styleSheetEvaluators[i] = new PSSetStyleSheetEvaluator(
                  app.getLocalizedURL(page.getStyleSheet()),
                  page,
                  page.getConditionals());
            } catch (java.net.MalformedURLException e) {
               Object[] args = { app.getName(), ds.getName(),
                  page.getStyleSheet() };
               throw new PSIllegalArgumentException(
                  com.percussion.design.objectstore.IPSObjectStoreErrors.STYLE_SHEET_BAD_URL,
                  args);
            }
         }
      }

      PSPipe pipe = ds.getPipe();

      // and our final step, prepare the post-processing extensions
      // now uses a vector
     m_resultDocProcessors= new Vector();
      loadExtensions(
         m_appHandler,
         pipe.getResultDataExtensions(),
         IPSResultDocumentProcessor.class.getName(),
         m_resultDocProcessors);

   }

   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process a data modification (insert, update or delete) request using
    * the input context information and data. The results will be written
    * to the specified output stream.
    *
    * @param   request      the request object containing all context
    *                      data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      makeRequest(request, true); // Make request and send response
   }

   /**
    * Process a data modification (insert, update, or delete) request using
    * the input context information and data.  Results can optionally be
    * sent as a response to the caller.  If sending a response, first checks the
    * {@link IPSHtmlParameters#DYNAMIC_REDIRECT_URL} parameter.  If a value is
    * found, that is used.  If not, processes any attached query resources.
    * If no query resources are attached, prepares and sends the default
    * statistics page.  In all three cases, the result doc exits are run before
    * sending the response.
    *
    * @param request the request object containing all context data
    * associated with the request
    *
    * @param sendResponse  should a response be sent?  <code>true</code>
    * indicates that a response should be sent, <code>false</code> indicates
    * that no response should be sent.
    *
    * @return  Any PSException that occurs, <code>null</code> on success.  If
    * sendReponse is <code>true</code>, this call will always return
    * <code>null</code> and send any information back to the requestor directly.
    */
   public PSException makeRequest(PSRequest request, boolean sendResponse)
   {
      PSExecutionData execData = null;
      int curExec = -1;
      IPSExecutionStep[] curPlan = null;
      PSRequestStatistics stats = request.getStatistics();
      /* Remember how many rows we started with so we can determine if any
       * rows were modified when an exception is thrown. This allows us to
       * determine what to do when there are security exceptions.
       */
      int startingStats = stats.getRowsDeleted() + stats.getRowsInserted()
            + stats.getRowsUpdated();

      try 
      {
         // build the execution data to store context
         execData = new PSExecutionData(m_appHandler, this, request);

         //create table change data object and set on execution data to notify
         //the listeners when the update is successful.
         if (!m_listeners.isEmpty())
         {
            PSTableChangeData tableChangeData = new PSTableChangeData(
               m_listeners.iterator());
            execData.setTableChangeData(tableChangeData);
         }

         // run any pre-processing extensions
         runPreProcessingExtensions(execData);

         // validate the request data
         validate(execData);

         // now execute the transaction set
         m_transactionSet.execute(execData);

         /* Some DBMSs do not flush the data until you release the
          * statement. Furthermore, by keeping the connection open in
          * our execData, a chained request must get its own connection.
          * We'll release our resources to avoid these problems.
          * This fixes bug id Rx-99-11-0044
          */
         execData.release();

         if (sendResponse)
         {
            /* now we need to build the response doc.
             *
             * 1. if a new request is supposed to be fired now, do so
             *
             * 2. if we're returning a formatted result page (eg, style sheet
             *    was defined), use it
             *
             * 3. otherwise, return the statistics as is
             */
            String psredirect = request.getParameter(
               IPSHtmlParameters.DYNAMIC_REDIRECT_URL);

            if (psredirect != null && psredirect.trim().length() > 0)
            {
               // run extensions for side effects and possibly trace
               Document doc = null;
               doc = processResultDocExtensions(execData, doc);

               // send the redirect
               request.getResponse().sendRedirect(psredirect, request);
            }
            else if (m_outputGenerator != null)
            {   // redirect to a query
               /* we may still want to run the processors for any side
                  effects that would be generated.  Bug Id: Rx-00-09-0046

                  NOTE:  The final resulting document will not have
                  any use once the result document processors are finished
                  running, as we will be redirecting to a query
                  and not processing a document!  */
               Document doc = null;
               doc = processResultDocExtensions(execData, doc);

               // this will take care of everything. Since doc is still
               // null, we won't do any more processing below
               m_outputGenerator.generateResults(execData);
            }
            else
            {
               // we need to build the statistics doc
               Document doc = PSXmlDocumentBuilder.createXmlDocument();
               stats.toXml(doc);

               applyStylesheetConditions(execData, doc);

               // run extensions and possibly trace
               doc = processResultDocExtensions(execData, doc);

               PSResponse resp = request.getResponse();

               if (request.getRequestPageType() == PSRequest.PAGE_TYPE_HTML)
               {
                  java.io.ByteArrayOutputStream bout
                     = new java.io.ByteArrayOutputStream();
                  PSStyleSheetMerger.merge(request, doc, bout);
                  java.io.ByteArrayInputStream in
                     = new java.io.ByteArrayInputStream(bout.toByteArray());
                  resp.setContent( in, bout.size(),
                     IPSMimeContentTypes.MIME_TYPE_TEXT_HTML);
               }
               else
               {
                  // we test this again as we will send raw XML if we can't
                  // find a merger for the specified style sheet (which
                  // sets style sheet to null as the flag)
                  resp.setContent(doc, IPSMimeContentTypes.MIME_TYPE_TEXT_XML);
               }
            }
         }
      }
      catch (PSErrorException err)
      {
         /* these are pre-formatted errors that occurred during update processing
          * If there is nothing but authorization exceptions and no rows were
          * updated, then we may need to allow the user to login. We do this
          * by passing control to maybeSendLoginPage.
          *
          * This is a hack. I would prefer to throw the exception from the
          * transaction set handler, but the execute method in the interface
          * doesn't throw an AuthorizationException and I don't want to change
          * the interface at this point.
          */
         PSLogError logErr = err.getLogError();
         PSBackEndUpdateProcessingError updErr = null;

         if ( logErr instanceof PSBackEndUpdateProcessingError )
         {
            updErr = (PSBackEndUpdateProcessingError) logErr;

            for ( ; null != updErr; updErr = updErr.getNext())
            {
               /* We have an authorization error */
               if ( updErr.getErrorCode() == IPSSecurityErrors.SESS_NOT_AUTHORIZED )
                  break;
            }
         }

         int currentStats = stats.getRowsDeleted() + stats.getRowsInserted()
               + stats.getRowsUpdated();

         /* If we have an updErr here then we flagged an authorization exception */
         if ( null != updErr && currentStats == startingStats )
         {
            // user is not authorized for any of the actions they want to perform
            PSAuthorizationException e = new PSAuthorizationException(
               m_appHandler.getRequestTypeName(PSApplicationHandler.REQUEST_TYPE_UPDATE),
               request.getRequestPage(),
               request.getUserSessionId());
            if (sendResponse)
               m_appHandler.handleAuthorizationException( request, e );
            else
               return e;
         }
         else
         {
            if (sendResponse)
               // Exceptions other than auth exceptions have occurred
               m_appHandler.reportError(request, err.getLogError());
            else
               return err;
         }
      }
      catch (Throwable t)
      {
         /* catch anything that comes our way */
         String source = "";
         if ((curPlan != null) && (curExec > 0) && (curExec < curPlan.length))
            source = curPlan[curExec].toString();

         String sessId = "";
         PSUserSession sess = request.getUserSession();
         if (sess != null)
            sessId = sess.getId();

         int errorCode;
         Object[] errorArgs;

         PSException e = null;

         if (t instanceof PSException) 
         {
            e = (PSException)t;
            errorCode = e.getErrorCode();
            errorArgs = e.getErrorArguments();
         }
         else 
         {
            errorCode = IPSServerErrors.RAW_DUMP;
            errorArgs = new Object[] { getExceptionText(t) };

            // needed for exception return!
            if (!sendResponse)
               e = new PSException(errorCode, errorArgs);
         }

         if (sendResponse)
         {
            PSBackEndUpdateProcessingError err =
               new PSBackEndUpdateProcessingError(
               m_appHandler.getId(), sessId, errorCode, errorArgs, source);
            m_appHandler.reportError(request, err);
         }
         else
            return e;
      }
      finally 
      {
         if (execData != null)
         {
            // release all connection related resources
            execData.release();

            // notify table change listeners
            PSTableChangeData tableChangeData = execData.getTableChangeData();
            if (tableChangeData != null)
               tableChangeData.notifyListeners();
         }
      }
      
      return null;
   }

   /**
    * Registers the supplied listener for table change events.  Any listeners
    * added will be notified of changes to any tables performed by this handler
    * as long as a call to {@link IPSTableChangeListener#getColumns(String)
    * IPSTableChangeListener.getColumns(tableName)} for the changed table does
    * not return <code>null</code>.
    *
    *
    * @param listener The listener to notify, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>listener</code> is
    * <code>null</code>.
    */
   public void addTableChangeListener(IPSTableChangeListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_listeners.add(listener);
   }


   /* ********* IPSInternalRequestHandler Interface Implementation ********* */

   // See IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest( PSRequest originalRequest )
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (originalRequest == null)
         throw new IllegalArgumentException("Request must be supplied.");

      PSRequest req = originalRequest;
      if (originalRequest.allowsCloning())
         req = originalRequest.cloneRequest();
      checkInternalRequestAuthorization(req);
      
      String sendUpdateResponse = req.getParameter(
         IPSHtmlParameters.SYS_SENDUPDATERESPONSE);
      boolean sendResponse = sendUpdateResponse != null && 
         sendUpdateResponse.trim().equalsIgnoreCase(IPSConstants.BOOLEAN_TRUE);

      PSException e = makeRequest(req, sendResponse);
      /* 
       * Exceptions are not returned when sendResponse is true. The exception
       * text is assigned to the request's response field. If the original 
       * request has been cloned, the response object within req and any error 
       * message it may contain will be lost when this method returns.
       */
      PSResponse response = req.getResponse();
      if (response != null && response.isErrorResponse())
      {
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         try
         {
            response.send(bout);
         }
         catch (IOException ioe)
         {
            // should never happen, ignore
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }         
         throw new PSInternalRequestCallException(
                  IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, bout.toString());
      }
      if (e != null)
      {
         if (e instanceof PSAuthorizationException)
            throw (PSAuthorizationException) e;
         if (e instanceof PSErrorException)
         {
            PSErrorException erExc = (PSErrorException) e;

            reportError(originalRequest, e);
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
               erExc.getLogError().toString(), e);
         }
         else
         {
            reportError(originalRequest, e);
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, e.toString(), e);
         }
      }

      return null;
   }

   /**
    * Returns <code>IPSInternalRequest.REQUEST_TYPE_UPDATE</code>.
    *
    * see {@link IPSInternalRequestHandler#getRequestType()} for details.
    */
   public int getRequestType()
   {
      return IPSInternalRequest.REQUEST_TYPE_UPDATE;
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   /**
    * Apply the style sheet conditions to the XML document. The
    * stylesheet processing instruction will be set in the XML document.
    */
   private java.net.URL applyStylesheetConditions(
      PSExecutionData data, Document doc)
      throws java.net.MalformedURLException
   {
      if (m_styleSheetEvaluators == null)
         return null;

      java.net.URL styleSheetURL   = null;
      PSRequest request = data.getRequest();
      PSXmlTreeWalker oldWalker = data.getInputDocumentWalker();

      try {
         if (doc != null)
            data.setInputDocumentWalker(new PSXmlTreeWalker(doc));

         /* run the conditionals to determine which style sheet is in use.
          * If no conditions are met, use our default style sheet.
          */
         for (int i = 0; i < m_styleSheetEvaluators.length; i++) {
            try {
               if (m_styleSheetEvaluators[i].isMatch(data)) {
                  styleSheetURL = m_styleSheetEvaluators[i].getStyleSheet();
                  break;
               }
            } catch (com.percussion.error.PSEvaluationException e) {
               // we obviously don't have a match, keep looking
            }
         }

         if (styleSheetURL != null) {
            PSXslStyleSheetMerger.setStyleSheet(request, doc, styleSheetURL);
         }

         return styleSheetURL;
      } finally {
         // reset the input document walker to the previous one
         data.setInputDocumentWalker(oldWalker);
      }
   }

   /**
    * Processes any result document extensions and then traces the post exit
    * CGI vars if that trace flag is on.
    *
    * @param data The execution data, assumed not <code>null</code>.
    * @param doc The document to process the extensions on.  May be
    * <code>null</code>.
    *
    * @return the result document after all extensions have run; might be
    * <code>null</code>.
    *
    * @throws PSParameterMismatchException if any extension are supplied invalid
    * parameters.
    * @throws PSExtensionProcessingException if any errors occur when running
    * the extensions.
    * @throws PSDataExtractionException if there is an error extracting any
    * input parameters.
    */
   private Document processResultDocExtensions(PSExecutionData data,
                                               Document doc)
      throws PSParameterMismatchException, PSExtensionProcessingException,
         PSDataExtractionException
   {
      if( m_resultDocProcessors != null )
      {
         int size=m_resultDocProcessors.size();
         for (int i = 0; i < size;i++ )
         {
            PSExtensionRunner proc =
               (PSExtensionRunner)m_resultDocProcessors.elementAt(i);
            doc = proc.processResultDoc(data, doc);
         }
      }

      // trace post exit CGI vars
      PSDebugLogHandler dh = m_appHandler.getLogHandler();
      if (dh.isTraceEnabled(PSTraceMessageFactory.POST_EXIT_CGI_FLAG))
      {
         dh.printTrace(PSTraceMessageFactory.POST_EXIT_CGI_FLAG,
            data.getRequest());
      }

      return doc;
   }

   /**
    * The list of listeners (instances of {{@link IPSTableChangeListener}) that
    * are to be notified when a table change event occurs. The listeners are
    * notified only if there is a change in a table for a specific action that
    * is in interest of listeners. Initialized to an empty list and never
    * <code>null</code> after that.
    */
   private List m_listeners = new ArrayList();

   /**
    * The transaction set groups all the inserts, updates and deletes.
    * It is capable of breaking up the incoming data and dispatching it to
    * the appropriate transaction members.
    */
   private PSTransactionSet               m_transactionSet;

   /**
    * The output generator for this update. It may fire a query link or
    * produce XML output based upon the update statistics.
    */
   private IPSResultGenerator               m_outputGenerator = null;

   /**
    * A vector from result doc processor extension refs to extension runners
    */
    private Vector m_resultDocProcessors=null;

   private PSSetStyleSheetEvaluator[]   m_styleSheetEvaluators;

}

