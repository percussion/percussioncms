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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSRelationshipChangeListener;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.data.PSSqlException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSBackEndUpdateProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSStandaloneException;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.PSCloneAlreadyExistsException;
import com.percussion.relationship.PSCloneLocator;
import com.percussion.relationship.PSRelationshipException;
import com.percussion.relationship.PSRelationshipProcessorException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSUserSession;
import com.percussion.server.config.PSConfigManager;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

/**
 * The relationship handler handles query and update requests for relationships.
 * This responds to requests that have the sys_command parameter set to
 * "relate". Several sub-commands like "create", "remove", etc. are supported.
 */
public class PSRelationshipCommandHandler extends PSCommandHandler
   implements IPSRelationshipHandlerCallback
{
   /**
    * Creates a handler to process requests for querying and modifying
    * relationships.
    *
    * @param ah The application handler for this app. May not be
    *    <code>null</code>.
    * @param ceh The content editor handler for this dataset. May not be
    *    <code>null</code>.
    * @param ce The dataset this command handler will process modify commands
    *    for. Must be a PSContentEditor. May not be <code>null</code>.
    * @param app The application created by the PSContentEditor for each
    *    command handler to add datasets to. The app is started and stopped
    *    by the ContentEditorHandler. May not be <code>null</code>.
    * @throws PSIllegalArgumentException if there is any invalid data.
    * @throws PSExtensionException if there is an error preparing an exception.
    * @throws PSNotFoundException if a udf or extension cannot be located.
    * @throws PSSystemValidationException if there is a problem starting an internal
    *    application.
    * @throws PSServerConfigException if a needed relationship or clone handler
    *    configuration failed to load.
    * @throws PSUnknownNodeTypeException for unknown XML nodes.
    * @throws IllegalArgumentException if any param is <code>null</code>.
    */
   public PSRelationshipCommandHandler(PSApplicationHandler ah,
      PSContentEditorHandler ceh, PSContentEditor ce, PSApplication app)
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException,
         PSIllegalArgumentException, PSServerConfigException,
         PSUnknownNodeTypeException
   {
      super(ah, ceh, ce, app);

      // Extract all exits and prepare them.
      prepareExtensions(COMMAND_NAME);

      // prepare redirects
      prepareRedirects(COMMAND_NAME);

      // load relationship configurations
      loadConfigs();

      m_copyHandler = new PSCopyHandler(ceh, ce, app, this, true);
   }

   /**
    * Registers the supplied listener for relationship change events. Listener
    * will be notified of any requests that add, remove or modify relationships.
    *
    * @param listener the listener to notify, may not be <code>null</code>.
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void addRelationshipChangeListener(
      IPSRelationshipChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_relationshipChangeListeners.add(listener);
   }

   /**
    * Unregisters the supplied listener from relationship change events.
    *
    * @param listener the listener to remove, may not be <code>null</code>.
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void removeRelationshipChangeListener(
      IPSRelationshipChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_relationshipChangeListeners.remove(listener);
   }

   /** see IPSRelationshipHandlerCallback for description */
   public void relate(String relationshipType, PSLocator owner,
      PSLocator dependent, PSExecutionData data) throws PSRelationshipException
   {
      if (relationshipType == null || owner == null || dependent == null ||
         data == null)
        throw new IllegalArgumentException("parameters cannot be null");

      try
      {
         PSRelationshipConfig config = ms_configs.getConfig(relationshipType);
         if (config == null)
         {
            throw new PSServerConfigException(
               IPSServerErrors.UNKNOWN_RELATIONSHIP_CONFIGURATION,
                  relationshipType);
         }

         PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();
         putRelationship(data, config, -1, owner, dependent,
            PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT, false, processor);
      }
      catch (PSException e)
      {
         throw new PSRelationshipException(e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Process a content editor clone request using the input context
    * information and data.  Creates a new content item with a new content id
    * and a revision of <code>1</code>.
    *
    * @param request the request object containing all context data associated
    * with the request.
    *
    * @throws IllegalArgumentException if request is <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      PSExecutionData execData = null;
      try
      {
         /*
          * Save original folder id for use in effects
          */
         String folderid = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
         if (folderid != null)
            request.setParameter(IPSHtmlParameters.SYS_ORIGINALFOLDERID, 
                  folderid);
         
         execData = new PSExecutionData(m_appHandler, this, request);
         boolean doRedirect = processRequest(execData);

         // do redirect
         if (doRedirect)
            processRedirect(execData);
      }
      catch (Throwable t)
      {
         /* catch anything that comes our way */
         PSConsole.printMsg("Cms", t);
         String source = COMMAND_NAME;

         String sessId = "";
         PSUserSession sess = request.getUserSession();
         if (sess != null)
            sessId = sess.getId();

         int errorCode;
         Object[] errorArgs;

         PSException e = null;

         if (t instanceof PSException)
         {
            e = (PSException) t;
            errorCode = e.getErrorCode();
            errorArgs = e.getErrorArguments();
         }
         else
         {
            errorCode = IPSServerErrors.RAW_DUMP;
            errorArgs = new Object[] { getExceptionText(t) };
         }

         PSBackEndUpdateProcessingError err =
            new PSBackEndUpdateProcessingError(
               m_appHandler.getId(), sessId, errorCode, errorArgs, source);
         m_appHandler.reportError(request, err);
      }
      finally
      {
         if (execData != null)
            execData.release();
      }
   }

   // see IPSRequestHandler interface for description
   public void shutdown()
   {
   }

   /**
    * See {@link PSCommandHandler#isUpdate()} for a description of this method.
    *
    * @return <code>true</code> always.
    */
   public boolean isUpdate()
   {
      return true;
   }

   /**
    * See {@link com.percussion.data.IPSInternalRequestHandler#makeInternalRequest(PSRequest)
    * IPSInternalRequestHandler} interface for method and parameter descriptions.
    *
    * @return <code>null</code> always.  The execution data created by this
    * request is released before returning to caller.
    */
   public PSExecutionData makeInternalRequest(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData execData = null;
      try
      {
         execData = new PSExecutionData(m_appHandler, this, request);
         processRequest(execData);
      }
      catch (PSException e)
      {
         if (e instanceof PSAuthorizationException)
            throw (PSAuthorizationException) e;

         if (e instanceof PSAuthenticationFailedException)
            throw (PSAuthenticationFailedException) e;

         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (Throwable t)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(t));
      }
      finally
      {
         if (execData != null)
            execData.release();
      }

      return null;
   }

   /**
    * Get the relationships for the specified request. The returned document
    * conforms to the PSXRelationshipSet.dtd.
    *
    * @param request the request to process
    * @return a document with all requested relationships, may be
    *    <code>null</code> on errors, might be empty.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    */
   public Document getResultDocument(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      checkInternalRequestAuthorization(request);

      PSExecutionData data = new PSExecutionData(m_appHandler, this, request);

      try
      {
         PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();
         PSRelationshipFilter filter = PSRelationshipDbProcessor
               .getFilterFromParameters(data.getRequest().getParameters());
         PSRelationshipSet relationships = processor.getRelationships(filter);
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         doc.appendChild(relationships.toXml(doc));

         return doc;
      }
      catch (PSCmsException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),
            e.getErrorArguments());
      }
      finally
      {
         if (data != null)
            data.release();
      }
   }

   /**
    * Process the supplied request for the execution data provided.
    *
    * @param data the execution data to operate on, assumed not
    *    <code>null</code>.
    *    
    * @return <code>true</code> if need to process redirect response;
    *   otherwise don't process the redirect response.
    * 
    * @throws PSRequestValidationException if the supplied request is invalid.
    * @throws PSAuthorizationException if the requestor is not authorized.
    * @throws PSInternalRequestCallException if anything goes wrong making
    *    internal requests.
    * @throws PSSystemValidationException for any validation failed.
    * @throws SQLException for any failed sql operations.
    * @throws PSAuthenticationFailedException if the requestor is not
    *    authenticated.
    * @throws PSNotFoundException for files/resources not found.
    * @throws PSExtensionProcessingException if any exit processing fails.
    * @throws PSDataExtractionException for all data extraction errors.
    * @throws PSParameterMismatchException for invalid exit parameters.
    * @throws PSServerConfigException if a needed relationship or clone handler
    *    configuration failed to load.
    * @throws PSObjectException if cloning is not allowed for the supplied
    *    object type.
    * @throws PSExtensionException if extensions fail in process checks.
    * @throws PSRelationshipProcessorException for all errors occurred while
    *    processing relationship effects
    * @throws PSErrorException for any error thrown while processing the
    *    pre- extensions.
    */
   private boolean processRequest(PSExecutionData data)
      throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSSystemValidationException, SQLException,
         PSAuthenticationFailedException, PSNotFoundException,
         PSExtensionProcessingException, PSDataExtractionException,
         PSParameterMismatchException, IOException, PSUnknownNodeTypeException,
         PSRelationshipException, PSServerConfigException, PSObjectException,
         PSExtensionException, PSRelationshipProcessorException,
         PSErrorException, PSCmsException
   {
      boolean doRedirect = true;
      
      // run pre exits
      runPreProcessingExtensions(data);

      try
      {
         String command = getParameter(data.getRequest(),
            IPSHtmlParameters.SYS_COMMAND);

         PSRelationshipDbProcessor processor = PSRelationshipDbProcessor.getInstance();

         if (command.equals(COMMAND_NAME) ||
            command.equals(COMMAND_NAME + "/" + COMMAND_CREATE))
            modifyRelationship(data, false, processor);
         else if (command.equals(COMMAND_NAME + "/" + COMMAND_INSERT))
            modifyRelationship(data, true, processor);
         else if (command.equals(COMMAND_NAME + "/" + COMMAND_REMOVE))
            removeRelationship(data, processor);
         else if (command.equals(COMMAND_NAME + "/" + COMMAND_QUERY))
         {
            queryRelationships(data, processor);
            doRedirect = false;
         }
      }
      catch (PSRequestValidationException e)
      {
         /**
          * Relationship pre- exits throw this exception to indicate we must
          * not process the request. This is used to implement constraints
          * such as in
          * <code>com.percussion.relationship.PSTranslationConstraint</code>.
          */
      }
      catch(PSCloneAlreadyExistsException e)
      {
         //The clone already exists. This is not an error and must be ignored.
      }

      // run post exits
      runPostProcessingExtensions(data, null);
      
      return doRedirect;
   }

   /**
    * Get all defined relationship configurations.
    *
    * @return a list of <code>PSRelationshipConfig</code> objects, never
    *    <code>null</code>, might be empty.
    */
   public static Iterator getRelationshipConfigs()
   {
      if (ms_configs == null) {
          try{
              reloadConfigs();
          } catch (PSServerConfigException e) {
              throw new IllegalStateException("ms_configs must be initialized",e);
          } catch (PSUnknownNodeTypeException e) {
              throw new IllegalStateException("ms_configs must be initialized",e);
          }
      }

      
      return ms_configs.iterator();
   }

   /**
    * Get all relationship configurations for a given category.
    *
    * @param category the relationship category, not <code>null</code> or empty.
    * 
    * @return a list of <code>PSRelationshipConfig</code> objects, never
    *    <code>null</code>, might be empty.
    */
   public static Iterator getRelationshipConfigs(String category)
   {
      if (ms_configs == null) {
          try {
              reloadConfigs();
          } catch (PSServerConfigException e) {
              throw new IllegalStateException("ms_configs must be initialized", e);
          } catch (PSUnknownNodeTypeException e) {
              throw new IllegalStateException("ms_configs must be initialized", e);
          }
      }

      return ms_configs.getConfigsByCategory(category);
   }

   /**
    * Get relationship configuration set.
    *
    * @return The relationship configuration set, never <code>null</code>.
    */
   public static PSRelationshipConfigSet getConfigurationSet()
   {
      if (ms_configs == null) {
          try {
              reloadConfigs();
          } catch (PSServerConfigException e) {
              throw new IllegalStateException("ms_configs must be initialized", e);
          } catch (PSUnknownNodeTypeException e) {
              throw new IllegalStateException("ms_configs must be initialized", e);
          }
      }

      return ms_configs;
   }

   /**
    * Get the requested relationship configuration.
    *
    * @param name the name or category to get the relationship configuration for,
    * not <code>null</code> or empty. It is assumed to be the name of the
    * relationship first. If not found a matching relationship config, it then
    * assumed to be the category of the relationship.
    * @return the requested relationship configuration or <code>null</code> if
    * it does not exist.
    * @see PSRelationshipConfigSet#getConfigByNameOrCategory for more details.
    */
   public static PSRelationshipConfig getRelationshipConfig(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "the configuration name cannot be null or empty");

      return ms_configs.getConfigByNameOrCategory(name);
   }

   /**
    * @return the next available relationship id.
    * 
    * @throws PSCmsException if failed to generate the id.
    */
   public static int getNextId() throws PSCmsException
   {
      try
      {
      return PSIdGenerator.getNextId("RXRELATEDCONTENT");
      }
      catch (SQLException e)
      {
         throw new PSCmsException(IPSCmsErrors.ID_GENERATOR_FAILED,
               PSSqlException.getFormattedExceptionText(e));
      }
   }
      
   /**
    * Gets the requested parameter and validates it.
    *
    * @param request the request to get the parameter from, not
    *    <code>null</code>.
    * @param name the parameter name to get, not <code>null</code> or empty.
    * @return the parameter value, never <code>null</code> or empty.
    * @throws IllegalArgumentException if the supplied request or name is
    *    <code>null</code> or the name is empty.
    * @throws PSRequestValidationException if the requested parameter is
    *    <code>null</code> or empty.
    */
   private String getParameter(PSRequest request, String name)
      throws PSRequestValidationException
   {
      if (request == null)
        throw new IllegalArgumentException("request cannot be null");

      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null or empty");

      String param = request.getParameter(name);
      if (param == null)
      {
         Object[] args = { name, "null" };
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }
      if (param.trim().length() == 0)
      {
         Object[] args = { name, "empty" };
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      return param;
   }

   /**
    * Get the requested parameter as integer.
    *
    * @param request the request to get the parameter from, not
    *    <code>null</code>.
    * @param name the parameter name, not <code>null</code> or empty.
    * @return the requested parameter as integer.
    * @throws IllegalArgumentException if the supplied request or name is
    *    <code>null</code> or the name is empty.
    * @throws PSRequestValidationException if the requested parameter is
    *    <code>null</code>, empty or cannot be parsed into an integer.
    */
   private int getParameterInt(PSRequest request, String name)
      throws PSRequestValidationException
   {
      if (request == null)
        throw new IllegalArgumentException("request cannot be null");

      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null or empty");

      String param = getParameter(request, name);

      int paramInt;
      try
      {
         paramInt = Integer.parseInt(param);
      }
      catch (NumberFormatException e)
      {
         Object[] args = { name, param };
         throw new PSRequestValidationException(
            IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
      }

      return paramInt;
   }

   /**
    * Modifies the requested relationship for the supplied request and
    * execution data. If owner and dependent locators are provided, this
    * will create a new relationship if none of this type exists or
    * otherwise update the existing one. If the dependent is not specified, this
    * will clone the owner if the relationship configuration allows that and
    * then create the relationship between the owner and the new clone.
    *
    * @param data the execution data to use, assumed not <code>null</code>.
    * @param forceInsert <code>true</code> to force an insert,
    *    <code>false</code> otherwise.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private void modifyRelationship(PSExecutionData data, boolean forceInsert,
         PSRelationshipDbProcessor processor) throws PSCmsException
   {
      try
      {
         PSRequest request = data.getRequest();
         String relationshipType = getParameter(request,
            IPSHtmlParameters.SYS_RELATIONSHIPTYPE);

         PSRelationshipConfig config = ms_configs.getConfig(relationshipType);
         if (config == null)
         {
            throw new PSServerConfigException(
               IPSServerErrors.UNKNOWN_RELATIONSHIP_CONFIGURATION, relationshipType);
         }

         int contentid = getParameterInt(request, IPSHtmlParameters.SYS_CONTENTID);
         int revision = -1;
         if (config.useOwnerRevision())
            revision = getParameterInt(request, IPSHtmlParameters.SYS_REVISION);
         if (revision == -1)
         {
            IPSCmsObjectMgr omgr = PSCmsObjectMgrLocator.getObjectManager();
            PSComponentSummary sum = omgr.loadComponentSummary(contentid);
            revision = sum.getCurrentLocator().getRevision();
         }
         PSLocator owner = new PSLocator(contentid, revision);

         int dependentid = -1;
         try
         {
            dependentid = getParameterInt(request,
               IPSHtmlParameters.SYS_DEPENDENTID);
         }
         catch (PSRequestValidationException e)
         {
            // ignore
         }

         PSLocator dependent = null;
         if (dependentid == -1)
         {
            PSLocator clone = request.getClonedLocator(owner.getId());
            if (clone == null)
            {
               IPSCloneHandler ch = PSCloneHandlerFactory.getCloneHandler(
                  PSCloneHandlerFactory.ITEM, m_copyHandler);

               Map params = new HashMap();
               params.put(IPSHtmlParameters.SYS_CONTENTID,
                  Integer.toString(contentid));
               if (config.useOwnerRevision())
                  params.put(IPSHtmlParameters.SYS_REVISION,
                     Integer.toString(revision));
               PSRelationshipFilter filter = new PSRelationshipFilter();
               filter.setOwner(new PSLocator(contentid, revision));
               filter.setCommunityFiltering(false);
               PSRelationshipProcessor proc =
                     PSRelationshipProcessor.getInstance();
               PSRelationshipSet relationships = proc.getRelationships(filter);

               Map cloneOverrideFields = buildCloneOverrideFields(config, data);
               request.setParameter(
                  PSCloneCommandHandler.SYS_CLONE_OVERRIDE_FIELDSET,
                  cloneOverrideFields);

               dependent = ch.clone(
                  owner, relationships.iterator(), data, this, this);
               if (!config.useDependentRevision())
                  dependent.setRevision(-1);
               boolean skip = false;
               if(dependent instanceof PSCloneLocator)
               {
                  PSCloneLocator ltor = (PSCloneLocator)dependent;
                  skip = ltor.isExisting();
               }
               if(!skip)
                  insertRelationship(data, config, owner, dependent, false,
                        processor);
            }
         }
         else
         {
            int dependentrevision = -1;
            if (config.useDependentRevision())
               dependentrevision = getParameterInt(request,
                  IPSHtmlParameters.SYS_DEPENDENTREVISION);
            dependent = new PSLocator(dependentid, dependentrevision);

            if (forceInsert)
               insertRelationship(data, config, owner, dependent, false,
                     processor);
            else
            {
               int updateId = getUpdateId(data, owner, dependent, processor);
               if (updateId >= 0)
                  updateRelationship(data, config, updateId, owner, dependent,
                        processor);
               else
                  insertRelationship(data, config, owner, dependent, false,
                        processor);
            }
         }
      }
      catch (IOException e)
      {
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (SQLException e)
      {
         throw new PSCmsException(IPSCmsErrors.SQL_EXCEPTION_WRAPPER,
            PSStandaloneException.formatSqlException(e));
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Builds map of field name-value pairs for each of the clone override
    * fields configured. The conditionals are evalauated to see if a field
    * needs to be overridden or not. The value is evaluated based on the
    * replacment value specified.
    * @param config relationship configuration object, assumed not <code>null</code>.
    * @param data execution data assumed not <code>null</code>.
    * @return A map field name-values for the fields to be overridden. Never
    * <code>null</code> may be empty.
    * 
    * @throws PSCmsException if error occurs.
    */
   private Map buildCloneOverrideFields(PSRelationshipConfig config,
      PSExecutionData data) throws PSCmsException
   {
      Map fieldValueMap = new HashMap();
      PSCloneOverrideFieldList list = config.getCloneOverrideFieldList();
      if (list == null)
         return fieldValueMap;
         
      try
      {
         for (int i=0; i<list.size();i++)
         {
            PSCloneOverrideField field = (PSCloneOverrideField) list.get(i);
            PSCollection rules = field.getRules();
            PSRuleListEvaluator evaluator = new PSRuleListEvaluator(rules);
            if (evaluator.isMatch(data))
            {
               IPSDataExtractor extractor =
                  PSDataExtractorFactory.createReplacementValueExtractor(
                  field.getReplacementValue());

               fieldValueMap.put(field.getName(), extractor.extract(data));
            }
         }
      }
      catch (PSDataExtractionException e)
      {
         throw new PSCmsException(e);
      }
      catch (PSNotFoundException e)
      {
         throw new PSCmsException(e);
      }
      catch (PSExtensionException e)
      {
         throw new PSCmsException(e);
      }
      
      return fieldValueMap;
   }

   /**
    * Get the relationship update id for the supplied owner and dependent.
    *
    * @param data the execution data to operate on, assumed not
    *    <code>null</code>.
    * @param owner the owner to get the update id for, assumed not
    *    <code>null</code>.
    * @param dependent the dependent to get the update id for, assumed not
    *    <code>null</code>.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @return the update id or -1 if not found.
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private int getUpdateId(PSExecutionData data, PSLocator owner,
         PSLocator dependent, PSRelationshipDbProcessor processor)
         throws PSCmsException
   {
      int updateId = -1;

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setDependent(dependent);
      PSRelationshipSet relationships = processor.getRelationships(filter);
      Iterator it = relationships.iterator();
      while (it.hasNext())
      {
         PSRelationship relationship = (PSRelationship) it.next();
         if (relationship.getOwner().equals(owner) &&
            relationship.getDependent().equals(dependent))
         {
            updateId = relationship.getId();
            break;
         }
      }

      return updateId;
   }

   /**
    * Queries the relationships for the supplied request and send the response
    * back to the requestor.
    *
    * @param data the execution data to operate on, assumed not
    *    <code>null</code>.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private void queryRelationships(PSExecutionData data,
         PSRelationshipDbProcessor processor)
      throws PSCmsException
   {
      PSRequest request = data.getRequest();

      PSRelationshipFilter filter = PSRelationshipDbProcessor
         .getFilterFromParameters(request.getParameters());
      
      PSRelationshipSet relationships = processor.getRelationships(filter);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      doc.appendChild(relationships.toXml(doc));

      request.getResponse().setContent(doc);
   }

   /**
    * Insert a new relationship for the supplied parameters.
    *
    * @param data the execution data to operate on, assmued not
    *    <code>null</code>.
    * @param config the relationship configuration to do the insert for,
    *    assumed not <code>null</code>.
    * @param owner the owner of the relationship beeing inserted, assumed not
    *    <code>null</code>.
    * @param dependent the dependent or the relationship being inserted,
    *    assumed not <code>null</code>.
    * @param useOriginatingRelationship <code>true</code> to use the originating
    *    relationship from the supplied execution data, <code>false</code> to
    *    create a new relationship from the supplied parameters.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSRelationshipProcessorException for all errors occurred while
    *    processing relationship effects.
    * @throws PSCmsException if any error occurr processing internal requests.
    * @throws SQLException for any failed SQL operation.
    */
   private void insertRelationship(PSExecutionData data,
      PSRelationshipConfig config, PSLocator owner, PSLocator dependent,
      boolean useOriginatingRelationship, PSRelationshipDbProcessor processor)
      throws PSRelationshipProcessorException, PSCmsException, SQLException
   {
      putRelationship(data, config, -1, owner, dependent,
         PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT,
         useOriginatingRelationship, processor);
   }

   /**
    * Update the specified relationship for the supplied parameters.
    *
    * @param data the execution data to operate on assmued not
    *    <code>null</code>.
    * @param config the relationship configuration to do the update for,
    *    assumed not <code>null</code>.
    * @param relationshipid the update id to use.
    * @param owner the owner of the relationship beeing updated, assumed not
    *    <code>null</code>.
    * @param dependent the dependent or the relationship beeing updated,
    *    assumed not <code>null</code>.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSRelationshipProcessorException for all errors occurred while
    *    processing relationship effects.
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private void updateRelationship(PSExecutionData data,
      PSRelationshipConfig config, int relationshipid, PSLocator owner,
      PSLocator dependent, PSRelationshipDbProcessor processor)
      throws PSRelationshipProcessorException, PSCmsException
   {
      putRelationship(data, config, relationshipid, owner, dependent,
         PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE, false, processor);
   }

   /**
    * Remove the relationships for the supplied request.
    *
    * @param data the execution data to operate on, assumed not
    *    <code>null</code>.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSRelationshipProcessorException for all errors occurred while
    *    processing relationship effects.
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private void removeRelationship(PSExecutionData data,
         PSRelationshipDbProcessor processor)
      throws PSRelationshipProcessorException, PSCmsException
   {
      putRelationship(data, null, -1, null, null,
         PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE, false, processor);
   }

   /**
    * Perform the requested update operation for the supplied parameters.
    *
    * @param data the execution data to operate on, assumed not
    *    <code>null</code>.
    * @param config the relationship configuration to do the update for,
    *    assumed not <code>null</code>.
    * @param relationshipid the update id to use, must use -1 for inserts.
    * @param owner the owner of the relationship beeing updated, assumed not
    *    <code>null</code>.
    * @param dependent the dependent or the relationship beeing updated,
    *    assumed not <code>null</code>.
    * @param requestType the update type to perform, one of
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE</code>,
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT</code>,
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE</code>.
    * @param useOriginatingRelationship <code>true</code> to use the originating
    *    relationship from the supplied execution data, <code>false</code> to
    *    create a new relationship from the supplied parameters.
    * @param processor the relationship processor, assumed not <code>null</code>.
    * 
    * @throws PSRelationshipProcessorException for all errors occurred while
    *    processing relationship effects.
    * @throws PSCmsException if any error occurr processing internal requests.
    */
   private void putRelationship(PSExecutionData data,
         PSRelationshipConfig config, int relationshipid, PSLocator owner,
         PSLocator dependent, String requestType,
         boolean useOriginatingRelationship, PSRelationshipDbProcessor processor)
         throws PSRelationshipProcessorException, PSCmsException
   {
      int action = PSRelationshipChangeEvent.ACTION_UNDEFINED;

      PSRequest request = data.getRequest();

      boolean delete =
         (requestType == PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE);

      PSRelationshipSet relationships = null;
      if (delete)
      {         
         PSRelationshipFilter filter = PSRelationshipDbProcessor
               .getFilterFromParameters(request.getParameters());
         processor.deleteRelationships(processor.getRelationships(filter));
      }
      else
      {
         relationships = new PSRelationshipSet();

         if (useOriginatingRelationship)
         {
            /**
             * Get the originating relationship from the execution data
             * because the user is allowed to change it during the cloning
             * process.
             */
            PSRelationship originatingRelationship =
               data.getOriginatingRelationship();
            if (originatingRelationship == null)
               throw new PSCmsException(
                  IPSCmsErrors.NO_ORIGINATING_RELATIONSHIP);

            relationships.add(originatingRelationship);
         }
         else
         {
            relationships.add(new PSRelationship(relationshipid, owner,
               dependent, config));
         }

         overrideProperties(relationships, request.getParameters());

         if (requestType == PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT)
            action = PSRelationshipChangeEvent.ACTION_ADD;

         processor.modifyRelationships(relationships);
      }

      // update the execution data with the newly inserted relationship
      if (requestType == PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT)
         request.addRelationships(relationships);
   }

   /**
    * Override all relationship properties for which they supplied an HTML
    * parameter value. To override a property one must provide an HTML parameter
    * with the same name as the property.
    *
    * @param relationships the relationships for which to override properties,
    *    assumed not <code>null</code>.
    * @param htmlParameters a map of HTML parameters to override with, assumed
    *    not <code>null</code>.
    */
   private void overrideProperties(PSRelationshipSet relationships,
      Map htmlParameters)
   {
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);

         if (relationship.getConfig().getUserProperties().isEmpty())
            continue;
         
         Set<String> propNames = relationship.getConfig().getUserProperties()
               .keySet();
         for (String pname : propNames)
         {
            Object paramValue = htmlParameters.get(pname);
            if (paramValue instanceof String &&
               paramValue.toString().trim().length() > 0)
            {
               relationship.setProperty(pname, (String) paramValue);
            }
         }
      }
   }

   /**
    * Loads the relationship configurations into a static member only if
    * not loaded yet, does nothing otherwise.
    *
    * @throws PSServerConfigException if a needed relationship or clone handler
    *    configuration failed to load.
    * @throws PSUnknownNodeTypeException for unknown XML nodes.
    */
   public static void loadConfigs()
      throws PSServerConfigException, PSUnknownNodeTypeException
   {
      if (ms_configs == null)
      {
         reloadConfigs();
      }
   }

   /**
    * Loads the relationship configurations into the internal static member.
    * <p>
    * Note, this must be called after the relationship configurations are
    * updated.
    *
    * @throws PSServerConfigException if a needed relationship or clone handler
    *    configuration failed to load.
    * @throws PSUnknownNodeTypeException for unknown XML nodes.
    */
   public static void reloadConfigs()
      throws PSServerConfigException, PSUnknownNodeTypeException
   {
      ms_configs = new PSRelationshipConfigSet(PSConfigManager.getInstance()
            .getXMLConfig(PSConfigurationFactory.RELATIONSHIPS_CFG)
            .getDocumentElement(), null, null);      
   }
   
   /**
    * Reset the relationship configuration set with the specified config set.
    *  
    * @param configs the new relationship configuration set, never <code>null</code>.
    */
   public static void reloadConfigs(PSRelationshipConfigSet configs)
   {
      if (configs == null)
         throw new IllegalStateException("configs may not be null.");
      
      ms_configs = configs;
   }
   
   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "relate";

   /**
    * The sub-command used to query relationships.
    */
   public static final String COMMAND_QUERY = "query";

   /**
    * The sub-command used to create a new relationship. This is the default if
    * no sub-command is supplied. It does an insert if not existing, an update
    * otherwise.
    */
   public static final String COMMAND_CREATE = "create";

   /**
    * List of {@link IPSRelationshipChangeListener} objects to notify when a
    * relationship is added, removed or modified.  Never <code>null</code>,
    * may be empty.
    * Listeners are added using
    * {@link #addRelationshipChangeListener(IPSRelationshipChangeListener)}
    * and can be removed using
    * {@link #removeRelationshipChangeListener(IPSRelationshipChangeListener)}.
    */
   protected List m_relationshipChangeListeners = new ArrayList();

   /**
    * The sub-command used to insert a relationship. This does an insert whether
    * or not the same relationship already exists.
    */
   public static final String COMMAND_INSERT = "insert";

   /**
    * The sub-command used to remove a relationship.
    */
   public static final String COMMAND_REMOVE = "remove";

   /**
    * The relationship name used for related content.
    */
   public static final String RELATED_CONTENT_RELATIONSHIP =
      PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

   /**
    * A collection of all relationship configurations. Initialized in ctor,
    * never <code>null</code> of changed after that.
    */
   private static PSRelationshipConfigSet ms_configs = null;

   /**
    * The copy handler used to copy items, initialized in ctor, never
    * <code>null</code> or changed after that.
    */
   private IPSCopyHandler m_copyHandler = null;
}
