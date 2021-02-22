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

package com.percussion.services.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSParameters;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSContentApproval;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSTransition.PSWorkflowCommentEnum;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.PSWorkFlowUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides help in calculating available workflow actions for one or
 * more items. It calculates the requested actions for each item specified
 * during construction and then returns the actions common for all items.
 * <p>
 * For optimal performance, this class should be used within a service call that
 * has initiated a transaction.
 */
public class PSWorkflowActionsHelper
{
   /**
    * Construct the actions helper with the information required to calculate
    * possible actions.
    * 
    * @param contentids A list of content ids, not <code>null</code> or empty,
    * for which actions will be calculated.
    * @param assignmentTypes The assignment types for each of the supplied
    * content ids, not <code>null</code> or empty, must contain the same
    * number of elements as the content id list.
    * @param userName The name of the user for whom the actions will be
    * calculated, not <code>null</code> or empty.
    * @param userRoles The names of the roles the user is a member of, not
    * <code>null</code>, may be empty.
    * @param locale The locale to use for localizing action labels, may be 
    * <code>null</code> or empty to use the default locale.
    */
   public PSWorkflowActionsHelper(List<IPSGuid> contentids,
      List<PSAssignmentTypeEnum> assignmentTypes, String userName,
      List<String> userRoles, String locale)
   {
      if (contentids == null || contentids.isEmpty())
         throw new IllegalArgumentException(
            "contentids may not be null or empty");

      if (assignmentTypes == null)
         throw new IllegalArgumentException("assignmentTypes may not be null");

      if (contentids.size() != assignmentTypes.size())
         throw new IllegalArgumentException("The number of contentids must "
            + "match the number of assignment types");

      if (StringUtils.isBlank(userName))
         throw new IllegalArgumentException("userName may not be null or empty");

      if (userRoles == null)
         throw new IllegalArgumentException("userRoles may not be null");

      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      Iterator<PSAssignmentTypeEnum> types = assignmentTypes.iterator();
      for (IPSGuid contentid : contentids)
      {
         PSAssignmentTypeEnum type = types.next();
         PSComponentSummary sum = mgr.loadComponentSummary(contentid.getUUID());
         if (sum == null)
            continue;

         PSItemInfo info = new PSItemInfo(contentid, type, sum
            .getWorkflowAppId(), sum.getContentStateId(), 
            sum.getCheckoutUserName());
         m_itemInfoList.add(info);
      }

      m_userName = userName;
      m_userRoles = userRoles;
      m_locale = locale;
   }

   /**
    * Get all actions including the CIAO and transition actions, calls
    * {@link #getCIAOActions()} and {@link #getTranstionActions()} and returns
    * the union of the results.
    * 
    * @return The list of actions, never <code>null</code>, may be empty.
    * 
    * @throws PSWorkflowException If there is an error determining the item 
    * status.
    */
   public List<PSMenuAction> getAllWorkflowActions() throws PSWorkflowException
   {
      List<PSMenuAction> actions = getCIAOActions();
      actions.addAll(getTranstionActions());

      return actions;
   }

