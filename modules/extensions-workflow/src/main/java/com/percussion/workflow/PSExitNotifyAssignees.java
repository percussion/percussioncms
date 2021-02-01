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
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSRoleManager;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.jexl.PSExtensionWrapper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.utils.jexl.PSJexlUtils;
import com.percussion.services.utils.jexl.PSVelocityUtils;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.IPSTemplateDictionary;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.PSMailException;
import com.percussion.workflow.mail.PSMailMessageContext;
import com.percussion.workflow.model.PSMessagePackage;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.w3c.dom.Document;

import javax.jcr.RepositoryException;
import javax.naming.NamingException;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This exit sends mail notifications to the assigned roles for the new state
 * after transition.
 */
public class PSExitNotifyAssignees implements IPSResultDocumentProcessor
{
   private static final Logger m_log = Logger.getLogger(PSExitNotifyAssignees.class.getName());

   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   /**
    *  Set the parameter count to not initialized
    */
   static private int ms_correctParamCount = IPSExtension.NOT_INITIALIZED;

   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
           throws PSExtensionException
   {
      if (ms_correctParamCount == IPSExtension.NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;

         Iterator<?> iter = extensionDef.getRuntimeParameterNames();
         while(iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }
         m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

   /**************  IPSExtension Interface Implementation ************* */
   @SuppressWarnings({"deprecation", "unchecked"})
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request, Document resDoc)
           throws PSParameterMismatchException,
           PSExtensionProcessingException
   {
      Logger l = LogManager.getLogger(getClass());
      PSWorkFlowUtils.printWorkflowMessage(request,
              "\nNotify Assignees: enter processResultDocument ");

      int transitionID = 0;
      int toStateID = 0;
      int fromStateID = 0;
      int contentID = 0;
      int workflowID = 0;
      int revisionID = 0;

      int nParamCount = 0;
      String userName = null;
      HashMap<Object,Object> htmlParams = null;
      PSWorkFlowContext wfContext = null;
      PSTransitionsContext tc = null;
      PSConnectionMgr connectionMgr = null;
      PSWorkflowRoleInfo wfRoleInfo = null;
      Exception except = null;
      String contentURL = null;
      String lang = null;
      try
      {
         if(null == request)
         {
            throw new PSExtensionProcessingException(
                    m_fullExtensionName,
                    new IllegalArgumentException("The request must not be null"));
         }
         htmlParams = request.getParameters();

         if(null == params || null == htmlParams)
         {
            return resDoc; //no parameters, exit with peace!
         }

         lang = (String)request.getSessionPrivateObject(
                 PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (lang == null)
            lang =   PSI18nUtils.DEFAULT_LANG;

         nParamCount = params.length;
         wfContext = (PSWorkFlowContext) request.getPrivateObject
                 (IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT);

         if (null == wfContext)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
                    "Notify assignees: - no transition was performed - " +
                            "no notifications will be sent");
            return resDoc;
         }

         revisionID = wfContext.getBaseRevisionNum();

         transitionID = wfContext.getTransitionID();
         if (IPSConstants.TRANSITIONID_NO_ACTION_TAKEN == transitionID ||
                 IPSConstants.TRANSITIONID_CHECKINOUT == transitionID)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
                    "Notify assignees: - no transition was performed - " +
                            "no notifications will be sent");
            return resDoc; //no action at all - no history
         }
         workflowID = wfContext.getWorkflowID();
         toStateID = wfContext.getStateID();

         try
         {
            if(ms_correctParamCount != nParamCount)
            {
               String[] exParams =
                       {Integer.toString(ms_correctParamCount),
                               String.valueOf(nParamCount)};
               throw new PSInvalidNumberOfParametersException(lang,
                       IPSExtensionErrors.INVALID_PARAM_NUM, exParams);
            }

            // Note: we could get content id from workflow context
            if(null == params[0] ||
                    0 == params[0].toString().trim().length())
            {
               return resDoc; //no content id means no notifications!
            }

            contentID = new Integer(params[0].toString()).intValue();
            if(0 == contentID)
            {
               return resDoc; //no content id means no notifications!
            }

            // Detects if percNavon.html is in request along with a rxworkflow.properties property.
            // Alternatively, something like PSNavUtil.isNavonItem could be used.
            boolean shouldSkipForNavons = false;
            String shouldSkipVal = PSWorkFlowUtils.getProperty("BLOCKNOTIFICATIONSFORNAVONS");
            if ("true".equals(shouldSkipVal) || "yes".equals(shouldSkipVal)) {
               shouldSkipForNavons = true;
            }
            if (PSWorkFlowUtils.PERCNAVON.equals(request.getRequestPage()) && shouldSkipForNavons) {
               m_log.debug("Detected navon page.  Skipping notification for item with content id: " + contentID);
               return resDoc; // no need to send notification for navOn item.
            }

            if(null == params[1] ||
                    0 == params[1].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(lang,
                       IPSExtensionErrors.EMPTY_USRNAME1);
            }

            userName = params[1].toString();
         }
         catch (PSInvalidNumberOfParametersException ne)
         {
            l.warn("Error while sending notification with user " +
                    userName + " and contentid " + contentID, ne);
            String language = ne.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(language,
                    m_fullExtensionName, ne);
         }
         catch (PSInvalidParameterTypeException te)
         {
            l.warn("Error while sending notification with user " +
                    userName + " and contentid " + contentID, te);
            String language = te.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(language,
                    m_fullExtensionName, te);
         }

