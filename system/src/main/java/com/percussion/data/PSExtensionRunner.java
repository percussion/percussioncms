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

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.error.PSResult;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.log.PSLogHandler;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSAttemptResult;
import com.percussion.relationship.PSCloneAlreadyExistsException;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.PSRecoverResult;
import com.percussion.relationship.PSTestResult;
import com.percussion.search.IPSSearchResultsProcessor;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * PSExtensionRunners are used by the com.percussion.data package to
 * encapsulate and simplify the process of binding runtime extension params
 * to their extractors before running a particular extension.
 * <P>
 * The binding process happens the same as in the old PSJavaExtensionSandbox
 * class. The runner lets the extension model itself take care of security
 * considerations.
 * <P>
 * Typically the caller will construct this extension runner with an
 * extension
 */
public class PSExtensionRunner
{
   private static final Logger ms_log = LogManager.getLogger(PSExtensionRunner.class);
   
   /**
    * Constructs a new extension runner that can run the given extension,
    * extracting data based on the params defined in the extension
    * call.
    *
    * @param call The extension call which defines the params to be
    * bound to the extension for each invocation. Must not be
    * <CODE>null</CODE>.
    *
    * @param ext The extension instance. Must not be <CODE>null</CODE>.
    * @return extension runner, never <code>null</code>.
    *
    */
   public static PSExtensionRunner createRunner(
      PSExtensionCall call,
      IPSExtension ext
      )
   {
      if (ext == null)
         throw new IllegalArgumentException("ext cannot be null");

      if (call == null)
         throw new IllegalArgumentException("call cannot be null");

      PSExtensionRunner runner = new PSExtensionRunner(call);
      boolean foundSupported = false;

      if (ext instanceof IPSRequestPreProcessor)
      {
         runner.m_rppExt = (IPSRequestPreProcessor)ext;
         foundSupported = true;
      }

      if (ext instanceof IPSResultDocumentProcessor)
      {
         runner.m_rdpExt = (IPSResultDocumentProcessor)ext;
         foundSupported = true;
      }

      if (ext instanceof IPSUdfProcessor)
      {
         runner.m_udfExt = (IPSUdfProcessor)ext;
         foundSupported = true;
      }

      if (ext instanceof IPSEffect)
      {
         runner.m_effect = (IPSEffect) ext;
         foundSupported = true;
      }

      if (ext instanceof IPSSearchResultsProcessor)
      {
         runner.m_searchResultsProcessor = (IPSSearchResultsProcessor) ext;
         foundSupported = true;
      }

      if (!foundSupported)
      {
         // TODO: i18n and code
         throw new IllegalArgumentException(
               "unknown extension type for runner: " + ext.getClass().getName());
      }

      return runner;
   }

   /**
    * Constructs a new extension runner that will run the given
    * request preprocessor extension.
    *
    * @param call The extension call which defines the params to be
    * bound to the extension for each invocation. Must not be
    * <CODE>null</CODE>.
    *
    * @param ext The extension instance. Must not be <CODE>null</CODE>.
    *
    */
   public PSExtensionRunner(
      IPSRequestPreProcessor ext,
      PSExtensionCall call
      )
   {
      this(call);
      if (ext == null)
         throw new IllegalArgumentException("ext cannot be null");

      m_rppExt = ext;
   }

   /**
    * Constructs a new extension runner that will run the given
    * result document processor extension.
    *
    * @param call The extension call which defines the params to be
    * bound to the extension for each invocation. Must not be
    * <CODE>null</CODE>.
    *
    * @param ext The extension instance. Must not be <CODE>null</CODE>.
    *
    */
   public PSExtensionRunner(
      IPSResultDocumentProcessor ext,
      PSExtensionCall call
      )
   {
      this(call);
      if (ext == null)
         throw new IllegalArgumentException("ext cannot be null");

      m_rdpExt = ext;
   }

   /**
    * Constructs a new extension runner that will run the given
    * UDF processor extension.
    *
    * @param call The extension call which defines the params to be
    * bound to the extension for each invocation. Must not be
    * <CODE>null</CODE>.
    *
    * @param ext The extension instance. Must not be <CODE>null</CODE>.
    *
    */
   public PSExtensionRunner(
      IPSUdfProcessor ext,
      PSExtensionCall call
      )
   {
      this(call);
      if (ext == null)
         throw new IllegalArgumentException("ext cannot be null");

      m_udfExt = ext;
   }