   /**
    * Get the checkin and checkout actions possible for the user and items
    * supplied during construction. If multiple items were specified, then the
    * intersection of the possible actions of each item is returned.
    * 
    * @return The list of actions, never <code>null</code>, may be empty.
    * 
    * @throws PSWorkflowException If there is an error determining the item 
    * status.
    */
   public List<PSMenuAction> getCIAOActions() throws PSWorkflowException
   {
      boolean isFirst = true;
      
      /* If checked out by the current user, create checkin action. If
       * checked out by another and user is Admin assignee type, create force
       * checkin action. If checked in, user is Assignee, and item is not in a
       * public state, create checkout action.
       */
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      PSMenuAction result = null;
      for (PSItemInfo info : m_itemInfoList)
      {
         PSMenuAction action = null;
         if (m_userName.equals(info.getCheckedOutUserName()))
         {
            action = createCIAOAction(PSMenuAction.CHECKIN_ACTION_NAME, CHECKIN_URL,
               PSWorkFlowUtils.TRIGGER_CHECK_IN, CHECKIN_ACTION_LABEL);
         }
         else if (info.isCheckedOut() && info.hasAdminAccess())
         {
            action = createCIAOAction(PSMenuAction.FORCE_CHECKIN_ACTION_NAME,  CHECKIN_URL,
               PSWorkFlowUtils.TRIGGER_FORCE_CHECK_IN,
               FORCE_CHECKIN_ACTION_LABEL);
         }
         else if (!info.isCheckedOut() && info.hasAssigneeAccess()
            && !svc.isPublic(info.getStateId(), info.getWorkflowId()))
         {
            action = createCIAOAction(PSMenuAction.CHECKOUT_ACTION_NAME, 
               CHECKOUT_URL, PSWorkFlowUtils.TRIGGER_CHECK_OUT, 
               CHECKOUT_ACTION_LABEL);
         }

         // handle intersection
         if (isFirst)
         {
            result = action;
            isFirst = false;
         }
         else
         {
            if (action == null || !action.equals(result))
            {
               result = null;
               break;
            }
         }
      }

      List<PSMenuAction> results = new ArrayList<>();
      if (result != null)
         results.add(result);
      
      return results;
   }
   
   /**
    * Get the name of the parameter used to specify the workflow action trigger.
    *  
    * @return The name, never <code>null</code> or empty.
    */
   public static String getTriggerParamName()
   {
      return ms_actionTriggerName;
   }

   /**
    * Create a checkin or checkout action fo rthe specified values.
    * 
    * @param name The name of the action, assumed not <code>null</code> or 
    * empty. 
    * @param url The action url, assumed not <code>null</code> or empty. 
    * @param triggerName The workflow property name that specifies the action 
    * trigger value, assumed not <code>null</code> or empty.
    * @param label The action label, assumed not <code>null</code> or empty and
    * to be in the default language.  It is translated based on the locale
    * supplied during construction.
    *  
    * @return The action, never <code>null</code>.
    */
   private PSMenuAction createCIAOAction(String name, String url, 
      String triggerName, String label)
   {
      String txnlabel = PSI18nUtils.getString(
         PSI18nUtils.PSX_CE_ACTION + PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +
         label, m_locale);

      String trigger = PSWorkFlowUtils.properties.getProperty(triggerName);
      PSMenuAction action = createAction(name, url, trigger, txnlabel, "", 
         false);
      
      return action;
   }
   
   /**
    * Create an action from the supplied info, adding in the standard properties
    * and parameters based on the value of <code>isAdhoc</code>.  
    * 
    * @param name The name of the action, assumed not <code>null</code> or 
    * empty.
    * @param url The action url, assumed not <code>null</code> or empty.
    * @param triggerName The value to set as the trigger, assumed not 
    * <code>null</code> or empty.
    * @param label The transition label to use, assumed not <code>null</code> or 
    * empty and to already have been translated based on the current locale.
    * @param transId The transition id to use, assumed not <code>null</code>, 
    * may be empty if one is not required (e.g. checkin or checkout).
    * @param isAdhoc <code>true</code> if this action supports adhoc assignment,
    * <code>false</code> if not.
    * 
    * @return The action, never <code>null</code>.
    */
   private PSMenuAction createAction(String name, String url, 
      String triggerName, String label, String transId, boolean isAdhoc)
   {
      PSProperties props = new PSProperties(isAdhoc ? ms_adhocProps : 
         ms_stdProps);
      
      PSParameters params = new PSParameters(ms_stdParams);
      
      String trigger = PSWorkFlowUtils.properties.getProperty(triggerName);
      if (StringUtils.isBlank(trigger))
         trigger = triggerName;
      
      params.setParameter(TRANSITION_NAME, label);
      params.setParameter(ms_actionTriggerName, triggerName);
      params.setParameter(SYS_TRANSITIONID, transId);
      
      PSMenuAction action = new PSMenuAction(name, label, 
         PSMenuAction.TYPE_MENUITEM, url, PSMenuAction.HANDLER_SERVER, 0);
      action.setParameters(params);
      action.setProperties(props);
      
      return action;
   }   
   
