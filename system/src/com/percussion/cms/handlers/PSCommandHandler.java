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
package com.percussion.cms.handlers;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSPageInfo;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSConditionalExitEvaluator;
import com.percussion.data.PSConditionalUrlEvaluator;
import com.percussion.data.PSContentItemStatusExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSDataHandler;
import com.percussion.data.PSErrorCollector;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSFieldSetValidationRulesEvaluator;
import com.percussion.data.PSFieldValidationRulesEvaluator;
import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSSingleHtmlParameterExtractor;
import com.percussion.data.PSValidationRulesEvaluator;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.design.objectstore.PSConditionalRequest;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSFieldTranslation;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSErrorException;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Base class for all command handlers, provides common functionality that
 * some or all command handlers will use.
 */
public abstract class PSCommandHandler extends PSDataHandler
{
   /**
    * The error log.
    */
   private static Log ms_log = LogFactory.getLog(PSCommandHandler.class); 

   public static String fixProxiedUrl(String url, String serverName, int serverPort) throws MalformedURLException {
      URL proxyTest = new URL(url);
      String replacementUrl = url;

      if(!proxyTest.getHost().equals(PSServer.getServerName().toLowerCase())
      ||!proxyTest.getHost().equals(serverName) ||
              proxyTest.getPort() != serverPort ||
               proxyTest.getPort() != PSServer.getListenerPort()) {
         String ref = proxyTest.getRef();
         if(ref == null)
            ref="";
         else
            ref="#"+ref;
         replacementUrl = proxyTest.getProtocol() + "://" + serverName +  (serverPort ==-1 ? "" : ":" +serverPort) +
                 proxyTest.getPath()  + ref + "?" + proxyTest.getQuery();
      }
      return replacementUrl;
   }
   /**
    * Constructor for this class.  Delegates preparation of dataset exits to
    * its base class, does not prepare data in any other way.  It is up to
    * the derived classes to call the methods to prepare any data that they
    * will need at runtime.
    *
    * @param appHandler The application handler for this app.  May not be
    * <code>null</code>.
    * @param ceh The content editor handler for this dataset.  May not be
    * <code>null</code>.
    * @param ce The Content Editor this command handler will process commands
    * for.  May not be <code>null</code>.
    * @param app The application created by the PSContentEditor for each
    * command handler to add datasets to.  The app is started and stopped by the
    * ContentEditorHandler. May not be <code>null</code>.
    *
    * @throws PSNotFoundException If an exit cannot be found.
    * @throws PSExtensionException If any problems occur during extension
    * initialization.
    */
   public PSCommandHandler(PSApplicationHandler appHandler,
      PSContentEditorHandler ceh, PSContentEditor ce, PSApplication app)
      throws PSExtensionException, PSNotFoundException
   {
      super(appHandler, ce);

      if (ceh == null)
         throw new IllegalArgumentException("ceh may not be null");

      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      m_ce = ce;
      m_internalApp = app;
      m_ceHandler = ceh;
      m_appHandler = appHandler;

      m_workflowidExtractor = new PSContentItemStatusExtractor(
         new PSContentItemStatus(IPSConstants.CONTENT_STATUS_TABLE, "WORKFLOWAPPID"));
      m_localeExtractor = new PSContentItemStatusExtractor(
         new PSContentItemStatus(IPSConstants.CONTENT_STATUS_TABLE, "LOCALE"));
      m_communityExtractor = new PSContentItemStatusExtractor(
         new PSContentItemStatus(IPSConstants.CONTENT_STATUS_TABLE, "COMMUNITYID"));
   }

   /**
    * Convenience method for retreiving a single id, delegates to {@link
    * #getNextIdBlock(String, int) getNextIdBlock}, passing 1 for blockSize.
    */
   public int getNextId(String key) throws SQLException
   {
      int[] result = getNextIdBlock(key, 1);
      return result[0];
   }

   /**
    * Returns the next id to use when inserting data.  Reserves a block of id's
    * based on the supplied blockSize.
    *
    * @param key The key value identifying the type of id to get.  May not be
    * <code>null</code> or empty.
    *
    * @param blockSize The number of id's to reserve.  Must be greater than
    * zero.
    *
    * @return An array of id's, whose length is equal to the blocksize.
    *
    * @throws SQLException if there is an error retrieving the id.
    */
   public int[] getNextIdBlock(String key, int blockSize) throws SQLException
   {
      if (key == null || key.length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      if (blockSize <= 0)
         throw new IllegalArgumentException(
            "blockSize must be greater than zero");

      int[] idBlock = PSIdGenerator.getNextIdBlock(key, blockSize);

      return idBlock;
   }

   /**
    * Creates a relative request string that can be used to obtain the result
    * handler from the server.
    *
    * @param appName The name of the application, may not be <code>null
    *    </code> or empty.
    * @param reqPage The name of the dataset for which you want to obtain a
    *    handler. May not be <code>null</code> or empty.
    * @return The fully qualified request name. Never <code>null</code> or
    *    empty.
    */
   public static String createRequestName(String appName, String reqPage)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (reqPage == null || reqPage.trim().length() == 0)
         throw new IllegalArgumentException("reqPage may not be null or empty");

      return appName + "/" + reqPage;
   }


   /**
    * This method is used to set the workflow access level. When a user tries
    * to do anything with a content editor, the authenticateUser exit is
    * executed first. This exit verifies that the user has at least a minimum
    * level of access, as specified by one of the exit's parameters. For some
    * handlers, all actions have the same access level, so it can be specified
    * in the system def. However, for others, they may have to set the access
    * level dynamically.
    * <p>If a handler needs to do this, it should call this method before the
    * pre-exits are processed.
    *
    * @param req The request that is currently being processed. Never <code>
    *    null</code>.
    *
    * @param level One of the ASSIGNMENT_TYPE_xxx values defined in
    *    com.percussion.workflow.PSWorkFlowUtils. This method does not
    *    validate the supplied type.
    */
   public static void setMinAccessLevel( PSRequest req, int level )
   {
      if ( null == req )
         throw new IllegalArgumentException( "request can't be null" );

      req.setParameter( MIN_ACCESS_PARAM_NAME, new Integer( level ));
   }


   /**
    * This method is used to set the workflow access level. When a user tries
    * to do anything with a content editor, the authenticateUser exit is
    * executed first. If this value has been set, and it is a new item, then
    * the exit will validate that the user is a member of one of the roles
    * necessary to edit and checkin the item after it has entered the initial
    * state of the workflow.
    * <p>If a handler needs to do this, it should call this method before the
    * pre-exits are processed.
    *
    * @param req The request that is currently being processed. Never <code>
    *    null</code>.
    *
    * @param workflowAppId The id of the workflow this content editor uses.
    * Usually retrieved by a call to {@link PSContentEditor#getWorkflowId()}.
    */
   public static void setWorkflowAppId(PSRequest req, int workflowAppId)
   {
      if (null == req)
         throw new IllegalArgumentException("request can't be null");

      req.setParameter(WORKFLOW_APP_ID_PARAM_NAME, new Integer(workflowAppId));
   }


