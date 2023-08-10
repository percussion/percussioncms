/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.workflow;

import com.github.javafaker.Bool;
import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.server.PSFolderSecurityManager;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.*;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.system.PSAssignmentTypeHelper;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public class PSExitAuthenticateUser implements IPSRequestPreProcessor
{
   Logger log = LogManager.getLogger(IPSConstants.WORKFLOW_LOG);

   /**
    * This is an inner class to encapsulate the parameters. We cannot keep
    * these as class variables due to threading issues. We instantiate this
    * object in the main processrequest method (called by server) and pass
    * around the methods. This is meant for convenience only.
    */
   private static class AuthParams
   {
      public int m_workflowAppID = 0;
      public boolean m_workflowIdSupplied = false;
      public boolean m_isNewItem = false;
      public int m_contentID = 0;
      public String m_userName = null;
      public String m_roleNameList = null;
      public String m_actionTrigger = null;
      public String m_checkedOutUser = null;
      public String m_checkInOutCondition = null;
      public boolean m_checkedOut = true;
      public int m_checkoutStatus = PSWorkFlowUtils.CHECKOUT_STATUS_NONE;
      public int m_requiredAccessLevel = PSWorkFlowUtils.ASSIGNMENT_TYPE_READER;
      public int m_assignmentType =
         PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW;
      public IPSRequestContext m_request = null;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   /**
    * This is used as a flag to indicate that the class hasn't been initialized
    * yet. There are certain cases where init can be called more than once
    * on the same loaded instance of a class.
    */
   static private int ms_correctParamCount = IPSExtension.NOT_INITIALIZED;

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      if (ms_correctParamCount == IPSExtension.NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;
         Iterator iter = extensionDef.getRuntimeParameterNames();
         while (iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }

         m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request,
         "\nAuthenticate User enter preProcessRequest... ");

      String lang =
         (String) request.getSessionPrivateObject(
            PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;

      PSConnectionMgr connectionMgr = null;
      try
      {
         Map<String,Object> htmlParams = request.getParameters();
         if (null == htmlParams)
         {
            htmlParams = new HashMap<>();
            request.setParameters(htmlParams);
         }

         if (null == params)
            return; //no parameters exit with peace!

         int nParamCount = params.length;

         int requiredAccessLevel = PSWorkFlowUtils.ASSIGNMENT_TYPE_READER;

         AuthParams localParams = new AuthParams();
         localParams.m_request = request;

         try
         {
            if (ms_correctParamCount != nParamCount)
            {
               String[] exParams =
                  {
                     String.valueOf(ms_correctParamCount),
                     String.valueOf(nParamCount)};

               throw new PSInvalidNumberOfParametersException(
                  lang,
                  IPSExtensionErrors.INVALID_PARAM_NUM,
                  exParams);
            }

            // Get contentid
            if (null == params[0] || 0 == params[0].toString().trim().length())
            {
               localParams.m_isNewItem = true;
            }
            else
            {
               localParams.m_contentID = Integer.parseInt(params[0].toString());
            }

            // Get username
            if (null == params[1] || 0 == params[1].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(
                  lang,
                  IPSExtensionErrors.EMPTY_USRNAME1);
            }

            localParams.m_userName = params[1].toString();

            if (0 == localParams.m_userName.length())
            {
               throw new PSInvalidParameterTypeException(
                  lang,
                  IPSExtensionErrors.EMPTY_USRNAME2);
            }
            // Get role list
            if (null == params[2] || 0 == params[2].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(
                  lang,
                  IPSExtensionErrors.EMPTY_ROLE_LIST,
                  localParams.m_userName);
            }
            localParams.m_roleNameList = params[2].toString();

            // Get CheckInOutCondition
            if (null != params[3])
            {
               localParams.m_checkInOutCondition = params[3].toString().trim();
               if (0 == localParams.m_checkInOutCondition.length())
               {
                  localParams.m_checkInOutCondition = null;
               }
            }
            else
            {
               localParams.m_checkInOutCondition = null;
            }

            // Get action trigger
            if (htmlParams
               .containsKey(PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME))
            {
               localParams.m_actionTrigger =
                  (String) htmlParams.get(
                     PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME);
            }

            /*
             *  Get required access level. If no parameter is specified, the
             *  previously specified default (READER) is used.
             */
            if (null != params[4] && 0 != params[4].toString().trim().length())
            {
               try
               {
                  requiredAccessLevel =
                          new Integer(params[4].toString());
               }
               catch (Exception e)
               {
                  requiredAccessLevel = PSWorkFlowUtils.ASSIGNMENT_TYPE_READER;
               }
            }

            if (PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN < requiredAccessLevel)
            {
               requiredAccessLevel = PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN;
            }
            else if (
               PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW
                  > requiredAccessLevel)
            {
               requiredAccessLevel =
                  PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW;
            }

            localParams.m_requiredAccessLevel = requiredAccessLevel;

            /* Get the optional workflow id.  If supplied, will check to
             * see if user has access to the initial state of that workflow
             */
            if (null != params[5] && 0 != params[5].toString().trim().length())
            {
               try
               {
                  //The parameter passed in seems to be 1 not the actual
                  //workflowid of the page.  So get the workflow from the request instead.
                  localParams.m_workflowAppID =
                          Integer.parseInt(
                                  request.getParameter("sys_workflowid"));
                  localParams.m_workflowIdSupplied = true;
               }
               catch (Exception e)
               {
                  String language = PSI18nUtils.DEFAULT_LANG;
                  log.error(PSExceptionUtils.getMessageForLog(e));
                  throw new PSInvalidParameterTypeException(
                     language,
                     IPSExtensionErrors.INVALID_WORKFLOWID,
                     params[5].toString());
               }
            }
         }
         catch (PSInvalidNumberOfParametersException | PSInvalidParameterTypeException ne)
         {
            log.error(PSExceptionUtils.getMessageForLog(ne));
            String language = ne.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(
               language,
               m_fullExtensionName,
               ne);
         }

         /*
          * if a new item and we're not validating the initial state, we're
          * done
          */
         if (localParams.m_isNewItem && !localParams.m_workflowIdSupplied)
         {
            return;
         }

         Connection connection;
         //Get the connection
         try
         {
            connectionMgr = new PSConnectionMgr();
            connection = connectionMgr.getConnection();
         }
         catch (Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            throw new PSExtensionProcessingException(m_fullExtensionName, e);
         }

         try
         {
            authenticateUser(lang, connection, localParams);

            //add all required HTML Params to the list
            request.setParameter(
               PSWorkFlowUtils.properties.getProperty(
                  "HTML_PARAM_CHECKOUTUSERNAME",
                  PSWorkFlowUtils.CHECKOUT_USER_NAME),
               localParams.m_checkedOutUser);

            request.setParameter(
               PSWorkFlowUtils.properties.getProperty(
                  "HTML_PARAM_CHECKOUTSTATUS",
                  PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_DOCUMENT),
               Integer.toString(localParams.m_checkoutStatus));

            request.setParameter(
               PSWorkFlowUtils.properties.getProperty(
                  "HTML_PARAM_ASSIGNMENTTYPE",
                  PSWorkFlowUtils.ASSIGNMENT_TYPE_CURRENT_USER),
               Integer.toString(localParams.m_assignmentType));
         }
         catch (Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            PSWorkFlowUtils.printWorkflowException(request, e);

            String language = null;
            if (e instanceof PSException)
               language = ((PSException) e).getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(
               language,
               m_fullExtensionName,
               e);
         }
      }
      finally
      {
         try
         {
            if (null != connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch (SQLException sqe)
         {
            // Ignore since this is cleanup
         }
         PSWorkFlowUtils.printWorkflowMessage(
            request,
            "Authenticate User : exit preProcessRequest ");
      }
   }

   /**
    * Does the work of authenticating the user.
    *
    * @param connection              open connection to back-end database
    *                                May not be <CODE>null</CODE>
    * @param localParams             the local parameters object
    *                                <ul>members used as input:
    *                                <li>m_request</li>
    *                                <li>m_isNewItem</li>
    *                                <li>m_contentID</li>
    *                                <li>m_userName</li>
    *                                <li>m_roleNameList</li>
    *                                <li>m_checkInOutCondition</li>
    *                                <li>m_requiredAccessLevel</li>
    *                                <li>m_assignmentType</li></ul>
    *                                <ul>members modified:
    *                                <li>m_assignmentType</li>
    *                                <li>m_checkedOutUser</li>
    *                                <li>m_checkedOut</li>
    *                                <li>m_checkoutStatus</li>
    *                                <li>m_request</li></ul>
   
    * @throws                        SQLException if an error occurs
    * @throws                        PSAuthorizationException if an
    *                                authorization error occurs
    *
    * @throws                        PSEntryNotFoundException if a database
    *                                record is not found
    * @throws                        PSRoleException if any role-related error
    *                                occurs
    */
   private void authenticateUser(
      String lang,
      Connection connection,
      AuthParams localParams)
           throws
           SQLException,
           PSAuthorizationException,
           PSEntryNotFoundException,
           PSRoleException, PSCmsException {

      PSWorkFlowUtils.printWorkflowMessage(
         localParams.m_request,
         "  Entering authenticateUser");

      PSContentStatusContext csc;
      int contentID = localParams.m_contentID;
      String userName = localParams.m_userName;
      String roleNameList = localParams.m_roleNameList;
      String checkInOutCondition = localParams.m_checkInOutCondition;
      int requiredAccessLevel = localParams.m_requiredAccessLevel;
      int assignmentType = localParams.m_assignmentType;
      List<Integer> actorRoles;
      List<String> actorRoleNames = new ArrayList<>();
      IWorkflowRoleInfo wfRoleInfo = new PSWorkflowRoleInfo();

      if (localParams.m_isNewItem)
      {
         // validate user is able to create doc in initial state
         if (!canUserCreate(connection, localParams))
         {
            throw new PSAuthorizationException(
               lang,
               IPSExtensionErrors.ILLEGAL_CONTENTTYPE,
               null);
         }

         // if a new item, we're all done
         PSWorkFlowUtils.printWorkflowMessage(
            localParams.m_request,
            "  New Item. Exiting authenticateUser");
         return;
      }

      try
      {
         csc = new PSContentStatusContext(connection, contentID);
      }
      catch (PSEntryNotFoundException e)
      {
         PSWorkFlowUtils.printWorkflowMessage(
            localParams.m_request,
            "  No entry for this content. Exiting authenticateUser");
         return; //no entry for this content so proceed to transition
      }
      csc.close(); //release the JDBC resources

      String command =
         localParams.m_request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (command == null)
      {
         command = "";
      }

      /*
       * [Vitaly: Oct 27 2003]: DO NOT compare the user community and
       * the item community. Communities were never designed to work
       * as a server security feature. Filtering by community, if desired, 
       * should be done at the action visibility level (already in place). Filtering here
       * makes it impossible to perform such relationships operation as
       * a Translation of an item with new copies going into another community.  
       * For more info see bug Rx-03-10-0057.
       */
      
      PSCmsObject cmsObject =
         PSServer.getCmsObjectRequired(csc.getObjectType());

      if (csc.getObjectType() == PSCmsObject.TYPE_FOLDER)
      {
         if (!PSFolderSecurityManager
            .verifyFolderPermissions(
               contentID,
               PSObjectPermissions.ACCESS_READ))
         {
            throw new PSAuthorizationException(
               IPSExtensionErrors.AUTHENTICATION_FAILED2,
               null);
         }
         return;
      }
      if (!cmsObject.isWorkflowable())
      { // There is no way to authenticate the user if not workflowable.
         // For example folder is not workflowable. This needs to be updated
         // after folder permission has been implemented.
         return;
      }

      int nWorkFlowAppID = csc.getWorkflowID();

      // Determine the checkout status and checkedout user

      String checkedOutUser = csc.getContentCheckedOutUserName();
      int checkoutstatus = PSWorkFlowUtils.CHECKOUT_STATUS_NONE;
      if (null == checkedOutUser || checkedOutUser.trim().length() < 1)
      {
         checkedOutUser = null;
      }
      else
      {
         checkedOutUser = checkedOutUser.trim();
         if (userName.equalsIgnoreCase(checkedOutUser))
         {
            checkoutstatus = PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER;
         }
         else
         {
            checkoutstatus = PSWorkFlowUtils.CHECKOUT_STATUS_OTHER;
         }
      }

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      localParams.m_checkoutStatus = checkoutstatus;
      localParams.m_checkedOutUser = checkedOutUser;


      // first check folder security
      if (!PSCms.canReadInFolders(contentID))
      {
         throw new PSAuthorizationException(
            lang,
            IPSExtensionErrors.AUTHENTICATION_FAILED2,
            null);
      }      
      
      // Check whether the user is Workflow admin

      boolean isAdmin = false;
      boolean isInternalUser = false;
      IPSWorkflowAppsContext wac;
      String sAdminName;
      

      if (userName.equals(IPSConstants.INTERNAL_USER_NAME))
      {
         isInternalUser = true;
         sAdminName = userName;
      }else{
         wac = cms.loadWorkflowAppContext(nWorkFlowAppID);
         sAdminName = wac.getWorkFlowAdministrator();
      }

      if (isInternalUser
         || PSWorkFlowUtils.isAdmin(sAdminName, userName, roleNameList))
      {
         isAdmin = true;
         localParams.m_assignmentType = PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN;
         actorRoleNames.add(sAdminName);
         wfRoleInfo.setUserActingRoleNames(actorRoleNames);
         localParams.m_request.setPrivateObject(
            PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT,
            wfRoleInfo);
      }

      //If the user is Workflow admin, there is no more to do
      if (isAdmin)
      {
         PSWorkFlowUtils.printWorkflowMessage(
            localParams.m_request,
            "  User is Admin, done. \n  Exiting authenticateUser");
         return;
      }

      /*
       * Validate the CheckInOutCondition if required.
       * If the condition is CHECKINOUT_CONDITION_CHECKIN the document must
       * currently be checked in.
       * If the condition is CHECKINOUT_CONDITION_CHECKOUT the document must
       * currently be checked out by the user, or the user must be an admin.
       */

      if (null != checkInOutCondition
         && !checkInOutCondition.equalsIgnoreCase(
            PSWorkFlowUtils.CHECKINOUT_CONDITION_IGNORE))
      {
         //
         // it's not checked out or
         // Someone else has it checked out
         if (checkInOutCondition
            .equalsIgnoreCase(PSWorkFlowUtils.CHECKINOUT_CONDITION_CHECKIN)
            && null != checkedOutUser)
         {
            throw new PSAuthorizationException(
               lang,
               IPSExtensionErrors.ILLEGAL_IF_CHECKEDOUT,
               null);
         }
         else if (
            checkInOutCondition.equalsIgnoreCase(
               PSWorkFlowUtils.CHECKINOUT_CONDITION_CHECKOUT)
               && (!userName
                    .equalsIgnoreCase(
                            checkedOutUser)))
         {
            // Checkout overridden by administrator
            if (localParams.m_actionTrigger != null
               && localParams.m_actionTrigger.equalsIgnoreCase(
                  PSWorkFlowUtils.properties.getProperty(
                     PSWorkFlowUtils.TRIGGER_CHECK_IN)))
            {
               throw new PSAuthorizationException(
                  lang,
                  IPSExtensionErrors.ILLEGAL_IF_CHECKEDOUT_OVERRIDE,
                  null);
            }
            else
            {
               // Not checked out, may have been overridden by administrator
               throw new PSAuthorizationException(
                  lang,
                  IPSExtensionErrors.ILLEGAL_IFNOT_CHECKEDOUT,
                  null);
            }
         }
      }

      PSStateRolesContext src;

      try
      {
         src =
            new PSStateRolesContext(
               nWorkFlowAppID,
               connection,
               csc.getContentStateID(),
               requiredAccessLevel);
      }

      catch (PSRoleException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));

         String language = e.getLanguageString();
         if (language == null)
            language = PSI18nUtils.DEFAULT_LANG;
         throw new PSAuthorizationException(
            language,
            IPSExtensionErrors.ROLE_ERROR_STATEID_WORKFLOWID,
            new Object[] {
               Integer.toString(csc.getContentStateID()),
               Integer.toString(nWorkFlowAppID),
               e.toString()});
      }

      catch (PSEntryNotFoundException e)
      {
         String language = e.getLanguageString();
         if (language == null)
            language = PSI18nUtils.DEFAULT_LANG;
         throw new PSAuthorizationException(
            lang,
            IPSExtensionErrors.ROLES_NOT_ASSIGNED,
            new Object[] {
               Integer.toString(requiredAccessLevel),
               Integer.toString(csc.getContentStateID()),
               Integer.toString(nWorkFlowAppID)});
      }

      actorRoles =
              PSWorkflowRoleInfoStatic.getActorRoles(
            contentID,
            src,
            userName,
            roleNameList,
            connection,
            true);

      if (null == actorRoles || actorRoles.isEmpty())
      {
         throw new PSAuthorizationException(
            lang,
            IPSExtensionErrors.AUTHENTICATION_FAILED1,
            null);
      }
      assignmentType = PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoles);

      actorRoleNames =
              PSWorkflowRoleInfoStatic.roleIDListToRoleNameList(actorRoles, src);

      wfRoleInfo.setUserActingRoleNames(actorRoleNames);
      localParams.m_request.setPrivateObject(
         PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT,
         wfRoleInfo);

      if (PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE == assignmentType)
      {
         throw new PSAuthorizationException(
            lang,
            IPSExtensionErrors.AUTHENTICATION_FAILED2,
            null);
      }
      localParams.m_assignmentType = assignmentType;
      PSWorkFlowUtils.printWorkflowMessage(
         localParams.m_request,
         "  Exiting authenticateUser");
   }

   /**
    * Determines if the user is able to access an item once it is created and
    * entered into the initial state of its workflow.
    *
    * @param connection The connection to use for queries, assumed not <code>
    * null</code>.
    * @param localParams The local param context containing the current state
    * of this exit, assumed not <code>null</code> and that all required values
    * have been set.
    *
    * @return <code>true</code> if one of the user role's has the required
    * access level for editing the document in the initial state, <code>false
    * </code> if not.
    *
    * @throws SQLException if there are any errors retrieving backend data.
    * @throws PSEntryNotFoundException if there is no state information found.
    */
   private boolean canUserCreate(Connection connection, AuthParams localParams)
      throws SQLException, PSEntryNotFoundException, PSRoleException, 
      PSAuthorizationException, PSCmsException
   {
      log.debug("Entering canUserCreate...");

      boolean canCreate = false;
      
      //See if the target folder for creating the item is present
      String folderid = localParams.m_request.getParameter(
        IPSHtmlParameters.SYS_FOLDERID, null);
      if (StringUtils.isNotEmpty(folderid))
      {
         log.debug("sys_folderid={}",folderid);
         log.debug("Checking if user {} can write to Folders...",localParams.m_userName);
          if(!PSCms.canWriteToFolders(localParams.m_request))
          {
              //User must have write access
              throw new PSAuthorizationException(
              IPSCmsErrors.FOLDER_PERMISSION_DENIED, new String[]{});
          }
      }

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSWorkflowAppsContext wcxt = 
         cms.loadWorkflowAppContext(localParams.m_workflowAppID);

      // build list of roles that can access the doc in the initial state
      List<String> stateRoleList = new ArrayList<>();
      PSStateRolesContext src;

      src =
         new PSStateRolesContext(
            localParams.m_workflowAppID,
            connection,
            wcxt.getWorkFlowInitialStateID(),
            localParams.m_requiredAccessLevel);

      stateRoleList = src.getStateRoleNames();
      
      String strCommId = (String) localParams.m_request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_COMMUNITY);
      int commId = Integer.parseInt(strCommId);

      Boolean communitiesEnabled = Boolean.parseBoolean(
              PSServer.getServerProps().getProperty(
                      IPSConstants.SERVER_PROP_COMMUNITIES_ENABLED,"no"));

      //Only filter by community if communities are enabled.
      if(communitiesEnabled) {
         // now filter roles by community
         PSAssignmentTypeHelper.filterAssignedRolesByCommunity(commId,
                 localParams.m_workflowAppID, stateRoleList);
      }
      // now see if user has one of those roles
      canCreate =
         PSWorkFlowUtils.compareRoleList(
            stateRoleList,
            localParams.m_roleNameList);
      PSWorkFlowUtils.printWorkflowMessage(
         localParams.m_request,
         "    Exiting canUserCreate");
      return canCreate;
   }
}
