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

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSWorkflowEvent;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayFieldElementBuilder;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSEditorDocumentBuilder;
import com.percussion.cms.PSModifyDocumentBuilder;
import com.percussion.cms.PSSystemMapping;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSInlineLinkProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipEffectProcessor;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSErrorCollector;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSValidationRulesEvaluator;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSBackEndQueryProcessingError;
import com.percussion.error.PSErrorException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSRejectTransition;
import com.percussion.relationship.PSRelationshipProcessorException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSPageCache;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.util.PSUniqueObjectGenerator;
import com.percussion.util.PSUrlUtils;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.workflow.PSConnectionMgr;
import com.percussion.workflow.PSTransitionsContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

/**
 * This class encapsulates behaviour to handle all workflow related commands.
 */
public class PSWorkflowCommandHandler extends PSCommandHandler
{
   private  PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
   private  PSWorkflowEvent psWorkflowEvent;
   /**
    * Logger
    */
   private static final Logger ms_logger = 
      LogManager.getLogger(PSWorkflowCommandHandler.class);
   
   /**
    * Looks in the system def for pre/post exits assigned to this handler and
    * initializes them, prepares all redirects and then creates the update
    * application which handles all workflow actions.
    *
    * @param ah The handler for the application that contains this editor
    *    resource.
    * @param ceh The parent handler of this handler.
    * @param ce The definition of the editor.
    * @param app Any resources that are created dynamically will be added to
    *    this application. Never <code>null</code>.
    * @throws PSNotFoundException If an exit cannot be found.
    * @throws PSIllegalArgumentException  todo: get rid of this?
    * @throws PSExtensionException If any problems occur druing extension
    *    initialization.
    * @throws IllegalArgumentException if app is <code>null</code>.
    */
   public PSWorkflowCommandHandler(PSApplicationHandler ah,
                                   PSContentEditorHandler ceh,
                                   PSContentEditor ce,
                                   PSApplication app)
         throws PSNotFoundException, PSIllegalArgumentException,PSExtensionException, PSSystemValidationException
   {
      super(ah, ceh, ce, app);
      if (app == null)
         throw new IllegalArgumentException("app cannot be null");

      m_ceHandler = ceh;
      m_ce = ce;

      // prepare all validators used for the entire lifetime of this handler
      prepareValidation(ce);

      // Extract all exits, translations, and validations and prepare them.
      prepareExtensions(COMMAND_NAME);

      // prepare redirects
      prepareRedirects(COMMAND_NAME);

      // create the update dataset for content status updates
      prepareStatusUpdate();

      // create the copy handler for new revisions on checkout
      m_copyHandler = new PSCopyHandler(ceh, ce, app, this, true);
   }

