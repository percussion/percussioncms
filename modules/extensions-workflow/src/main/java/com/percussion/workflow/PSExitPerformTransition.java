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

package com.percussion.workflow;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.exceptions.PSORMException;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Performs the requested workflow transition (such as approval, check-in or
 * check-out) based on the action trigger from the HTML form submitted by the
 * user, after validating the conditions for the transition. These conditions
 * include the role required to perform the transition, the check-in/check-out
 * status of the content, and whether a transition comment is required and has
 * been provided. Information concerning the workflow context and the workflow
 * actions for this transition is placed into private objects in the request
 * context.
 */
public class PSExitPerformTransition implements IPSRequestPreProcessor
{
   /**
    * This is an inner class to encapsulate the parameters. We cannot keep
    * these as class variables due to threading issues. We instantiate this
    * object in the main processrequest method (called by server) and pass
    * around the methods. This is meant for convenience only.
    */
   private class Params
   {

      /**
       * the workflow app ID for this transition
       */
      public int     m_workflowAppID = 0;

      /**
       * the ContentID for this transition
       */
      public int     m_contentID = 0;

      /**
       * The revision number for the content item in the workflow context:
       * <ul><li>for transitions - current revision</li>
       * <li>for checkin - revision being checked in</li>
       * <li>for checkout - base revision for the item being checked out:
       *                    either 1, or the revision of the item copied to
       *                    create the revision checked out</li></ul>
       */
      public int     m_contextRevision =
      IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;

      /**
       * The revision number for the content item for html parameter
       * <ul> After reading the system_revision html parameter
       * <li>for transitions, or if value is not set - 0 </li>
       * <li>for checkin - revision being checked in (optional) - </li>
       * <li>checkout - revision requested for check out</li></ul>
       * <ul> After processing checkin/checkout value to set for
       *     system_revision html parameter
       * <li>for transitions - 0 - do not set html parameter </li>
       * <li>for checkin - revision checked in</li>
       * <li>checkout - revision actually checked out: either 1, or a new
       *                revision with a content item that must be copied from
       *                the item with the revision requested.</li></ul>
       */
      public int     m_htmlRevision =
      IPSConstants.NO_CORRESPONDING_REVISION_VALUE;

      /**
       * the transition ID for this transition
       */
      public int     m_transitionID =
      IPSConstants.TRANSITIONID_NO_ACTION_TAKEN;

      /**
       * the user that will perform this transition or action
       * including check in and check out
       */
      public String  m_userName = null;

      /**
       * action trigger name e.g. "reject"
       */
      public String  m_actionTrigger = null;

      /**
       * the state ID for the state in which the content item begins this
       * transition.
       */
      public int     m_transitionFromStateID = 0;

      /**
       * the state ID for the state in which the content item  this
       * transition.
       */
      public int     m_transitionToStateID = 0;

      /**
       * <CODE>true</CODE> if a transition was performed.
       *  else <CODE>false</CODE>.
       */
      public boolean b_transitionPerformed  = false;

      /**
       * the name of the user who has this content item checked out, or
       * <CODE>null</CODE> if the item is not checked out.
       */
      public String  m_checkedOutUser = null;

      /**
       * <CODE>true</CODE> if this a check in or check out,
       *  else <CODE>false</CODE>.
       */
      public boolean m_isCheckinOrCheckout  = false;

      /**
       * <CODE>true</CODE> if this a check in
       *  else <CODE>false</CODE>.
       */
      public boolean m_isCheckin  = false;

      /**
       * Status code indicating whether this item is checked out:
       * <ul>
       * <li>
       * PSWorkFlowUtils.CHECKOUT_STATUS_NONE - Not checked-out by anybody
       * </li>
       * <li>
       * PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER - Checked out by current
       *                                                user
       * </li>
       * <li>
       * PSWorkFlowUtils.CHECKOUT_STATUS_OTHER - Checked out by some body else
       * </li>
       * </ul>
       */
      public int     m_checkoutStatus = PSWorkFlowUtils.CHECKOUT_STATUS_NONE;

      /**
       * <CODE>true</CODE> if this item is checked out,
       * <CODE>false</CODE> if not.
       */
      public boolean m_checkedOut = true;

      /**
       * the request context for this extension call (used for tracing).
       */
      public IPSRequestContext m_request = null;

      /**
       * <CODE>true</CODE> if the user is an administrator,
       * <CODE>false</CODE> if not.
       */
      public boolean m_isAdministrator = false;

      /**
       * descriptive comment for this transition. maybe <CODE>null</CODE>
       */
      public String  m_transitionComment = null;

      /**
       *  list of proposed new adhoc users. May be <CODE>null</CODE>
       */
      public String m_adhocUserList = null;

      /**
       * Object containing role information. Not <CODE>null</CODE> once
       * initialized.
       */
      public PSWorkflowRoleInfo m_wfRoleInfo = null;
      /**
       * Item's Community
       */
      public int m_itemCommunity = -1;