   /**
    * This method is used as part of the authentication process. When a user
    * tries to do anything with a content editor, the authenticateUser exit is
    * executed first. This exit checks the minimum access level and the state
    * of the doc as to whether it is checked in, checked out. For some handlers,
    * this is the same regardless of how the handler is called. In this case,
    * the value is set as a constant in the system def. If the value needs to
    * change depending on what the user is actually trying to do, this method
    * must be called before the pre-exits are executed.
    *
    * @param req The request that is currently being processed. Never <code>
    *    null</code>.
    *
    * @param condition One of the CHECKINOUT_CONDITION_xxx types defined in
    *    com.percussion.workflow.PSWorkFlowUtils. This method does not
    *    validate the supplied type.
    */
   public static void setCheckInOutCondition( PSRequest req, String condition )
   {
      if ( null == req || null == condition || condition.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "one or more params was null or empty" );
      }

      req.setParameter( CHECKINOUT_CONDITION_PARAM_NAME, condition );
   }


   /**
    * Retrieves and prepares the conditional redirects for the specified
    * handler.
    *
    * @param commandName The name of the handler to prepare redirects for.  May
    * not be <code>null</code> or empty.
    *
    * @throws PSNotFoundException if an udf specified in a rule does not exist.
    * @throws PSExtensionException if there is an error preparing an extension
    * or a rule.
    * @throws PSValidationException if no redirects are found for the specified
    * commandName.
    */
   protected void prepareRedirects(String commandName)
      throws PSNotFoundException,
      PSExtensionException, PSValidationException
   {
      if (commandName == null || commandName.trim().length() == 0)
         throw new IllegalArgumentException(
            "commandName may not be null or empty");

      PSApplicationFlow appFlow = m_ce.getApplicationFlow();
      List<PSConditionalUrlEvaluator> reqEvaluators = 
         new ArrayList<PSConditionalUrlEvaluator>(4);
      Iterator requests = appFlow.getRedirects(commandName);

      if (requests == null || !requests.hasNext())
         throw new PSValidationException(IPSServerErrors.CE_MISSING_REDIRECTS,
            commandName);

      while (requests.hasNext())
      {
         PSConditionalRequest request =
            (PSConditionalRequest) requests.next();
         PSConditionalUrlEvaluator reqEval =
            new PSConditionalUrlEvaluator(request.getConditions(), request);
         reqEvaluators.add(reqEval);
      }

      m_redirects = new PSConditionalUrlEvaluator[reqEvaluators.size()];
      reqEvaluators.toArray(m_redirects);
   }

   /**
    * Determines the correct redirect to make, updates the response, and
    * sends it.  {@link #prepareRedirects(String) prepareRedirects()} must have
    * been called prior to this method.
    *
    * @param data The execution data.  May not be <code>null</code>.
    *
    * @throws IOException if there is an error sending the response.
    * @throws IllegalStateException if redirects have not been prepared.
    * @throws PSDataExtractionException for any other errors.
    */
   protected void processRedirect(PSExecutionData data) throws IOException,
      PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      // create the redirect url
      String url = getRedirectURL(data);
      if (url == null)
         throw new PSDataExtractionException(
            IPSServerErrors.CE_NO_REDIRECT_URL);

      sendRedirect(data, processUrlReplacementParameters(url, data));
   }

   /**
    * Processes all html parameters marked as replacement values. Currently we
    * only support single html parameters. The markup expected for that is
    * <code>:PSXSingleHtmlParamter/parameterName</code>. The extractor
    * identifier <code>:PSXSingleHtmlParamter</code> is not case sensitive.
    *
    * @param url the url to be processed, not <code>null</code>, may be empty.
    * @param data the execution data from which to extract the replacement
    *    values, not <code>null</code>.
    * @throws PSDataExtractionException if the data extraction fails.
    */
   protected String processUrlReplacementParameters(String url,
      PSExecutionData data) throws PSDataExtractionException
   {
      if (url == null || data == null)
         throw new IllegalArgumentException("parameters cannot be null");

      String replacementParamName = ":PSXSingleHtmlParameter";
      String nameDelimiter = "/";
      String paramDelimiter = "=";

      // 1st collect all parameters that define replacement values
      Map<String, String> replaceParameters = new HashMap<String, String>();
      if (url.indexOf(replacementParamName) >= 0)
      {
         StringTokenizer parameters = new StringTokenizer(url, "?&");
         while (parameters.hasMoreElements())
         {
            String parameter = parameters.nextToken();
            if (parameter.indexOf(replacementParamName) >= 0)
            {
               int index = parameter.indexOf(paramDelimiter);
               if (index >= 0)
               {
                  int nameStart = parameter.indexOf(nameDelimiter, index) +
                     nameDelimiter.length();
                  if (nameStart <= 0)
                     throw new IllegalArgumentException(
                        "No parameter name was specified for an HTML " +
                        "parameter replacement value.");

                  String name = parameter.substring(nameStart);
                  replaceParameters.put(name, parameter);
               }
            }
         }
      }

      try
      {
         // 2nd replace the replacement parameters with the real value
         Iterator<String> replacements = replaceParameters.keySet().iterator();
         while (replacements.hasNext())
         {
            String name = replacements.next();
            String parameter = replaceParameters.get(name);

            IPSDataExtractor extractor =
               PSDataExtractorFactory.createReplacementValueExtractor(
                  new PSSingleHtmlParameter(name));

            String realParameter = name + paramDelimiter +
               extractor.extract(data).toString();

            int parameterIndex = url.indexOf(parameter);
            url = url.substring(0, parameterIndex) + realParameter +
               url.substring(parameterIndex + parameter.length());
         }

         return url;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * This will get the response out of the provided execution data and set it
    * up to redirect the client to the provided URL. Then it sends the
    * redirected response back to the client.
    *
    * @param data the execution data containing the response, not
    *    <code>null</code>.
    * @param url the fully qualified URL, including all parameters, anchors,
    *    etc. Not <code>null</code> or empty.
    */
   protected void sendRedirect(PSExecutionData data, String url)
      throws IOException
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("url cannot be null or empty");

      //Make sure the url
       String isBehindProxy = PSServer.getProperty("requestBehindProxy") == null ? "" : PSServer.getProperty("requestBehindProxy");
       if((isBehindProxy.equalsIgnoreCase("true")) && !(data.getRequest().getServletRequest().getServerName().equalsIgnoreCase("localhost") || data.getRequest().getServletRequest().getServerName().equalsIgnoreCase("127.0.0.1") )) {
           int port = Integer.valueOf(PSServer.getProperty("proxyPort"));
           String scheme = PSServer.getProperty("proxyProtocol");
           String domainName = PSServer.getProperty("publicCmsHostname");
           String URL ="";
           if(port==443 || port ==80){
               port =-1;
           }
           url = fixProxiedUrl(url,domainName, port);
       }else{
           url = fixProxiedUrl(url,
                   data.getRequest().getServletRequest().getServerName(),
                   data.getRequest().getServletRequest().getServerPort());
       }



      data.getRequest().getResponse().sendRedirect(url, data.getRequest());
   }

   /**
    * Gets and prepares all extensions used by this handler.
    *
    * @param commandName The name of the handler to prepare redirects for.  May
    * not be <code>null</code> or empty.
    *
    * @throws PSNotFoundException if an udf specified in a rule does not exist.
    * @throws PSExtensionException if there is an error preparing an extension
    * or a rule.
    */
   protected void prepareExtensions(String commandName)
      throws PSExtensionException, PSNotFoundException
   {
      if (commandName == null || commandName.trim().length() == 0)
         throw new IllegalArgumentException(
            "commandName may not be null or empty");

      PSContentEditorPipe pipe = (PSContentEditorPipe) m_ce.getPipe();

      // get the sysdef from ceh and override the exit defs in the base
      PSContentEditorSystemDef sysDef = m_ceHandler.getSystemDef();

      // get dataset and cmd pre and post exits and prepare them
      PSExtensionCallSet preExits = new PSExtensionCallSet();
      preExits.addAll(new PSCollection(
            sysDef.getInputDataExits(commandName)));
      PSExtensionCallSet pipePreExits = pipe.getInputDataExtensions();
      if (pipePreExits != null)
         preExits.addAll(pipePreExits);
      setPreProcExits(preExits);

      PSExtensionCallSet postExits = new PSExtensionCallSet();
      postExits.addAll(new PSCollection(
            sysDef.getResultDataExits(commandName)));
      PSExtensionCallSet pipePostExits = pipe.getResultDataExtensions();
      if (pipePostExits != null)
         postExits.addAll(pipePostExits);
      setResultDocExits(postExits);
   }


   /**
    * Gets and prepares all item input translation exits.
    *
    * @throws PSNotFoundException if a udf specified in a rule does not exist.
    * @throws PSExtensionException if there is an error preparing an extension
    * or a rule.
    */
   protected void prepareInputTranslations() throws PSExtensionException,
      PSNotFoundException
   {
      Iterator i = m_ce.getInputTranslations();
      while (i.hasNext())
      {
         PSConditionalExit e = (PSConditionalExit)i.next();
         m_itemInputTranslations.add(new PSConditionalExitEvaluator(e,
            m_appHandler, IPSRequestPreProcessor.class.getName()));
      }
   }


   /**
    * Gets and prepares all item output translation exits.
    *
    * @throws PSNotFoundException if a udf specified in a rule does not exist.
    * @throws PSExtensionException if there is an error preparing an extension
    * or a rule.
    */
   protected void prepareOutputTranslations() throws PSExtensionException,
      PSNotFoundException
   {
      Iterator i = m_ce.getOutputTranslations();
      while (i.hasNext())
      {
         PSConditionalExit e = (PSConditionalExit)i.next();
         m_itemOutputTranslations.add(new PSConditionalExitEvaluator(e,
            m_appHandler, IPSResultDocumentProcessor.class.getName()));
      }
   }


   /**
    * Determines the list of input translation exits that should be run and
    * then executes them.
    *
    * @param data The executution data used to evaluate the exit's conditional
    * rules and to apply the exits to.  May not be <code>null</code>.
    *
    * @throws PSErrorException If any exception occurs which prevents the proper
    * handling of this request
    */
   protected void runInputTranslations(PSExecutionData data)
      throws PSErrorException
   {

      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      runPreProcessingExtensions(data, getAppliedExtensions(data,
         m_itemInputTranslations));
   }

   /**
    * Determines the list of output translation exits that should be run and
    * then executes them.
    *
    * @param data The executution data used to evaluate the exit's conditional
    * rules and to apply the exits to.  May not be <code>null</code>.
    * @param doc The result XML document, can be <code>null</code>
    *
    * @return The result doc with any modifications applied from the exits.  May
    * be <code>null</code>.
    *
    * @throws PSParameterMismatchException If the runtime parameters specified
    * in a call are incorrect for the usage of that extension.
    *
    * @throws PSDataExtractionException if there is an error extracting any
    * parameters.
    *
    * @throws PSExtensionProcessingException If any other exception occurs which
    * prevents the proper handling of this request
    */
   protected Document runOutputTranslations(PSExecutionData data,
      Document doc)
      throws PSExtensionProcessingException, PSParameterMismatchException,
         PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      return runPostProcessingExtensions(data, doc,
         getAppliedExtensions(data, m_itemOutputTranslations));
   }

   /**
    * Creates the input field translations for all mappings in the provided
    * mapper for the supplied field set.
    *
    * @param childId the childid for which this will produce the input field
    *    translations.
    * @param mapper the display mapper to walk all mappings, not
    *    <code>null</code>.
    * @param fieldSet the fieldSet, containing all fields to prepare the
    *    translations for, not <code>null</code>.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    */
   @SuppressWarnings("unchecked")
   protected void prepareInputFieldTranslations(int childId,
      PSDisplayMapper mapper, PSFieldSet fieldSet)
      throws PSExtensionException, PSNotFoundException
   {
      Map runnerMap = new HashMap();

      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if ( null == o)
            o = fieldSet.getChildField(fieldName,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);

         if (o instanceof PSFieldSet)
         {
            PSFieldSet fs = (PSFieldSet) o;
            if (fs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
               prepareSimpleChildTranslations(
                  mapping.getDisplayMapper(), runnerMap);
         }
         else if (o instanceof PSField)
         {
            PSField field = (PSField) o;
            String submitName = field.getSubmitName();

            PSTransformRunner in = getPreparedExits(
               field.getInputTranslation());
            
            if (in != null)
               runnerMap.put(submitName, in);
         }
      }

      m_inputFieldTranslations.put(Integer.toString(childId), runnerMap);
   }

   /**
    * Prepare the input translations for a simple child. The prepared
    * runners will be put to the provided map.
    *
    * @param mapper the mapper to walk for all fields, assumed not
    *    <code>null</code>.
    * @param runnerMap the map to which all created runner will be added,
    *    assumed not <code>null</code>.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    */
   @SuppressWarnings("unchecked")
   private void prepareSimpleChildTranslations(PSDisplayMapper mapper,
      Map runnerMap)
      throws PSExtensionException, PSNotFoundException
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) m_ce.getPipe();
      PSFieldSet fieldSet =
         pipe.getMapper().getFieldSet(mapper.getFieldSetRef());
      if (fieldSet == null)
         return;

      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping =
            (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if (o instanceof PSField)
         {
            PSField field = (PSField) o;
            String submitName = field.getSubmitName();

            PSTransformRunner  in = getPreparedExits(
               field.getInputTranslation());
            
            if (in != null)
               runnerMap.put(submitName, in);
         }
      }
   }

   /**
    * Gets and prepares all field translation exits used by this handler.
    *
    * @param ce the content editor to create the translations for, not
    *    <code>null</code>.
    * @throws PSNotFoundException if an udf specified in a rule does not exist.
    * @throws PSExtensionException if there is an error preparing an extension
    *    or a rule.
    * @throws PSValidationException if there are any mismatches between the
    *    fieldset list and the display mappings
    */
   protected void prepareOutputFieldTranslations(PSContentEditor ce)
      throws PSExtensionException, PSNotFoundException, PSValidationException
   {
      if (ce == null)
         throw new IllegalArgumentException("ce cannot be null");

      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSDisplayMapper dispMapper =
            pipe.getMapper().getUIDefinition().getDisplayMapper();

      prepareOutputFieldTranslations(ce, dispMapper);
   }

   /**
    * Prepares all 'output' field translations. This is called recursively
    * so all field translations for all item pages will be prepared.
    *
    * @param ce the content editor, assumed not <code>null</code>.
    * @param dispMapper the display mapper, assumed not <code>null</code>.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSValidationException if there are any mismatches between the
    *    fieldset list and the display mappings
    */
   private void prepareOutputFieldTranslations(PSContentEditor ce,
      PSDisplayMapper dispMapper)
      throws PSNotFoundException, PSExtensionException, PSValidationException
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSFieldSet fieldSet =
            pipe.getMapper().getFieldSet(dispMapper.getFieldSetRef());

      if ( null == fieldSet )
      {
         throw new PSValidationException( IPSServerErrors.CE_MISSING_FIELDSET,
               dispMapper.getFieldSetRef());
      }

      Iterator mappings = dispMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if ( null == o)
            o = fieldSet.getChildField(fieldName,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);

         if ( null == o )
         {
            String label = "unlabeled";
            if ( null != mapping.getUISet().getLabel())
               label = mapping.getUISet().getLabel().getText();
            String [] args =
            {
               fieldName,
               label
            };
            throw new PSValidationException(
                  IPSServerErrors.CE_MISSING_FIELD, args );
         }

         if (o instanceof PSFieldSet)
         {
            PSFieldSet fs = (PSFieldSet) o;
            PSDisplayMapper childMapper = mapping.getDisplayMapper();
            if (fs.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
               prepareOutputFieldTranslations(ce, childMapper);

            /*
             * NOTE: input field translations are currently not supported
             * for simple children (choices).
             */
         }
         else if (o instanceof PSField)
         {
            PSField field = (PSField) o;
            String submitName = field.getSubmitName();

            PSTransformRunner out = getPreparedExits(
               field.getOutputTranslation());
            if (out != null)
               m_outputFieldTranslations.put(submitName, out);
         }
      }
   }

   /**
    * Prepares all translation exits and returns them. It adds the implicit
    * parameter for the fields submit name. The actual data will be filled
    * in when the exit import run.
    *
    * @param translations the field translations to prepare the exits for,
    *    might be <code>null</code>.
    * @return The transform runner containing a list of prepared exits, 
    * <code>null</code> if there are no field translations.
    * 
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSExtensionException If there is an error preparing an extension.
    */
   private PSTransformRunner getPreparedExits(PSFieldTranslation translations)
      throws PSNotFoundException, PSExtensionException
   {
      try
      {
         if (translations == null)
            return null;

         PSExtensionCallSet exitSet = translations.getTranslations();
         if (exitSet == null)
            return null;

         List<PSExtensionRunner> preparedExits = 
            new ArrayList<PSExtensionRunner>();
         PSDataHandler.loadExtensions(m_appHandler, exitSet,
            IPSUdfProcessor.class.getName(), preparedExits);
         if (preparedExits.isEmpty())
            return null;
         String dispText = translations.getErrorMessage() != null ? 
            translations.getErrorMessage().getText() : null;
            
         return new PSTransformRunner(preparedExits, dispText);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Walks List of PSConditionalExitEvaluators and for each entry, checks for a
    * match. If <code>true</code>, add its prepared extensions to a list it
    * will then return an iterator over.
    *
    * @param data The execution data to use to evaluate the rules.  May not be
    * <code>null</code>.
    * @param extList List of PSConditionalExitEvaluator object.  May not be
    * <code>null</code>, may be empty.
    *
    * @return An Iterator over <code>zero</code> or more prepared extensions.
    */
   @SuppressWarnings("unchecked")
   protected Iterator getAppliedExtensions(PSExecutionData data, List extList)
   {
      if (data == null || extList == null)
         throw new IllegalArgumentException("data or extList may not be null");

      List results = new ArrayList();
      Iterator entries = extList.iterator();
      while (entries.hasNext())
      {
         PSConditionalExitEvaluator exitEval =
            (PSConditionalExitEvaluator)entries.next();
         if (exitEval.isMatch(data))
            results.addAll(exitEval.getExits());
      }

      return results.iterator();
   }

   /**
    * Gets the proper redirect URL.  Checks for the {@link
    * IPSHtmlParameters#DYNAMIC_REDIRECT_URL} parameter.  If found, returns its
    * value.  If not, evaluates all conditional requests and uses the matching
    * reqeust to construct the redirect url.
    *
    * @param data The execution data. Assumed not <code>null</code>.
    *
    * @return The full URL for the selected request, might be
    *    <code>null</code>.
    *
    * @throws IllegalStateException if redirects have not been prepared.
    */
   private String getRedirectURL(PSExecutionData data) throws
      PSDataExtractionException
   {
      if (m_redirects == null)
         throw new IllegalStateException("No redirects have been prepared");

      String url = null;

      // if an update, see if psredirect param has been specified.
      if (isUpdate())
      {
         String psredirect = data.getRequest().getParameter(
         IPSHtmlParameters.DYNAMIC_REDIRECT_URL);

         if (psredirect != null && psredirect.trim().length() > 0)
            url = psredirect;
      }

      // if we didn't get a redirect from the params, use the stored requests.
      if (url == null)
      {
         // need to truncate any params that are lists
         PSRequest request = data.getRequest();
         request.setParameters((HashMap)request.getTruncatedParameters());

         for (int i = 0; i < m_redirects.length; i++)
         {
            if (m_redirects[i].isMatch(data))
            {
               url = m_redirects[i].getUrl(data);
               break;
            }
         }
      }

      return url;
   }

   /**
    * Get the pageid from the supplied map of PSPageInfo objects for the
    * provided childid.
    *
    * @param pages a map of PSPageInfo objects with all known pages for the
    *    current content editor, not <code>null</code>.
    * @param childId the childid we want the pageid for.
    * @return the pageid found, always a valid id.
    * @throws RuntimeException if no pageid could be found for the provided
    *    childid.
    */
   public Integer getPageId(Map pages, int childId)
   {
      if (pages == null)
         throw new IllegalArgumentException("page map cannot be null");

      Iterator pageIds = pages.keySet().iterator();
      while (pageIds.hasNext())
      {
         Integer pageId = (Integer) pageIds.next();
         Object info = pages.get(pageId);
         if (info instanceof PSPageInfo &&
            ((PSPageInfo) info).getChildId() == childId)
            return pageId;
      }

      // this should never happen
      throw new RuntimeException(
         "No pageid found for childid: " + childId);
   }

   /**
    * Prepares the validation for the entire content editor provided.
    *
    * @param ce the content editor to prepare validation for, not
    *   <code>null</code>.
    * @throws PSNotFoundException if a validation extension could not be
    *    found.
    * @throws PSExtensionException if the extension preparation fails.
    */
   protected void prepareValidation(PSContentEditor ce)
      throws PSNotFoundException, PSExtensionException
   {
      if (ce == null)
         throw new IllegalArgumentException("ce cannot be null");

      // prepare field validation
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSDisplayMapper dispMapper =
            pipe.getMapper().getUIDefinition().getDisplayMapper();

      int pageId = PSQueryCommandHandler.ROOT_PARENT_PAGE_ID;
      prepareValidation(ce, dispMapper, pageId);

      // prepare item validation
      m_itemEvaluator = new PSValidationRulesEvaluator(
         ce.getValidationRules(), m_appHandler);
   }

   /**
    * Prepare the field evaluators for all fields in the provided content
    * editor. This is called recursively for each display mapper. For each
    * item screen a map entry for m_fieldEvaluatorMap is created and stored
    * to be used while processing the request.
    *
    * @param ce the content editor, assumed not <code>null</code>.
    * @param dispMapper the display mapper, assumed not <code>null</code>.
    * @param pageId the actual page id used as key to the lookup map.
    */
   @SuppressWarnings("unchecked")
   private void prepareValidation(PSContentEditor ce,
      PSDisplayMapper dispMapper, int pageId)
   {
      int nextPageId = pageId;
      Map fieldEvaluators = new LinkedHashMap();
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSFieldSet fieldSet =
            pipe.getMapper().getFieldSet(dispMapper.getFieldSetRef());

      Iterator mappings = dispMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if (o instanceof PSFieldSet)
         {
            PSFieldSet fs = (PSFieldSet) o;
            PSDisplayMapper childMapper = mapping.getDisplayMapper();
            if (fs.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            {
               int repeatability = fs.getRepeatability();
               if (repeatability == PSFieldSet.REPEATABILITY_ONE_OR_MORE ||
                   repeatability == PSFieldSet.REPEATABILITY_COUNT)
               {
                  PSFieldSetValidationRulesEvaluator evaluator =
                     new PSFieldSetValidationRulesEvaluator(fs,
                        mapping.getUISet());

                  fieldEvaluators.put(fieldName, evaluator);
               }

               // all but the main mapper produce 2 pageids, summary and edit
               nextPageId += 2;
               prepareValidation(ce, childMapper, nextPageId);
            }
            else if (fs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
            {
               // We need to inherit the parent mapping's uiSet for any child
               // mappings that contain uiSets without labels
               Iterator it = childMapper.iterator();
               PSDisplayMapping childMapping = null;
               while(it.hasNext())
               {
                  childMapping = (PSDisplayMapping)it.next();
                  if(null == childMapping.getUISet().getLabel())
                     childMapping.setUISet(mapping.getUISet());
               }
               prepareSimpleChildValidation(ce, fieldEvaluators, childMapper);
            }
         }
         else
         {
            if (o == null)
            {
               // is this a multi property simple child?
               PSFieldSet sdmpFieldSet = fieldSet.getChildsFieldSet(fieldName,
                  PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
               if (sdmpFieldSet != null)
                  o = sdmpFieldSet.get(fieldName);
            }

            if (o instanceof PSField)
            {
               PSField field = (PSField) o;
               PSFieldValidationRulesEvaluator evaluator =
                  new PSFieldValidationRulesEvaluator(field,
                     mapping.getUISet());

               fieldEvaluators.put(fieldName, evaluator);
            }
         }
      }

      // add a map entry for each page, even if its empty
      m_fieldEvaluatorMap.put(new Integer(pageId), fieldEvaluators);
   }

   /**
    * Prepare validation for a simple child.
    *
    * @param ce the content editor, assumed not <code>null</code>.
    * @param fieldEvaluators the map of evaluators.  We will add any we
    *    create here.
    * @param dispMapper the display mapper, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void prepareSimpleChildValidation(PSContentEditor ce,
      Map fieldEvaluators, PSDisplayMapper dispMapper)
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
      PSFieldSet fieldSet =
            pipe.getMapper().getFieldSet(dispMapper.getFieldSetRef());

      Iterator mappings = dispMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
         Object o = fieldSet.get(fieldName);
         if (o instanceof PSField)
         {
            PSField field = (PSField) o;
            PSFieldValidationRulesEvaluator evaluator =
               new PSFieldValidationRulesEvaluator(field,
                  mapping.getUISet());

            fieldEvaluators.put(fieldName, evaluator);
         }
      }
   }

   /**
    * Process field validations for the current contentid, revision and the
    * provided pageid. Error information will be collected into the provided
    * collector.
    *
    * @param pageId the pageid which we are validating, assumed not
    *    <code>null</code>.
    * @param errorCollector the container into which we fill all error
    *    information collected during validation, assumed not
    *    <code>null</code>.
    * @param data the execution data for the page to be validated, assumed not
    *    <code>null</code>.
    * @return <code>true</code> if no field errors found, <code>false</code>
    *    otherwise.
    */
   protected boolean processFieldValidation(Integer pageId,
      PSErrorCollector errorCollector, PSExecutionData data)
   {
      Map evaluators = (Map) m_fieldEvaluatorMap.get(pageId);
      if (evaluators == null)
         return true;

      Iterator evals = evaluators.entrySet().iterator();
      while (evals.hasNext())
      {
         Map.Entry entry = (Map.Entry)evals.next();
         Object objFieldName = entry.getKey();
         Object objEval = entry.getValue();

         if (!(objFieldName instanceof String))
         {
            throw new IllegalStateException(
               "Illegal key in fieldEvaluatorMap");
         }

         if (objEval instanceof PSFieldValidationRulesEvaluator)
         {
            /* if there is no applyWhen, make sure the field has a value before
             * we try to validate
             */
            PSFieldValidationRulesEvaluator eval =
               (PSFieldValidationRulesEvaluator) objEval;

            boolean doEval = true;
            if (eval.getApplyWhen() == null)
            {
               String fieldName = (String)objFieldName;
               String fieldVal = data.getRequest().getParameter(fieldName);
               if (fieldVal == null || fieldVal.length() == 0)
                  doEval = false;
            }

            if (doEval)
            {
               eval.isValid(pageId, data, errorCollector);
               if (errorCollector.maxErrorsExceeded())
                  break;
            }
         }
      }

      return !errorCollector.hasErrors();
   }

   /**
    * Process the item validation and fill in the error(s) found to the
    * supplied error collector.
    *
    * @param errorCollector the container into which we fill all error
    *    information collected during validation.
    * @param data the item document containing all fields and their data.
    *    This document will be passed to the item validation extension.
    * @return <code>true</code> if no field error's found, <code>false</code>
    *    otherwise.
    */
   protected boolean processItemValidation(PSErrorCollector errorCollector,
      PSExecutionData data)
   {
      // avoid eclipse warnings
      if (errorCollector == null);
      if (data == null);
      
      return false;
   }

   /**
    * Get the maximum number of errors allowed before stopping the validation
    * process. This will walk all field evaluators and return the smallest
    * specified maxErrorsToStop.
    *
    * @param pageId the pageid we want the maximal allowed errors before stop
    *    for, not <code>null</code>.
    * @return the smallest specified maxErrorsToStop found in all evaluators
    *    for the supplied pageid, or the default (10) if not found.
    */
   public int getMaxErrorsToStop(Integer pageId)
   {
      if (pageId == null)
         throw new IllegalArgumentException("pageId cannot be null");

      int maxErrorsToStop = Integer.MAX_VALUE;
      Map evalMap = (Map) m_fieldEvaluatorMap.get(pageId);
      if (evalMap != null && evalMap.size() > 0)
      {
         Iterator evals = evalMap.values().iterator();
         while (evals.hasNext())
         {
            Object o = evals.next();
            if (o instanceof PSFieldValidationRulesEvaluator)
            {
               PSFieldValidationRulesEvaluator eval =
                  (PSFieldValidationRulesEvaluator) o;

               PSFieldValidationRules rules =
                  eval.getField().getValidationRules();
               if (rules == null)
                  continue;

               int test = rules.getMaxErrorsToStop();
               if (test < maxErrorsToStop)
                  maxErrorsToStop = test;
            }
         }
      }
         
      /**
       * If non of the fields specified a lower maximum error to stop, we use
       * the default.
       */
      if (maxErrorsToStop == Integer.MAX_VALUE)
         maxErrorsToStop = 10;

      return maxErrorsToStop;
   }

   /**
    * Get the pageid for the supplied execution data.
    *
    * @param data the execution data to extract the pageid from, might be
    *    <code>null</code>.
    * @return the pageid, never <code>null</code>.
    */
   public Integer getPageId(PSExecutionData data)
   {
      PSSingleHtmlParameterExtractor extractor =
         new PSSingleHtmlParameterExtractor(new PSSingleHtmlParameter(
            PSContentEditorHandler.PAGE_ID_PARAM_NAME));

      Object o = extractor.extract(data);
      int pageId;
      if (o == null || o.toString().trim().length() == 0)
         pageId = PSEditCommandHandler.ROOT_PARENT_PAGE_ID;
      else
         pageId = Integer.parseInt(o.toString());

      return new Integer(pageId);
   }

   /**
    * Get the cacheid for the supplied execution data.
    *
    * @param data the execution data to extract the cacheid from, might be
    *    <code>null</code>.
    * @return the cacheid, might be <code>null</code>.
    */
   public static Integer getCacheId(PSExecutionData data)
   {
      PSSingleHtmlParameterExtractor extractor =
         new PSSingleHtmlParameterExtractor(new PSSingleHtmlParameter(
            PSContentEditorHandler.CACHE_ID_PARAM_NAME));

      Object o = extractor.extract(data);
      Integer cacheId = null;
      if (o != null && o.toString().trim().length() != 0)
         cacheId = new Integer(Integer.parseInt(o.toString()));

      return cacheId;
   }

   /**
    * Get the transitionid for the supplied execution data.
    *
    * @param data the execution data to extract the transitionid from, might
    *    be <code>null</code>.
    */
   public static Integer getTransitionId(PSExecutionData data)
   {
      PSSingleHtmlParameterExtractor extractor =
         new PSSingleHtmlParameterExtractor(new PSSingleHtmlParameter(
            IPSHtmlParameters.SYS_TRANSITIONID));

      Object o = extractor.extract(data);
      Integer transitionId = null;
      if (o != null && o.toString().trim().length() != 0)
         transitionId = new Integer(Integer.parseInt(o.toString()));

      return transitionId;
   }

   /**
    * Get the complete list of all pageid's available for this content editor.
    *
    * @return the list of all pageid's (a list of Integer objects), never
    *    <code>null</code> or empty.
    */
   public Iterator getPageIdList()
   {
      return m_fieldEvaluatorMap.keySet().iterator();
   }

   /**
    * Returns <code>IPSInternalRequest.REQUEST_TYPE_CONTENT_EDITOR</code>
    *
    * see {@link com.percussion.data.IPSInternalRequestHandler#getRequestType()}
    * for details.
    */
   public int getRequestType()
   {
      return IPSInternalRequest.REQUEST_TYPE_CONTENT_EDITOR;
   }

   /**
    * Does this handler process update requests?  This method will return
    * <code>false</code>.  Derived classes that perform updates should override
    * this method and return <code>true</code>.
    *
    * @return <code>true</code> if this handler processes requests that update
    * data and require a redirect to return a response, <code>false</code> if
    * it handles query requests which do not require a redirect.
    */
   public boolean isUpdate()
   {
      return false;
   }

   /**
    * Extracts the workflow id from the supplied execution data for the
    * current processed item.
    *
    * @param data the execution data from which to extract the workflowid,
    *    not <code>null</code>.
    * @return the workflowid, may be <code>null</code> if the processed item
    *    does not exist in the repository yet.
    * @throws PSDataExtractionException if anything goes wrong extracting the
    *    requested data.
    */
   public String extractWorkflowId(PSExecutionData data)
      throws PSDataExtractionException
   {
      Object obj = m_workflowidExtractor.extract(data);
      if (obj != null)
         return obj.toString();

      return null;
   }

   /**
    * Extracts the community from the supplied execution data for the
    * current processed item.
    *
    * @param data the execution data from which to extract the community,
    *    not <code>null</code>.
    * @return the community, may be <code>null</code>.
    * @throws PSDataExtractionException if anything goes wrong extracting the
    *    requested data.
    */
   public String extractCommunity(PSExecutionData data)
      throws PSDataExtractionException
   {
      Object obj = m_communityExtractor.extract(data);
      if (obj != null)
         return obj.toString();

      return null;
   }

   /**
    * Extracts the locale from the supplied execution data for the
    * current processed item.
    *
    * @param data the execution data from which to extract the locale,
    *    not <code>null</code>.
    * @return the locale, may be <code>null</code>.
    * @throws PSDataExtractionException if anything goes wrong extracting the
    *    requested data.
    */
   public String extractLocale(PSExecutionData data)
      throws PSDataExtractionException
   {
      Object obj = m_localeExtractor.extract(data);
      if (obj != null)
         return obj.toString();

      return null;
   }

   /**
    * Registers the supplied listener for editor change events.  Listener will
    * be notified of any requests that modify the content or state of the item.
    *
    * @param listener The listener to notify, may not be <code>null</code>.
    */
   @SuppressWarnings("unchecked") void addEditorChangeListener(IPSEditorChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_changeListeners.add(listener);
   }

   /**
    * Convenience method that calls {@link #notifyEditorChangeListeners(
    * PSExecutionData, int, PSChangeEventProcessor) 
    * notifyEditorChangeListeners(data, action, null)}. 
    */
   protected void notifyEditorChangeListeners(PSExecutionData data, int action)
   {
      notifyEditorChangeListeners(data, action, null);
   }
   
   /**
    * Notifies all registered change listeners with the supplied event.  If a
    * revision id is not specified in the <code>data</code>, the current 
    * revision is used.
    *
    * @param data The execution data, may not be <code>null</code>.
    * @param action The action to set on the event.  Must be one of the
    * <code>PSEditorChangeEvent.ACTION_xxx</code> types.
    * @param proc If supplied, will be called to process the event just before
    * listeners are notified with it.  May be <code>null</code>.
    */
   protected void notifyEditorChangeListeners(PSExecutionData data, int action, 
      PSChangeEventProcessor proc)
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      // need to notify listeners of this event
      String contentIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
      String revisionIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.REVISION_ID_PARAM_NAME);
      String childIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CHILD_ID_PARAM_NAME);
      String childRowIdParamName = m_ceHandler.getParamName(
         PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
      
      PSRequest request = data.getRequest();
      String strContentId = request.getParameter(contentIdParamName);
      String strRevisionId = request.getParameter(revisionIdParamName, "-1");      
      String strChildId = request.getParameter(childIdParamName, "-1");
      
      // may have mulitple child rows submitted in a single request
      Object[] childRowIds = request.getParameterList(childRowIdParamName);
      if (childRowIds == null)
      {
         if (strChildId.equals("-1") || strChildId.equals("0"))
         {
            childRowIds = new Object[] {"-1"};
         }
         else
         {
            ms_log.warn("No listeners were notified. "
                  + " A child field was specified,"
                  + " but no child row ids provided "
                  + " for content item " + strContentId
                  + ", child id " + strChildId
                  + ", action " + action);
            return;
         }
      }

      try
      {
         int contentId = Integer.parseInt(strContentId);
         int revisionId = Integer.parseInt(strRevisionId);
         int childId = Integer.parseInt(strChildId);

         // if revision is -1, get the current revision and use that.
         if (revisionId == -1)
         {
            PSCmsException ex = null;
            try
            {
               // TODO: use new centralized current revision calculator once
               // it's ready
               revisionId = getCurrentRevision(data.getRequest(), contentId);
            }
            catch (PSCmsException e)
            {
               // unexpected trouble, fall thru to leave revision at -1
               ex = e;
            }
            
            if (revisionId == -1)
            {
               // we couldn't look up the revision, but we should definitely
               // have a valid content id at this point
               String msg = 
                  "Unable to determine current revision for contentId: " + 
                     contentId;
               if (ex != null)
                  msg += ("  Error was: " + ex.getLocalizedMessage());
               //don't throw an error if the action is DELETE, since the item is
               // deleted due to the Delete Plans
               if ( action != PSEditorChangeEvent.ACTION_DELETE )   
                   throw new RuntimeException(msg);
            }            
         }

         
         for (int i = 0; i < childRowIds.length; i++)
         {  
            int childRowId = childRowIds[i] != null ? Integer.parseInt(
               childRowIds[i].toString()) : -1;
         
            PSEditorChangeEvent e = new PSEditorChangeEvent(
               action, contentId, revisionId, childId, childRowId, 
               m_ceHandler.getContentTypeId());
            if (proc != null)
               e = proc.processEvent(e);
            updateChangeListners(e);
            
         }
      }
      catch (NumberFormatException e)
      {
         // all id's should have already been checked by the command handler
         // before reaching this method, so this would be some kind of bug
         throw new RuntimeException("Invalid id supplied to change event");
      }
   }
   
   public void updateChangeListners(PSEditorChangeEvent e)
   {
      Iterator listeners = m_changeListeners.iterator();
      while (listeners.hasNext())
      {
         IPSEditorChangeListener listener =
            (IPSEditorChangeListener)listeners.next();
         listener.editorChanged(e);
      }
   }
   /**
    * Looks up the current revision for the specified content id.
    * 
    * @param req The request to use, may not be <code>null</code>.
    * @param contentId The content id of the item.
    * 
    * @return The current revision, or -1 if the <code>contentId</code> is not
    * valid.
    * 
    * @throws PSCmsException if there are any errors.
    *  
    */
   @SuppressWarnings("unused")
   public static int getCurrentRevision(PSRequest req, int contentId) 
      throws PSCmsException
   {
      // TODO: use new centralized current revision calculator once
      // it's ready and remove/deprecate this method

      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      int revision = -1;
      
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = cms.loadComponentSummary(contentId);

      if (sum != null)
      {
         revision = sum.getCurrentLocator().getRevision();            
      }
      
      return revision;
   }

   /**
    * Class to handle processing or modifying an editor change event before
    * it is dispatched.
    */
   protected abstract class PSChangeEventProcessor
   {
      /**
       * This method is called before listeners are notified with the supplied
       * event.
       * 
       * @param e The event that will be dispatched, never <code>null</code>.
       * 
       * @return The event to dispatch, never <code>null</code>.
       */
      public abstract PSEditorChangeEvent processEvent(PSEditorChangeEvent e);
   }
   
   /**
    * Class to encapsulate a list of field transformations and an error message.
    */
   protected class PSTransformRunner
   {
      /**
       * ctor
       * 
       * @param transforms The list of transforms, not <code>null</code> or 
       * empty.
       * @param errMsg The error message to use, may be <code>null</code> or 
       * empty.
       */
      public PSTransformRunner(List<PSExtensionRunner> transforms, 
         String errMsg)
      {
         if (transforms == null || transforms.isEmpty())
            throw new IllegalArgumentException(
               "transforms may not be null or empty");
         
         m_transforms = transforms;
         if (errMsg == null)
            m_errMsg = "";
         else
            m_errMsg = errMsg;
      }

      /**
       * Get the error message to use.
       * @return The message, never <code>null</code>, may be empty.
       */
      public String getErrorMsg()
      {
         return m_errMsg;
      }

      /**
       * Get the list of transforms.
       * 
       * @return The m_transforms, never <code>null</code> or empty.
       */
      public List<PSExtensionRunner> getTransforms()
      {
         return m_transforms;
      }
      
      /**
       * List of transforms provided to ctor, immutable
       */
      private List<PSExtensionRunner> m_transforms;
      
      /**
       * Error msg provided to ctor, immutable
       */
      private String m_errMsg;
   }
   
   /**
    * The application handler used. Never <code>null</code>, never changes
    * once set in ctor.
    */
   protected PSApplicationHandler m_appHandler = null;

   /**
    * The main handler that delegates to this one. Never <code>
    * null</code>, never changes once set in ctor.
    */
   protected PSContentEditorHandler m_ceHandler = null;

   /**
    * The content editor dataset this handler is processing commands
    * for, saved as a PSContentEditor reference for convenience. Never
    * <code>null</code> after construction.
    */
   protected PSContentEditor m_ce = null;


   /**
    * The application used to internally perform queries and updates.
    * Obtained in the constructor, never <code>null</code> after that.
    */
   protected PSApplication m_internalApp = null;


   /**
    * List of conditional requests prepared as PSConditionalUrlEvaluators.
    * The order of this list is important, and it should be evaluated top
    * to bottom at run time. Initialized by call to {@link #prepareRedirects(String)},
    * never <code>null</code> after that.
    */
   protected PSConditionalUrlEvaluator[] m_redirects = null;

   /**
    * List of PSConditionalExitEvaluator objects to use for item input
    * translations. Never <code>null</code>, added to by call to
    * {@link #prepareInputTranslations()}, never modified after that.
    */
   protected List<PSConditionalExitEvaluator> m_itemInputTranslations = 
      new ArrayList<PSConditionalExitEvaluator>();

   /**
    * List of PSConditionalExitEvaluator objects to use for item output
    * translations. Never <code>null</code>, added to by call to
    * {@link #prepareOutputTranslations()}, never modified after that.
    */
   protected List<PSConditionalExitEvaluator> m_itemOutputTranslations = 
      new ArrayList<PSConditionalExitEvaluator>();

   /**
    * Key in the next number table for new content ids.
    */
   public static final String NEXT_CONTENT_ID_KEY = "CONTENT";

   /** The redirect location header */
   protected static final String LOCATION_CMD_HEADER = "Location";

   /**
    * The item evaluator for this content editor. It is initialized during
    * construction, might be <code>null</code> if no item validation is
    * defined.
    */
   protected PSValidationRulesEvaluator m_itemEvaluator = null;

   /**
    * The storage for all field evaluators used in this content editor
    * command handler.  The key is the pageId as an Integer, and the value is
    * a Map of PSFieldValidationRulesEvaluator, with a fieldSet name or a
    * field's submitName as the key.
    * Never <code>null</code> or empty.
    */
   protected Map<Integer, Map<Integer, Object>>  m_fieldEvaluatorMap = new HashMap<Integer, Map<Integer, Object>>();

   /**
    * This is the (temporary) HTML parameter that is used to pass the access
    * level to the authenticateUser exit as the 4th parameter. If a handler
    * needs to specify different values at different times, then the parameter
    * value for that handler's copy of the exit must use the
    * PSHtmlSingleParameter type, using this html name. The value of this
    * parameter must be one of the
    * com.percussion.workflow.PSWorkFlowUtils.ASSIGNMENT_TYPE_xxx types.
    * Never <code>null</code> or empty.
    */
   private static final String MIN_ACCESS_PARAM_NAME = "sys_minaccesslevel";

   /**
    * This is the (temporary) HTML parameter that is used to pass the current
    * 'state' of the document regarding whether it is checked in or out as the
    * 3rd parameter of the authenticateUser exit. Very
    * similar to {@link #MIN_ACCESS_PARAM_NAME} but instead, the allowed
    * values come from the set of
    * PSWorkFlowUtils.CHECKINOUT_CONDITION_xxx. Never <code>null</code> or
    * empty.
    */
   private static final String CHECKINOUT_CONDITION_PARAM_NAME =
         "sys_checkinoutcondition";

   /**
    * This is the (temporary) HTML parameter that is used to pass the workflow
    * app id to the authenticateUser exit as the 6th parameter. If a handler
    * needs to specify different values at different times, then the parameter
    * value for that handler's copy of the exit must use the
    * PSHtmlSingleParameter type, using this html name.
    * Never <code>null</code> or empty.
    */
   private static final String WORKFLOW_APP_ID_PARAM_NAME = "sys_workflowappid";

   /**
    * The store for all field input translations of the entire item. Stored in a
    * map with the childid as key (String) and a map (HashMap) as value. The
    * value map has the fields submit name as key (String) and a list
    * (ArrayList) of {@link PSTransformRunner} objects as value. Initialized
    * during construction, never <code>null</code> after that.
    */
   protected Map<String, Map<String, PSTransformRunner>> 
      m_inputFieldTranslations = 
         new HashMap<String, Map<String, PSTransformRunner>>();

   /**
    * The store for all field output translations of the entire item. Stored
    * in a map with the fields submit name as key (String) and a list (List)
    * of PSExtensionRunner objects. Initialized during construction, never
    * <code>null</code> after that.
    */
   protected Map<String, PSTransformRunner> m_outputFieldTranslations = 
      new HashMap<String, PSTransformRunner>();

   /**
    * List of {@link IPSEditorChangeListener} objects to notify when item is
    * modified or its state changes.  Never <code>null</code>, may be empty.
    * Listeners are added using
    * {@link #addEditorChangeListener(IPSEditorChangeListener)}.
    */
   protected List<IPSEditorChangeListener> m_changeListeners = new CopyOnWriteArrayList<IPSEditorChangeListener>();

   /**
    * An extractor to get the workflowid of the processed content item from
    * the execution data. Initialized during construction, never
    * <code>null</code> or changed after that.
    */
   protected PSContentItemStatusExtractor m_workflowidExtractor = null;

   /**
    * An extractor to get the locale of the processed content item from
    * the execution data. Initialized during construction, never
    * <code>null</code> or changed after that.
    */
   protected PSContentItemStatusExtractor m_localeExtractor = null;

   /**
    * An extractor to get the community of the processed content item from
    * the execution data. Initialized during construction, never
    * <code>null</code> or changed after that.
    */
   protected PSContentItemStatusExtractor m_communityExtractor = null;

}