   /**
    * Gets the list of possible transition actions common to each item based on 
    * the current workflow state of the item, the user's assignment type, and 
    * the transition settings of the state.
    * 
    * @return The list of actions, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public List<PSMenuAction> getTranstionActions()
   {
      List<PSMenuAction> results = new ArrayList<>();
      
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      boolean isFirst = true;
      for (PSItemInfo info : m_itemInfoList)
      {
         List<PSMenuAction> curResults = getTransitionActions(svc, info);
         
         if (isFirst)
         {
            isFirst = false;
            results.addAll(curResults);
         }
         else
         {
            // take intersection, remove adhoc 
            curResults = removeAdhocActions(curResults);
            results = ListUtils.intersection(results, curResults);
            
            // if empty, we're done
            if (results.isEmpty())
               return results;
         }
      }

      return results;
   }

   /**
    * Returns a copy of the supplied list with any adhoc actions removed.
    *  
    * @param actions The actions to filter, assumed not <code>null</code>, may
    * be empty.
    * 
    * @return The filtered list, never <code>null</code>.
    */
   private List<PSMenuAction> removeAdhocActions(List<PSMenuAction> actions)
   {
      List<PSMenuAction> results = new ArrayList<>();
      
      for (PSMenuAction action : actions)
      {
         if (PSMenuAction.VAL_BOOLEAN_TRUE.equals(action.getParameters().getParameter(
            PSMenuAction.SHOW_ADHOC)))
         {
            continue;
         }
         
         results.add(action);
      }
      
      return results;
   }

   /**
    * Get the transition actions for the supplied item information.
    * 
    * @param svc The workflow service to use, assumed not <code>null</code>.
    * @param info The item info to use, assumed not <code>null</code>.
    * 
    * @return A list of actions, never <code>null</code>, may be empty.
    */
   private List<PSMenuAction> getTransitionActions(IPSWorkflowService svc, 
      PSItemInfo info)
   {
      List<PSMenuAction> results = new ArrayList<>();
      
      // no access if 
      if (!info.hasAssigneeAccess())
         return results;
     
      // non-admin, item is checked out by a different user
      if (!info.hasAdminAccess() && info.isCheckedOut() &&
            !info.getCheckedOutUserName().equals(m_userName))
      {
         return results;
      }
      
      // load the state
      PSState state = svc.loadWorkflowState(info.getStateId(), 
         info.getWorkflowId());
      if (state == null)
      {
         // bad data, return no actions
         String msg = "Failed to calculate workflow actions for item with " +
            "contentid {0}: No state found for workflowid {1} and " +
            "stateid {2}";
         Object[] args = new Object[] {info.getContentId().getUUID(), 
            info.getWorkflowId().getUUID(), info.getStateId().getUUID()};
         
         ms_logger.error(MessageFormat.format(msg, args));
         results.clear();
         return results;
      }
      

      // check multiple approvals (user can act only once on an item)
      if (hasUserActed(svc, info))
      {
         return results;
      }

      // walk transitions
      for (PSTransition trans : state.getTransitions())
      {
         // if not admin, check transition roles
         if (!info.hasAdminAccess() && !canActInRole(svc, info, trans))
            continue;

         results.add(createTransitionAction(svc, trans, info));
      }   
      
      return results;
   }
   