      /**
       * user's login community
       */
      public int m_userCommunity = -1;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   /* Set the parameter count to not initialized */
   static private int ms_correctParamCount = IPSExtension.NOT_INITIALIZED;


   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      if (ms_correctParamCount == IPSExtension.NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while(iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }

         m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

   /* *******  IPSRequestPreProcessor Interface Implementation ******* */

   /**
    * Performs the transition or checkout or checkin action after validating
    * the input data. Creates a workflow context private object. if any
    * transition workflow actions exist, they are put in a private object.
    *
    * @param  params          the parameters for this extension.
    *         params[0]       the content ID
    *         params[1]       the user name
    *         params[2]       the action trigger
    * @param request          the context of the request associated with this
    *                         extension.
    *                         names of request HTML parameters used as input:
    *                         assignment type: value of workflow property
    *                           HTML_PARAM_ASSIGNMENTTYPE, default value is
    *                           PSWorkFlowUtils.ASSIGNMENT_TYPE_CURRENT_USER
    *                         transition comments: value of workflow property
    *                           HTML_PARAM_TRANSITION_COMMENT, default value is
    *                           PSWorkFlowUtils.TRANSITION_COMMENT
    *                         system revision:  IPSHtmlParameters.SYS_REVISION
    *                         <P>
    *                         Modifications to <CODE>request</CODE> by this
    *                         method
    *                         <ul>
    *                         <li>
    *                         creation of the workflow context private object
    *                         which has the key <CODE>
    *                        IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT
    *                         </CODE>
    *                         </li>
    *                         <li>if a list of workflow action extensions
    *                         exists a private object with key <CODE>
    *                         IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT
    *                         </CODE> is  created.
    *                         </li>
    *                         <li> a private object with key <CODE>
    *                         PSWorkflowRoleInfo.
    *                         WORKFLOW_ROLE_INFO_PRIVATE_OBJECT </CODE>
    *                         is used to obtain the roles in which the user is
    *                         acting. It is updated with content adhoc user
    *                         information.
    *                         </li>
    *                         <li>
    *                         HTML parameters used as input that may be
    *                         modified:
    *                         assignment type,
    *                         system revision - value
    *                         <ul><li>for transition - not set</li>
    *                         <li>for checkin - revision checked in</li>
    *                         <li>checkout - revision actually checked out:
    *                         either 1, or a new revision with a content item
    *                         that must be copied from the item with the
    *                         revision requested.</li></ul>
    *                         HTML parameters with the following names are set:
    *                         new state id: value of workflow property
    *                           HTML_PARAM_NEWSTATEID,  default value is
    *                           PSWorkFlowUtils.DEFAULT_NEWSTATEID_NAME
    *                         value is new state ID if a transition was
    *                           performed, otherwise equals the empty string
    *                         checkout user name: value of workflow property
    *                           HTML_PARAM_CHECKOUTUSERNAME, default value is
    *                           PSWorkFlowUtils.CHECKOUT_USER_NAME
    *                         checkout status: value of workflow property
    *                           HTML_PARAM_CHECKOUTSTATUS, default value is
    *                          PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_DOCUMENT
    *
    *                         </li>
    *                         </ul>
    * @throws                 PSExtensionProcessingException if
    *                         <ul>
    *                         <li>content ID is <CODE>null</CODE> or
    *                         empty. </li>
    *                         <li>user name parameter is <CODE>null</CODE> or
    *                         empty on input, or <CODE>null</CODE> after
    *                         removing host names. </li>
    *                         <li>if a parameter of the wrong type is
    *                         supplied</li>
    *                         <ul>
    *                         <li>if wrong number of parameters is supplied.
    *                         </li>
    *                         <li><CODE>request</CODE> is <CODE>null</CODE>
    *                         </li>
    *                         <li>an SQL exception is caught
    *                         </li>
    *                         <li>a PSEntryNotFoundException is caught because
    *                         an expected data base entry does not exist
    *                         </li>
    *                         </ul>
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "\nPerform Transition: enter preProcessRequest");

      PSConnectionMgr connectionMgr = null;
      HashMap htmlParams = null;
      int nParamCount = 0;
      Params localParams = new Params();
      String sRoleNameList = "";
      List actorRoleList = null;
      String assignmentType = "";
      Connection connection = null;
      int currentStateID = 0;
      PSWorkFlowContext wfContext = null;
      PSWorkflowRoleInfo wfRoleInfo = null;
      int nAssignmentType = 0;
      String adhocUserList = "";
      Exception except = null;
      try
      {
         if(null == request)
         {
            throw new PSExtensionProcessingException(
               PSWorkFlowUtils.ERROR_INVALID_PARAMETER_TYPE,
               "The request must not be null");
         }

         String lang = (String)request.getSessionPrivateObject(
               PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (lang == null)
            lang =   PSI18nUtils.DEFAULT_LANG;

         htmlParams = request.getParameters();
         if(null == params)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "perform transition: no parameters - exiting");
            return; //no parameters - exit with peace!
         }

         nParamCount = params.length;

         if(null == htmlParams)
         {
            htmlParams = new HashMap();
            request.setParameters(htmlParams);
         }

         localParams.m_request = request;

         try
         {
            if(ms_correctParamCount != nParamCount)
            {
               String[] exParams = {String.valueOf(ms_correctParamCount),
                                    String.valueOf(nParamCount)};

               throw new PSInvalidNumberOfParametersException(lang,
                IPSExtensionErrors.INVALID_PARAM_NUM, exParams);
            }

            if (null == params[0] || 0 ==
                params[0].toString().trim().length())
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                  "perform transition: no contentid  - exiting");
               return; //no contentid means do nothing
            }
            localParams.m_contentID =
                  new Integer(params[0].toString()).intValue();
            if(null == params[1] || 0 ==
               params[1].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(lang,
                IPSExtensionErrors.EMPTY_USRNAME1);
            }

            localParams.m_userName = params[1].toString();

            if( 0 == localParams.m_userName.length())
            {
               throw new PSInvalidParameterTypeException(lang,
                IPSExtensionErrors.EMPTY_USRNAME2);
            }

            if(null == params[2] || 0 == params[2].toString().trim().length())
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                  "perform transition:no action trigger  - exiting");
               return; //no action trigger means do nothing
            }
            localParams.m_actionTrigger = params[2].toString().trim();
         }
         catch(PSInvalidNumberOfParametersException ne)
         {
            String language = ne.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(lang,
             m_fullExtensionName, ne);
         }
         catch(PSInvalidParameterTypeException te)
         {
            String language = te.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(lang,
             m_fullExtensionName, te);
         }
         assignmentType = (String)htmlParams.get(
            PSWorkFlowUtils.properties.getProperty(
               "HTML_PARAM_ASSIGNMENTTYPE",
               PSWorkFlowUtils.ASSIGNMENT_TYPE_CURRENT_USER)
            );

         if(assignmentType != null && assignmentType.equals(
            String.valueOf((PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN))))
         {
            localParams.m_isAdministrator = true;
         }

         localParams.m_transitionComment =
            PSWorkFlowUtils.getTransitionCommentFromHTMLParams(htmlParams);

         if (localParams.m_transitionComment != null
               && localParams.m_transitionComment.length() > 255)
         {
               
               throw new PSExtensionProcessingException(lang,
                     IPSExtensionErrors.WF_COMMENT_CANNOT_EXCEED_255);
         }

         adhocUserList = (String)
            htmlParams.get(IPSHtmlParameters.SYS_WF_ADHOC_USERLIST);

         localParams.m_adhocUserList = adhocUserList;

         wfRoleInfo = (PSWorkflowRoleInfo) request.getPrivateObject(
            PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT);

         if (null == wfRoleInfo)
         {
            throw new PSExtensionProcessingException(lang,
               m_fullExtensionName, new PSRoleException(lang,
                IPSExtensionErrors.ROLEINFO_OBJ_NULL));
         }
         localParams.m_wfRoleInfo = wfRoleInfo;

         actorRoleList = wfRoleInfo.getUserActingRoleNames();
         if (null != actorRoleList && !actorRoleList.isEmpty())
         {
            sRoleNameList = PSWorkFlowUtils.listToDelimitedString(
            actorRoleList,
            PSWorkFlowUtils.ROLE_DELIMITER);
         }

         // Check if this a check in or check out
         if (localParams.m_actionTrigger.equalsIgnoreCase(
            PSWorkFlowUtils.properties.getProperty("TRIGGER_CHECK_IN")))
         {
            localParams.m_isCheckinOrCheckout = true;
            localParams.m_isCheckin = true;
         }

         // Check if this a force check in
         if (localParams.m_actionTrigger.equalsIgnoreCase(
             PSWorkFlowUtils.properties.getProperty("TRIGGER_FORCE_CHECK_IN")))
         {
             localParams.m_isCheckinOrCheckout = true;
             localParams.m_isCheckin = true;
         }

         if (localParams.m_actionTrigger.equalsIgnoreCase(
            PSWorkFlowUtils.properties.getProperty("TRIGGER_CHECK_OUT")))
         {
            localParams.m_isCheckinOrCheckout = true;
         }
         if (localParams.m_isCheckinOrCheckout)
         {
            localParams.m_htmlRevision =
                  getRevisionFromHTMLParams(htmlParams);
         }

         //Get the connection
         try
         {
            connectionMgr = new PSConnectionMgr();
            connection = connectionMgr.getConnection();
         }
         catch(Exception e)
         {
            throw new PSExtensionProcessingException(
               m_fullExtensionName, e);
         }

         try
         {
            /* ***************** DO THE WORK *****************/
            performTransition(lang, connection, sRoleNameList, localParams,
                  request);

            /*
             * Create a workflow context object, and put it as a private object
             * in the request context.
             * The the current state of the content item will be the
             * "transition to" state if a transition has been  completed,
             * otherwise it will be the "from" state.
             */

            currentStateID =
                  (localParams.b_transitionPerformed) ?
                  localParams.m_transitionToStateID:
                  localParams.m_transitionFromStateID;

            wfContext = new PSWorkFlowContext(localParams.m_workflowAppID,
                                              localParams.m_contentID,
                                              localParams.m_contextRevision,
                                              localParams.m_transitionID,
                                              currentStateID);

            request.setPrivateObject(
               IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT,
               wfContext);

            if (IPSConstants.NO_CORRESPONDING_REVISION_VALUE !=
                localParams.m_htmlRevision)
            {
               setRevisionFromHTMLParams(htmlParams,
                                         localParams.m_htmlRevision);
            }

            /*
             * Make the new state id html parameter empty if no transition has
             * taken place.
             */
            if (localParams.b_transitionPerformed)
            {
               htmlParams.put(PSWorkFlowUtils.properties.getProperty(
                  "HTML_PARAM_NEWSTATEID",
                  PSWorkFlowUtils.DEFAULT_NEWSTATEID_NAME),
                              Integer.toString(currentStateID));
            }
            else
            {
               htmlParams.put(PSWorkFlowUtils.properties.getProperty(
                  "HTML_PARAM_NEWSTATEID",
                  PSWorkFlowUtils.DEFAULT_NEWSTATEID_NAME),
                              "");
            }

            htmlParams.put(PSWorkFlowUtils.properties.getProperty(
               "HTML_PARAM_CHECKOUTUSERNAME",
               PSWorkFlowUtils.CHECKOUT_USER_NAME),
                           localParams.m_checkedOutUser);

            htmlParams.put(PSWorkFlowUtils.properties.getProperty(
               "HTML_PARAM_CHECKOUTSTATUS",
               PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_DOCUMENT),
                           Integer.toString(localParams.m_checkoutStatus));
            nAssignmentType =
                  PSExitAddPossibleTransitionsEx.getAssignmentType(
                     localParams.m_workflowAppID,
                     localParams.m_contentID,
                     connection,
                     (localParams.m_transitionFromStateID ==
                      localParams.m_transitionToStateID) ?
                     localParams.m_transitionFromStateID:
                     localParams.m_transitionToStateID,
                     localParams.m_userName,
                     sRoleNameList,
                     request);
            nAssignmentType = PSWorkFlowUtils.modifyAssignmentType(nAssignmentType,
               localParams.m_itemCommunity,localParams.m_userCommunity);
            htmlParams.put(PSWorkFlowUtils.properties.getProperty(
               "HTML_PARAM_ASSIGNMENTTYPE",
               PSWorkFlowUtils.ASSIGNMENT_TYPE_CURRENT_USER),
                           Integer.toString(nAssignmentType));
         }
         catch(SQLException e)
         {
            except = e;
         }
         catch(PSCheckInCheckOutException e)
         {
            /*
             * checkinout action failed let others know that the action is not
             * really performed.
             */
            request.setPrivateObject(IPSConstants.WF_ACTION_PERFORMED,
               IPSConstants.BOOLEAN_FALSE);
               
            /*
             * Only treat this as an exception if the error isn't 
             * DOC_NOT_CHECKEDOUT. That error only indicates that we 
             * tried to checkin a doc what wasn't checked out, really
             * just a warning condition.
             */
            if (IPSExtensionErrors.DOC_NOT_CHECKEDOUT != e.getErrorCode())
            {
               except = e;
            }
         }
         catch(PSTransitionException e)
         {
            except = e;
         }
         catch(PSEntryNotFoundException e)
         {
            if (e.getLanguageString() == null)
               e.setLanguageString(lang);
            except = e;
         }
         catch(PSDuplicateApprovalException e)
         {
            except = e;
         }
         catch (PSRoleException e)
         {
            if (e.getLanguageString() == null)
               e.setLanguageString(lang);
            except = e;
         }
         catch (Exception e)
         {
            except = e;
         }
      }
      finally
      {
         try
         {
            if(null != connectionMgr)
            {
               connectionMgr.releaseConnection();
            }
         }
         catch(SQLException sqe)
         {
            // Ignore since this is cleanup
         }
         if (null != except)
         {
            PSWorkFlowUtils.printWorkflowException(request, except);
            if (except instanceof PSException)
            {
               String language = ((PSException)except).getLanguageString();
               if (language == null)
                  language = PSI18nUtils.DEFAULT_LANG;
               throw new PSExtensionProcessingException(language,
               m_fullExtensionName, except);
            }
            else
               throw new PSExtensionProcessingException(m_fullExtensionName,
                except);
         }
         PSWorkFlowUtils.printWorkflowMessage(request,
            "Perform Transition: exit preProcessRequest ");
      }