         //Get the connection
         Connection connection = null;
         try
         {
            connectionMgr = new PSConnectionMgr();
            connection = connectionMgr.getConnection();
         }
         catch(Exception e)
         {
            l.warn("SQL Exception while sending notification " +
                    "Error while sending notification with user " +
                    userName + " and contentid " + contentID, e);
            throw new PSExtensionProcessingException(
                    m_fullExtensionName, e);
         }

         try
         {
            tc = new PSTransitionsContext(transitionID,
                    workflowID,
                    connection);
            tc.close();
            fromStateID = tc.getTransitionFromStateID();
         }

         catch(PSEntryNotFoundException e)
         {
            l.warn("Error while sending notification with user " +
                    userName + " and contentid " + contentID, e);
            // error message should be improved
            String language = e.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(language,
                    m_fullExtensionName, e);
         }
         catch(SQLException e)
         {
            l.warn("SQL Exception while sending notification with user " +
                    userName + " and contentid " + contentID, e);
            // error message should be improved
            throw new PSExtensionProcessingException(m_fullExtensionName, e);
         }

         wfRoleInfo = (PSWorkflowRoleInfo) request.getPrivateObject(
                 PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT);

         if (null == wfRoleInfo)
         {
            throw new PSExtensionProcessingException(
                    m_fullExtensionName,
                    new PSRoleException(lang,IPSExtensionErrors.ROLEINFO_OBJ_NULL));
         }