   // see IPSRequestHandler interface for description
   public void processRequest(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req cannot be null");

      if (! m_ceHandler.getCmsObject().isWorkflowable())
      {
         throw new UnsupportedOperationException(
            "This content editor is not workflowable, its object type is: " +
            m_ceHandler.getCmsObject().getName());
      }

      PSExecutionData execData = null;
      Throwable exception = null;
      try
      {
         normalizeTransitionIdParameter(req);
         
         execData = new PSExecutionData(m_appHandler, this, req);

         // add the workflow id to the request parameters
         req.setParameter(IPSHtmlParameters.SYS_WORKFLOWID,
            extractWorkflowId(execData));


         // determine if this is a checkout or checkin action
         boolean isCheckin = false;
         boolean isCheckout = false;
         String wfAction = req.getParameter(
               IPSConstants.DEFAULT_ACTION_TRIGGER_NAME);
         if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_CHECKOUT))
            isCheckout = true;
         else if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_CHECKIN))
            isCheckin = true;
         else if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_FORCE_CHECKIN))
            isCheckin = true;


         if (!isCheckin && !isCheckout)
         {
            // skip validation if currently in a public state
            // and the server setting is not enabled.
            if (isItemValidationNeeded(req))
            {
               // Each request needs a new item validator
               ItemValidator itemValidator = new ItemValidator(execData, m_itemEvaluator);

               /*
                * Perform the actual item validation and redirect to error page
                * if necessary.
                */
               if (!validate(itemValidator))
               {
                  itemValidator.redirectToError(execData);
                  return;
               }
            }
         }

         executeWorkflowRequest(req, execData, isCheckin, isCheckout);

         // do redirect
         processRedirect(execData);
      }
      catch (PSErrorException err)
      {
         exception = err;
         // these are pre-formatted error we've thrown
         m_appHandler.reportError(req, err.getLogError());
      }
      catch (Throwable t)
      {
         exception = t;
         // catch anything that comes our way
         String source = COMMAND_NAME;

         String sessId = "";
         PSUserSession sess = req.getUserSession();
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
            errorArgs = new Object[]{ getExceptionText(t) };
         }

         PSBackEndQueryProcessingError err =
               new PSBackEndQueryProcessingError(m_appHandler.getId(), sessId,
                     errorCode, errorArgs, source);
         m_appHandler.reportError(req, err);
      }
      finally
      {
         if (exception != null )
            ms_logger.warn("Possible problem in workflow transition: ", exception);
         if (execData != null)
            execData.release();
      }
   }

   /**
    * Determines if the item should be validated or not
    * when the item is not checked in or checked out during workflow transition.
    * The item will always be validated unless the item is in a public state and the server property
    * <code>allowItemValidationOnPublicItems</code> is not set to <code>true</code> or <code>yes</code>.
    * 
    * @param req not null.
    * @return <code>true</code> if validation is needed.
    * @throws PSCmsException
    */
   private boolean isItemValidationNeeded(PSRequest req) throws PSCmsException
   {
      String prop = PSServer.getServerProps().getProperty("allowItemValidationOnPublicItems", "false");
      boolean enabled = equalsIgnoreCase(prop, "yes") || equalsIgnoreCase(prop, "true");
      enabled = !PSCms.isPublishable(new Object[] {"y"}, new PSRequestContext(req)) || enabled;
      return enabled;
   }

   /**
    * If the parameter <code>sys_transitionid</code> is missing, then
    * calculate the value required from <code>WFAction</code> and set the
    * <code>sys_transitionid</code> parameter on the request.
    * 
    * @param req the request, assumed not <code>null</code>.
    * @throws Exception when something goes wrong.
    */
   private void normalizeTransitionIdParameter(PSRequest req) 
      throws Exception
   {
      if (req.getParameter(IPSHtmlParameters.SYS_TRANSITIONID) == null)
      {
         String wfaction = req.getParameter(
            IPSConstants.DEFAULT_ACTION_TRIGGER_NAME);
         
         if (wfaction.equalsIgnoreCase(IPSConstants.TRIGGER_CHECKIN) || 
            wfaction.equalsIgnoreCase(IPSConstants.TRIGGER_CHECKOUT))
         {
            /*
             * Check-in and check-out actions are handled through the workflow
             * handler but do not have a transition id. Because of that we
             * must treat them special and just return without setting the
             * <code>sys_transitionid</code>. The check-in and check-out 
             * trigger names are case-insensitive.
             */
            return;
         }
         
         // Try numeric first
         int transition_id = 0;
         try
         {
            transition_id = Integer.parseInt(wfaction);
         }
         catch (NumberFormatException nfe)
         {
            Connection connection = null;
            PSConnectionMgr connectionMgr = null;
            try
            {
               // get the name of the item
               IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
               String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
               PSComponentSummary summary = cms.loadComponentSummary(Integer
                     .parseInt(contentid));
               
               connectionMgr = new PSConnectionMgr();
               connection = connectionMgr.getConnection();
               
               // Now try and translate the name to a transition name
               PSTransitionsContext ctx = new PSTransitionsContext(summary
                     .getWorkflowAppId(), connection, wfaction, summary
                     .getContentStateId());
               transition_id = ctx.getTransitionID();
            }
            finally
            {
               if (connectionMgr != null && connection != null)
               {
                  try
                  {
                     connectionMgr.releaseConnection(connection);
                  }
                  catch (SQLException e)
                  {
                     ms_logger.error("Couldn't release connection", e);
                  }
               }
            }
         }

         req.setParameter(IPSHtmlParameters.SYS_TRANSITIONID, Integer
               .toString(transition_id));
      }
   }

   // see IPSRequestHandler interface for description
   public void shutdown()
   {
   }

   // see IPSInternalRequestHandler interface for description
   public PSExecutionData makeInternalRequest(PSRequest req)
      throws PSInternalRequestCallException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (req == null)
         throw new IllegalArgumentException("req cannot be null");

      if (! m_ceHandler.getCmsObject().isWorkflowable())
      {
         throw new UnsupportedOperationException(
            "This content editor is not workflowable, its object type is: " +
            m_ceHandler.getCmsObject().getName());
      }

      checkInternalRequestAuthorization(req);

      try
      {
         normalizeTransitionIdParameter(req);
         
         PSExecutionData execData = new PSExecutionData(m_appHandler, this, 
            req);

         // determine if this is a checkout or checkin action
         boolean isCheckin = false;
         boolean isCheckout = false;
         String wfAction = req.getParameter(
               IPSConstants.DEFAULT_ACTION_TRIGGER_NAME);
         if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_CHECKOUT))
            isCheckout = true;
         else if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_CHECKIN))
            isCheckin = true;
         else if (wfAction != null && wfAction.trim().equalsIgnoreCase(
               IPSConstants.TRIGGER_FORCE_CHECKIN))
            isCheckin = true;


         if (!isCheckin && !isCheckout)
         {
            // skip validation if currently in a public state
            if (!PSCms.isPublishable(new Object[] {"y"}, 
               new PSRequestContext(req)))
            {
               // Each request needs a new item validator
               ItemValidator itemValidator = new ItemValidator(execData, m_itemEvaluator);

               /*
                * Perform the actual item validation and redirect to error page
                * if necessary.
                */
               if (!validate(itemValidator))
               {
                  recordErrorToRequest(execData, 
                     itemValidator.getErrorCollector(), true);
                  
                  return null;
               }            
            }
         }

         executeWorkflowRequest(req, execData, isCheckin, isCheckout);
         return execData;
      }
      catch (PSException e)
      {
         throw new PSInternalRequestCallException(e.getErrorCode(),e,
            e.getErrorArguments());
      }
      catch (Exception e)
      {
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION, getExceptionText(e),e);
      }
   }

   /**
    * See {@link PSCommandHandler#isUpdate()} for a description of this method.
    *
    * @return <code>true</code> always.
    */
   @Override
   public boolean isUpdate()
   {
      return true;
   }

   /**
    * Executes the provided workflow request.
    *
    * @param req the request to be executed, assumed not <code>null</code>.
    * @param execData the execution data to use, assumed not <code>null</code>.
    * @param isCheckin <code>true</code> if this is a checkin request,
    *    <code>false</code> otherwise.
    * @param isCheckout <code>true</code> if this is a checkout request,
    *    <code>false</code> otherwise.
    * @throws PSErrorException for any errors occurred.
    * @throws PSRequestValidationException for invalid requests.
    * @throws PSAuthorizationException if the user is not authorize to perform
    *    the request.
    * @throws PSRequestValidationException for any failed request validation.
    * @throws SQLException for any failed SQL operation.
    * @throws PSNotFoundException for any file not found.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user failed to
    *    authenticate.
    * @throws PSSystemValidationException for any failed validation.
    * @throws IOException for any IO error occurred.
    * @throws PSUnknownNodeTypeException if the requested document does not
    *    contain a valid relationship set.
    * @throws PSCmsException if anything goes wrong looking up existing
    *    relationships.
    * @throws PSRelationshipProcessorException if anything goes wrong executing
    *    relationship effects.
    * @throws PSRejectTransition is never thrown.
    */
   private PSExecutionData executeWorkflowRequest(PSRequest req,
      PSExecutionData execData, boolean isCheckin, boolean isCheckout)
      throws PSErrorException, PSRequestValidationException,
         PSInternalRequestCallException, PSExtensionProcessingException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSDataExtractionException, PSParameterMismatchException,
         PSNotFoundException, SQLException, PSSystemValidationException, IOException,
         PSUnknownNodeTypeException, PSCmsException,
         PSRelationshipProcessorException, PSRejectTransition,
         PSExtensionException, PSIllegalArgumentException
   {
      PSExecutionData resultData = null;
      try
      {

         /* TODO: We have a problem here: we need to run the authenticateUser
               exit before we translate or validate (we don't want to
               return an error implying the user has authorization to do what
               they are trying to do if they don't), but we don't want to run
               any other pre-exits. This would mean we need to break that exit
               out and run it independently. One thought was to add a special
               security exit to the system def that would be run before
               anything else. */

         // Set params needed for authenticateUser exit
         int level;
         if (isCheckout)
            level = IPSConstants.ASSIGNMENT_TYPE_READER;
         else
            level = IPSConstants.ASSIGNMENT_TYPE_ASSIGNEE;
         PSCommandHandler.setMinAccessLevel(req, level);

         // process effects before the workflow action
         if (!isCheckin && !isCheckout)
         {
            PSRelationshipEffectProcessor effectProcessor =
               new PSRelationshipEffectProcessor(execData,
               IPSExecutionContext.RS_PRE_WORKFLOW);
            effectProcessor.process();
         }

         String condition;
         if (isCheckin)
         {
            // process effects before the checkin
            PSRelationshipEffectProcessor effectProcessor =
               new PSRelationshipEffectProcessor(
                  execData,
                  IPSExecutionContext.RS_PRE_CHECKIN);
            effectProcessor.process();

            /* This looks backwards, but it is interpreted as follows: the
               condition specifies the necessary current state (in this
               case the document must be checked out) for the desired action
               to be allowed. */
            condition = IPSConstants.CHECKINOUT_CONDITION_CHECKOUT;
         }
         else
            condition = IPSConstants.CHECKINOUT_CONDITION_CHECKIN;
         PSCommandHandler.setCheckInOutCondition(req, condition);

         //Just make sure to reset the flag to null
         req.setPrivateObject(IPSConstants.WF_ACTION_PERFORMED, null);

         // run pre exits, which performs the actual checkin/chkout or
         // workflow transitions (if requested)
         runPreProcessingExtensions(execData);

         /* determine if the item is being checked out - if so we will create
          * a new revision if necessary
          */
         if (isCheckout)
         {
            // process effects after the checkout
            PSRelationshipEffectProcessor effectProcessor =
               new PSRelationshipEffectProcessor(execData,
               IPSExecutionContext.RS_POST_CHECKOUT);
            effectProcessor.process();
            
            createCheckedOutRevision(execData);
         }
         else
         {
            // no checkout, so just update content status
            req.setParameter(m_internalApp.getRequestTypeHtmlParamName(),
                  m_internalApp.getRequestTypeValueUpdate());
            String reqName = createRequestName(m_internalApp.getName(),
                  m_updateStatusResource);
            IPSInternalRequestHandler rh = PSServer.getInternalRequestHandler(reqName);
            resultData = rh.makeInternalRequest(req);
         }

         String wfAction =
            req.getParameter(IPSConstants.DEFAULT_ACTION_TRIGGER_NAME);
         // notify listeners if checkin, checkout, or a transition
         int action = PSEditorChangeEvent.ACTION_UNDEFINED;
         if (isCheckin)
            action = PSEditorChangeEvent.ACTION_CHECKIN;
         else if (isCheckout)
            action = PSEditorChangeEvent.ACTION_CHECKOUT;
         else if (req.getParameter(IPSHtmlParameters.SYS_TRANSITIONID) != null
                  || (wfAction!=null && wfAction.length()>0))
            action = PSEditorChangeEvent.ACTION_TRANSITION;

         if (action != PSEditorChangeEvent.ACTION_UNDEFINED)
         {
            Object wfActionPerformed = req
               .getPrivateObject(IPSConstants.WF_ACTION_PERFORMED);
            if(wfActionPerformed == null || !wfActionPerformed.toString()
               .equalsIgnoreCase(IPSConstants.BOOLEAN_FALSE))
            {
               notifyEditorChangeListeners(execData, action);
            }
            req.setPrivateObject(IPSConstants.WF_ACTION_PERFORMED, null);
         }

         // run post exits, updating content history and last public revision...
         runPostProcessingExtensions(execData, null);

         // process effects after the workflow action
         if (!isCheckin && !isCheckout)
         {
            PSRelationshipEffectProcessor effectProcessor =
               new PSRelationshipEffectProcessor(execData,
               IPSExecutionContext.RS_POST_WORKFLOW);
            effectProcessor.process();
         }

         PSComponentSummary summary = PSWebserviceUtils.getItemSummary(Integer.parseInt(req.getParameter("sys_contentid")));
         PSWorkflow wf = PSWebserviceUtils.getWorkflow(summary.getWorkflowAppId());
         PSState currState = PSWebserviceUtils.getStateById(wf, summary.getContentStateId());
         PSLegacyGuid ps= new PSLegacyGuid(summary.getContentId(),summary.getCurrRevision());
         String currentState=currState.getName();

         if(!wfAction.equalsIgnoreCase("Pending")){
            wfAction="Quick Edit";
         }


         psWorkflowEvent=new PSWorkflowEvent(wfAction,currentState, PSWorkflowEvent.WorkflowEventActions.update, req.getServletRequest(),req.getParameter("sys_contentid"), ps.toString(), PSActionOutcome.SUCCESS.name());

         psAuditLogService.logWorkflowEvent(psWorkflowEvent);

         return execData;
      }
      finally
      {
         if (resultData != null)
            resultData.release();
            
         //clear content item status cache 
         req.clearContentItemStatusCache();
      }
   }

   /**
    * Creates dataset used to perform the actual updates against the backend
    * data.
    *
    * @throws PSSystemValidationException if there the app is not properly defined.
    */
   private void prepareStatusUpdate()
         throws PSSystemValidationException
   {
      // make the content status update data mappings
      List<PSSystemMapping> updateSysMappings =
            PSApplicationBuilder.getSystemUpdateMappings(m_ceHandler, m_ce);

      PSDataMapper updateSystemMapper =
            PSApplicationBuilder.createSystemMappings(
                  updateSysMappings.iterator());

      // create resource name
      m_updateStatusResource = PSUniqueObjectGenerator.makeUniqueName(
            CONTENTSTATUS_UPDATE);

      // create the dataset
      PSApplicationBuilder.createUpdateDataset(m_internalApp,
            m_updateStatusResource, m_ce, null, null, updateSystemMapper, false,
            PSApplicationBuilder.FLAG_ALLOW_UPDATES);

   }


   /**
    * Checks to see if the base revision matches the current revision, and if
    * not creates a new revision by copying the current revision.
    *
    * @param data The execution data for this request.  Assumed not <code>
    * null</code>.
    *
    * @return <code>true</code> if this is a new revision has been created,
    * <code>false</code> if not.
    *
    * @throws PSRequestValidationException if there are any errors extracting
    * parameters.
    * @throws PSNotFoundException if the content and revision ids do not
    * specify an existing content item revision.
    * @throws PSAuthorizationException if the user is not authorized to execute
    * the copy.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSInternalRequestCallException if there is an error executing the
    * copy.
    * @throws SQLException if there is an error generating new Ids for any
    * item children.
    * @throws PSSystemValidationException for any failed validation.
    * @throws IOException for any IO error occurred.
    * @throws PSUnknownNodeTypeException if the requested document does not
    *    contain a valid relationship set.
    * @throws PSCmsException if anything goes wrong looking up existing
    *    relationships.
    */
   @SuppressWarnings("unchecked")
   private boolean createCheckedOutRevision(PSExecutionData data)
         throws PSRequestValidationException, PSAuthorizationException,
         PSInternalRequestCallException, PSAuthenticationFailedException,
         PSNotFoundException, SQLException, PSSystemValidationException, IOException,
         PSUnknownNodeTypeException, PSCmsException
   {
      boolean result = false;

      PSRequest req = data.getRequest();

      IPSWorkFlowContext wfCtx =
            (IPSWorkFlowContext) req.getPrivateObject(
                  IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT);

      if (wfCtx != null)
      {
         String contentIdParam = m_ceHandler.getParamName(
               PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
         
         String revisionIdParam = m_ceHandler.getParamName(
               PSContentEditorHandler.REVISION_ID_PARAM_NAME);

         String strContentId = req.getParameter(contentIdParam);         
         String strRevision = req.getParameter(revisionIdParam);
         int contentId;
         int curRevision;
         try
         {
            contentId = Integer.parseInt(strContentId);
            strContentId = null;
            curRevision = Integer.parseInt(strRevision);
            strRevision = null;
         }
         catch (NumberFormatException e)
         {
            String paramName = (strContentId != null ? contentIdParam :
                  revisionIdParam);
            String paramVal = (paramName.equals(contentIdParam) ? strContentId :
                  strRevision);
            Object[] args = {paramName, paramVal == null ? "null" : paramVal};
            throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }

         int baseRevision = wfCtx.getBaseRevisionNum();

         // if revisions do not match, then create a new revision
         if (baseRevision != curRevision)
         {
            m_copyHandler.createCopy(contentId, baseRevision, contentId,
                  curRevision, data);
            Map<Integer, PSRelationship>inlineRelationships = 
               PSCloneCommandHandler.copyRelatedContent(contentId, 
                  baseRevision, contentId, curRevision, data, m_ceHandler);

            PSLocator item = new PSLocator(contentId, curRevision);
            try
            {
               // the item should have checked out by now, 
               // no need to checkout or checkin
               PSInlineLinkProcessor.processInlineLinkItem(data.getRequest(), 
                  item, inlineRelationships, -1, false, false);
            }
            catch (PSException e)
            {
               throw new PSCmsException(e);
            }

            result = true;
         }
      }

      return result;
   }

   /**
    * Does a field level validation to check the occurrence settings. For any
    * missing field an error is produced to the m_errorCollector. If field
    * validation was successful, item level validation is performed. Also
    * item level errors will be added to the m_errorCollector.
    *
    * @param validator the ItemValidator for the current request
    *
    * @return <code>true</code> if the validations pass, or the validator is
    *          <code>null</code>; <code>false</code> if the validations fail
    * @throws RuntimeException if any error occurs during validation
    */
   private static boolean validate(ItemValidator validator)
   {
      try
      {
         //Field level required validation is removed as part of the bug fixes: 
         //CML-4650 and CML-4761 as it was creating a problem on applying PSApplyWhen
         //rule.

         if (null == validator) return true;

         
         if (!validator.validateItem())
         {
            validator.createItemErrors();
            return false;
         }

         return true;
      }
      catch (PSExtensionProcessingException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
      catch (PSDataExtractionException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
      catch (PSParameterMismatchException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * Creates and caches the error page for the supplied error collector. The
    * error is always recorded into the request supplied through the execution
    * data. Then the called is redirected to the cached error page if so 
    * requested.
    *
    * @param data the execution data for which to record the validation error, 
    *    assumed not <code>null</code>.
    * @param errorCollector the error collector object that contains the 
    *    validation errors, assumed not <code>null</code>.
    * @param redirect <code>true</code> to redirect to the cached error page 
    *    created, <code>false</code> otherwise.
    * @throws PSDataExtractionException if the redirect URL could not be
    *    extracted.
    * @throws IOException if the redirection failed.
    */
   private void recordErrorToRequest(PSExecutionData data, 
      PSErrorCollector errorCollector, boolean redirect)
      throws PSDataExtractionException, IOException
   {
      try
      {
         // get all html parameters
         PSRequest request = data.getRequest();
         PSRequestContext context = new PSRequestContext(request);

         // get the error stylesheet
         String stylesheet = null;
         IPSReplacementValue itemErrorStylesheet = m_ceHandler.getInitParam(
            COMMAND_NAME, "com.percussion.defaultItemError");
         if (itemErrorStylesheet != null)
            stylesheet = itemErrorStylesheet.getValueText();

         Document errorPage = errorCollector.getErrorDocument(request);

         int cacheId = PSPageCache.getInstance().addPage(errorPage);
         String url = request.getRequestFileURL();
         Map<String, String> params = new HashMap<String, String>();
         params.put(PSContentEditorHandler.COMMAND_PARAM_NAME,
            PSEditCommandHandler.COMMAND_NAME);
         params.put(PSContentEditorHandler.CACHE_ID_PARAM_NAME,
            Integer.toString(cacheId));
         if (stylesheet != null)
            params.put(PSContentEditorHandler.USE_STYLESHEET, stylesheet);
         URL completeUrl = PSUrlUtils.createUrl(null, null, url,
            params.entrySet().iterator(), null, context);

         Document errorDoc = errorCollector.getErrorDocument(request);
         String errorMsg = PSXmlDocumentBuilder.toString(errorDoc);
         request.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, errorMsg);

         if (redirect)
            sendRedirect(data, completeUrl.toExternalForm());
      }
      catch (MalformedURLException e)
      {
         // should never happen
         throw new PSDataExtractionException(
            IPSServerErrors.CE_NO_REDIRECT_URL);
      }
   }


   /**
    * The internal name of this handler. When handler names are used in
    * config files, this is the name that must be used.
    */
   public static final String COMMAND_NAME = "workflow";

   /**
    * The base dataset name of the contentstatus_update resource. This
    * dataset is used to update the last modified columns in the CONTENTSTATUS
    * table.
    */
   private static final String CONTENTSTATUS_UPDATE = "contentstatus_update";

   /**
    * Used to perform the actual copying of the item.  Initialized in the ctor,
    * never <code>null</code> after that.
    */
   private PSCopyHandler m_copyHandler = null;

   /**
    * The name of the dataset to use to update the content status table.
    * Set during construction, never <code>null</code> or modified after that.
    */
   private String m_updateStatusResource = null;

   /**
    * Wrapper for all item level validation functionality.
    */
   private class ItemValidator
   {
      /**
       * Constructs a new item validator.
       *
       * @param data the execution data to construct the validator for, not
       *    <code>null</code>.
       * @param fieldEvaluatorMap a map of field evaluators to be validated
       *    in this validator, not <code>null</code>.
       * @param itemEvaluator the item evaluator which performs the actual
       *    item validation, not <code>null</code>.
       * @param lang language/locale string in XML language attribute sytax
       * (en-us, fr-ca)used to produce localized error message. If <code>null</code>
       * or <code>empty</code>, default language string is used.
       * @throws IllegalArgumentException if any parameter except lang is
       *    <code>null</code>.
       * @throws PSRequestValidationException if the data does not contain the
       * necessary parameters.
       * @throws PSCmsException If there is an error loading item data for 
       * validation.
       */
      public ItemValidator(PSExecutionData data, PSValidationRulesEvaluator itemEvaluator ) 
         throws PSCmsException, PSRequestValidationException
      {
         if (data == null || itemEvaluator == null)
             throw new IllegalArgumentException("parameters cannot be null");

         initValidation(data);
         mi_data = data;
         mi_itemEvaluator = itemEvaluator;
      }

 
      /**
       * Checks if any validation errors occurred and redirects the client
       * to an error page if so.
       *
       * @param data the execution data, not <code>null</code>.
       * @throws IllegalArgumentException if the provided data is
       *    <code>null</code>.
       * @throws PSDataExtractionException if the redirect URL could not be
       *    extracted.
       * @throws IOException if the redirection failed.
       */
      public void redirectToError(PSExecutionData data)
         throws PSDataExtractionException, IOException
      {
         if (data == null)
            throw new IllegalArgumentException("data cannot be null");

         recordErrorToRequest(data, mi_errorCollector, true);            
      }

      /**
       * Get the error collector containing all collected errors occurred
       * during validation. Should be used as read only.
       *
       * @return the error collector, never <code>null</code>.
       */
      public PSErrorCollector getErrorCollector()
      {
         return mi_errorCollector;
      }

      /**
       * After item validation is done and validation errors occurred, use
       * this method to create the appropriate error messages in the error
       * collector using the provided item document.
       *
       * @throws IllegalArgumentException if the provided item document is
       *    <code>null</code>.
       */
      public void createItemErrors()
      {
         mi_errorCollector.createItemErrors(mi_pageMap);
      }

      /**
       * Prepares the validation process. Makes an internal request to all pages
       * within the current item. Parses the summary page to determine the
       * sys_pageid and row id for complex child pages.
       * Each page XML document is stored in a map.
       * An XML document containing the entire item will also be created. This
       * is used in the validation process.
       *
       * @param data the execution data to prepare the validation process for,
       *    assumed not <code>null</code>.
       * 
       * @throws PSRequestValidationException if the data does not contain the
       * necessary parameters.
       * @throws PSCmsException If there is an error loading item data for 
       * validation.
       */
      @SuppressWarnings("unchecked")
      private void initValidation(PSExecutionData data) throws PSCmsException, 
         PSRequestValidationException
      {
         // get the internal command request handler
         String reqName = createRequestName(m_ceHandler.getName(),
               m_ce.getName());
         IPSInternalCommandRequestHandler rh = 
            (IPSInternalCommandRequestHandler) PSServer
               .getInternalRequestHandler(reqName);
         mi_pageMap = new HashMap<Integer, Map<Integer, Document>>();
         int maxErrorsToStop = Integer.MAX_VALUE;
         
         // set the correct revision - always validate the tip revision as the 
         // item may be checked out and the transition will force a checkin if
         // the user is an admin (e.g. aging transitions or AA mandatory)
         PSRequest request = data.getRequest();
         setTipRevision(request);
         Map params = request.getParameters();

         //get parent page with pageId = 0
         Integer parentPageId = new Integer(0);
         params.put(PSContentEditorHandler.PAGE_ID_PARAM_NAME,
            parentPageId.toString());
         String cmdHandlerName = PSEditCommandHandler.COMMAND_NAME;
         Document doc = rh.makeInternalRequest(request, cmdHandlerName);
         Map<Integer, Document> parentMap = new HashMap<Integer, Document>();
         parentMap.put(parentPageId,doc);
         //put the parent page
         mi_pageMap.put(parentPageId, parentMap);

         //Check all the table definition (complex child)
         //to find all the page ids
         NodeList controls =
            doc.getElementsByTagName(PSDisplayFieldElementBuilder.CONTROL_NAME);
         for(int i = 0; i < controls.getLength(); i++)
         {
            Element control = (Element) controls.item(i);
            String dimension = control.getAttribute(
                           PSDisplayFieldElementBuilder.DIMENSION_NAME);
            //check for complex child
            if(dimension.equals(PSDisplayFieldElementBuilder.DIMENSION_TABLE))
            {
               NodeList tableActionLink = control.getElementsByTagName(
                  PSEditorDocumentBuilder.ACTION_NAME);
               int childPageId = 0;
               Map<Integer, Document> childMap = new HashMap<Integer, Document>();
               for( int j = 0; j < tableActionLink.getLength(); j++)
               {
                  Element action = (Element) tableActionLink.item(j);
                  NodeList paramList =
                     action.getElementsByTagName(
                        PSEditorDocumentBuilder.PARAM_NAME);
                  for(int k = 0; j < paramList.getLength(); k++)
                  {
                     Element param = (Element)paramList.item(k);
                     String name = param.getAttribute("name");
                     if(name.equals(PSContentEditorHandler.PAGE_ID_PARAM_NAME))
                     {
                        String test = ((Text) param.getFirstChild()).getData();
                        childPageId = Integer.parseInt(
                              test.trim());
                        //if child page Id is odd + 1 or else
                        if(childPageId%2 != 0)
                           childPageId++;
                        break;
                     }
                  }
               }
               NodeList rows = control.getElementsByTagName("Row");
               for(int l = 0; l < rows.getLength(); l++)
               {
                  //if row exist then request the page for each row
                  Element row = (Element)rows.item(l);
                  String rownum = row.getAttribute(
                     PSEditorDocumentBuilder.CHILDKEY_ATTRIB);
                  params.put(PSContentEditorHandler.PAGE_ID_PARAM_NAME,
                     Integer.toString(childPageId));
                  params.put(
                     PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, rownum);
                  Document childDoc =
                     rh.makeInternalRequest(request, cmdHandlerName);
                  //remove the child row id param name. Assume parent page
                  //does not have this parameter.
                  params.remove(
                     PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
                  childMap.put(new Integer(rownum), childDoc);
               }
               /*
                * if child Map is empty that means that the table has no row.
                * so insert a new page for that childPageId. This is needed for
                * occurance validation.
                */
               if(childMap.isEmpty())
               {
                  params.put(PSContentEditorHandler.PAGE_ID_PARAM_NAME,
                     Integer.toString(childPageId));
                  Document childDoc =
                     rh.makeInternalRequest(request, cmdHandlerName);
                  childMap.put(new Integer(childPageId), childDoc);
               }
               mi_pageMap.put(new Integer(childPageId), childMap);
               int max = getMaxErrorsToStop(new Integer(childPageId));
               if (max < maxErrorsToStop)
                  maxErrorsToStop = max;
            }
         }

         mi_errorCollector = new PSErrorCollector(PSErrorCollector.TYPE_ITEM,
               maxErrorsToStop);
         mi_item = createItemDocument();
      }

      /**
       * Using the supplied request, sets the revision html param with the value 
       * of the tip revision of the specified content id.
       *  
       * @param req The request that specifies the content id and in which the
       * revision is set, assumed not <code>null</code>.
       * 
       * @throws PSRequestValidationException if the content id is not specified
       * in the supplied request. 
       * @throws PSCmsException If the items summary cannot be loaded in order
       * to determine the tip revision. 
       */
      private void setTipRevision(PSRequest req) throws PSCmsException, 
         PSRequestValidationException
      {
         String contentIdParam = m_ceHandler.getParamName(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
         String revisionParam = m_ceHandler.getParamName(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME);

         PSLocator loc;
         String strContentId = req.getParameter(contentIdParam);
         try
         {
            loc = new PSLocator(strContentId);
         }
         catch (Exception e)
         {
            Object[] args = {contentIdParam, strContentId == null ? "null" : 
               strContentId};
            throw new PSRequestValidationException(
                  IPSServerErrors.CE_MODIFY_INVALID_PARAM, args);
         }
         
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = cms.loadComponentSummary(loc.getId());
         int revision = summary.getTipLocator().getRevision();
         req.setParameter(revisionParam, String.valueOf(revision));
      }


      /**
       * Given a map of item screens this will create a document containing all
       * item information combined.
       * <ol><li>
       *    The ContentEditor element will be taken from the first page.
       * </li>
       * <li>
       *    The ControlNameSet element will be recreated with all ControlName
       *    elements found in every page. This is not really necessary, but
       *    makes the produced document conform to the sys_ContentEditor.dtd.
       * </li>
       * <li>
       *    The ItemContent element will be recreated with all DisplayFields
       *    found in every page. The DisplayFields are ordered from page 0 to n.
       * </li>
       * <li>
       *    The UserStatus element will be added from the first page.
       * </li>
       * <li>
       *    The Workflow element will be taken from the first page.
       * </li></ol>
       */
      private Document createItemDocument()
      {
         Document item = PSXmlDocumentBuilder.createXmlDocument();
         Element contentEditor = null;
         Element controlNameSet = item.createElement(
               PSEditorDocumentBuilder.CONTROLNAMES_NAME);
         Element itemContent = item.createElement(
               PSEditorDocumentBuilder.ITEM_NAME);
         Element userStatus = null;
         Element workflow = null;

         Map<String, Element> controlMap = new HashMap<String, Element>();
         Iterator<Map<Integer, Document>> pages = mi_pageMap.values().iterator();
         while (pages.hasNext())
         {
            Map <Integer, Document>pageMappers = pages.next();
            Iterator<Document> pageMapper = pageMappers.values().iterator();
            while( pageMapper.hasNext())
            {
               Document page = pageMapper.next();

               // set the attributes from the first page's content editor
               if (contentEditor == null)
               {
                  Element ce = page.getDocumentElement();
                  contentEditor = (Element)item.importNode(ce, true);
               }

               // append all control name fields to the control name set
               NodeList cns = page.getElementsByTagName(
                     PSEditorDocumentBuilder.CONTROLNAME_NAME);
               for (int j = 0; j < cns.getLength(); j++)
               {
                  Element controlName = (Element) cns.item(j);
                  String control = ((Text) controlName.getFirstChild()).getData();
                  if (controlMap.get(control) == null)
                  {
                     Node importNode = item.importNode(controlName, true);
                     controlNameSet.appendChild(importNode);
                     controlMap.put(control, controlName);
                  }
               }

               // append all display fields to the item content
               NodeList dvs = page.getElementsByTagName(
                     PSDisplayFieldElementBuilder.DISPLAYFIELD_NAME);
               for (int j = 0; j < dvs.getLength(); j++)
               {
                  Node importNode = item.importNode(dvs.item(j), true);
                  itemContent.appendChild(importNode);
               }

               // get the first user status found
               if (userStatus == null)
               {
                  NodeList uss = page.getElementsByTagName(
                        PSEditorDocumentBuilder.USERSTATUS_NAME);
                  if (uss.getLength() > 0)
                     userStatus = (Element) (item.importNode(uss.item(0), true));
               }

               // get the first workflow info found
               if (workflow == null)
               {
                  NodeList wfs = page.getElementsByTagName(
                        PSModifyDocumentBuilder.WORKFLOW_NAME);
                  if (wfs.getLength() > 0)
                     workflow = (Element) (item.importNode(wfs.item(0), true));
               }
            }
         }

         // assemble the combined document
         contentEditor.appendChild(controlNameSet);
         contentEditor.appendChild(itemContent);
         if (userStatus != null)
            contentEditor.appendChild(userStatus);
         if (workflow != null)
            contentEditor.appendChild(workflow);
         item.appendChild(contentEditor);

         return item;
      }

      /**
       * Validate the item.
       *
       * @return <code>true</code> if successful, <code>false</code>
       *    otherwise.
       * @throws @link IPSResultDocumentProcessor#processResultDocument for
       *    exception description.
       */
      public boolean validateItem()
            throws PSExtensionProcessingException, PSDataExtractionException,
            PSParameterMismatchException
      {
         return mi_itemEvaluator.isValid(mi_data, mi_item, mi_errorCollector);
      }

      /**
       * The error collector used in this validator. Initialized during
       * construction, never <code>null</code> after that.
       */
      private PSErrorCollector mi_errorCollector = null;

      /**
       * A map of item screen pages, queried during construction. The key is
       * is the pageid as Integer and the value is a map between row id
       * and the page XML as Document. If there is no row id the the map will
       * have key of parent page and the new page as a Document. For parent page
       * the map  will contain parent page id and parent page document
       * Never <code>null</code> or empty after construction.
       */
      private Map<Integer, Map<Integer, Document>> mi_pageMap = null;

      /**
       * The execution data used for this item validator. Initialized during
       * constuction, never <code>null</code> after that.
       */
      private PSExecutionData mi_data = null;

      /**
       * The combined item document needed for item validation. Created
       * during construction, never <code>null</code> after that.
       */
      private Document mi_item = null;


      /**
       * See {@link PSCommandHandler#m_itemEvaluator} for the description.
       */
      private PSValidationRulesEvaluator mi_itemEvaluator = null;

   }
}