   /**
    * Create a transition action for the supplied transition and item 
    * information.
    * 
    * @param svc The workflow service to use, assumed not <code>null</code>.
    * @param trans The transition for which the action is created, assumed not 
    * <code>null</code>.
    * @param info The item info to use, assumed not <code>null</code>.
    * 
    * @return The action, never <code>null</code>.
    */
   private PSMenuAction createTransitionAction(IPSWorkflowService svc, 
      PSTransition trans, PSItemInfo info)
   {
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      PSState toState = svc.loadWorkflowState(guidMgr.makeGuid(
         trans.getToState(), PSTypeEnum.WORKFLOW_STATE),
         info.getWorkflowId());
      String label = PSI18nUtils.getString(
         PSI18nUtils.PSX_WORKFLOW_TRANSITION +
         PSI18nUtils.LOOKUP_KEY_SEPARATOR +
         String.valueOf(info.getWorkflowId().longValue()) +
         PSI18nUtils.LOOKUP_KEY_SEPARATOR +
         String.valueOf(trans.getGUID().longValue()) +
         PSI18nUtils.LOOKUP_KEY_SEPARATOR_LAST +
         trans.getLabel(), m_locale);
      
      // see if to-state allows adhoc
      boolean isAdhoc = toState.isAdhocEnabled();
      PSMenuAction action = createAction(trans.getName(), isAdhoc
         ? ADHOC_TRANS_URL : TRANS_URL, trans.getTrigger(), label, 
            String.valueOf(trans.getGUID().longValue()), isAdhoc);
      
      PSParameters params = action.getParameters();
      params.setParameter(ms_actionTriggerName, trans.getTrigger());
      if (isAdhoc)
         action.setAdhocParam(true);
      
      String commentVal = null;
      PSWorkflowCommentEnum commentEnum = trans.getRequiresComment();
      if (commentEnum.equals(PSWorkflowCommentEnum.REQUIRED))
        commentVal = PSMenuAction.VAL_BOOLEAN_TRUE;
      else if (commentEnum.equals(PSWorkflowCommentEnum.DO_NOT_SHOW))
         commentVal = PSMenuAction.VAL_HIDE;
      
      if (commentVal != null)
      {
         action.setCommentRequired(commentVal);
      }
      return action;
   }


   /**
    * Determine if the user has acted on the specified item in the current 
    * workflow state.
    * 
    * @param svc The workflow service to use, assumed not <code>null</code>.
    * @param info Specifies the item information to use, assumed not 
    * <code>null</code>. 
    * 
    * @return <code>true</code> if the user has already acted, 
    * <code>false</code> if not.
    */
   private boolean hasUserActed(IPSWorkflowService svc, PSItemInfo info)
   {
      if (m_approvals == null)
      {
         m_approvals = svc.findApprovalsByUser(m_userName);
      }
      
      for (PSContentApproval approval : m_approvals)
      {
         if (approval.getWorkflowId() == info.getWorkflowId().longValue() &&
            approval.getStateId() == info.getStateId().longValue() && 
            approval.getContentId() == info.getContentId().getUUID())
         {
            return true;
         }
      }
      
      return false;
   }
   
   /**
    * Determine if the user can perform the specified transition on the
    * specified item in one of their current roles. Does not check assignment
    * type, assumes that is checked elsewhere.
    * 
    * @param svc The workflow service to use, assumed not <code>null</code>.
    * @param info The item information, assumed not <code>null</code>.
    * @param trans The transition to check against, assumed not 
    * <code>null</code>.
    * 
    * @return <code>true</code> if the user can act, <code>false</code>
    * otherwise.
    */
   private boolean canActInRole(IPSWorkflowService svc, PSItemInfo info, 
      PSTransition trans)
   {
      // if not using transition roles, then user can act
      if (trans.isAllowAllRoles())
         return true;
      
      // get intersection of user roles and trans roles
      PSWorkflow wf = svc.loadWorkflow(info.getWorkflowId());
      Set<Integer> roleIds = wf.getRoleIds(m_userRoles);
      List<PSTransitionRole> transRoles = trans.getTransitionRoles();
      Set<Integer> actingRoles = new HashSet<>();
      
      for (PSTransitionRole transRole : transRoles)
      {
         // safe cast
         int roleId = (int) transRole.getRoleId();
         if (roleIds.contains(roleId))
         {
            actingRoles.add(roleId);
         }
      }
      
      // if user does not match any trans roles, then cannot act
      if (actingRoles.isEmpty())
         return false;
      
      // now see if anyone has acted in one of these roles already
      List<PSContentApproval> approvals = 
         svc.findApprovalsByItem(info.getContentId());      
      for (PSContentApproval approval : approvals)
      {
         if (approval.getWorkflowId() != info.getWorkflowId().getUUID())
            continue;
         if (approval.getStateId() != info.getStateId().getUUID())
            continue;
         actingRoles.remove(approval.getRoleId());
      }
      
      return !actingRoles.isEmpty();
   }