         try
         {
            String extension = PSWorkFlowUtils.getProperty(PSWorkFlowUtils.NOTIFICATION_LINK_GEN_EXIT_PROP);
            if (StringUtils.isNotBlank(extension))
            {
               String context = extension.substring(0, extension.lastIndexOf('/') + 1);
               String name = extension.substring(extension.lastIndexOf('/') + 1);

               URL baseUrl = PSUrlUtils.createUrl(null, null, null, null, null, request);

               PSExtensionWrapper ext = new PSExtensionWrapper(context, name);
                URL url = null;
                String isBehindProxy = PSServer.getProperty("requestBehindProxy") == null ? "" : PSServer.getProperty("requestBehindProxy");
                if(isBehindProxy.equalsIgnoreCase("true")){
                    int port = Integer.valueOf(PSServer.getProperty("proxyPort"));
                    String scheme = PSServer.getProperty("proxyScheme");
                    String domainName = PSServer.getProperty("publicCmsHostname");
                    url = (URL) ext.call(contentID, revisionID, domainName, port,
                            PSWorkFlowUtils.isSSLEnabledForNotification());
                }else{
                    url = (URL) ext.call(contentID, revisionID, baseUrl.getHost(), baseUrl.getPort(),
                        PSWorkFlowUtils.isSSLEnabledForNotification());
                }
               if (url != null)
               {
                  contentURL = url.toString();
               }
            }
            else
            {
               contentURL = PSWorkFlowUtils.getContentItemURL(contentID, revisionID, request, true);
            }
         }
         catch (PSConversionException e)
         {
            except = e;
         }
         catch (MalformedURLException e)
         {
            except = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            except = e;
         }
         catch (PSAuthorizationException e)
         {
            except = e;
         }
         finally
         {
            if (null != except)
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                       "Unable to obtain content item URL.");
            }
         }

         PSContentStatusContext csc = null;
         try
         {
            csc = new PSContentStatusContext(connection, contentID);
            String enableNotification = PSWorkFlowUtils.getProperty(PSWorkFlowUtils.NOTIFICATION_ENABLE);

            if(enableNotification!=null && enableNotification.equalsIgnoreCase("Y")){
               sendNotifications(contentID, revisionID, contentURL, workflowID,
                       transitionID,
                       fromStateID, toStateID, userName, wfRoleInfo, request,
                       connection, String.valueOf(csc.getCommunityID()));
            }


         }
         catch(SQLException e)
         {
            except = e;
         }
         catch(PSEntryNotFoundException e)
         {
            if (e.getLanguageString() == null)
               e.setLanguageString(lang);
            except = e;
         }
         catch(PSMailException e)
         {
            if (e.getLanguageString() == null && e.getThrowable() == null)
               e.setLanguageString(lang);
            except = e;
         }
         catch (Exception e)
         {
            except = e;
         }
         finally
         {
            if (csc != null)
            {
               try
               {
                  csc.close();
               }
               catch (Exception ex)
               {
                  //no-op
               }
            }
         }
      }
      finally
      {
         try
         {
            if(null != connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch(SQLException sqe)
         {
         }
         if (null != except)
         {
            PSWorkFlowUtils.printWorkflowException(request, except);
            if (except instanceof PSException)
            {
               String language = ((PSException)except).getLanguageString();
               if (language == null)
                  language = PSI18nUtils.DEFAULT_LANG;
               throw new PSExtensionProcessingException(lang,
                       m_fullExtensionName, except);
            }
            throw new PSExtensionProcessingException(m_fullExtensionName, except);
         }
         PSWorkFlowUtils.printWorkflowMessage(request,
                 "Notify Assignees: exit processResultDocument ");
      }
      return resDoc;
   }

   /**
    * Executive method for sending mail notifications, Gets notifications,
    * constructs recipient list and sends the mail notifications.
    * @param contentid The content id of the item being processed.
    * @param revisionid The revision id of the item, used to lookup any item
    *   fields specified in the message, ignored if no fields are referenced.
    * @param contentURL  URL of the content item
    * @param workflowID  WorkflowID for the content item
    * @param transitionID ID of the transition
    * @param fromStateID ID of content state before transition
    * @param toStateID   ID of content state after transition
    * @param userName    name of user sending the notification
    * @param  wfRoleInfo  Object containing role info such as from and to state
    *                    adhoc user information.
    * @param request     request context for the exit
    * @param connection  connection to back-end database
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the ccommunity filter.
    *
    * @throws            PSMailException if an error occurs while sending the
    *                    mail.
    * @throws            SQLException if a database error occurs
    * @throws            PSEntryNotFoundException if there is no data base
    *                    entry for the content item.
    * @throws            NamingException if a datasource cannot be resolved
    * @throws            RepositoryException if item field replacement fails
    */
   @SuppressWarnings({"unchecked"})
   static void sendNotifications(int contentid, int revisionid, String contentURL,
                                 int workflowID, int transitionID, int fromStateID, int toStateID,
                                 String userName, IWorkflowRoleInfo wfRoleInfo,
                                 IPSRequestContext request, Connection connection, String communityId)
           throws PSEntryNotFoundException, PSMailException, SQLException,
           NamingException, RepositoryException
   {
      PSWorkFlowUtils.printWorkflowMessage(request, "  Entering Method sendNotifications");

      List<String> toStateUserList = new ArrayList<String>();
      List<String> fromStateUserList = new ArrayList<String>();
      List<String> emailToList = new ArrayList<String>();
      Map<String, PSSubject> psSubjects = new HashMap<>();
      String emailToString = "";
      List<String> CCList = null;
      List<String> emailAndCcList = new ArrayList<>();
      String emailCCString = "";
      PSNotificationsContext nc = null;
      PSTransitionNotificationsContext tnc = null;
      int notificationID = 0;
      String additionalRecipientString = "";
      List<String> additionalRecipientList = null;
      String subject = "";
      String body = "";
      List<String> fromStateRoleNotificationList = null;
      List<String> fromStateAdhocActorNotificationList = null;
      List<String> toStateRoleNotificationList = null;
      List<String> toStateAdhocActorNotificationList = null;
      PSStateRolesContext fromStateRoleContext = null;
      PSStateRolesContext toStateRoleContext = null;
      IPSContentAdhocUsersContext fromStateAdhocContext = wfRoleInfo
              .getFromStateCauc();
      IPSContentAdhocUsersContext toStateAdhocContext = wfRoleInfo
              .getToStateCauc();

      if ((null == userName) || userName.length() == 0)
      {
         throw new IllegalArgumentException(
                 "User name may not be null or empty");
      }

      //Get the notification messages for the transition
      try {
         tnc = new PSTransitionNotificationsContext(workflowID,
                 transitionID, connection);
      } catch (SQLException e2) {
         m_log.error("SQL exception occurred while getting notification message for the transition.", e2);
         throw new SQLException(e2);
      } catch (NamingException e2) {
         m_log.error("Naming exception occurred while getting notification message for the transition.", e2);
         throw new NamingException(e2.getMessage());
      }
      if (0 == tnc.getCount())
      {
         PSWorkFlowUtils.printWorkflowMessage(request,
                 "  There are no notifications for the transition " +
                         transitionID +  " in the workflow " + workflowID + ".");
         return;
      }


      // Get whichever state role lists will be needed
      if (tnc.requireFromStateRoles())
      {
         try
         {
            fromStateRoleContext = new PSStateRolesContext(
                    workflowID,
                    connection,
                    fromStateID,
                    PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE);

            fromStateRoleNotificationList = PSWorkflowRoleInfoStatic.getStateRoleNameNotificationList(fromStateRoleContext, contentid);

            Set<IPSTypedPrincipal> fromStatePrinces = PSWorkflowRoleInfoStatic.getPrincipals(fromStateRoleNotificationList);

            fromStateAdhocActorNotificationList = PSWorkflowRoleInfoStatic.getStateAdhocActorNotificationList(fromStateAdhocContext,
                    fromStateRoleContext, contentid, request);
            Set<IPSTypedPrincipal> fromAdhocStatePrinces = PSWorkflowRoleInfoStatic.getPricipalsFromNames(fromStateAdhocActorNotificationList);

            fromStateUserList.addAll(getUserEmails(contentid, revisionid,
                    communityId, workflowID, fromStateID, fromStatePrinces,
                    fromAdhocStatePrinces, psSubjects));
         }
         catch(PSEntryNotFoundException e)
         {
            m_log.warn("Entry not found. ");
         }
         catch(PSRoleException e)
         {
            m_log.warn("Role exception occurred. ");
         }
         catch (Exception e)
         {
            m_log.error("An exception occurred. ", e);
            throw new RuntimeException(e);
         }

         if (fromStateUserList.isEmpty())
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
                    "  No 'from' state role recipients for state "
                            + fromStateID + " in the workflow " + workflowID + ".");
         }
      }

      if (tnc.requireToStateRoles())
      {
         try
         {
            /*
             * Don't need to get state role info if this is a self
             * transition and from state role info was required.
             */
            Set<IPSTypedPrincipal> princes = new HashSet<IPSTypedPrincipal>();
            if (fromStateID == toStateID && null != fromStateRoleContext)
            {
               toStateRoleContext = fromStateRoleContext;
            }
            else
            {
               toStateRoleContext = new PSStateRolesContext(
                       workflowID,
                       connection,
                       toStateID,
                       PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE);

               toStateRoleNotificationList = PSWorkflowRoleInfoStatic.getStateRoleNameNotificationList(toStateRoleContext,
                       contentid);
               Set<IPSTypedPrincipal> toStatePrinces = PSWorkflowRoleInfoStatic.getPrincipals(toStateRoleNotificationList);
               princes.addAll(toStatePrinces);
            }

            /*
             * However, the add-hoc users may be different even for a self
             * transition, because they must be respecified, unless it is
             * an aging transition.
             */
            toStateAdhocActorNotificationList = PSWorkflowRoleInfoStatic.getStateAdhocActorNotificationList(toStateAdhocContext,
                    toStateRoleContext,
                    contentid, request);
            Set<IPSTypedPrincipal> toStateAdhocPrinces = PSWorkflowRoleInfoStatic.getPricipalsFromNames(toStateAdhocActorNotificationList);

            toStateUserList.addAll(getUserEmails(contentid, revisionid,
                    communityId, workflowID, toStateID, princes,
                    toStateAdhocPrinces, psSubjects));
         }
         // Ignore case where data base context does not exist
         catch(PSEntryNotFoundException e)
         {
            m_log.warn("Entry not found. ");
         }
         catch(PSRoleException e)
         {
            m_log.warn("Role exception occurred. ");
         }
         catch (Exception e)
         {
            m_log.error("An exception occurred. ", e);
            throw new RuntimeException(e);
         }

         if (toStateUserList.isEmpty())
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
                    "  No 'to' state role recipients for state "
                            + toStateID + " in the workflow " + workflowID + ".");
         }
      }

      // get the values for the supported tokens once for the request
      Map<String,String> tokenValues = getTokenValues(request);

      do // Loop over the notifications for this transition
      {
         /* reset the loop variables */
         emailToString = "";
         if (!emailToList.isEmpty()) {
            emailToList.clear();
         }

         emailCCString = "";
         if (null != CCList) {
            CCList.clear();
         }

         additionalRecipientString = "";
         if (null != additionalRecipientList) {
            additionalRecipientList.clear();
         }

         nc = null;
         notificationID = 0;
         notificationID = tnc.getNotificationID();

         // This will throw an exception if the notification does not exist
         try {
            nc = new PSNotificationsContext(workflowID, notificationID,
                    connection);
         } catch (PSEntryNotFoundException e1) {
            m_log.error("Notification entry not found. ", e1);
            throw new PSEntryNotFoundException(e1.getMessage());
         } catch (SQLException e1) {
            m_log.error(
                    "SQL exception occurred. May be notification does not exist.",
                    e1);
            throw new SQLException(e1);
         } catch (NamingException e1) {
            m_log.error(
                    "Naming exception occurred. Possibly the notification with the given name does not exist.",
                    e1);
            throw new NamingException(e1.getMessage());
         }

         additionalRecipientString = tnc.getAdditionalRecipientList();

         if (null != additionalRecipientString) {
            additionalRecipientString = additionalRecipientString.trim();
            if (additionalRecipientString.trim().length() > 0) {
               additionalRecipientList = PSWorkFlowUtils.tokenizeString(
                       additionalRecipientString,
                       PSWorkFlowUtils.EMAIL_STRING_DELIMITER);
            }
         }

         emailCCString = tnc.getCCList();

         if (null != emailCCString) {
            emailCCString = emailCCString.trim();
            if (emailCCString.trim().length() > 0) {
               CCList = PSWorkFlowUtils.tokenizeString(emailCCString,
                       PSWorkFlowUtils.EMAIL_STRING_DELIMITER);
            }
         }

         if (null != CCList && !CCList.isEmpty()) {
            CCList = PSWorkFlowUtils.caseInsensitiveUniqueList(CCList);
            if (null != CCList && !CCList.isEmpty()) {
               emailCCString = PSWorkFlowUtils.listToDelimitedString(
                       CCList, PSWorkFlowUtils.EMAIL_STRING_SEPARATOR, "");
            }
         }

         if (tnc.notifyToStateRoles() && !toStateUserList.isEmpty()) {
            emailToList.addAll(toStateUserList);
         }

         if (tnc.notifyFromStateRoles() && !fromStateUserList.isEmpty()) {
            emailToList.addAll(fromStateUserList);
         }

         if (null != additionalRecipientList
                 && !additionalRecipientList.isEmpty()) {
            emailToList.addAll(additionalRecipientList);
         }

         if (!emailToList.isEmpty()) {
            emailToList = PSWorkFlowUtils
                    .caseInsensitiveUniqueList(emailToList);
            if (!emailToList.isEmpty()) {
               emailToString = PSWorkFlowUtils.listToDelimitedString(
                       emailToList,
                       PSWorkFlowUtils.EMAIL_STRING_SEPARATOR, "");
            }
         } else {
            /*
             * A message must have "To" recipients. Therefore if there are
             * "CC" recipients but no "To" recipients, send the mail "To"
             * the CC list. This is desired because they should get mail
             * even if all the roles have turned off notification.
             */
            if ((null != emailCCString) && emailCCString.length() > 0) {
               PSWorkFlowUtils
                       .printWorkflowMessage(
                               request,
                               "  There are no \"to\" recipients for notification  "
                                       + notificationID
                                       + " in the workflow "
                                       + workflowID
                                       + ". \"to\" email will be sent to the \"CC\" recipients");
               emailToString = emailCCString;
               emailCCString = "";
            }

            /*
             * There are no recipients, just print a trace message and go on
             * to the next notification.
             */
            else {
               PSWorkFlowUtils
                       .printWorkflowMessage(
                               request,
                               "  There are no \"to\" or \"CC\"  recipients for notification  "
                                       + notificationID
                                       + " in the workflow "
                                       + workflowID
                                       + ", so this notification will not be sent.");
               continue;
            }
         }

         String userEmailAddress = "";
         List<String> userEmailAddressList = PSWorkflowRoleInfoStatic
                 .getSubjectEmailAddresses(userName, request, communityId);
         if (null != userEmailAddressList && !userEmailAddressList.isEmpty()) {
            userEmailAddress = userEmailAddressList.get(0);
            if ((null == userEmailAddress)
                    || userEmailAddress.length() == 0) {
               userEmailAddress = userName;
            }
         } else {
            userEmailAddress = userName;
         }

         if (PSWorkFlowUtils.RXSERVER.equals(userEmailAddress)) {
            String defaultFrom = PSWorkFlowUtils.getProperty("SMTP_DEFAULTFROM");
            if (StringUtils.isNotBlank(defaultFrom)) {
               userEmailAddress = defaultFrom;
            }
         }

         /*
          * Send mail using the mail domain, host and plugin specified in the
          * workflow properties file.
          */
         String ccMessage = "";
         if ((null != emailCCString) && emailCCString.length() > 0) {
            ccMessage = " , CC to " + emailCCString;
         }
         PSWorkFlowUtils.printWorkflowMessage(request, "Email sent to "
                 + emailToString + ccMessage + ". " + "Email sent by "
                 + userEmailAddress);

         /**
          * Combining these two lists as per RHYT-1933. Sending personalized
          * e-mails (by user) to each recipient whether it be a Cc or a 'to'
          * address.
          */
         if (CCList != null) {
            emailAndCcList.addAll(CCList);
            m_log.debug("Adding Ccs to entire mail list: " + CCList);
         }

         if (!emailToList.isEmpty()) {
            emailAndCcList.addAll(emailToList);
            m_log.debug("Adding all 'to' recipients to list: " + emailToList);
         }

         subject = nc.getSubject();
         body = nc.getBody();

         // get field names from content item
         Set<String> fieldNames = new HashSet<String>();
         parseFields(subject, fieldNames);
         parseFields(body, fieldNames);

         // create an evaluator for use with Velocity by using the field values,
         // tokens ($wflink, $wfcomment), and general e-mail bindings (set below).
         PSJexlEvaluator eval = getFieldValues(contentid, fieldNames);
         eval = parseTokens(tokenValues, eval);
         eval.bind(PSWorkFlowUtils.WORKFLOW_LINK_TOKEN, contentURL);

         List<PSMessagePackage> messages = new ArrayList<>();
         boolean containsWfLink = body.contains(PSWorkFlowUtils.WORKFLOW_LINK_TOKEN);
         for (String email : emailAndCcList) {

            // need to get the body and subject here again so that each
            // e-mail has a fresh copy of the raw subject and body for Velocity processing.
            subject = nc.getSubject();
            body = nc.getBody();

            PSMessagePackage pkg = new PSMessagePackage();
            eval = addEmailBindings(userName, email, eval, psSubjects);
            subject = processString(eval, subject);
            body = processString(eval, body);

            if (!containsWfLink) {
               m_log.debug("Adding content. $wflink not detected.");
               body += "\r\n\r\n" + contentURL;
            }

            pkg.setEmailBody(body);

            pkg.setSubj(subject);
            pkg.setEmailToStr(email);
            pkg.setUserEmail(userEmailAddress);

            messages.add(pkg);
         }

         final List<PSMessagePackage> pkgs = messages;

          sendMail(pkgs);

      } while (tnc.moveNext()); // End Loop over mail notifications

      PSWorkFlowUtils.printWorkflowMessage(request,
              "Exiting Method sendNotifications");
   }

   /**
    * Gets the Velocity context based on the evaluation bindings set above.
    * Can be used to process text using Velocity.  Currently being used to process
    * the subject and body of workflow e-mail notifications.
    * @param eval the PSJexlEvaluator object with bindings set.
    * @param text the string to process with Velocity
    * (subject and body of workflow notification currently).
    * @return the contents of the text after being processed by Velocity.
    */
   private static String processString(PSJexlEvaluator eval, String text) {
      @SuppressWarnings("unchecked")
      VelocityContext velContext = PSVelocityUtils.getContext(eval.getVars());

      RuntimeServices rs = new RuntimeInstance();

      try (StringWriter writer = new StringWriter()) {
         Template t = PSVelocityUtils.compileTemplate(text,
                 "EmailContents", rs);
         t.merge(velContext, writer);
         String message = writer.toString();
         text = message;
      } catch (Exception e) {
         m_log.error("Error processing subject or body of e-mail message: ", e);
      }

      return text;
   }

   /**
    * Determines if the given workflow state contains an role that has admin or assignee
    * permission and the role is also enabled the add-hoc.
    *
    * @param workflowId the workflow ID.
    * @param stateId the state ID.
    *
    * @return <code>true</code> if there is such role; otherwise return <code>false</code>.
    */
   private static boolean hasAddHocRole(int workflowId, int stateId)
   {
      IPSWorkflowService srv = PSWorkflowServiceLocator.getWorkflowService();
      IPSGuid stateGuid = new PSGuid(PSTypeEnum.WORKFLOW_STATE, stateId);
      IPSGuid wfGuid = new PSGuid(PSTypeEnum.WORKFLOW, workflowId);
      PSState state = srv.loadWorkflowState(stateGuid, wfGuid);
      for (PSAssignedRole role : state.getAssignedRoles())
      {
         if (PSAdhocTypeEnum.DISABLED == role.getAdhocType())
            continue;

         if (role.getAssignmentType() == PSAssignmentTypeEnum.ADMIN ||
                 role.getAssignmentType() == PSAssignmentTypeEnum.ASSIGNEE)
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Loads the specified item and obtains values for all supplied field names.
    * Sets the field names and values as bindings on the e-mail template.
    * See {@link #processString(PSJexlEvaluator, String)}.
    * The {@see PSWorkFlowUtils.WORKFLOW_COMMENT_PROP} field name is assigned the value of
    * {@see PSWorkFlowUtils.WORKFLOW_COMMENT_TOKEN}.
    *
    * @param contentId
    *            The content id of the item.
    * @param fieldNames
    *            The set of field names to obtain values for, assumed not
    *            <code>null</code>, may be empty in which case the method
    *            simply returns an empty map.
    * @return The Jexl evaluator of field names set to bindings, never <code>null</code>,
    *         may be empty if <code>fieldNames</code> is empty.
    */
   private static PSJexlEvaluator getFieldValues(int contentId,
                                                 Set<String> fieldNames) {
      PSJexlEvaluator eval = new PSJexlEvaluator();

      if (fieldNames.isEmpty()) {
         return eval;
      }

      PSContentEditorSystemDef sysDef = PSServer.getContentEditorSystemDef();
      PSFieldSet sysFieldSet = sysDef.getFieldSet();

      List<Integer> ids = new ArrayList<Integer>();
      ids.add(contentId);
      IPSExecutableSearch search = PSExecutableSearchFactory
              .createExecutableSearch(PSRequest.getContextForRequest(),
                      fieldNames, ids);

      IPSSearchResultRow row;
      try {
         PSWSSearchResponse result = search.executeSearch();
         List<IPSSearchResultRow> rows = result.getRowList();
         if (rows.isEmpty()) {
            throw new RuntimeException(
                    "Failed to locate content item with id " + contentId);
         }

         row = rows.get(0);
      } catch (PSSearchException e) {
         // indicates a bug of some sort
         throw new RuntimeException(
                 "Error loading field values for content " + "item with id "
                         + contentId + ": " + e.getLocalizedMessage(), e);
      }

      String formatStr = PSWorkFlowUtils.properties
              .getProperty(PSWorkFlowUtils.DATE_FORMAT_PROP);
      SimpleDateFormat dateFormat = StringUtils.isBlank(formatStr) ? new SimpleDateFormat()
              : new SimpleDateFormat(formatStr);

      for (String fieldName : fieldNames) {
         String value = "";
         // replace newly formatted wfcomment token with old format
         if (PSWorkFlowUtils.WORKFLOW_COMMENT_PROP.equals(fieldName)) {
            value = PSWorkFlowUtils.WORKFLOW_COMMENT_TOKEN;
         } else {
            value = row.getColumnDisplayValue(fieldName);
            if (value == null)
               value = "";
            else if (value.trim().length() > 0) {
               // handle date formatting
               PSField field = sysFieldSet.getFieldByName(fieldName);
               if (field != null
                       && (field.getDataType().equals(PSField.DT_DATE)
                       || field.getDataType().equals(
                       PSField.DT_DATETIME) || field
                       .getDataType().equals(PSField.DT_TIME))) {
                  // parse value back to a date object
                  Date date = PSDataTypeConverter
                          .parseStringToDate(value);
                  if (date != null) {
                     value = dateFormat.format(date);
                  }
               }
            }
         }

         eval.bind("$" + fieldName, value);
      }

      return eval;
   }

   /**
    * Parses the supplied text for field tokens and adds them to the supplied
    * set.
    *
    * @param text The text to parse, assumed not <code>null</code>, may be
    * empty.
    * @param fieldNames The set to which discovered field names are added,
    * assumed not <code>null</code>.
    */
   private static void parseFields(String text, final Set<String> fieldNames)
   {
      PSStringTemplate template = getFieldTemplate(text);
      try
      {
         template.expand(new IPSTemplateDictionary() {

            public String lookup(String key)
            {
               if (!StringUtils.isBlank(key))
                  fieldNames.add(key);
               return "";
            }});
      }
      catch (PSStringTemplateException e)
      {
         // won't happen, ignore
      }
   }

   /**
    * Creates a template expander for field tokens, set to ignore unmatched
    * tokens.
    *
    * @param text The text to parse, assumed not <code>null</code>, may be
    * empty
    *
    * @return The field template, never <code>null</code>.
    */
   private static PSStringTemplate getFieldTemplate(String text)
   {
      PSStringTemplate template = new PSStringTemplate(text, PSWorkFlowUtils.FIELD_TOKEN_START,
              PSWorkFlowUtils.FIELD_TOKEN_END);
      template.setIgnoreUnmatchedSequence(true);
      return template;
   }

   /**
    * Returns a map containing the token name (<code>String</code>) as key
    * and token value (<code>String</code>) as value. This map can be used
    * to substitute the token name with the token value in a string.
    *
    * @param request request context for the exit, assumed not
    * <code>null</code>, used to obtain the values for the tokens
    *
    * @return the map containing the token name and values. This map contains
    * non-<code>null</code> value for all supported token names. The value
    * may be empty if this token does not the corresponding property defined in
    * "rxconfig/Workflow/rxworkflow.properties" file and a non-empty value
    * could not be obtained from the request context object. If a
    * non-<code>null</code> and non-empty value is obtained from the request
    * context then it is used as the token value, otherwise the value
    * configured in "rxconfig/Workflow/rxworkflow.properties" file is used
    * (which defaults to empty if no property containing the token name as
    * key is defined).
    */
   @SuppressWarnings({"deprecation"})
   private static Map<String,String> getTokenValues(IPSRequestContext request)
   {
      Map<String, String> tokenValues = new HashMap<String, String>(
              MAIL_TOKENS_DEFAULT_VALUE);
      for (int i = 0; i < PSWorkFlowUtils.MAIL_TOKENS.length; i++)
      {
         String tokenName = PSWorkFlowUtils.MAIL_TOKENS[i];
         if (tokenName.equals(PSWorkFlowUtils.WORKFLOW_COMMENT_TOKEN))
         {
            String transitionComment =
                    PSWorkFlowUtils.getTransitionCommentFromHTMLParams(
                            request.getParameters());
            if ((transitionComment != null) &&
                    (transitionComment.trim().length() > 0))
            {
               tokenValues.put(tokenName, transitionComment);
            }
         }
      }
      return tokenValues;
   }

   /**
    * This method adds generic and e-mail related bindings to the workflow
    * notification template.
    *
    * @param userName the userName of who triggered the workflow notification.
    * @param email the email address of the recipient.
    * @param eval the current Jexl evaluator with bindings for processing the template.
    * @param psSubjects the map of psSubjects populated by
    * {@link #getUserEmails(int, int, String, int, int, Set, Set, Map)}
    *
    * @return a new copy of the Jexl evaluator with updated/additional bindings.
    */
   private static PSJexlEvaluator addEmailBindings(String userName,
                                                   String email, PSJexlEvaluator eval, Map<String, PSSubject> psSubjects) {
      PSJexlEvaluator evaluator = eval;

      PSSubject subject = psSubjects.get(email);

      try {
         if (subject != null) {
            evaluator.bind("$userSubject", subject);
         }

         evaluator.bind("$wfemail", email);

         Map<String, Object> jexlTools = PSJexlUtils.getToolsMap();

         if (jexlTools != null) {
            evaluator.bind("$tools", jexlTools);
         }
         m_log.debug("The bindings on the e-mail template are: " + evaluator.bindingsToString());
      } catch (IllegalStateException e) {
         m_log.error("There was an error adding e-mail bindings to the template: ", e);
      }

      return evaluator;
   }

   /**
    * Gets the user's email addresses from the non-add-hoc and add-hoc principals.
    *
    * The psSubjects param is populated within this empty and should be <code>empty</code>
    * when passed in.  The method populates this map with e-mail address and PSSubject key/value
    * pairs.  The map can be used to determine if an e-mail set in the 'to' or 'Cc' lists
    * belong to valid Roles in the CMS and a PSSubject can be returned for that e-mail if it
    * exists.
    *
    * @param contentid the content ID.
    * @param revisionid the revision ID.
    * @param communityId the community ID.
    * @param nonAddHocUsers the none add-hoc principals, assumed not <code>null</code>.
    * @param addHocUsers the add-hoc principals, assumed not <code>null</code>.
    * @param psSubjects an <code>empty</code> Map populated with an e-mail address and
    * affiliated PSSubject.
    *
    * @return the user's email addresses, never <code>null</code>, but may be empty.
    *
    * @throws Exception if error occurs.
    */
   private static List<String> getUserEmails(int contentid, int revisionid,
                                             String communityId, int workflowId, int stateId,
                                             Set<IPSTypedPrincipal> nonAddHocUsers,
                                             Set<IPSTypedPrincipal> addHocUsers, Map<String, PSSubject> psSubjects) throws Exception {

      Set<IPSTypedPrincipal> principals = new HashSet<IPSTypedPrincipal>();

      // ignore non-addHoc users if "notify add-hoc users only" is enabled
      if (!PSWorkflowRoleInfoStatic.isNotifyAddHocOnly() || (!hasAddHocRole(workflowId, stateId)))
         principals.addAll(nonAddHocUsers);

      principals.addAll(addHocUsers);
      principals = PSWorkflowRoleInfoStatic.filterPrincipalsByCommunityAndFolderSecurity(principals, contentid, revisionid);

      for (IPSTypedPrincipal prince : principals) {
         @SuppressWarnings("unchecked")
         List<PSSubject> subjects = PSRoleManager.getInstance()
                 .getSubjectGlobalAttributes(prince.getName(),
                         PSSubject.SUBJECT_TYPE_USER, null, null);

         if (subjects.get(0) != null) {
            PSSubject subject = subjects.get(0);
            m_log.debug("Adding the current user to psSubjects map: " + subjects.get(0).getName());
            PSAttribute emailAttr = subject.getAttributes().getAttribute(PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE);
            if (emailAttr != null) {
               String email = (String) emailAttr.getValues().get(0);
               if (StringUtils.isNotEmpty(email)) {
                  psSubjects.put(email, subject);
               }
            }
            else if (StringUtils.isNotEmpty(PSWorkFlowUtils.getProperty("MAIL_DOMAIN"))) {
               String email = PSWorkFlowUtils.getProperty("MAIL_DOMAIN");
               psSubjects.put(email, subject);
            }

         }
      }

      return PSWorkflowRoleInfoStatic.getRoleEmailAddresses(principals, communityId);
   }

   /**
    * Replaces the all the token names contained in the specified map with
    * the corresponding token value in the specified string.
    *
    * @param tokenValues map containing the token name (<code>String</code>)
    * as key and the token value (<code>String</code>) as value. Assumed
    * not <code>null</code>.
    *
    * @param eval jexl evaluator
    *
    * @return the modified string with the token names substituted with the
    * corresponding token value, may be <code>null</code> or empty if
    * <code>str</code> is <code>null</code> or empty.
    */
   private static PSJexlEvaluator parseTokens(Map<String, String> tokenValues,
                                              PSJexlEvaluator eval) {
      PSJexlEvaluator evaluator = eval;

      Iterator<Map.Entry<String, String>> it = tokenValues.entrySet()
              .iterator();
      while (it.hasNext()) {
         Map.Entry<String, String> item = it.next();
         evaluator.bind(item.getKey(), item.getValue());
      }
      return evaluator;
   }

   /**
    * Send mail using the specified from, to, cc, subject, and body (to which a
    * URL can be appended); the mail domain, SMTP host and optionally the mail
    * plugin to be used are specified by properties in the rxworkflow
    * properties file. If no custom mail plugin is specified, the Rhythmyx
    * javamail plugin will be used.
    *
    * @param packages - a light weight class used to contain the
    * subject, body, to and from e-mail addresses, etc.
    * See {@link PSMessagePackage}
    */
   public static void sendMail(List<PSMessagePackage> packages)
   {
      try {
         String mailDomain;
         String smtpHost;
         String smtpUsername;
         String smtpPassword;
         String smtpSSLPort;
         String smtpIsTLSEnabled;
         String smtpPort;
         String smtpBounceAddr;
         IPSMailMessageContext messageContext = null;

         String msgFrom;
         String msgTo;
         String mailSubject;
         String mailBody;

         for (PSMessagePackage msg : packages) {
            msgFrom = msg.getUserEmail();
            msgTo = msg.getEmailToStr();
            mailSubject = msg.getSubj();
            mailBody = msg.getEmailBody();

            if (null == msgFrom) {
               throw new IllegalArgumentException(
                       "Message From may not be null.");
            }
            msgFrom = msgFrom.trim();
            if (0 == msgFrom.length()) {
               throw new IllegalArgumentException(
                       "Message From may not be empty.");
            }

            if (null == msgTo) {
               throw new IllegalArgumentException(
                       "Message To may not be null.");
            }
            msgTo = msgTo.trim();
            if (0 == msgTo.length()) {
               throw new IllegalArgumentException(
                       "Message To may not be empty.");
            }

            if (null == mailSubject) {
               mailSubject = "";
            }

            if (null == mailBody) {
               mailBody = "";
            }

            mailDomain = PSWorkFlowUtils.properties.getProperty(
                    "MAIL_DOMAIN", "");
            if (null == mailDomain) {
               throw new PSMailException(
                       IPSExtensionErrors.MAIL_DOMAIN_NULL);
            }
            mailDomain = mailDomain.trim();
            if (0 == mailDomain.length()) {
               throw new PSMailException(
                       IPSExtensionErrors.MAIL_DOMAIN_EMPTY);
            }

            smtpHost = PSWorkFlowUtils.properties.getProperty("SMTP_HOST", "");
            if (null == smtpHost) {
               throw new PSMailException(IPSExtensionErrors.SMTP_HOST_NULL);
            }

            smtpHost = smtpHost.trim();
            if (0 == smtpHost.length()) {
               throw new PSMailException(
                       IPSExtensionErrors.SMTP_HOST_EMPTY);
            }

            smtpUsername = PSWorkFlowUtils.properties.getProperty(
                    "SMTP_USERNAME", "");
            smtpPassword = PSWorkFlowUtils.properties.getProperty(
                    "SMTP_PASSWORD", "");
            smtpPort = PSWorkFlowUtils.properties.getProperty("SMTP_PORT",
                    "");
            smtpIsTLSEnabled = PSWorkFlowUtils.properties.getProperty(
                    "SMTP_TLSENABLED", "");
            smtpSSLPort = PSWorkFlowUtils.properties.getProperty(
                    "SMTP_SSLPORT", "");
            smtpBounceAddr = PSWorkFlowUtils.properties.getProperty(
                    "SMTP_BOUNCEADDR", "");

            /* Build the message context to send to the plugin */
            messageContext =
                    new PSMailMessageContext(msgFrom,
                            msgTo,
                            null,
                            mailSubject,
                            mailBody,
                            null,
                            mailDomain,
                            smtpHost,
                            smtpUsername,
                            smtpPassword,
                            smtpIsTLSEnabled,
                            smtpPort,
                            smtpSSLPort,
                            smtpBounceAddr);

            // Send the message
            IPSSystemService svc = PSSystemServiceLocator.getSystemService();
            svc.sendEmail(messageContext);
         }
      } catch (PSMailException e) {
         m_log.error("Error occurred during mail sending !", e);
      }
   }



   /**************  IPSExtension Interface Implementation ************* */
   public boolean canModifyStyleSheet()
   {
      return true;
   }


   /**
    * Map containing the token name (<code>String</code>) as key and the
    * default value for the token (<code>String</code>) as value. Default value
    * for the token can be specified in the
    * "rxconfig/Workflow/rxworkflow.properties" properties file in the format:
    * <p>
    * wfcomment=No comment available
    * <p>
    * If any token is missing from the properties file, its value defaults to
    * empty.
    * Default value for the token is used when the user does not enter any
    * value for the token.
    */
   private static final Map<String, String> MAIL_TOKENS_DEFAULT_VALUE =
           new HashMap<String, String>();

   static
   {
      for (int i = 0; i < PSWorkFlowUtils.MAIL_TOKENS.length; i++)
      {
         String propertyName = PSWorkFlowUtils.MAIL_TOKENS[i].substring(1);
         MAIL_TOKENS_DEFAULT_VALUE.put(PSWorkFlowUtils.MAIL_TOKENS[i],
                 PSWorkFlowUtils.properties.getProperty(propertyName, ""));
      }
   }
}