   /**
    * Constructs a new extension runner that will run the given relationship
    * effect.
    *
    * @param effect the effect instance, not <code>null</code>.
    * @param call the extension call which defines the params to be
    *    bound to the extension for each invocation, not <code>null</code>.
    */
   public PSExtensionRunner(IPSEffect effect, PSExtensionCall call)
   {
      this(call);
      if (effect == null)
         throw new IllegalArgumentException("effect cannot be null");

      m_effect = effect;
   }

   /**
    * Private constructor that builds extractors for the params defined
    * in the given extension call.
    *
    * @param call The extension call. Must not be <CODE>null</CODE>.
    *
    */
   protected PSExtensionRunner(PSExtensionCall call)
   {
      if (call == null)
         throw new IllegalArgumentException("call cannot be null");

      buildExtractors(call);
      m_extCall = call;

      //the manager is used to get the IPSExtensionDef later on
      IPSExtensionManager extMgr = PSServer.getExtensionManager(null);

      if (extMgr==null)
         throw new IllegalStateException("extMgr cannot be null");

      PSExtensionRef extRef = getExtensionRef();

      try
      {
         //set extension def
         m_extDef = extMgr.getExtensionDef(extRef);
      }
      catch(Exception ex)
      {
          throw new
          IllegalStateException("Error when getting metadata for extension "
                             + extRef.toString() + ", msg: " + ex.getMessage());
      }
   }

   /**
    * Gets an iterator over the extractors that correspond to the
    * extension call with which this runner was constructed.
    *
    * @return An Iterator over 0 or more possibly <CODE>null</CODE>
    * IPSDataExtractor objects. Never <CODE>null</CODE>.
    */
   public Iterator getExtractors()
   {
      return m_extractors.iterator();
   }

   /**
    * @see IPSResultDocumentProcessor#processResultDocument(Object[], IPSRequestContext, Document)
    */
   public Document processResultDoc(PSExecutionData data, Document doc)
      throws PSExtensionProcessingException,
         PSParameterMismatchException,
         PSDataExtractionException
   {
      // TODO: check for m_rdpExt == null (runtime error condition)
      Object[] args = extractData(data);

      // Trace processing message
      PSLogHandler lh = data.getLogHandler();
      if (lh instanceof PSDebugLogHandler)
      {
         PSDebugLogHandler dh = (PSDebugLogHandler) lh;
         if (dh.isTraceEnabled(PSTraceMessageFactory.EXIT_PROC_FLAG))
         {
            Object[] traceArgs = {
                     "IPSResultDocumentProcessor",
                     m_extCall.getExtensionRef().getExtensionName(),
                     args};
            dh.printTrace(PSTraceMessageFactory.EXIT_PROC_FLAG, traceArgs);
         }
      }

      PSRequestContext context = new PSRequestContext(data.getRequest());
      doc = m_rdpExt.processResultDocument(args, context, doc);

      // trace initial doc for post exit xml
      if (lh instanceof PSDebugLogHandler)
      {
         PSDebugLogHandler dh = (PSDebugLogHandler) lh;
         if (dh.isTraceEnabled(PSTraceMessageFactory.POST_EXIT_XML_FLAG))
            dh.printTrace( PSTraceMessageFactory.POST_EXIT_XML_FLAG,
               new Object[] { doc,
                              m_extCall.getExtensionRef().getExtensionName()
               } );
      }

      return doc;
   }

   /**
    * @see IPSRequestPreProcessor#preProcessRequest
    */
   public void preProcessRequest(PSExecutionData data)
      throws PSExtensionProcessingException,
         PSParameterMismatchException,
         PSRequestValidationException,
         PSDataExtractionException,
         PSAuthorizationException
   {
      // TODO: check for m_rppExt == null (runtime error condition)
      Object[] args = extractData(data);

      // Trace message
      PSDebugLogHandler dh = (PSDebugLogHandler)data.getLogHandler();
      if (dh.isTraceEnabled(PSTraceMessageFactory.EXIT_PROC_FLAG))
      {
         Object[] traceArgs = {
                  "IPSRequestPreProcessor",
                  m_extCall.getExtensionRef().getExtensionName(),
                  args};
         dh.printTrace(PSTraceMessageFactory.EXIT_PROC_FLAG, traceArgs);
      }
      PSRequestContext context = new PSRequestContext(data.getRequest());
      m_rppExt.preProcessRequest(args, context);
   }