   /**
    * A list of item information objects representing the items supplied during
    * construction, never <code>null</code> or modified after that.
    */
   private List<PSItemInfo> m_itemInfoList = new ArrayList<>();

   /**
    * The user name supplied during construction, never <code>null</code>, empty 
    * or modified after that.
    */
   private String m_userName;

   /**
    * The list of user role names supplied during construction, never
    * <code>null</code> or modified after that, may be empty.
    */
   private List<String> m_userRoles;
   
   /**
    * The user's current local supplied during construction, never
    * <code>null</code>, empty or modified after that.
    */
   private String m_locale;
   
   /**
    * A list of all of the user's current approvals, <code>null</code> until
    * lazily loaded by the first call to
    * {@link #hasUserActed(IPSWorkflowService, PSItemInfo)}, never
    * <code>null</code> after that, may be empty.
    */
   private List<PSContentApproval> m_approvals;

   /**
    * Encapsulates a the information needed to determine actiions for a content 
    * item.
    */
   private class PSItemInfo
   {
      /**
       * Ctor
       * 
       * @param contentid The content id, assumed not <code>null</code>.
       * @param assignmentType The user's assignment type for the item, assumed
       * not <code>null</code>.
       * @param workflowid The workflow id of the item, assumed not
       * <code>null</code>.
       * @param stateid The id of the workflow state the item is in, assumed not
       * <code>null</code>.
       * @param checkoutUserName The current checked out user, may be
       * <code>null</code> or empty if the item is not checked out.
       */
      PSItemInfo(IPSGuid contentid,
         PSAssignmentTypeEnum assignmentType, int workflowid, int stateid,
         String checkoutUserName)
      {
         IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
         m_contentid = contentid;
         m_assignmentType = assignmentType;
         m_workflowid = mgr.makeGuid(workflowid, PSTypeEnum.WORKFLOW);
         m_stateid = mgr.makeGuid(stateid, PSTypeEnum.WORKFLOW_STATE);
         if (!StringUtils.isBlank(checkoutUserName))
            m_checkoutUser = checkoutUserName;
      }

      /**
       * Get the content id supplied during ctor.
       * 
       * @return The id, never <code>null</code>
       */
      public IPSGuid getContentId()
      {
         return m_contentid;
      }

      /**
       * Get the assignment type supplied during ctor.
       * 
       * @return The type, never <code>null</code>
       */
      public PSAssignmentTypeEnum getAssignmentType()
      {
         return m_assignmentType;
      }

      /**
       * Get the checked out user name supplied during ctor.
       * 
       * @return The id, may be <code>null</code>, never empty.
       */
      public String getCheckedOutUserName()
      {
         return m_checkoutUser;
      }
      
      /**
       * Get the workflow id supplied during ctor.
       * 
       * @return The id, never <code>null</code>
       */
      public IPSGuid getWorkflowId()
      {
         return m_workflowid;
      }
      
      /**
       * Get the state id supplied during ctor.
       * 
       * @return The id, never <code>null</code>
       */
      public IPSGuid getStateId()
      {
         return m_stateid;
      }
      
      /**
       * Determine if the user has administrator access to this item.
       * 
       * @return <code>true</code> if so, <code>false</code> otherwise.
       */
      public boolean hasAdminAccess()
      {
         return m_assignmentType.equals(PSAssignmentTypeEnum.ADMIN);
      }
      
      /**
       * Determine if the user has this item checked out.
       * 
       * @return <code>true</code> if so, <code>false</code> otherwise.
       */
      public boolean isCheckedOut()
      {
         return m_checkoutUser != null;
      }

      /**
       * Determine if the user has assignee access to this item.
       * 
       * @return <code>true</code> if so, <code>false</code> otherwise.
       */
      public boolean hasAssigneeAccess()
      {
         return m_assignmentType.getValue() >= 
            PSAssignmentTypeEnum.ASSIGNEE.getValue();
      }
      
      /**
       * The content id supplied during construction, immutable.
       */
      private IPSGuid m_contentid;

