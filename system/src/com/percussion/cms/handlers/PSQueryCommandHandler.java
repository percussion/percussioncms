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
package com.percussion.cms.handlers;

import com.percussion.cms.PSContentEditorWalker;
import com.percussion.cms.PSEditorDocumentBuilder;
import com.percussion.cms.PSPageInfo;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSConditionalUrlEvaluator;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.data.PSStyleSheetMerger;
import com.percussion.data.PSUnsupportedConversionException;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSConditionalStylesheet;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSBackEndQueryProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSHtmlProcessingError;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSXmlProcessingError;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.log.PSLogError;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSPageCache;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSResponse;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This class encapsulates behavior that is common to all query command
 * handlers. Derived classes must implement the 
 * {@link #getDocumentBuilder(int, PSExecutionData)
 * getDocumentBuilder} method and may implement the {@link #getAppList(int,
 * PSExecutionData,boolean) getAppList} method.
 * <p>The class performs the following steps in the processing loop:
 * <ol>
 * <li>Get the apps from the derived class that generate the result sets
 *    needed by the builders.</li>
 * <li>Execute the apps and store the result sets on the execution data stack</li>
 * <li>Get the builder based on a passed in id. If no id is passed in, the
 *    root builder is obtained.</li>
 * <li>Build the documents using the builder</li>
 * <li>Apply the stylesheet, if needed.</li>
 * <li>Clean up the internal handlers and result sets</li>
 * </ol>
 */