   /**
    * @see PSUdfCallExtractor#extract
    */
   public Object processUdfCallExtractor(PSExecutionData data)
      throws
//      PSExtensionProcessingException,
         PSConversionException,
         PSDataExtractionException
   {
      // TODO: check for m_udfExt == null (runtime error condition)
      Object[] args = extractData(data);

      PSRequestContext context = new PSRequestContext(data.getRequest());
      Object result = m_udfExt.processUdf(args, context);

      PSLogHandler lh = data.getLogHandler();
      if (lh instanceof PSDebugLogHandler)
      {
         PSDebugLogHandler dh = (PSDebugLogHandler) lh;
         if (dh.isTraceEnabled(PSTraceMessageFactory.MAPPER_FLAG))
         {
            Object[] traceArgs = {
               m_extCall.getExtensionRef().getExtensionName(), args, result };
            dh.printTrace(PSTraceMessageFactory.MAPPER_FLAG, traceArgs);
         }
      }

      return result;

   }

   /**
    * Executes the test() method of the relationship effect associated with
    * this extension runner.
    *
    * @param data the execution data to operate on, not <code>null</code>.
    * @param context the execution context to operate on, may be <code>null</code>.
    * @return <code>PSResult</code> object with status data, <code>null</code>
    * only when the effect associated with this object is <code>null</code>.
    * @throws PSExtensionProcessingException for any error occurred while
    * processing the effect.
    * @throws PSDataExtractionException if the required data could not be
    * extracted from the execution data.
    * incorrect.
    * @throws PSParameterMismatchException if the supplied parameters are
    * incorrect.
    */
   public PSResult testEffect(PSExecutionData data,
      IPSExecutionContext context)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      return executeEffect(data, context, 1, null);
   }
   
   /**
    * Run the search result processsor given the data and the search result rows
    * to be processed by the runner.
    * 
    * @param data execution data object, must not be <code>null</code>.
    * @param searchResultRows search result rows to be processed, must not be
    *           <code>null</code> or empty.
    * @return search result rows processed by the extension, may be
    *         <code>null</code> or empty.
    * @throws PSDataExtractionException if required extension arguments could
    *            not be extracted from the execution data.
    * @throws PSExtensionProcessingException if thrown by the extension
    *            implementation.
    * 
    * @see IPSSearchResultsProcessor#processRows(Object[], List,
    *      IPSRequestContext)
    */
   public List runSearchResultProcessor(PSExecutionData data,
         List searchResultRows) throws PSDataExtractionException,
         PSExtensionProcessingException
   {
      if (data == null)
         throw new IllegalArgumentException("data must not be null");

      if (searchResultRows == null || searchResultRows.isEmpty())
         throw new IllegalArgumentException(
               "searchResultRows must not be null or empty");
      
      Object[] args = extractData(data);

      PSRequestContext request = new PSRequestContext(data.getRequest());

      List rows = m_searchResultsProcessor.processRows(args,
            searchResultRows, request);

      // Trace processing message
      PSLogHandler lh = data.getLogHandler();
      if (lh instanceof PSDebugLogHandler)
      {
         PSDebugLogHandler dh = (PSDebugLogHandler) lh;
         if (dh.isTraceEnabled(PSTraceMessageFactory.EXIT_PROC_FLAG))
         {
            Object[] traceArgs =
            {"IPSSearchResultsProcessor",
                  m_extCall.getExtensionRef().getExtensionName(), args};
            dh.printTrace(PSTraceMessageFactory.EXIT_PROC_FLAG, traceArgs);
         }
      }
      return rows;
   }

   /**
    * Executes the attempt() method of the relationship effect associated with
    * this extension runner.
    *
    * @param data the execution data to operate on, not <code>null</code>.
    * @param context the execution context to operate on, may be <code>null</code>.
    * @return <code>PSResult</code> object with status data, <code>null</code>
    * only when the effect associated with this object is <code>null</code>.
    * @throws PSExtensionProcessingException for any error occurred while
    * processing the effect.
    * @throws PSDataExtractionException if the required data could not be
    * extracted from the execution data.
    * incorrect.
    * @throws PSParameterMismatchException if the supplied parameters are
    * incorrect.
    */
   public PSResult attemptEffect(PSExecutionData data,
      IPSExecutionContext context)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      return executeEffect(data, context, 2, null);
   }

   /**
    * Executes the recover() method of the relationship effect associated with
    * this extension runner.
    *
    * @param data the execution data to operate on, not <code>null</code>.
    * @param context the execution context to operate on, may be <code>null</code>.
    * @param e <code>PSExtensionProcessingException</code> object that caused
    * recovery to invoke.
    * @return <code>PSResult</code> object with status data, <code>null</code>
    * only when the effect associated with this object is <code>null</code>.
    * @throws PSExtensionProcessingException for any error occurred while
    * processing the effect.
    * @throws PSDataExtractionException if the required data could not be
    * extracted from the execution data.
    * incorrect.
    * @throws PSParameterMismatchException if the supplied parameters are
    * incorrect.
    */
   public PSResult recoverEffect(PSExecutionData data,
      IPSExecutionContext context, PSExtensionProcessingException e)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      return executeEffect(data, context, 3, e);
   }

   /**
    * Helper method that executes one of the methods (viz. test() or attempt()
    * or recover() which have somehwhat similar signature) of the relationship
    * effect associated with this extension runner.
    *
    * @param data the execution data to operate on, not <code>null</code>.
    * @param context the execution context to operate on, may be <code>null</code>.
    * @param executeMethod 1 to run the test() method and 2 to run the attempt()
    * method.
    * @param e <code>PSExtensionProcessingException</code> object that caused
    * recovery to invoke. Will be meaningful only for recover() method and hence
    * is <code>null</code> for other methods.
    * @return <code>PSResult</code> object with status data, <code>null</code>
    * only when the effect associated with this object is <code>null</code>.
    * @throws PSExtensionProcessingException for any error occurred while
    * processing the effect.
    * @throws PSDataExtractionException if the required data could not be
    * extracted from the execution data.
    * incorrect.
    * @throws PSParameterMismatchException if the supplied parameters are
    * incorrect.
    */
   protected PSResult executeEffect(PSExecutionData data,
      IPSExecutionContext context, int executeMethod,
      PSExtensionProcessingException e)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      if (m_effect != null)
      {
         Object[] args = extractData(data);

         PSRequestContext request = new PSRequestContext(data.getRequest());
         PSEffectResult result = null;

         try
         {
            switch(executeMethod)
            {
               case 1:
                  result = new PSTestResult();
                  m_effect.test(args, request, context, result);
                  break;
               case 2:
                  result = new PSAttemptResult();
                  m_effect.attempt(args, request, context, result);
                  break;
               case 3:
                  result = new PSRecoverResult();
                  m_effect.recover(args, request, context, e, result);
                  break;
               default:
            }
         } catch (PSCloneAlreadyExistsException ce)
         {
            String msg = "Existing clone (" + ce.getDependent().getId() 
               + ") found for item " 
               + ce.getOwner().getId()
               + " while attempting to create a " 
               + ce.getRelationshipType()
               + " relationship.";
            ms_log.info(msg);
            throw ce;
         } catch(PSExtensionProcessingException | PSParameterMismatchException | RuntimeException e1)
         {
            String message = getExceptionInfo(args, m_effect, executeMethod);
            ms_log.error(message);
            ms_log.debug(e1);
            throw e1;

         }

         // Trace processing message
         PSLogHandler lh = data.getLogHandler();
         if (lh instanceof PSDebugLogHandler)
         {
            PSDebugLogHandler dh = (PSDebugLogHandler) lh;
            if (dh.isTraceEnabled(PSTraceMessageFactory.EXIT_PROC_FLAG))
            {
               Object[] traceArgs =
               {
                  "IPSEffect",
                  m_extCall.getExtensionRef().getExtensionName(),
                  args
               };
               dh.printTrace(PSTraceMessageFactory.EXIT_PROC_FLAG, traceArgs);
            }
         }
         return result;
      }
      return null;
   }

   /**
    * Create error message for exception during effects processing
    * 
    * @param args the arguments passed, assumed not <code>null</code>
    * @param effect the effect being run, assumed not <code>null</code>
    * @param executeMethod the result being created
    * @return a message string describing what was being run
    */
   private String getExceptionInfo(Object[] args, IPSEffect effect, 
         int executeMethod)
   {
      StringBuilder buf = new StringBuilder();
      buf.append("Problem while processing effect: ");
      buf.append(effect.getClass().getName());
      buf.append("\nargs: ");
      for (Object arg : args) {
         buf.append(arg.toString());
         buf.append(" ");
      }
      
      buf.append("\nrunning ");
     
      switch(executeMethod)
      {
         case 1:
            buf.append("test");
            break;
         case 2:
            buf.append("attempt");
            break;
         case 3:
            buf.append("recover");
            break;
         default:
      }
      
      return buf.toString();
   }

   /**
    * @return an extension identifier object, never <code>null</code>
   */
   public PSExtensionRef getExtensionRef()
   {
      if (m_extCall==null)
         throw new IllegalStateException("m_extCall cannot not be null");

      return m_extCall.getExtensionRef();
   }

   /**
    * @return the metadata for the extension, never <code>null</code>
   */
   public IPSExtensionDef getExtensionDef()
   {
      if (m_extDef==null)
         throw new IllegalStateException("m_extDef cannot be null");

      return m_extDef;
   }

   /**
    * Uses the prebuilt extractors to extract execution data.
    *
    * @param data The execution data associated with a request. Must not
    * be <CODE>null</CODE>.
    *
    * @return An array of Objects, corresponding to the extracted value
    * in the same position (defined by the PSExtensionCall). Extractors
    * that are <CODE>null</CODE> will cause a <CODE>null</CODE> value
    * to be stored at the corresponding position in this returned array.
    * Never <CODE>null</CODE>.
    *
    * @see #buildExtractors
    *
    * @throws PSDataExtractionException
    */
   private Object[] extractData(PSExecutionData data)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");

      Object[] args = new Object[m_extractors.size()];
      int extractorNum = 0;
      for (Object m_extractor : m_extractors) {
         IPSDataExtractor extractor = (IPSDataExtractor) m_extractor;
         if (extractor != null)
            args[extractorNum++] = extractor.extract(data);
         else
            args[extractorNum++] = null;
      }

      return args;
   }

   /**
    * Builds an extractor for each non-<CODE>null</CODE> parameter
    * defined in the extension call. Any parameter values defined as
    * <CODE>null</CODE> will cause a <CODE>null</CODE> extractor
    * to be added to the extractors list.
    *
    * @param call The extension call. Must not be <CODE>null</CODE>.
    *
    */
   private void buildExtractors(PSExtensionCall call)
   {
      ArrayList extractors = new ArrayList();
      PSExtensionParamValue[] vals = call.getParamValues();
      for (int i = 0; i < vals.length; i++)
      {
         PSExtensionParamValue val = vals[i];
         if (vals[i] != null)
         {
            extractors.add(
               PSDataExtractorFactory.createReplacementValueExtractor(val.getValue()));
         }
         else
         {
            extractors.add(null); // TODO: is this correct
         }
      }

      extractors.trimToSize(); // optimize memory consumption
      m_extractors = extractors;
   }

   /** Request preprocessor. Could be <CODE>null</CODE>. */
   protected IPSRequestPreProcessor m_rppExt;

   /** Result document processor. Could be <CODE>null</CODE>. */
   protected IPSResultDocumentProcessor m_rdpExt;

   /** UDF processor. Could be <CODE>null</CODE>. */
   protected IPSUdfProcessor m_udfExt;

   /**
    * The relationship effect processor, initialized in ctor, may be
    * <code>null</code>.
    */
   protected IPSEffect m_effect = null;

   /**
    * The search results processor, initialized in ctor, may be
    * <code>null</code>.
    */
   protected IPSSearchResultsProcessor m_searchResultsProcessor = null;

   /**
   * extension def interface, once this runner is created never <code>null</code>
   */
   private IPSExtensionDef m_extDef;

   /** Extractors. */
   private Collection m_extractors;

   /** The PSExtensionCall */
   private PSExtensionCall m_extCall;
}