      /**
       * The assignment type supplied during construction, immutable.
       */
      private PSAssignmentTypeEnum m_assignmentType;

      /**
       * The workflow id supplied during construction, immutable.
       */
      private IPSGuid m_workflowid;

      /**
       * The state id supplied during construction, immutable.
       */
      private IPSGuid m_stateid;

      /**
       * The checked out user name supplied during construction, immutable.
       */
      private String m_checkoutUser = null;
   }
   
   /**
    * Constant for the checkin action label.
    */
   static private final String CHECKIN_ACTION_LABEL = "Check-in";
   
   /**
    * Constant for the checkout action label.
    */   
   static private final String CHECKOUT_ACTION_LABEL = "Check-out";
   
   /**
    * Constant for the force checkin action label.
    */   
   static private final String FORCE_CHECKIN_ACTION_LABEL = "Force Check-in";
   
   /**
    * Constant for the checkout action url.
    */   
   static private final String CHECKOUT_URL = "../sys_action/checkout.xml";
   
   /**
    * Constant for the checkin action url.
    */   
   static private final String CHECKIN_URL = "../sys_action/checkin.xml";
   
   /**
    * Constant for the workflow command
    */   
   static private final String WORKFLOW_COMMAND = "workflow";
   
   /**
    * Constant for the transition action url.
    */   
   static private final String TRANS_URL = "../sys_action/checkintransition.xml";
   
   /**
    * Constant for the adhoc transition action url.
    */   
   static private final String ADHOC_TRANS_URL = "../sys_uiSupport/wfTransition.html";

   /**
    * Constant for the action param specifying the transition id.
    */
   private static final String SYS_TRANSITIONID = "sys_transitionid";

   /**
    * Constant for the action param specifying the transition name.
    */
   private static final String TRANSITION_NAME = "transitionName";

   /**
    * Commons logger, never <code>null</code>.
    */
   static Log ms_logger = LogFactory.getLog(PSWorkflowActionsHelper.class);   
   
   /**
    * Default properties to use for action, never <code>null</code>.
    */
   static private PSProperties ms_stdProps = new PSProperties();
   
   /**
    * Properties to use for an ad-hoc action, never <code>null</code>.
    */
   static private PSProperties ms_adhocProps = new PSProperties();
   
   /**
    * Action trigger name as defined in the workflow.properties file, never
    * <code>null</code> or empty.
    */
   static private String ms_actionTriggerName = "";
   
   /**
    * Default parameters to use for action, never <code>null</code>.
    */   
   static private PSParameters ms_stdParams = new PSParameters();
   

   // Initialize the default properties, params, and action trigger name.
   static
   {
      ms_actionTriggerName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.ACTION_TRIGGER_NAME,
         PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME);
      
      ms_stdProps.setProperty("batchProcessing", "yes");
      ms_stdProps.setProperty("SupportsMultiSelect", "yes");
      ms_stdProps.setProperty("launchesWindow", "no");
      
      ms_adhocProps.setProperty("batchProcessing", "no");
      ms_adhocProps.setProperty("launchesWindow", "yes");
      ms_adhocProps.setProperty("target", "workflowtransition");
      ms_adhocProps.setProperty("targetStyle", 
         "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0," +
         "resizable=1,width=260,height=345");
      

      Map systemParams =
         PSServer.getContentEditorSystemDef().getParamNames();
      String commandParam = IPSHtmlParameters.SYS_COMMAND;
      commandParam = null == systemParams.get( commandParam ) ? commandParam
         : (String) systemParams.get( commandParam );
      ms_stdParams.setParameter( commandParam, WORKFLOW_COMMAND );
      ms_stdParams.setParameter(IPSHtmlParameters.SYS_CONTENTID, 
         "$" + IPSHtmlParameters.SYS_CONTENTID);
      ms_stdParams.setParameter(IPSHtmlParameters.SYS_REVISION, 
         "$" + IPSHtmlParameters.SYS_REVISION);
      ms_stdParams.setParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL, 
         "../sys_cxSupport/blank.html");
   }
}