public abstract class PSQueryCommandHandler extends PSCommandHandler implements
   IPSInternalCommandRequestHandlerEx
{
   /**
    * The page id for the root parent, which is the row editor for the main
    * page of the content item. It is set to 0 so page ids could be used as
    * indexes into an array if desired.
    */
   public static final int ROOT_PARENT_PAGE_ID = 0;

   /**
    * Looks in the system def for pre/post exits assigned to this handler and
    * initializes them.
    *
    * @param ah The handler for the application that contains this editor
    *    resource. See base class for requirements.
    *
    * @param ceh The parent handler of this handler. Never <code>null</code>.
    *
    * @param ce The definition of the editor. See base class for requirements.
    *
    * @param app Any resources that are created dynamically will be added to
    *    this application. Never <code>null</code>.
    *
    * @param cmdName The internal name of the command handler. Never empty.
    *
    * @throws IllegalArgumentException if ceh is <code>null</code> or cmdName
    *    is empty.
    *
    * @throws PSNotFoundException If an exit cannot be found.
    *
    * @throws PSIllegalArgumentException  We only throw this because the
    *    existing base class does.
    *
    * @throws PSExtensionException If any problems occur druing extension
    *    initialization.
    */
   public PSQueryCommandHandler( PSApplicationHandler ah,
         PSContentEditorHandler ceh, PSContentEditor ce, PSApplication app,
         String cmdName )
      throws PSNotFoundException, PSIllegalArgumentException,
         PSExtensionException, PSSystemValidationException
   {
      super( ah, ceh, ce, app );

      if ( null == cmdName || cmdName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "command name cannot be null or empty" );
      }

      m_cmdName = cmdName;

      // get dataset and cmd pre and post exits and prepare them
      prepareExtensions(cmdName);

      // prepare output translations
      prepareOutputTranslations();

      // prepare output field translations only
      prepareOutputFieldTranslations(ce);

      Iterator stylesheets = ce.getStylesheetSet().getStylesheets(cmdName);
      List<PSConditionalUrlEvaluator> ssEvaluators = 
         new ArrayList<>(4);

      while ( stylesheets.hasNext())
      {
         PSConditionalStylesheet ss =
               (PSConditionalStylesheet) stylesheets.next();
         PSConditionalUrlEvaluator ssEval = new PSConditionalUrlEvaluator(
            ss.getConditions(), ss.getRequest() );
         ssEvaluators.add(ssEval);
      }

      //System.out.println( "found " + ssEvaluators.size() + " stylesheet evals" );
      m_stylesheetEvaluators =
         new PSConditionalUrlEvaluator[ssEvaluators.size()];
      ssEvaluators.toArray(m_stylesheetEvaluators);

      PSSingleHtmlParameter pageId = new PSSingleHtmlParameter(
            ceh.getParamName( PSContentEditorHandler.PAGE_ID_PARAM_NAME ));
      m_pageIdExtractor =
            PSDataExtractorFactory.createReplacementValueExtractor( pageId );

   }

   /**
    * Executes the provided query request to produce the execution data.
    *
    * @param req the request, assumed not <code>null</code>.
    * @param data the execution data to process, assumed not
    *    <code>null</code>.
    * @param execDataCleanupList a list of execution data objects that need
    *    cleanup when done with the request.
    * @param resultSetCleanupList a list of result set objects that need
    *    cleanup when done with the request.
    */
   @SuppressWarnings("unchecked")
   private void executeQueryRequest(PSRequest req, PSExecutionData data,
      List execDataCleanupList, List resultSetCleanupList)
      throws PSUnsupportedConversionException, PSErrorException,
         PSDataExtractionException, PSRequestValidationException,
         PSNotFoundException,  PSInternalRequestCallException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSConversionException, PSExtensionProcessingException,
         PSParameterMismatchException, SQLException
   {
      // We only support requests for htm?, xml or txt
      if (PSRequest.PAGE_TYPE_UNKNOWN == req.getRequestPageType())
      {
         String pageExt = req.getRequestPageExtension();
         throw new PSUnsupportedConversionException(
               IPSDataErrors.HTML_CONV_EXT_NOT_SUPPORTED, pageExt);
      }

      // run any pre-processing extensions
      runPreProcessingExtensions(data);

      PSRequestStatistics stats = req.getStatistics();

      stats.incrementCacheMisses();

      /* Create all of the result sets.
         The apps are sequenced so the result sets end up on the stack
         with the parent at the top and each child below it in the order
         it will be needed during document processing (top-down). */
      Stack rsStack = data.getResultSetStack();

      PSEditorDocumentBuilder builder = getDocumentBuilder(data);

      Iterator apps = getAppList(getPageId(data).intValue(), data,
         builder.isNewDocument(data, builder.getDocContext().isRowEditor()));
      Stack tmpStack = new Stack();
      while (apps.hasNext())
      {
         IPSInternalResultHandler appHandler =
               (IPSInternalResultHandler) apps.next();
         PSExecutionData reqData = appHandler.makeInternalRequest(req);
         execDataCleanupList.add(reqData);
         ResultSet rs = appHandler.getResultSet(reqData);
         resultSetCleanupList.add(rs);
         tmpStack.push(rs);
      }

      while (!tmpStack.empty())
         rsStack.push(tmpStack.pop());
   }

   /**
    * Get the document builder for the provided execution data.
    *
    * @param data the execution data to get the document builder for, not
    *    <code>null</code>.
    * @return the document builder, never <code>null</code>.
    * @throws PSRequestValidationException for all request validation errors.
    * @throws PSNotFoundException if the document builder could not be found
    *    for the provided request.
    * @throws PSDataExtractionException if the pageid could not be extracted
    *    from the provided data.
    */
   private PSEditorDocumentBuilder getDocumentBuilder(PSExecutionData data)
      throws PSRequestValidationException, PSNotFoundException,
         PSDataExtractionException
   {
      int pageId = getPageId(data);
      PSEditorDocumentBuilder builder = getDocumentBuilder(pageId, data);
      if (null == builder)
      {
         throw new PSNotFoundException(IPSServerErrors.CE_INVALID_PAGEID,
            Integer.toString(pageId));
      }

      return builder;
   }

   /**
    * This part of the query request execution builds the document builder
    * for the provided request.
    *
    * @param data the execution data to process, assumed not
    *    <code>null</code>.
    * @param builder the document builder to use, assumed not <code>null</code>.
    * @return the result XML document built, never <code>null</code>.
    */
   private Document produceResultDocument(PSExecutionData data,
      PSEditorDocumentBuilder builder)
      throws PSUnsupportedConversionException, PSErrorException,
         PSDataExtractionException, PSRequestValidationException,
         PSNotFoundException,  PSInternalRequestCallException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSConversionException, PSExtensionProcessingException,
         PSParameterMismatchException, SQLException
   {
      // generate the xml result document
      builder.prepareExecutionData(data);
      Document resultDoc = builder.createResultDocument(data);

      resultDoc = runPostProcessingExtensions(data, resultDoc);
      resultDoc = runOutputTranslations(data, resultDoc);

      // process field translation
      processFieldTranslations(resultDoc, data);

      return builder.postProcessDocument(resultDoc, data);
   }

   /**
   * This is a convenience method that allows us
   * to do some pre processing before process request
   * does any real work, but allow any exceptions to be handled
   * by the processRequest's try/catch block.
   *
   * @param req the request to be preprocessed.
   *  Must not be <code>null</code>
   * @throws Throwable so any type of throwable exception
   *   can be passed through to processRequest
   */
   public void preProcessRequest(PSRequest req)
      throws Throwable
   {
      // Empty implementation
   }



   // see interface for description
   public void processRequest(PSRequest req)
   {
      ByteArrayOutputStream out = null;
      ByteArrayInputStream in = null;

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();
      // we close all result sets obtained from the execdata objects
      List resultSetCleanupList = new ArrayList();

      PSExecutionData data = null;

      try
      {

         preProcessRequest(req);
         data = new PSExecutionData(m_appHandler, this, req);

         Document resultDoc = null;
         String stylesheet = null;

         // if a cacheid is provided get the document from the error cache
         Integer cacheId = PSCommandHandler.getCacheId(data);
         if (cacheId == null)
         {
            executeQueryRequest(req, data, execDataCleanupList,
               resultSetCleanupList);

            resultDoc = produceResultDocument(data, getDocumentBuilder(data));
         }
         else
         {
            resultDoc = PSPageCache.getInstance().getPage(cacheId);
            PSResponse resp = req.getResponse();
            if (resultDoc == null)
            {
               /*
                * The page probably timed out and was removed from the
                * cache. Return an error.
                */
               resp.setStatus(IPSHttpErrors.HTTP_NOT_FOUND);
               return;
            }
            else
            {
               /*
                * Run all post exits and translations on the error page with 
                * the current data.
                */
               resultDoc = runPostProcessingExtensions(data, resultDoc);
               resultDoc = runOutputTranslations(data, resultDoc);
            }
            
            // Request is successful but we are serving an error page
            resp.setStatus(IPSHttpErrors.HTTP_INTERNAL_SERVER_ERROR);
            stylesheet = req.getParameter(
               PSContentEditorHandler.USE_STYLESHEET);
         }

         /* Don't include the stylesheet PI in the output if text is requested.
            This allows the requestor to see the page in IE w/o IE trying to
            render it. */
         URL mergeStylesheet = null;
         if (PSRequest.PAGE_TYPE_TEXT == req.getRequestPageType())
         {
            PSXMLDomUtil.removeStyleSheetPiFromDoc(resultDoc);
         }
         else
         {
            mergeStylesheet = applyStylesheetConditions(data, resultDoc,
                  stylesheet, true);
         }

         String contentHeader = req.getContentHeaderOverride();
         PSResponse resp = req.getResponse();
         if ( PSRequest.PAGE_TYPE_HTML == req.getRequestPageType())
         {
            // merge the doc w/ the stylesheet
            out = new ByteArrayOutputStream();
            PSStyleSheetMerger merger =
                  PSStyleSheetMerger.getMerger( mergeStylesheet );
            if ( !( merger instanceof PSXslStyleSheetMerger ))
            {
               throw new PSConversionException(
                     IPSServerErrors.CE_UNSUPPORTED_MERGER,
                     mergeStylesheet.toString());
            }
            PSXslStyleSheetMerger xslMerger = (PSXslStyleSheetMerger) merger;

            /* if this refereneces a different app, we need to strip off the
               leading .. */
            String urlPath = mergeStylesheet.getFile();
            if ( urlPath.startsWith( "../" ))
               urlPath = urlPath.substring( 3 );
            mergeStylesheet = new URL( "file:" + urlPath );
            xslMerger.merge(req, resultDoc, out, mergeStylesheet );
            in = new ByteArrayInputStream(out.toByteArray());

            if (null == contentHeader)
            {
               String mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_HTML;
               // stylesheet could have set any encoding, so don't specify one
               contentHeader = PSBaseHttpUtils.constructContentTypeHeader(mimeType,
                  null);
            }

            resp.setContent( in, out.size(), contentHeader, false );
            in = null;
         }
         else
         {
            if (null == contentHeader)
            {
               String mimeType = null;
               int type = req.getRequestPageType();
               switch ( type )
               {
                  case PSRequest.PAGE_TYPE_TEXT:
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN;
                     break;

                  case PSRequest.PAGE_TYPE_XML:
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_XML;
                     break;

                  case PSRequest.PAGE_TYPE_JSON:
                     mimeType = IPSMimeContentTypes.MIME_TYPE_JSON;
                     break;
               }

               // we don't specify an encoding here because
               // setContent(Document, String) knows to add the default charset
               contentHeader = PSBaseHttpUtils.constructContentTypeHeader(mimeType,
                  null);
            }
            resp.setContent( resultDoc, contentHeader );
         }

      }
      catch (PSErrorException err)
      {
         // these are pre-formatted error we've thrown
         m_appHandler.reportError(req, err.getLogError());
      }
      catch (Throwable t)
      {
         PSConsole.printMsg("Cms", t);

         /* catch anything that comes our way */
         String source = m_cmdName +  " handler";
         String sessId = "";
         PSUserSession sess = req.getUserSession();
         if (sess != null)
            sessId = sess.getId();

         int errorCode;
         Object[] errorArgs;

         if (t instanceof PSException)
         {
            PSException e = (PSException)t;
            errorCode = e.getErrorCode();
            errorArgs = e.getErrorArguments();
         }
         else
         {
            errorCode = IPSServerErrors.RAW_DUMP;
            errorArgs = new Object[] { getExceptionText(t) };
         }

         PSLogError err = null;

         if (t instanceof PSConversionException)
         {
            int pageType = req.getRequestPageType();
            if (pageType == PSRequest.PAGE_TYPE_HTML)
            {
               // TODO can't get stylesheet so passing "" for now
               err = new PSHtmlProcessingError(m_appHandler.getId(),
                  sessId, errorCode, errorArgs, "");
            }
            else if ((pageType == PSRequest.PAGE_TYPE_XML) ||
               (pageType == PSRequest.PAGE_TYPE_TEXT))
            {
               // TODO can't get xml element so passing null for now
               err = new PSXmlProcessingError(m_appHandler.getId(),
                  sessId, errorCode, errorArgs, null);
            }
            else
            {
               // Processing error for now...
               err = new PSBackEndQueryProcessingError(m_appHandler.getId(),
                  sessId, errorCode, errorArgs, source);
            }
         }
         else
         {
            err = new PSBackEndQueryProcessingError(m_appHandler.getId(),
               sessId, errorCode, errorArgs, source);
         }

         m_appHandler.reportError(req, err);
      }
      finally
      {
         // clean up
         try
         {
            if ( null != in )
            {
               in.close();
               in = null;
            }
            if ( null != out )
            {
               out.close();
               out = null;
            }
         }
         catch (IOException ioe)
         {
            /* this should not happen on byte streams */
         }

         cleanup(data, resultSetCleanupList, execDataCleanupList);
      }
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();
      // we close all result sets obtained from the execdata objects
      List resultSetCleanupList = new ArrayList();

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         executeQueryRequest(request, data, execDataCleanupList,
            resultSetCleanupList);

         return data;
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e));
      }
      finally
      {
         cleanup(null, resultSetCleanupList, execDataCleanupList);
      }
   }

   // See IPSInternalResultHandler interface for description
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();
      // we close all result sets obtained from the execdata objects
      List resultSetCleanupList = new ArrayList();

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         executeQueryRequest(request, data, execDataCleanupList,
            resultSetCleanupList);

         return produceResultDocument(data, getDocumentBuilder(data));
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (SQLException e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e));
      }
      finally
      {
         cleanup(data, resultSetCleanupList, execDataCleanupList);
      }
   }

   // See IPSInternalResultHandler interface for description
   public PSMimeContentResult getMimeContent(PSExecutionData data,
      boolean setResponse) throws PSInternalRequestCallException
   {
      throw new UnsupportedOperationException(
         "This operation is not supported. Use getResultDoc or getResulSet instead.");
   }

   // See IPSInternalResultHandler interface for description
   public ResultSet getResultSet(PSExecutionData data)
      throws PSInternalRequestCallException
   {
      ResultSet rs = null;

      if (data != null)
      {
         Stack stack = data.getResultSetStack();

         if (stack.size() > 0)
            rs = (ResultSet) stack.pop();
      }
      else
         throw new IllegalArgumentException("Execution data must not be null.");

      return rs;
   }

   /**
    * Makes an internal request using the supplied request.
    *
    * @param request the request to make, not <code>null</code>.
    * @return the result document creted through the provided request,
    *    never <code>null</code>.
    */
   public Document makeInternalRequestEx(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      // store handlers so we can clean them up at the end.
      List execDataCleanupList = new ArrayList();
      // we close all result sets obtained from the execdata objects
      List resultSetCleanupList = new ArrayList();

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         executeQueryRequest(request, data, execDataCleanupList,
            resultSetCleanupList);

         return produceResultDocument(data, getDocumentBuilder(data));
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e));
      }
      finally
      {
         cleanup(data, resultSetCleanupList, execDataCleanupList);
      }
   }

   /**
    * Cleanup all resources created during a request. Provide <code>null</code>
    * to keep the execution data and clean it yourself.
    *
    * @param data the execution data to be cleaned, <code>null</code> if nothing
    *    to clean.
    * @param resultSetCleanupList a list of result sets to be cleaned,
    *    assumed not <code>null</code>.
    * @param execDataCleanupList a list of execution data to be cleaned,
    *    assumed not <code>null</code>.
    */
   private void cleanup(PSExecutionData data, List resultSetCleanupList,
      List execDataCleanupList)
   {
      /*
       * close all result sets before we clean up the handlers that
       * generated them
       */
      Iterator resultSets = resultSetCleanupList.iterator();
      while (resultSets.hasNext())
      {
         try
         {
            ((ResultSet) resultSets.next()).close();
         }
         catch (SQLException e)
         {
            // ignore at this point
         }
      }

      // clean up all the app handlers that we used
      Iterator execData = execDataCleanupList.iterator();
      while (execData.hasNext())
      {
         PSExecutionData reqData = (PSExecutionData) execData.next();
         reqData.release();
      }

      // finally, release any resources held by the execution context
      if (data != null)
         data.release();
   }

   /**
    * Performs resource cleanup when the handler is shut down. If derived
    * classes override this method, they must perform their own cleanup, then
    * call the base class.
    */
   public void shutdown()
   {
      // dynamic app shutdown by content editor handler
   }


   /**
    * Each handler has a set of 0 or more applications used to generate the
    * result sets used to create the result document. The derived class must
    * provide this list in the proper order for processing by the document
    * builder it supplies via the
    * {@link #getDocumentBuilder(int, PSExecutionData) getDocumentBuilder}
    * method. The {@link #processRequest(PSRequest) processRequest} method will
    * walk this list to generate all result sets, which will then be used by the
    * document builder. The result set for the parent editor <em>MUST</em> be
    * at the top of the stack, even if it is not the first result set used by
    * the builder. This is required because the top result set is always
    * prepared before any builders are called. This class returns an empty list.
    * 
    * @param id The value in the sys_pageId html param, or 0 if the param is not
    * present.
    * 
    * @param data The execution data before any result sets have been added.
    * 
    * @param isNewDoc A flag that indicates whether this request is to obtain an
    * editor to insert a new item (<code>true</code>) or edit an existing
    * one.
    * 
    * @return a list of 0 or more IPSInternalResultHandler objects, never
    * <code>null</code>. By default, no apps are returned.
    */
   @SuppressWarnings("unused") 
   protected Iterator getAppList( int id, PSExecutionData data,
         boolean isNewDoc )
      throws PSDataExtractionException
   {
      return PSIteratorUtils.emptyIterator();
   }

   /**
    * Each derived class must supply a builder that can create the result
    * document from the PSExecutionData object.
    *
    * @param id The value passed in as the ~rxchildid HTML param. Will be 0
    *    if this param is not present.
    *
    * @param data The execution data, it may not be <code>null</code>.
    * 
    * @return The builder identified by the supplied key, or <code>null</code>
    *    if the key can't be found.
    */
   abstract protected PSEditorDocumentBuilder getDocumentBuilder( int id, PSExecutionData data );


   /**
    * Extracts the child id list from the supplied pageInfo and builds a map
    * that just contains these lists. The generated map has the same keys
    * as the supplied map, only the values differ.
    *
    * @param pageInfo A map that contains 1 or more entries. For each entry,
    *    the key must be an Integer whose value is the pageId of the value
    *    object (i.e. the parent id). The value is a PSPageInfo object (which
    *    contains the child id list).
    *
    * @return A map containing all of the parent ids found in the page info,
    *    as keys, and their list of child page ids as the value for the entry.
    */
   @SuppressWarnings("unchecked")
   protected Map createPageMap( Map pageInfo )
   {
      Map idMap = new HashMap();
      Iterator pi = pageInfo.entrySet().iterator();
      while ( pi.hasNext())
      {
         Map.Entry entry = (Map.Entry) pi.next();
         Object parentId = entry.getKey();
         PSPageInfo info = (PSPageInfo) entry.getValue();
         Iterator childIds = info.getPageIdList();
         List childIdList = new ArrayList();
         while ( childIds.hasNext())
            childIdList.add( childIds.next());
         idMap.put( parentId, childIdList );
      }
      return idMap;
   }


   /**
    * Apply the style sheet conditions to the XML document.
    *
    * Processes the set of conditional stylesheets for this handler, finding the
    * first one that evaluates to <code>true</code> and sets the stylesheet
    * processing instruction in the XML document.
    *
    * @param data The execution data for the current request. Used to
    *    process the conditionals associated with the stylesheets. Never
    *    <code>null</code>
    *
    * @param doc The document to which the stylesheet PI will be added. Never
    *    <code>null</code>
    *
    * @param stylesheet the stylesheet url-string to use, if not provided
    *    (<code>null</code>) the one from the stylesheet evaluators will
    *    be retrieved.
    *
    * @param fixupURL If <code>true</code>, the url specified in the
    *    stylesheet evaluator will be made in an external form, including
    *    the protocol (http), server, port, etc. If <code>false</code>, the
    *    url is placed in the output document without modification.
    *
    * @return a URL for the stylesheet before it is fixed up, never <code>null
    *    </code>.
    *
    * @throws NullPointerException if execData or doc is <code>null</code>.
    */
   private URL applyStylesheetConditions(PSExecutionData data, Document doc,
         String stylesheet, boolean fixupURL)
      throws PSDataExtractionException, MalformedURLException
   {
      // run the conditionals to determine which style sheet is in use.
      URL stylesheetURL = null;
      if (stylesheet == null)
      {
         for ( int i = 0; i < m_stylesheetEvaluators.length; ++i )
         {
            if ( m_stylesheetEvaluators[i].isMatch( data ))
            {
               stylesheetURL = new URL(m_stylesheetEvaluators[i].getUrl(data));
               break;
            }
         }
      }
      else
         stylesheetURL = new URL(stylesheet);

      // we're guaranteed that at least 1 conditional will evaluate to true
      if (stylesheetURL != null)
      {
         String urlText, ssType;

         // make this "remote ready" by converting FILE URL to HTTP URL
         if (fixupURL)
            urlText = m_appHandler.getExternalURLString(stylesheetURL);
         else
            urlText = stylesheetURL.toExternalForm();

         int extPos = urlText.lastIndexOf('.');
         if (extPos == -1)
            ssType = "xsl";   // assume it's XSL by default
         else
            ssType = urlText.substring(extPos+1).toLowerCase();
         if(!PSXMLDomUtil.hasStyleSheetPiInDoc(doc))
         {
            ProcessingInstruction pi = doc.createProcessingInstruction(
               XSL_STYLESHEET_PI, ("type=\"text/" + ssType + "\" href=\"" + urlText + "\""));
            doc.insertBefore(pi, doc.getDocumentElement());
         }
      }

      return stylesheetURL;
   }

   /**
    * Process all output field translations for the provided document and
    * execution data.
    *
    * @param doc the document to process, assumed not <code>null</code>.
    * @param data the execution data to run the translations for, assumed
    *    not <code>null</code>.
    * @throws PSConversionException if the UDF fails.
    * @throws PSDataExtractionException if data extraction failed.
    */
   private void processFieldTranslations(Document doc, PSExecutionData data)
      throws PSConversionException, PSDataExtractionException
   {
      data.setInputDocument(doc);
      PSRequest req = data.getRequest();
      Iterator names = m_outputFieldTranslations.keySet().iterator();
      while (names.hasNext())
      {
         String name = (String) names.next();
         Element control =
            PSContentEditorWalker.getControlElement(doc, name);
         if (control == null)
            continue;

         PSTransformRunner transform = m_outputFieldTranslations.get(name);
         List<PSExtensionRunner> runners = transform.getTransforms();
         String errMsg = transform.getErrorMsg();
         if (!StringUtils.isBlank(errMsg))
            errMsg += ": ";

         // currently only one exit supported for field translations
         PSExtensionRunner runner = runners.get(0);
         try
         {
            List values = PSContentEditorWalker.getValues(control);
            Object val;
            if (values.isEmpty())
               val = "";
            else if (values.size() == 1)
               val = values.get(0);
            else
               val = values;
            
            req.setParameter(name, val);
            
            Object result = runner.processUdfCallExtractor(data);
            if (result != null)
               PSContentEditorWalker.replaceOrAddValue(doc, control, result);
         }
         catch (Exception e)
         {
            Object[] args = {name, errMsg + e.getLocalizedMessage()};
            if (e instanceof PSDataExtractionException)
            {
               throw new PSDataExtractionException(
                  IPSServerErrors.FIELD_TRANSFORM_ERROR, args);               
            }
            
            throw new PSConversionException(
               IPSServerErrors.FIELD_TRANSFORM_ERROR, args);
         } 
      }
      data.setInputDocument(null);
   }

   /**
    * It is possible for 1 or more stylesheets to be specified for a command
    * handler. This list contains 1 or more these. They are evaluated at run
    * time and the winning one is used to render the output document. Never
    * <code>null</code>, immutable once set in ctor.
    */
   private PSConditionalUrlEvaluator [] m_stylesheetEvaluators;

   /**
    * Used to get the page id at runtime. The page id identifies which of the
    * row/summary editors are being accessed. Never <code>null</code> and
    * immutable after construction.
    */
   private IPSDataExtractor m_pageIdExtractor;

   /**
    * The internal name of the command handler derived from this base class.
    * Never empty and immutable after set in ctor.
    */
   private String m_cmdName;

   /**
    * Name of the stylesheet processing instruction inside xml document.
    */
   private static final String XSL_STYLESHEET_PI = "xml-stylesheet";
}
