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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.data;

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.error.PSApplicationAuthorizationError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSRequestPreProcessingError;
import com.percussion.error.PSValidationError;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSCollection;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * The PSDataHandler abstract class contains support methods used by
 * data request handlers. All data request handlers should extend this
 * class and implement the processRequest method.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSDataHandler implements IPSRequestHandler,
   IPSInternalRequestHandler  {
   /**
    * Logger used to report problems.
    */
   private static Log ms_log = LogFactory.getLog(PSDataHandler.class);
   
   /**
    * Message for logging, uses {@link MessageFormat} for output.
    */
   private static String ms_errorMessage = "Application {0}, Dataset {1}, " +
        "Request {2}\nParameters: {3}\nhad the following problem: {4}";
   
   /**
    * Construct a data handler for the specified application.
    *
    * @param app the application handler managing this data request, never
    * <code>null</code>.
    *
    * @param ds the data set containing the query pipe(s) this
    * object will handle, never <code>null</code>.
    *
    * @throws PSNotFoundException if the definition for an extension in the
    * dataset cannot be located.
    * @throws PSExtensionException if there is a problem intializing the
    * extensions in the dataset.
    */
   protected PSDataHandler(PSApplicationHandler app, PSDataSet ds)
      throws PSExtensionException, PSNotFoundException
   {
      super();
      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      if (ds == null)
         throw new IllegalArgumentException("ds may not be null");

      m_appHandler = app;
      m_dataset = ds;
      m_dataSetName = ds.getName();

      PSRequestor requestor = ds.getRequestor();
      if ( null != requestor )
         m_validationRules = requestor.getValidationRules();

      if ((m_validationRules == null) || (m_validationRules.size() == 0))
      {
         m_validationRules = null;
         m_validationHandler = null;
      }
      else
         m_validationHandler = new PSConditionalEvaluator(m_validationRules);

      PSPipe pipe = ds.getPipe();

      setPreProcExits( pipe.getInputDataExtensions());
      setResultDocExits( pipe.getResultDataExtensions());
   }


   // TODO: implement this method for update resources by following the redirect
   // to a query resource
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      Thread.dumpStack();  // DEBUG: find out who called us
      throw new UnsupportedOperationException("not yet supported for updates");
   }

   /** @see IPSInternalResultHandler interface for description */
   public ByteArrayOutputStream getMergedResult(PSExecutionData data)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      Thread.dumpStack();  // DEBUG: find out who called us
      throw new UnsupportedOperationException("not supported");
   }
   
   public boolean isBinary(PSRequest req)
   {
      throw new UnsupportedOperationException("No implemented");
   }

   public static String getExceptionText(Throwable t)
   {
      String errorText  = "";

      if (t instanceof SQLException) {
         errorText = PSSqlException.getFormattedExceptionText((SQLException)t);
      }
      else if (t instanceof PSUnsupportedConversionException) {
         errorText = t.getMessage();
      }
      else {
         errorText = t.toString();
      }

      return errorText;
   }

   /**
    * Get the data set name this handler is servicing.
    */
   public String getDataSetName()
   {
      return m_dataSetName;
   }

   /**
    * Run the pre-processing extensions or extensions
    * associated with this request page. If the
    * authorization or validation fails, or any processing errors occur,
    *   *   an exception is thrown. This method should be called prior to calls
    *   *   to validate() as the pre-processing may make the request valid or invalid.
    *
    * @param   data        the request execution context
    *
    * @exception  PSErrorException
    *                                                                      the Error (validation/authorization, etc.)
    *                                                                      wrapped as an exception
    */
   public void runPreProcessingExtensions(PSExecutionData data)
      throws PSErrorException
   {

      /* Run the pre-processing extensions before validating */
      runPreProcessingExtensions(data, m_preparedPreProcExts.iterator());
   }

   /**
    * Run the pre-processing extensions or extensions
    * associated with this request page. If the
    * authorization or validation fails, or any processing errors occur,
    * an exception is thrown. This method should be called prior to calls
    * to validate() as the pre-processing may make the request valid or invalid.
    *
    * @param   data        the request execution context.  May not be
    * <code>null</code>.
    * @param   extensionRunners an Iterator over <code>zero</code> or more
    * prepared extensions.  May not be <code>null</code>.
    *
    * @throws PSErrorException any Exceptions (validation/authorization, etc.)
    * wrapped as an ErrorException.
    */
   public void runPreProcessingExtensions(PSExecutionData data,
      Iterator extensionRunners) throws PSErrorException
   {

      /* Run the pre-processing extensions before validating */
      if (extensionRunners.hasNext())
      {
         boolean restoreParamsOnError = false;

         try
         {
            while ( extensionRunners.hasNext())
            {
               PSExtensionRunner proc =
                  (PSExtensionRunner)extensionRunners.next();

               if (restoreParamsOnError == false &&
                   proc.getExtensionDef().isRestoreRequestParamsOnError()==true)
               {
                  //save request params, so that they can be later restored
                  data.getRequest().saveParams();

                  //even though PSRequest.saveParams() is smart enough to save
                  //only once, we want to be explicit that we do it only once.
                  restoreParamsOnError = true;
               }

               proc.preProcessRequest(data);

            }

            // trace html/cgi post exit processing
            PSDebugLogHandler dh = m_appHandler.getLogHandler();
            if (dh.isTraceEnabled(
               PSTraceMessageFactory.POST_PREPROC_HTTP_VAR_FLAG))
            {
               dh.printTrace(PSTraceMessageFactory.POST_PREPROC_HTTP_VAR_FLAG,
                              data.getRequest());
            }
         }
         catch (PSRequestValidationException valExc)
         {
            PSRequest request = data.getRequest();
            PSValidationError err = new PSValidationError(
                  m_appHandler.getId(),   // application id
                  request.getUserSessionId(),   // user session id
                  valExc.getErrorCode(),  // error code
                  valExc.getErrorArguments(),   // error text
                  null);

            throw new PSErrorException(err, valExc);
         }
         catch (PSAuthorizationException secExc)
         {
            PSRequest request = data.getRequest();
            PSApplicationAuthorizationError err =
               new PSApplicationAuthorizationError(
                  m_appHandler.getId(),   // application id
                  null, // host ip
                  request.getUserSessionId(), // supposed to be user id
                  secExc.getErrorCode(),  // error code
                  secExc.toString()
                  );

            throw new PSErrorException(err, secExc);
         }
         catch (PSException e)
         {
            PSRequestPreProcessingError err = new PSRequestPreProcessingError(
                  null,  // ip address?
                  e.getErrorCode(),
                  e.getErrorArguments());

            throw new PSErrorException(err, e);
         }
      }
   }

   /**
    * Run the post-processing extensions or extensions
    * associated with this request page. If the
    * authorization or validation fails, or any processing errors occur,
    * an exception is thrown.
    *
    * @param data The request execution context.
    * @param doc The document to run the extensions against.  May be
    * <code>null</code>.
    *
    * @return The processed document. If no changes are made, returns the
    * resultDoc which was passed in.
    *
    * @throws PSExtractionException if an error occurs extracting the
    * required information from the supplied execution data.
    * @throws PSParameterMismatchException the runtime parameters specified in
    * a call are incorrect for the usage of that extension.
    * @throws PSExtensionProcessingException if any other errors occur.
    */
   public Document runPostProcessingExtensions(PSExecutionData data,
      Document doc)
      throws PSExtensionProcessingException, PSDataExtractionException,
         PSParameterMismatchException
   {
      Document retDoc = runPostProcessingExtensions(data, doc,
         m_preparedPostProcExts.iterator());
      return retDoc;
   }

   /**
    * Run the post-processing extensions or extensions
    * associated with this request page. If the
    * authorization or validation fails, or any processing errors occur,
    * an exception is thrown.
    *
    * @param data The request execution context.
    * @param doc The document to run the extensions against.  May be
    * <code>null</code>.
    * @param extensionRunners Iterator over <code>zero</code> or more prepared
    * result document extensions.  May not be <code>null</code>.
    *
    * @return The processed document. If no changes are made, returns the
    * resultDoc which was passed in.
    *
    * @throws PSExtractionException if an error occurs extracting the
    * required information from the supplied execution data.
    * @throws PSParameterMismatchException the runtime parameters specified in
    * a call are incorrect for the usage of that extension.
    * @throws PSExtensionProcessingException if any other errors occur.
    */
   public Document runPostProcessingExtensions(PSExecutionData data,
      Document doc, Iterator extensionRunners)
      throws PSExtensionProcessingException, PSDataExtractionException,
         PSParameterMismatchException
   {

      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      if (extensionRunners == null)
         throw new IllegalArgumentException("extensionRunners may not be null");

      Document retDoc = doc;

      while ( extensionRunners.hasNext())
      {
         PSExtensionRunner proc = (PSExtensionRunner) extensionRunners.next();
         retDoc = proc.processResultDoc(data, retDoc);
      }

      return retDoc;
   }

   /**
    * Run the validations associated with this request page. If the
    * validation fails, an exception is thrown.
    *
    * @param   data        the request execution context
    *
    * @exception  PSErrorException
    *                                                                      the PSValidationError wrapped as an exception
    */
   public void validate(PSExecutionData data)
      throws PSErrorException
   {
      if (m_validationHandler == null)
         return;

      boolean isMatch;
      try {
         isMatch = m_validationHandler.isMatch(data);
      }
      catch (Exception e) {
         isMatch = false;  // throw a validation exception below
      }

      if (!isMatch)
      {
         PSRequest request = data.getRequest();

         org.w3c.dom.Document inDoc = request.getInputDocument();
         if (inDoc == null)
         {
            inDoc = PSHtmlParameterTree.generateHtmlParameterTree(request);
         }

         PSRequestValidationException exc
            = new PSRequestValidationException(m_validationRules);

         PSValidationError err = new PSValidationError(
               m_appHandler.getId(),   // application id
               request.getUserSessionId(),   // user session id
               exc.getErrorCode(),  // error code
               exc.getErrorArguments(),   // error text
               ((inDoc == null) ? null : inDoc.getDocumentElement()) );

         throw new PSErrorException(err, exc);
      }
   }


   /**
    * Loads and prepares the supplied exits so they can be efficiently
    * executed at run time. The exits are executed at the beginning of the
    * request processing. See {@link #loadExtensions(PSApplicationHandler,
    * PSCollection, String, List) loadExtensions} for a description of the
    * exceptions. Existing exits are discarded.
    *
    * @param exits The set of exits to use as pre-processing exits. If <code>
    *    null</code> or empty, the current exits are cleared.
    */
   protected final void setPreProcExits( PSExtensionCallSet exits )
      throws PSNotFoundException, PSExtensionException
   {
      m_preparedPreProcExts.clear();

      loadExtensions(m_appHandler, exits,
            IPSRequestPreProcessor.class.getName(), m_preparedPreProcExts);
   }


   /**
    * Loads and prepares the supplied exits so they can be efficiently
    * executed at run time. The exits are executed at the end of the
    * request processing, after the result doc has been created. See {@link
    * #loadExtensions(PSApplicationHandler, PSCollection, String, List)
    * loadExtensions} for a description of the exceptions. Existing exits
    * are discarded.
    *
    * @param exits The set of exits to use as post-processing exits. If <code>
    *    null</code> or empty, the current exits are cleared.
    */
   protected final void setResultDocExits( PSExtensionCallSet exits )
      throws PSNotFoundException, PSExtensionException
   {
      m_preparedPostProcExts.clear();

      loadExtensions( m_appHandler, exits,
            IPSResultDocumentProcessor.class.getName(),
            m_preparedPostProcExts);
   }



   /**
    * Prepares extension instances for each of the given extension calls
    * whose extension implements the given class or interface, storing
    * each prepared extension in the given collection.
    *
    * @param extCalls A collection of extension calls. Will not be modified.
    * Can be <CODE>null</CODE>, in which case this method will do nothing.
    *
    * @param interfaceName The fully qualified classname of the interface
    * that determines which of the referenced extensions will be prepared.
    * If <CODE>null</CODE>, all referenced extensions will be prepared.
    *
    * @param map The Map into which prepared extensions will be put, using
    * the corresponding PSExtensionRef as a key.
    */
   public static void loadExtensions(
         PSApplicationHandler appHandler,
         PSCollection extCalls,
         String interfaceName,
         List instances
         )
      throws PSNotFoundException, PSExtensionException
   {
     final int size = (extCalls == null) ? 0 : extCalls.size();
     for (int i = 0; i < size; i++)
      {
         PSExtensionCall call = (PSExtensionCall)extCalls.get(i);

         PSExtensionRef ref = call.getExtensionRef();
         if (ref == null)
         {
            throw new IllegalArgumentException("exitcall exit null");
         }

         if (appHandler.extensionImplements(ref, interfaceName))
         {
           IPSExtension ext = appHandler.prepareExtension(ref);
           instances.add(PSExtensionRunner.createRunner(call, ext));
         }
         else
         {
            Object[] args = {ref.toString(), interfaceName};
            throw new PSExtensionException(
               IPSExtensionErrors.INVALID_EXT_TYPE_EXCEPTION, args);
         }
      }
   }

   /**
    * Check the internal application security for the specified request.
    *
    * @param request The request context.
    *
    * @throws PSAuthorizationException if the user of the specified request
    *    is not authorized.
    *
    * @throws PSAuthenticationFailedException if the user in the specified
    *    request fails to authenticate.
    */
   public void checkInternalRequestAuthorization( PSRequest request )
      throws PSAuthorizationException, PSAuthenticationFailedException
   {
      m_appHandler.initInternalRequestAppSecurity(request, this);
   }

   /**
    * Returns the dataset provided to this handler in the ctor.
    *
    * @return The dataset, never <code>null</code>.
    */
   public PSDataSet getDataSet()
   {
      return m_dataset;
   }

   /**
    * Returns the name of the application for which this handler will process
    * requests.
    *
    * @return The app name, never <code>null</code> or empty.
    */
   public String getAppName()
   {
      return m_appHandler.getApplicationDefinition().getName();
   }
   
   /**
    * Log a problem to the console along with enough information to identify
    * the request and application being processed. Used by the update and query
    * handlers to report this information. The developer must examine the
    * console to obtain the logged info.
    * 
    * @param request the original request, never <code>null</code>.
    * @param th the throwable, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   protected void reportError(PSRequest request, Throwable th)
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request may not be null");
      }
      String requestPage = "<undefined>";
      StringBuilder params = new StringBuilder();
      if (getDataSet().getRequestor() != null)
      {
         requestPage = getDataSet().getRequestor().getRequestPage();
      }
      Map<String,Object> pmap = request.getParameters();
      boolean firstParam = true;
      for(String pname : pmap.keySet())
      {
         if (!firstParam)
         {
            params.append(',');
         }
         Object value = pmap.get(pname);
         params.append(pname);
         params.append("=");
         if (value instanceof ArrayList)
         {
            boolean first = true;
            for(Object oneValue : (ArrayList) value)
            {
               if (!first)
               {
                  params.append(',');
               }
               params.append(oneValue.toString());
               first = false;
            }
         }
         else
            params.append(value == null ? "" : value.toString());
         firstParam = false;
      }
      String message = MessageFormat.format(ms_errorMessage,
            getAppName(), getDataSetName(), requestPage, params.toString(),
            th.getLocalizedMessage());
      ms_log.error(message, th);
   }

   /**
    * List of prepared extensions to be run against the input data. May be
    * empty, never <code>null</code>.
    */
   protected List m_preparedPreProcExts = new ArrayList(3);

   /**
    * List of prepared extensions to be run against the result doc. May be
    * empty, never <code>null</code>.
    */
   protected List m_preparedPostProcExts = new ArrayList(3);

   protected PSApplicationHandler m_appHandler;
   protected String m_dataSetName;
   protected PSConditionalEvaluator m_validationHandler;
   protected PSCollection m_validationRules;

   /**
    * The dataset this handler was initialized with, never <code>null</code> or
    * modified after construction.
    */
   private PSDataSet m_dataset;
}