      /*
       * It is a real transition (not checkins or out) and failed for some
       * reason, let others know that the action is not really performed.
       */
      if(localParams.m_transitionID != IPSConstants.TRANSITIONID_CHECKINOUT &&
         !localParams.b_transitionPerformed)
      {
            request.setPrivateObject(IPSConstants.WF_ACTION_PERFORMED,
               IPSConstants.BOOLEAN_FALSE);
      }
      return;
   }

   /**
    * Does the work of performing the transition, delegates work to
    * for check in and checkout<CODE>checkInOut</CODE>.
    *
    * @param connection      data base connection
    * @param userRoleList    comma separated list of user's roles
    * @param localParams     the local parameters object
    *                        <ul>members used as input:
    *                        <li>m_request</li>
    *                        <li>m_transitionComment</li>
    *                        <ul>members modified:
    *                        <li>m_workflowAppID</li>
    *                        <li>m_contentID</li>
    *                        <li>m_transitionFromStateID</li>
    *                        <li>m_transitionToStateID</li>
    *                        <li>m_transitionID</li>
    *                        <li>m_checkedOutUser</li>
    *                        <li>m_checkedOut</li>
    *                        <li>m_checkoutStatus</li></ul>
    *                        <li>m_contextRevision</li>
    *                        <li>m_htmlRevision</li></ul>
    * @param request         current request, assumed not <code>null</code>.
    * 
    * @throws                SQLException if an SQL error occurs
    * @throws                PSCheckInCheckOutException the user is
    *                        not authorized to perform the checkin or checkout
    * @throws                PSTransitionException if an error occurs
    * @throws                PSEntryNotFoundException if a data base entry is
    *                        not found
    * @throws PSORMException 
    */
   private void performTransition(String lang, Connection connection,
                                  String userRoleList,
                                  Params localParams,
                                  IPSRequestContext request)
      throws SQLException, PSCheckInCheckOutException, PSTransitionException,
      PSEntryNotFoundException, PSDuplicateApprovalException,
      PSRoleException, PSORMException
   {
      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
         "  Entering Method performTransition");
   //   int  approvalsRequired = 0;
      List transitionRequiredRoles = null;
      PSTransitionsContext tc = null;
      List transitionActions  = null;
      PSContentStatusContext csc =
            new PSContentStatusContext(connection,
                                       localParams.m_contentID);

      try
      {
         String usercomm = (String)localParams.m_request.getSessionPrivateObject(
            IPSHtmlParameters.SYS_COMMUNITY);
         if(usercomm != null)
            localParams.m_userCommunity = Integer.parseInt(usercomm);
      }
      catch(Exception e)
      {
         localParams.m_userCommunity = -1;
      }
      localParams.m_itemCommunity = csc.getCommunityID();


      /**
       * [Vitaly: Oct 27 2003]: DO NOT compare the user community and
       * the item community. Communities were never designed to work
       * as a server security feature. Filtering by community, if desired, 
       * should be done at the action visibility level (already in place). Filtering here
       * makes it impossible to perform such relationships operation as
       * a Translation of an item with new copies going into another community.  
       * For more info see bug Rx-03-10-0057.
       */
      
      PSStateRolesContext toStateSrc = null;
      PSStateRolesContext fromStateSrc = null;
      PSContentAdhocUsersContext toStateCauc = null;
      PSContentAdhocUsersContext fromStateCauc = null;
      csc.close(); //release connection

      localParams.m_workflowAppID = csc.getWorkflowID();
      localParams.m_transitionFromStateID = csc.getContentStateID();
      localParams.m_checkedOutUser = csc.getContentCheckedOutUserName();

      if (null != localParams.m_checkedOutUser)
      {
         localParams.m_checkedOutUser = localParams.m_checkedOutUser.trim();
         localParams.m_checkedOut = (!localParams.m_checkedOutUser.equals(""));
      }
      else
      {
         localParams.m_checkedOut = false;
      }

      if(localParams.m_checkedOut)
      {
         if(localParams.m_userName.equalsIgnoreCase(
            localParams.m_checkedOutUser))
         {
            localParams.m_checkoutStatus =
                  PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER;
         }
         else
         {
            localParams.m_checkoutStatus =
                  PSWorkFlowUtils.CHECKOUT_STATUS_OTHER;
         }
      }

      if(localParams.m_isCheckinOrCheckout)
      {
         checkInOut(lang, csc, localParams.m_isCheckin, connection,
          localParams, request);
         PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
         "  Done with checkInOut. Exiting Method performTransition");
         return;
      }

      localParams.m_contextRevision = csc.getCurrentRevision();

      try
      {
         // if the actionTrigger is a numeric, it must be a transitionId
         // use a different constructor if that is the case
         boolean isId = true;
         int transitionId = -1;
         String tmp = localParams.m_actionTrigger;
         try
         {
            transitionId = Integer.parseInt(tmp);
         }
         catch (NumberFormatException e)
         {
            isId = false;
         }

         if (isId && transitionId != -1)
         {
            tc = new PSTransitionsContext(transitionId,
            localParams.m_workflowAppID, connection);
         }
         else
         {
           tc = new PSTransitionsContext(localParams.m_workflowAppID,
                                 connection,
                                 localParams.m_actionTrigger,
                                 localParams.m_transitionFromStateID);
         }
         tc.close();  //release JDBC objects
      }
      catch(PSEntryNotFoundException e)
      {
         Object[] args =
            {
               new Integer(localParams.m_contentID),
               new Integer(localParams.m_workflowAppID),
               new Integer(localParams.m_transitionFromStateID),
               localParams.m_actionTrigger
            };
         throw new PSTransitionException(lang,
            IPSExtensionErrors.MISSING_TRANSITION, args);
      }

      //The current stateid must match with from state id of the transition
      if(localParams.m_transitionFromStateID != tc.getTransitionFromStateID())
      {
         Object[] args =
            {
               new Integer(localParams.m_contentID),
               new Integer(localParams.m_workflowAppID),
               new Integer(localParams.m_transitionFromStateID),
               new Integer(tc.getTransitionFromStateID()),
               localParams.m_actionTrigger
            };
         throw new PSTransitionException(lang,
            IPSExtensionErrors.INVALID_TRANSITION, args);
      }

      /* Only an admin can transition an item that is checked out */
      if(localParams.m_checkedOut && !localParams.m_isAdministrator)
      {
         throw new PSTransitionException(lang,
          IPSExtensionErrors.ADMIN_CHECKOUT_ONLY);
      }

      localParams.m_transitionToStateID = tc.getTransitionToStateID();
   //   approvalsRequired = tc.getTransitionApprovalsRequired();
      localParams.m_transitionID = tc.getTransitionID();

      /**
       * Make transition specific checks that this user is authorized to
       * perform this transition. Only the aging agent may perform  aging
       * transitions.
       * Otherwise, the user must belong the transition-required role list, if
       * one exists.
       */
      if (tc.isAgingTransition())
      {
//           // check that it's  the aging agent
//           if (!tc.isAgingAgent(localParams.m_userName))
//           {
//              PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
//                 "  perform transition: only the Aging agent may perform " +
//                 "aging transitions. Transition ID = " +
//                 localParams.m_transitionID + ".");
//              return;
//           }
      }
      // Transition required roles are ignored for an administrator
      else if (!localParams.m_isAdministrator)
      {
         transitionRequiredRoles = tc.getTransitionRoles();
         if (null != transitionRequiredRoles &&
             transitionRequiredRoles.size() > 0)
         {
            if (!PSWorkFlowUtils.compareRoleList(transitionRequiredRoles,
                                                 userRoleList))
            {
               throw new PSTransitionException(lang,
                IPSExtensionErrors.INVALID_TRANSITION_ROLE);
            }
         }
      }

      /*
       * If a transition comment is required, make sure it is present. Note,
       * an aging transtion will never require a transition comment.
       */

      if (tc.isTransitionCommentRequired() &&
          null == localParams.m_transitionComment)
      {
               throw new PSTransitionException(lang,
                IPSExtensionErrors.TRANSITION_COMMENT_NOT_SPECIFIED);
      }

      toStateSrc =
            new PSStateRolesContext(localParams.m_workflowAppID,
                                    connection,
                                    localParams.m_transitionToStateID,
                                    PSWorkFlowUtils.ASSIGNMENT_TYPE_READER);

      /*
       * Check that any proposed adhoc users can be given an assignment.
       * Throw an exception if any proposed adhoc users have no home.
       */
      toStateCauc = PSWorkflowRoleInfoStatic.classifyAdhocUsers(
         localParams.m_contentID,
         localParams.m_adhocUserList,
         toStateSrc,
         localParams.m_request);

      /*
       * If the content item is checked out, and is being transitioned  to a
       * different state it must be must first be checked in.
       * This is only allowed for admins, and under ordinary
       * circumstances, this will only happen for aging transitions.
       */
      if(localParams.m_checkedOut &&
         tc.isTransitionToDifferentState())
      {
         checkInOut(lang, csc, true, connection, localParams, request);
      }
      Date now = new Date(new java.util.Date().getTime());

        /*
       * Save the 'from state' adhoc context info to the role info object,
       * then delete adhoc context from the data base, and update it with the
       * new 'to state' adhoc context info
       */
      fromStateSrc =
            new PSStateRolesContext(localParams.m_workflowAppID,
                                    connection,
                                    localParams.m_transitionFromStateID,
                                    PSWorkFlowUtils.ASSIGNMENT_TYPE_READER);



      localParams.b_transitionPerformed =
            processTransition(csc,
                              tc,
                              localParams.m_userName,
                              now,
                              localParams.m_request,
                              connection,
                              fromStateSrc.getStateRoleNameMap(),
                              localParams
                              );

      /* Exit if no transition was performed */
      if (!localParams.b_transitionPerformed)
      {
         PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
         "  No transition was performed. Exiting Method performTransition");
         return;
      }

      /*
       * If there are workflow transition actions put the list as a private
       * object in the request context.
       */
      transitionActions = tc.getTransitionActions();
      if (null != transitionActions)
      {
         localParams.m_request.setPrivateObject(
            IPSWorkflowAction.WORKFLOW_ACTIONS_PRIVATE_OBJECT,
            transitionActions);
      }

      fromStateCauc = new PSContentAdhocUsersContext(localParams.m_contentID,
                                                     connection);

      localParams.m_wfRoleInfo.setFromStateCauc(fromStateCauc);
      localParams.m_wfRoleInfo.setToStateCauc(toStateCauc);
      localParams.m_request.setPrivateObject(
         PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT,
         localParams.m_wfRoleInfo);

      if (null != fromStateCauc)
      {
         // clear the repository entries, but keep the in memory data to use for
         // notifications later on
         fromStateCauc.emptyAdhocUserEntries(connection, false);
      }

      if (null != toStateCauc)
      {
         toStateCauc.commit(connection);
      }

      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
         "  Exiting Method performTransition");
   }

   /**
    * Updates the checked out user name in the content status context object
    * if the user is  authorized to perform the checkin or checkout.
    *
    * @param csc           the content status context object for the content
    *                      item.<P>
    *                      the checked out user name will be updated if the
    *                      action is successful
    * @param connection    data base connection
    * @param isCheckin     <CODE>true</CODE> if this a check in
    *                      <CODE>false</CODE> if this a check out.
    * @param localParams   the local parameters object
    *                      <ul>members used as input:
    *                      <li>m_request</li>
    *                      <li>m_transitionID</li>
    *                      <li>m_actionTrigger</li>
    *                      <li>m_userName</li>
    *                      <li>m_checkedOutUser</li>
    *                      <li>m_checkedOut</li>
    *                      <li>m_checkoutStatus</li></ul>
    *                      <li>m_isAdministrator</li>
    *                      <ul>members modified:
    *                      <li>m_contextRevision</li>
    *                      <li>m_htmlRevision</li></ul>
    * @param request       current request, assumed not <code>null</code>.
    *
    * @throws              SQLException if an SQL error occurs
    * @throws              PSCheckInCheckOutException if the user is
    *                      not authorized to perform the checkin or checkout
    * @throws PSEntryNotFoundException if an expected data base entry does
    *    not exist.
    * @throws PSORMException 
    */
   private void checkInOut(String lang, PSContentStatusContext csc,
                           boolean isCheckin,
                           Connection connection,
                           Params localParams,
                           IPSRequestContext request)
      throws SQLException, PSCheckInCheckOutException, PSEntryNotFoundException, PSORMException
   {
      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
                                           "    Enter checkInOut");
      // Indicate this is check-in or check-out action, not a transition
      localParams.m_transitionID = IPSConstants.TRANSITIONID_CHECKINOUT;

      int editRevision = csc.getEditRevision();
      int tipRevision = csc.getTipRevision();
      int checkedoutRevision = 1;

      // Set the tip revision if it does not yet have a meaningful value
      if (IPSConstants.NO_CORRESPONDING_REVISION_VALUE ==
          tipRevision)
      {
         tipRevision = 1;
         csc.setTipRevision(1);
      }

      //Checkin action
      if(isCheckin)
      {
         // Make sure the user is eligible to check in the document
         if(!localParams.m_checkedOut) // It must be checked out
         {
            throw new PSCheckInCheckOutException(lang,
             IPSExtensionErrors.DOC_NOT_CHECKEDOUT);
         }

         // It must have an edit revision
         else if (IPSConstants.NO_CORRESPONDING_REVISION_VALUE == editRevision)
         {
            throw new PSCheckInCheckOutException(lang,
             IPSExtensionErrors.EDIT_REVISION_MISSING);
         }

         /*
          * Do not allow checkin if current user is not an administrator and is
          * not the person who has checked out the document.
          */
         else if (!localParams.m_isAdministrator &&
                  (PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER !=
                   localParams.m_checkoutStatus))
         {
            throw new PSCheckInCheckOutException(lang,
             IPSExtensionErrors.CHECKIN_NOT_ALLOWED);
         }

         /*
          * The user is eligible to check in the document. Update the
          * content status context.
          */
         csc.setContentCheckedOutUserName(""); // checked out user is now null

         /*
          * The edit revision becomes the current revision, the  workflow
          * context revision and the html parameter revision.
          */
         csc.setCurrentRevision(editRevision);
         localParams.m_contextRevision = editRevision;
         localParams.m_htmlRevision = editRevision;

         // Clear the edit revision
         csc.setEditRevision(IPSConstants.NO_CORRESPONDING_REVISION_VALUE);

         // Calculate aging data - do this each time we check-in in case a value
         // that the next aging time is based on was modified.
         Date now = new Date(new java.util.Date().getTime());
         updateAgingInformation(csc,
                                null,  // no transition context for checkin
                                now,
                                localParams.m_request,
                                connection);
      }

      //Checkout action
      else
      {
         /**
          * We do not allow to checkout an item that is in a public state. The
          * user must first transition the item to a non-public state and then
          * check it out.
          */
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         IPSStatesContext stateContext = cms.loadWorkflowState(csc.getWorkflowID(),
            csc.getContentStateID());
         if (stateContext.getIsValid())
            throw new PSCheckInCheckOutException(lang,
               IPSExtensionErrors.CHECKOUT_FROM_PUBLIC_STATE);

         // The default checkout revision is the tip revision
         int checkoutRequestRevision =
               (IPSConstants.NO_CORRESPONDING_REVISION_VALUE ==
                localParams.m_htmlRevision) ? tipRevision
               :localParams.m_htmlRevision;
         // if the document is already checked out ...
         if(localParams.m_checkedOut)
         {
            // It's an error if document is checked out by somebody else
            if (localParams.m_checkoutStatus !=
                PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER)
            {
               throw new PSCheckInCheckOutException(lang,
                IPSExtensionErrors.CHECKOUT_NOT_ALLOWED);
            }

            // You have checked it out, but it must be the edit revision
            else if (checkoutRequestRevision != editRevision )
            {
               throw new PSCheckInCheckOutException(lang,
                IPSExtensionErrors.CHECKOUT_REVISION_MISMATCH,
                 new Object[]{Integer.toString(checkoutRequestRevision),
                  Integer.toString(editRevision)});
            }
            else
            {
               /*
                * This revision is already checked out by you - coool!
                * Indicate there was no action, so content status history
                * will not be updated.
                */
               localParams.m_transitionID =
                     IPSConstants.TRANSITIONID_NO_ACTION_TAKEN;
               localParams.m_contextRevision = editRevision;
               localParams.m_htmlRevision = checkoutRequestRevision;
               PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
                  "    Document already checked out by current user.\n" +
                  "    Exiting Method performTransition");
               return;
            }

         }
         // Checkout revision must not exceed the tip revision
         else if (checkoutRequestRevision > tipRevision)
         {
            throw new PSCheckInCheckOutException(lang,
             IPSExtensionErrors.CHECKOUT_REVISION_LIMIT,
              new Object[]{Integer.toString(checkoutRequestRevision),
               Integer.toString(tipRevision)});
         }
         /*
          * The document is not checked out, no edit revision should exist
          * Log a warning to the trace file.
          */
         else if (IPSConstants.NO_CORRESPONDING_REVISION_VALUE != editRevision)
         {
            PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
               "    Warning: Although this document is not checked out it has "
               + "an edit revision. Content ID = " + localParams.m_contentID +
               " edit revision = " + editRevision);
         }

         /*
          * The user is eligible to check out the document. Update the
          * revision and other information in the content status context
          */
         csc.setContentCheckedOutUserName(localParams.m_userName);

         // Get the checkout same revision flag. 
         String checkoutSameRev = request
               .getParameter(IPSHtmlParameters.SYS_CHECKOUT_SAME_REVISION);
         boolean isCheckoutSameRev = IPSConstants.BOOLEAN_TRUE.equals(
               checkoutSameRev);
         
         /*
          * Don't check out a copy of the content if isCheckoutSameRev flag is on;
          */
         /*
          * Unless this is the first revision of the document, and the revision
          * lock is not set, the user will check out a copy of the document.
          * The revision number for this copy will be one more than the
          * current tip revision number.
          */

         if ((!isCheckoutSameRev) && 
               (checkoutRequestRevision != 1 || csc.isRevisionLocked()))
         {
            tipRevision += 1;
            checkedoutRevision = tipRevision;
            csc.setTipRevision(tipRevision);
         }
         csc.setEditRevision(checkedoutRevision);


         // The workflow context revision will be the base revision
         localParams.m_contextRevision = checkoutRequestRevision;

         // The html parameter revision will be the checked out revision
         localParams.m_htmlRevision = checkedoutRevision;
      }
      csc.commit(connection);
      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
                                           "    Exit checkInOut");
   }

   /**
    * @todo This method should really be private.
    * Checks whether the appropriate number of approvals have been provided,
    * and updates the content status and content approvals contexts if needed;
    * calls method to update aging related information.
    * This method has been given package scope for testing purposes, but should
    * not be invoked outside of this exit in production code.
    *
    * @param csc                      the content status context object for the
    *                                 content item.<P>
    *                                 if the  action is successful, the
    *                                 following members will be updated as
    *                                 needed: content state ID, last transition
    *                                 date, next aging transition, next aging
    *                                 transition date, state entered date, last
    *                                 repeated transition date. These changes
    *                                 are not committed here. Assumed not 
    *                                 <code>null</code>.
    * @param tc                       the data base context object for this
    *                                 transition.  Assumed not 
    *                                 <code>null</code>.
    * @param userName                 the user that will perform this
    *                                 transition or action including check in
    *                                 and check out, may be <code>null</code> or 
    *                                 empty.
    * @param now                      time at which the transition occurs, 
    *                                 assumed not <code>null</code>.
    * @param request                  the request context for this extension
    *                                 call (used for tracing), assumed not 
    *                                 <code>null</code>.
    * @param connection               the data base connection, assumed not 
    *                                 <code>null</code>.
    * @param roleIdNameMap            A map of role id as key and role name
    *                                 as value to be used in determining if this
    *                                 transition can proceed, assumed not 
    *                                 <code>null</code>.
    * @param localParams              The local parameters object, assumed not 
    *                                 <code>null</code>.
    *                                 
    * @return                         <CODE>true</CODE> if a transition was
    *                                 performed else <CODE>false</CODE>.
    *                                 
    * @throws                         SQLException if an SQL error occurs
    * @throws                         PSEntryNotFoundException if a data base
    *                                 entry is not found
    * @throws                         PSDuplicateApprovalException if an
    *                                 attempt is made to insert an approval for
    *                                 a user/state/workflow that already has
    *                                 one.
    * @throws PSORMException 
    */
   private static boolean processTransition(PSContentStatusContext csc,
                                    PSTransitionsContext tc,
                                    String userName,
                                    Date now,
                                    IPSRequestContext request,
                                    Connection connection,
                                    Map roleIdNameMap,
                                    Params localParams
                                    )
      throws SQLException,
             PSEntryNotFoundException,
             PSDuplicateApprovalException, PSORMException
   {
      PSWorkFlowUtils.printWorkflowMessage(request,
            "    Entering Method processTransition");

      int nApproved = 0;
      int workflowID = csc.getWorkflowID();
      int contentID = csc.getContentID();
      int transitionToStateID = tc.getTransitionToStateID();
      PSContentApprovalsContext cac = null;
      IPSStatesContext sc = null;
      boolean bHasActed = false;
      boolean eachRoleNeeded = false;
      boolean specifiedRolesOnly = false;
      int approvalsNeeded = tc.getTransitionApprovalsRequired();
      PSWorkflowRoleInfo workflowInfo = localParams.m_wfRoleInfo;

      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      // -1 = each role must approve before transitioning.
      if(approvalsNeeded < 0)
      {
         eachRoleNeeded = true;
      }

      // get all roles or specified roles:
      String tranRoleType = tc.getTransitionRoleColumnValue();

      // use the roles in the specified in the transition:
      if(tranRoleType != null && tranRoleType.
         equals(IPSTransitionsContext.SPECIFIED_ROLE_TRANSITION_RESTRICTION))
      {
         specifiedRolesOnly = true;
         roleIdNameMap = tc.getTransitionRoleNameIdMap();
      }

      /**
       * Determine if there are sufficient approvals to perform the transition.
       * Always create the approvals context. Some approvals may exist and need
       * to be cleared. They may exist because the number of  required
       * approvals may have changed, or they may be from a different
       * transition.
       */

      cac = new PSContentApprovalsContext(workflowID,
                                          connection,
                                          contentID,
                                          tc);
      if (!localParams.m_isAdministrator)
      {
         /*
          * Decrement the number of required approvals by the number of
          * existing approvals for this transition.
          */
         nApproved = cac.getApprovedUserCount();
         approvalsNeeded -= nApproved;
   
         /**
          * If more approvals are needed and the current user has already acted 
          * on this document, it is an error;
          * otherwise, decrement the required approvals by 1
          */
         bHasActed = cac.hasUserActed(userName);
         if (bHasActed)
         {
            throw new PSDuplicateApprovalException(lang,
             IPSExtensionErrors.TRANSITION_ATTEMPT, userName);
         }

         HashSet userRoles = new HashSet(workflowInfo.getUserActingRoleNames());

         // get the list of specified roles, these are the roles that need to
         // approve a transition if necessary, otherwise we'll use the state
         // roles
         if (specifiedRolesOnly)
         {
            // if the user contains no roles in specified role list toss them
            userRoles.retainAll(tc.getTransitionRoles());
            if (userRoles.isEmpty())
            {
               throw new PSEntryNotFoundException(
                  lang,
                  IPSExtensionErrors.INVALID_TRANSITION_ROLE);
            }
         }

         // check if role acted, we do this by taking the list of roles,
         // possibly intersected above, and for each one that we have already 
         // acted upon we remove, in the end if there are none left, we throw 
         // the error, otherwise we use that list to do the multiple approvals
         ArrayList keepRoles = new ArrayList();
         Iterator iter = userRoles.iterator();
         while (iter.hasNext())
         {
            String tmpRole = (String)iter.next();
            int roleId =
               PSWorkFlowUtils.getRoleIdFromMap(roleIdNameMap, tmpRole);
            
            // if the role has been found and it has not been acted upon keep it 
            if (roleId != -1 && !cac.hasRoleActed(roleId))
            {
               keepRoles.add(tmpRole);
            }
         }
         if (keepRoles.isEmpty())
         {
            throw new PSDuplicateApprovalException(
               lang,
               IPSExtensionErrors.TRANSITION_ATTEMPT,
               userRoles + " Role(s)");
         }
         // be sure to reset the userRoles set with only the ones that have
         // not been acted upon
         userRoles = new HashSet(keepRoles);
   
         // If there are not enough approvals, add the new one(s) and exit.
         if (eachRoleNeeded)
         {
            Integer roleId = null;
            Iterator it = roleIdNameMap.keySet().iterator();
            boolean doTransition = true;
            while (it.hasNext())
            {
               roleId = (Integer)it.next();
               // if this is one of the roles we are about to approve on 
               // skip it and continue checking the other roles
               boolean found = false;
               iter = userRoles.iterator();
               while (iter.hasNext())
               {
                  String tmpRole = (String)iter.next();
                  if (PSWorkFlowUtils.getRoleIdFromMap(roleIdNameMap, tmpRole)
                     == roleId.intValue())
                  {
                     found = true;
                     break;
                  }
               }
               if (found)
                  continue;

               if (!cac.roleIdList().contains(roleId))
               {
                  doTransition = false;
                  break;
               }
            }

            if (!doTransition)
            {
               addRoleApproval(
                  cac,
                  roleIdNameMap,
                  userRoles,
                  userName);

               return false; // no transition
            }
         }
   
         // this guy hasn't acted, but currently is so decrement.
         approvalsNeeded--;

         //  If there are not enough approvals, add the new one(s) and exit.
         if (approvalsNeeded > 0)
         {
            addRoleApproval(
               cac,
               roleIdNameMap,
               userRoles,
               userName);

            // after the role approvals above, recalculate the amount
            // of approvals still to be done
            int count =
               tc.getTransitionApprovalsRequired() - cac.getApprovedUserCount();

            PSWorkFlowUtils.printWorkflowMessage(
               request,
               "    " + count + " are still needed for this transition.");

            return false; // no transition
         }
      }
      
      /*
       * There are enough approvals. The transition bookkeeping - updates to
       * the content status table - must be done, and the approvals, if they
       * exist, must be deleted.
       */
      csc.setLastTransitionDate(now);
      csc.setContentStateID(transitionToStateID);

      /*
       * If the transition is to a valid (publishable) state, turn on the
       * revision lock, so the revision will be incremented when it is next
       * checked out.
       */
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      sc = cms.loadWorkflowState(workflowID,                             
                               transitionToStateID);

      if (sc.getIsValid())
      {
         csc.lockRevision();
      }
      /*
       * Update aging start dates, and next aging transition data in the
       * content status context
       */

      updateAgingInformation(csc,
                             tc,
                             now,
                             request,
                             connection);

      csc.commit(connection);

      // Empty any approvals that may exist for this content item
      cac.emptyApprovals();

      PSWorkFlowUtils.printWorkflowMessage(request,
            "    Exiting Method processTransition");

      return true;      // transition occurred
   }

   /**
    * Private helper method to add an approval for the first role assigned to
    * the specified user.  See RX-15731 for details.
    * @param cac The content approval context, used to actually set the state
    *    of the approval for each of the specified roles, assumed not <code>
    *    null</code>
    * @param roleIdNameMap A map of role id as key and role name as value to be 
    *    used in determining if this transition can proceed, assumed not <code>
    *    null</code>.
    * @param userRoles The list of roles to be approved on, assumed not <code>
    *    null</code>.
    * @param userName The specific user used to execute the transition, assumed
    *    not <code>null</code>. 
    * 
    * @throws SQLException if an SQL error occurs
    */
   @SuppressWarnings("unchecked")
   private static void addRoleApproval(
      PSContentApprovalsContext cac,
      Map roleIdNameMap,
      HashSet userRoles,
      String userName)
      throws SQLException
   {
      Iterator iter = userRoles.iterator();

      if (iter.hasNext())
      {
         String tmpRole = (String)iter.next();

         cac.addContentApproval(
            userName,
            PSWorkFlowUtils.getRoleIdFromMap(roleIdNameMap, tmpRole));
      }
   }

   /**
    * Updates aging start dates, and next aging transition data in the content
    * status context.
    * This method has been given package scope for testing purposes, but should
    * not be be invoked outside of this exit in production code.
    *
    * @param csc                      the content status context object for the
    *                                 content item. may not be
    *                                 <CODE>null </CODE><P>
    *                                 if the  action is successful, the
    *                                 following members will be updated as
    *                                 needed: next aging transition, next aging
    *                                 transition date, state entered date, last
    *                                 repeated transition date. These changes
    *                                 are not committed here.
    * @param tc                       the transition data base context object
    *                                 if this is a transition
    * @param now                      time at which the transition occurs
    *                                 <CODE>null</CODE> if this is a checkin.
    * @param request                  the request context for this extension
    *                                 call (used for tracing), may be
    *                                 <CODE>null </CODE> if no application
    *                                 tracing is required
    * @param connection               open data base connection,  may not be
    *                                 <CODE>null </CODE>
    * @throws                         SQLException if an SQL error occurs
    * @throws                         PSEntryNotFoundException if a data base
    *                                 entry is not found
    */
   static void updateAgingInformation(PSContentStatusContext csc,
                                      PSTransitionsContext tc,
                                      Date now,
                                      IPSRequestContext request,
                                      Connection connection)
      throws SQLException
   {
      PSWorkFlowUtils.printWorkflowMessage(request,
                 "      Entering Method updateAgingInformation");
      if (null == csc)
      {
          throw new IllegalArgumentException(
             "The content status context object may not be null.");
      }

      if (null == connection)
      {
          throw new IllegalArgumentException(
             "Connection may not be null.");
      }

      PSTransitionsContext candidateTC  = null;
      Calendar maximumAgingCal = null;
      Calendar minimumAgingCal = null;
      Calendar workingVarCal =  null;
      Date absoluteIntervalAgingStart = csc.getStateEnteredDate();
      Date repeatedIntervalAgingStart =
            csc.getRepeatedAgingTransitionStartDate();
      int nextAgingTransition = 0;
      String systemField = "";
      Date nextAgingDate = csc.getNextAgingDate();
      int agingStateID = 0;
      int previousAgingTransitionID = 0;
      int candidateTransitionID = 0;
      Date systemFieldDate = null;


      if (null != tc)
      {
         previousAgingTransitionID = tc.getTransitionID();
      }

      /*
       * Aging information is only recomputed for check-ins of items with no
       * previous aging computations, aging  transitions to a new state,
       * or for aging self transitions.
       */

      Date lastTransitionDate = csc.getLastTransitionDate();

      /*
       * For check-ins of items with no previous aging computation and
       * set all aging related dates.
       */
      if (null == tc)
      {
         /*
          * For a non-null last transition date, set all aging related dates
          * to the last transition date.
          */
         if (null != lastTransitionDate)
         {
            csc.setStateEnteredDate(lastTransitionDate);
            csc.setRepeatedAgingTransitionStartDate(lastTransitionDate);
            absoluteIntervalAgingStart =  lastTransitionDate;
            repeatedIntervalAgingStart = lastTransitionDate;
         }
         /*
          * For a null last transition date, which corresponds to an item
          * that has just been created, set all aging related dates
          * to the content created date.
          */
         else
         {
            Date stateEnteredDate = csc.getContentCreatedDate();
            csc.setStateEnteredDate(stateEnteredDate);
            csc.setRepeatedAgingTransitionStartDate(stateEnteredDate);
            absoluteIntervalAgingStart =  stateEnteredDate;
            repeatedIntervalAgingStart = stateEnteredDate;
            csc.setLastTransitionDate(stateEnteredDate);
         }
      }

      /*
       * For transitions to a new state set all aging related dates to the
       * current time.
       */
      else if (tc.isTransitionToDifferentState())
      {
         csc.setStateEnteredDate(now);
         csc.setRepeatedAgingTransitionStartDate(now);
         absoluteIntervalAgingStart =  now;
         repeatedIntervalAgingStart = now;
         /*
          * This date was already set if this is a transition. We set it anyway
          * to ensure that all current times are exactly the same.
          */
         csc.setLastTransitionDate(now);
      }

      /*
       * Don't recompute aging information for check ins where aging
       * computations have been done, non-aging self transitions,self
       * transitions for which we previously found that no next aging
       * transition existed for this state (null  nextAgingDate)
       * Also skip cases where aging has
       * never been computed, which should happen only  for content items
       * that were created before aging was implemented, and have not yet
       * undergone any transitions to a new state.
       */
      else if ((null == tc && !csc.neverAged()) ||
               // Cases below are all self-transitions
               !tc.isAgingTransition() ||
                csc.neverAged()  ||
               (null == nextAgingDate))
      {
         return;
      }
      /*
       * This is an aging self transition and the content item has had
       * aging data computed previously
       */
      else
      {
         /*
          * The next aging date must increase in time, in order to avoid
          * repeating the exact same aging transition.
          */
         minimumAgingCal =
               PSWorkFlowUtils.calendarFromDate(minimumAgingCal,
                                                nextAgingDate);
         /*
          * If this is a repeated interval aging transition,  the associated
          * start date must be incremented.
          */
         if (tc.getAgingType() ==
             IPSTransitionsContext.REPEATED_INTERVAL_AGING_TRANSITION )
         {
            if (null != nextAgingDate)
            {
               csc.setRepeatedAgingTransitionStartDate(nextAgingDate);
               repeatedIntervalAgingStart = nextAgingDate;
            }
         }
      }

      // Reset Next Aging info, and then recompute it
      nextAgingDate = null;
      csc.setNextAgingDate(null);
      csc.setNextAgingTransition(0);

      // Get the state for which aging must be computed
      if (null ==  tc)
      {
          agingStateID = csc.getContentStateID();
      }
      else
      {
         agingStateID = tc.getTransitionToStateID();
      }

      /*
       * Loop over all transitions from the new state to find the soonest
       * aging transition time.
       */
      try
      {
         candidateTC = new PSTransitionsContext(csc.getWorkflowID(),
                                               connection,
                                               agingStateID);

         do
         {
            if (!candidateTC.isAgingTransition())
            {
                continue;
            }

            candidateTransitionID = candidateTC.getTransitionID();
            systemFieldDate = null;

            /*
             * Find the candidate aging transition time, depending on
             * transition type.
             */
            switch (candidateTC.getAgingType())
            {
               case IPSTransitionsContext.ABSOLUTE_INTERVAL_AGING_TRANSITION:
                  /* absolute interval transitions should not repeat */
                  if (candidateTransitionID == previousAgingTransitionID)
                  {
                     workingVarCal = null;
                     continue;
                  }
                  workingVarCal = PSWorkFlowUtils.incrementCalendar(
                     workingVarCal,
                     absoluteIntervalAgingStart,
                     candidateTC.getAgingInterval());
                  break;

               case IPSTransitionsContext.REPEATED_INTERVAL_AGING_TRANSITION:
                  workingVarCal = PSWorkFlowUtils.incrementCalendar(
                     workingVarCal,
                     repeatedIntervalAgingStart,
                     candidateTC.getAgingInterval());
                  break;

               case IPSTransitionsContext.SYSTEM_FIELD_AGING_TRANSITION:
                  /* system field transitions should not repeat */
                  if (candidateTransitionID == previousAgingTransitionID)
                  {
                     workingVarCal = null;
                     continue;
                  }

                  systemField = candidateTC.getSystemField();
                  if (null == systemField ||
                     systemField.length() == 0)
                  {
                     workingVarCal = null;
                     PSWorkFlowUtils.printWorkflowMessage(
                        request,
                        "      Type of system field not specified for " +
                        "transition with ID " + candidateTransitionID);
                     break;
                  }
                  systemField = systemField.trim();
                  /*
                   * For system fields do a lookup based on the type.
                   * Currently only a few specific values are supported.
                   * TODO - make a more general algorithm that would do
                   * a database query based on the field name
                   */
                  if (systemField.equals("CONTENTSTARTDATE"))
                  {
                     systemFieldDate = csc.getContentStartDate();
                  }
                  else if  (systemField.equals("CONTENTEXPIRYDATE"))
                  {
                     systemFieldDate = csc.getContentExpiryDate();
                  }
                  else if  (systemField.equals("REMINDERDATE"))
                  {
                     systemFieldDate =  csc.getReminderDate();
                  }
                  else
                  {
                     // Not supported, log and ignore
                     workingVarCal = null;

                     PSWorkFlowUtils.printWorkflowMessage(request,
                           "      Aging by system field " + systemField +
                           " is not currently supported.");
                     break;
                  }

                  if (null == systemFieldDate)
                  {
                     PSWorkFlowUtils.printWorkflowMessage(
                        request,
                        "      Value of system field " + systemField +
                        " is null for transition with ID " +
                        candidateTransitionID);
                     break;
                  }
                  workingVarCal = PSWorkFlowUtils.calendarFromDate(
                     workingVarCal, systemFieldDate);
                  break;
            }


            /*
             * The computed transition time will become the candidate aging
             * transition time under the following circumstances:
             * 1) A time was computed
             * 2) It is later than the previous aging transition time, if one
             *    existed.
             * 3) It is earlier than the current candidate aging
             *   transition time
             */
            if (null != workingVarCal &&
                (null == minimumAgingCal ||
                 /*
                  * Check that workingVarCal is after minimumAgingCal,
                  * taking into account that dates in the database may have
                  * their seconds truncated.
                  * This is needed to keep transitions from repeating.
                  * TODO: We need to allow for different transitions that
                  * might have the same scheduled time. [See the next block
                  * comment for more details.]
                  * Note that users should be able to reasonably schedule
                  * aging transitions so that overlap does not occur.
                  */
                 PSWorkFlowUtils.timeDiffSecs(workingVarCal,
                                              minimumAgingCal) >= 59)  &&
                /*
                 * This is done to find the earliest possible transition to
                 * perform.
                 * TODO: take possible data base truncation of dates into
                 * account. Also deal with case where aging transitions
                 * of different times happen at the same time (within
                 * truncation limit.) Now we take them on a first come basis.
                 * Ideally we need to 1: take them in priority order:
                 * system field, absolute interval, repeated interval, and
                 * 2: not repeat the choice the second time through.
                 */
                (null == maximumAgingCal ||
                 workingVarCal.before(maximumAgingCal)))
            {
               maximumAgingCal = (Calendar)workingVarCal.clone();
               nextAgingTransition = candidateTC.getTransitionID();;
            }
         }
         while(candidateTC.moveNext()); //End loop over all candidatetransitions

         /*
          * If a candidate was found it becomes the next aging transition for
          * this content item.
          */
         if (null != maximumAgingCal)
         {
            csc.setNextAgingTransition(nextAgingTransition);
            csc.setNextAgingDate(
               PSWorkFlowUtils.sqlDateFromCalendar(maximumAgingCal));
         }
      }
      catch (PSEntryNotFoundException e)
      {
         /*
          * Ignore, this is the case of a terminal state with no transitions
          * or a workflow with no transitions at all.
          */
      }
      finally
      {
         if (candidateTC != null)
            candidateTC.close();
      }

      PSWorkFlowUtils.printWorkflowMessage(request,
               "      Exiting Method updateAgingInformation");
   }

   /**
    * Gets the revision for checkout or checkin from the HTML Parameter
    * hash map.
    *
    * @param   htmlParams hash map containing the HTML parameters for this
    *          request
    *
    * @return  <li>for transitions, or if value is not set - 0 </li>
    *          <li>for checkin - revision being checked in (optional) - </li>
    *          <li>checkout - revision requested for check out</li>
    *          </ul>
    */
   private static int getRevisionFromHTMLParams(HashMap htmlParams)
   {
      int revision = IPSConstants.NO_CORRESPONDING_REVISION_VALUE;
      String sRevision = (String)
            htmlParams.get(IPSHtmlParameters.SYS_REVISION);

      if (null != sRevision)
      {
         revision = Integer.parseInt(sRevision);
      }

      return revision;
   }

   /**
    * Sets the revision HTML Parameter in a hash map; used to set the revision
    * actually checked out, which will differ from the revision requested for
    * checkout if copying is required.
    *
    * @param   htmlParams hash map containing the HTML parameters for this
    *          request
    *
    * @param   revision  <ul> revision HTML Parameter after processing
    *          checkin/checkout value
    *           <li>checkout - revision actually checked out: either 1, or a
    *              new revision with a content item that must be copied from
    *              the item with the revision requested.</li>
    *           <li>for transitions - 0 - do nothing </li>
    *           <li>for checkin - revision checked in</li></ul>
    */
   private static void setRevisionFromHTMLParams(HashMap htmlParams,
                                                 int revision)
   {
      if (0 == revision )
      {
         return;
      }
      String sRevision = String.valueOf(revision);
      htmlParams.put(IPSHtmlParameters.SYS_REVISION,
                     sRevision);
   }
}
