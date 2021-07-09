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
package com.percussion.workflow;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSRoleManager;
import com.percussion.security.PSRunAsUser;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.system.PSAssignmentTypeHelper;
import com.percussion.util.PSCms;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.server.PSServer.getProperty;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.Validate.notNull;

/**
 * The class <CODE>PSWorkflowRoleInfo</CODE> provides static
 * methods for obtaining, manipulating and classifying state role and content
 * adhoc user information, and can be instantiated to provide methods for
 * setting and getting "state" variables such as the roles in which the user
 * is acting, enabling the sharing this information between contexts. This
 * sharing is done using a private object created by
 * <CODE>IPSRequestContext.setPrivateObject</CODE> with key  {@link
 * #WORKFLOW_ROLE_INFO_PRIVATE_OBJECT }
 */
@SuppressWarnings("unchecked")
public class PSWorkflowRoleInfoStatic {
   /* Manipulate state role information */

   /**
    * Given a list of state role IDs, produce a list of the corresponding
    * state role names.
    *
    * @param roleIDList list of state role IDs, may be <CODE>null</CODE>
    * @param src        state role context containing the roles, may not be
    *                   <CODE>null</CODE>
    * @return           list of the corresponding state role names,
    *                   <CODE>null</CODE> or empty list if original list is
    *                   <CODE>null</CODE> or empty.
    */
   public static List roleIDListToRoleNameList(List roleIDList,
                                               PSStateRolesContext src)
   {
      List roleNameList =
            PSWorkFlowUtils.applyMapList(roleIDList,
                                         src.getStateRoleNameMap());
      return roleNameList;
   }

   /* Obtain actor and role information */

   /*
    * Get list of state roles in which a user is acting, including adhoc roles
    *
    * @param contentID          ID of the content item
    * @param src                context of state roles meeting the minimum
    *                           assignment types, may not be <CODE>null</CODE>
    *                           or empty
    * @param userName           The user's name, cannot be <CODE>null</CODE>
    * @param userRoleNameList   A comma-delimited list of the user's roles,
    *                           may not be <CODE>null</CODE> or empty
    * @param connection         data base connection, may be not be
    *                           <CODE>null</CODE>
    * @param authUser           if true indicates that PSExitAuthenticateUser
    *                           is the caller, false otherwise
    *
    * @return                   list of state roles in which a user is acting
    *                           <CODE>null</CODE> if the user has no roles
    *
    * @throws                   SQLException if a SQL error occurs
    * @throws                   IllegalArgumentException if any of the input
    *                           parameters is not valid.
    * @throws                   PSRoleException if the content adhoc user
    *                           records contain invalid data
    */
   static public List getActorRoles(int contentID,
                                    PSStateRolesContext src,
                                    String userName,
                                    String userRoleNames,
                                    Connection connection,
                                    boolean authUser)
      throws SQLException, PSRoleException
   {
      List actorRoles = null;
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }
      if (null == connection)
      {
         throw new IllegalArgumentException(
            "Connection may not be null.");
      }
      if (null == userName || 0 == userName.length())
      {
         throw new IllegalArgumentException(
            "User name may not be null or empty.");
      }

      userName = userName.trim();

      if (0 == userName.length())
      {
         throw new IllegalArgumentException(
            "User name may not be empty after trimming.");
      }

      if (null == userRoleNames)
      {
         throw new IllegalArgumentException(
            "User role names string may not be null.");
      }

      userRoleNames = userRoleNames.trim();
      if (0 == userRoleNames.length())
      {
         throw new IllegalArgumentException(
            "User role names string  may not be empty after trimming " +
            "in getActorRoles.");
      }
      PSContentAdhocUsersContext cauc = null;

      cauc = new PSContentAdhocUsersContext(contentID, connection);

      actorRoles = getActorRoles(userName, userRoleNames, src, cauc, authUser);

      return actorRoles;
   }

   /*
    * Get list of state roles in which a user is acting, including adhoc roles
    *
    * @param userName           The user's name, cannot be <CODE>null</CODE>
    *                           or empty
    * @param userRoleNames      A comma-delimited list of the user's roles.
    *                           may be <CODE>null</CODE> or empty
    * @param src                context of state roles meeting the minimum
    *                           assignment types, cannot be <CODE>null</CODE>
    * @param cauc               ad hoc user context for the content item
    *                           not <CODE>null</CODE>
    * @param authUser           if true indicates that PSExitAuthenticateUser
    *                           is the caller, false otherwise
    * @return                   list of state roles in which a user is acting,
    *                           <CODE>null</CODE> if the user has no roles
    * @throws                   IllegalArgumentException if any of the input
    *                           parameters is not valid.
    */
   @SuppressWarnings("unchecked")
   static public List getActorRoles(String userName,
                                    String userRoleNames,
                                    PSStateRolesContext src,
                                    PSContentAdhocUsersContext cauc,
                                    boolean authUser)
   {
      List actorNonAdhocRoles  = null;
      List actorAdhocNormalRoles  = null;
      List userRoleNameList = null;
      List userAdhocNormalRoles = null;
      List userAdhocAnonymousRoles = null;
      List stateAdhocNormalRoles = null;
      List stateAdhocAnonymousRoles = null;
      List emptyAdhocNormalRoles = null;
      List emptyAdhocAnonymousRoles = null;
      List adhocAnonymousUserNames = null;
      List adhocNormalUserNames = null;

      List actorRoles = new ArrayList();

      if (null == src)
         throw new IllegalArgumentException(
            "State roles context may not be null.");

      if (null == userName)
         throw new IllegalArgumentException("User name may not be null.");

      if (cauc == null)
         throw new IllegalArgumentException("cauc may not be null");


      userName = userName.trim();

      if (0 == userName.length())
         throw new IllegalArgumentException("User name may not be empty.");

      if (null == userRoleNames)
         return null;

      userRoleNames = userRoleNames.trim();
      if (0 == userRoleNames.length())
         return null;

      userRoleNameList =
            PSWorkFlowUtils.tokenizeString(userRoleNames.toLowerCase(),
                                           PSWorkFlowUtils.ROLE_DELIMITER);

      if (null == userRoleNameList || userRoleNameList.isEmpty())
         return null;

      Map nonAdhocStateRoleNameToRoleIDMap =
            src.getNonAdhocStateRoleNameToRoleIDMap();
      Map adhocNormalStateRoleNameToRoleIDMap =
            src.getAdhocNormalStateRoleNameToRoleIDMap();
      stateAdhocAnonymousRoles =
            src.getAdhocAnonymousStateRoleIDs();
      stateAdhocNormalRoles =
            src.getAdhocNormalStateRoleIDs();

      if (!cauc.isEmpty())
      {
         userAdhocNormalRoles = cauc.getUserAdhocNormalRoleIDs(userName);
         userAdhocAnonymousRoles = cauc.getAdhocAnonymousRoleIDs();

         adhocAnonymousUserNames = cauc.getAdhocAnonymousUserNames();
         adhocNormalUserNames = cauc.getAdhocNormalUserNames();
      }

      emptyAdhocNormalRoles =
            cauc.getEmptyAdhocRoles(stateAdhocNormalRoles);
      emptyAdhocAnonymousRoles =
            cauc.getEmptyAdhocRoles(stateAdhocAnonymousRoles);

      /*
       * An actor non adhoc role must meet 2 criteria
       * 1) be a role to which the user belongs
       * 2) be a role with an adhoc assignment type of non adhoc in the
       *    state role context
      */
      if (null != nonAdhocStateRoleNameToRoleIDMap &&
          !nonAdhocStateRoleNameToRoleIDMap.isEmpty())
      {
         actorNonAdhocRoles =
               PSWorkFlowUtils.applyMapList(userRoleNameList,
                                            nonAdhocStateRoleNameToRoleIDMap);

         if (null != actorNonAdhocRoles && !actorNonAdhocRoles.isEmpty())
         {
            actorRoles.addAll(actorNonAdhocRoles);
         }
      }

      /*
       * An actor adhoc normal role must meet 4 criteria
       * 1) be a role to which the user belongs
       * 2) be a role with an adhoc assignment type of adhoc normal in the
       *    state role context
       * 3) be a role with an adhoc assignment type of adhoc normal in the
       *    content adhoc users context
       * 4) actor's user name must be present in the normal adhoc list assigned
       * to the current state
      */
      if (userAdhocNormalRoles != null && !userAdhocNormalRoles.isEmpty())
      {
         List uAdhocNormalRoles = null;
         if (authUser == true)
         {
            //make sure that for a normal adhoc roles match
            if (null !=  adhocNormalStateRoleNameToRoleIDMap &&
               !adhocNormalStateRoleNameToRoleIDMap.isEmpty())
            {
                  actorAdhocNormalRoles = PSWorkFlowUtils.applyMapList(
                                          userRoleNameList,
                                          adhocNormalStateRoleNameToRoleIDMap);

                  uAdhocNormalRoles = actorAdhocNormalRoles;
            }
         }
         else
         {
            uAdhocNormalRoles = userAdhocNormalRoles;
         }


         if (uAdhocNormalRoles != null && !uAdhocNormalRoles.isEmpty())
         {
            addAdhocActorRoles(actorRoles, userName,
                               uAdhocNormalRoles,
                               adhocNormalUserNames,
                               stateAdhocNormalRoles,
                               authUser);
         }
      }


      /*
       * An actor adhoc anonymous role must meet 2 criteria
       * 1) be a role with an adhoc assignment type of adhoc anonymous in the
       *    state role context
       * 2) be a role with an adhoc assignment type of adhoc anonymous in the
       *    content adhoc users context
      */
      if(userAdhocAnonymousRoles != null && !userAdhocAnonymousRoles.isEmpty())
      {
          addAdhocActorRoles(actorRoles, userName,
                             userAdhocAnonymousRoles,
                             adhocAnonymousUserNames,
                             stateAdhocAnonymousRoles,
                             authUser);
      }

      /*
       * Ad hoc roles that have no ad hoc assigness are also effectively
       * non ad hoc
       */
      if (null != emptyAdhocNormalRoles && !emptyAdhocNormalRoles.isEmpty())
      {
         if (authUser == true)
         {
            actorAdhocNormalRoles =
              PSWorkFlowUtils.applyMapList(userRoleNameList,
                                           adhocNormalStateRoleNameToRoleIDMap);

            // for Adhoc normal the user needs to belong to the role
            emptyAdhocNormalRoles =
                  PSWorkFlowUtils.intersectLists(emptyAdhocNormalRoles,
                                                 actorAdhocNormalRoles);


         }

         if (null != emptyAdhocNormalRoles &&  !emptyAdhocNormalRoles.isEmpty())
         {
            actorRoles.addAll(emptyAdhocNormalRoles);
         }
      }

      if (null != emptyAdhocAnonymousRoles &&
          !emptyAdhocAnonymousRoles.isEmpty())
      {
         // for Adhoc anonymous the user need not belong to the role
         actorRoles.addAll(emptyAdhocAnonymousRoles);
      }

      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(cauc.getContentId(),
         actorRoles);

      return actorRoles;
   }


   /**
    * Add a list of adhoc roles if a user is acting as eather normal adhoc or
    * anonymous adhoc. If adhoc user name(s) are found then it compares current
    * user name with those adhoced. This user name check override user role by
    * only allowing the user with a user name who was adhoced to act on the state
    *
    * @param actorRoles a list of role ids in which the actor is acting
    * @param userName current user name
    * @param userAdhocRoles a list of role ids which were adhoced to the state
    * @param adhocUserNames a list of user names which were adhoced to the state
    * @param stateAdhocRoles a list of role ids who are state assignees
    * @param authUser if true indicates that PSExitAuthenticateUser
    * is the caller, false otherwise
   */
   private static void addAdhocActorRoles(List actorRoles,
                                          String userName,
                                          List userAdhocRoles,
                                          List adhocUserNames,
                                          List stateAdhocRoles,
                                          boolean authUser)
   {
      if(actorRoles == null || userName == null ||
         userAdhocRoles == null || userAdhocRoles.isEmpty())
      {
         return;
      }

      List resAdhocRoles = null;

      if (authUser == true && stateAdhocRoles!=null)
      {

          resAdhocRoles =
                 PSWorkFlowUtils.intersectLists(userAdhocRoles,
                                                stateAdhocRoles);
      }
      else
      {
          resAdhocRoles = userAdhocRoles;
      }

      if (adhocUserNames != null && !adhocUserNames.isEmpty())
      {
         //compare actor's user name with an adhoced user name
         //if case-insensitive match is found add user's role to the list
         for (Iterator i = adhocUserNames.iterator(); i.hasNext();)
         {
           String adhocUser = (String)i.next();

           if (userName.equalsIgnoreCase(adhocUser))
           {
              //this user was ad-hoced, allow him to act on the state
              if (resAdhocRoles!=null && !resAdhocRoles.isEmpty())
                  actorRoles.addAll(resAdhocRoles);

              break;
           }
         }
       }
       else
       {
           if (stateAdhocRoles!=null && !stateAdhocRoles.isEmpty())
           {
              //no user names have been adhoced yet
              resAdhocRoles = new ArrayList(stateAdhocRoles);

              if (resAdhocRoles!=null && !resAdhocRoles.isEmpty())
                  actorRoles.addAll(resAdhocRoles);
           }
       }
   }

   /**
    * Gets the highest assignment type for a list of state roles in which a
    * user is acting.
    *
    * @param src            state role context containing the roles
    *
    * @param actorRoleList  list of ID of roles in which the user is acting.
    *
    * @return               highest assignment type for the state roles
    */
   static public int getAssignmentType(PSStateRolesContext src,
                                       List actorRoleList)
   {
      int assignmentType = PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW;
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }

      if (null == actorRoleList || actorRoleList.isEmpty())
      {
         return assignmentType;
      }

      return getAssignmentType(src.getStateRoleAssignmentTypeMap(),
                               actorRoleList);
   }

   /**
    * Gets the highest assignment type for a list of state roles in which a
    * user is acting.
    *
    * @param assignmentTypeMap Map with role ID as key and value =
    *                          assignment type, cannot be <CODE>null</CODE>
    *
    * @param actorRoleList  list of ID of roles in which the user is acting.
    *                       may be <CODE>null</CODE> or empty
    *
    * @return               highest assignment type for the state roles or
    *              <CODE>PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW</CODE>
    *                       if the actor role list is <CODE>null</CODE> or
    *                       empty
    * @throws               IllegalArgumentException if any of the input
    *                       parameters is not valid.
    */
   static int getAssignmentType(Map assignmentTypeMap,
                                List actorRoleList)
   {
      // validate arguments
      int assignmentType = PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW;
      int assignmentTemp = 0;
      Integer roleID = null;

      if (null == assignmentTypeMap || assignmentTypeMap.isEmpty())
      {
          throw new IllegalArgumentException(
             "Assignment type map may not be null or empty.");
      }
      if (null == actorRoleList || actorRoleList.isEmpty())
      {
         return assignmentType;
      }

      Iterator roleIter = actorRoleList.iterator();
      while (roleIter.hasNext())
      {
         roleID = (Integer) roleIter.next();
         assignmentTemp = ((Integer)assignmentTypeMap.get(roleID)).intValue();
         if (assignmentTemp > assignmentType)
         {
             assignmentType = assignmentTemp;
         }
      }
      return assignmentType;
   }

   /**
    * Given a user name and a state role context, return a list of IDs for
    * the user's adhoc normal state roles
    *
    * @param userName name of user for whom ad hoc roles are desired
    *                 cannot be <CODE>null</CODE> or empty
    * @param src      context of state roles with assignment type of at
    *                 least assignee, cannot be <CODE>null</CODE>
    * @param request  the context of the request associated with
    *                 the extension from which the method is called.
    *                 cannot be <CODE>null</CODE>
    * @return         List of IDs for user's adhoc normal state roles
    *                 <CODE>null</CODE> if there are none
    * @throws         IllegalArgumentException if any of the input
    *                 parameters is not valid.
    */
   static List findAdhocNormalRoles(String userName,
                                    PSStateRolesContext src,
                                    IPSRequestContext request)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method findAdhocNormalRoles");

      Map adhocNormalStateRoleNameToRoleIDMap = null;
      List userRoleNameList = null;
      List actorAdhocNormalRoles = null;

      if (null == userName || 0 == userName.length())
      {
         throw new IllegalArgumentException(
            "User name may not be null or empty.");
      }

      userName = userName.trim();

      if (0 == userName.length())
      {
         throw new IllegalArgumentException(
            "User name may not be empty after trimming.");
      }

      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }
      userRoleNameList = getSubjectRoles(userName, request);

      if (null == userRoleNameList || userRoleNameList.isEmpty())
      {
         return null;
      }

      adhocNormalStateRoleNameToRoleIDMap =
            src.getAdhocNormalStateRoleNameToRoleIDMap();
      if (null == adhocNormalStateRoleNameToRoleIDMap ||
          adhocNormalStateRoleNameToRoleIDMap.isEmpty())
      {
         return null;
      }
      actorAdhocNormalRoles =
            PSWorkFlowUtils.applyMapList(
               PSWorkFlowUtils.lowerCaseList(userRoleNameList),
               adhocNormalStateRoleNameToRoleIDMap);
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method findAdhocNormalRoles");
      return actorAdhocNormalRoles;
   }

   /* Methods for classifying a list of proposed adhoc users */

   /**
    * Classify a list of proposed adhoc users producing a content adhoc users
    * context specifying adhoc normal users and their roles, and lists of
    * adhoc anonymous users and roles. For adhoc normal roles checks with
    * server that user is actually in that role.
    *
    * @param contentID       item content ID
    * @param adhocUserNames  list of proposed adhoc users to classify
    *                        may be <CODE>null</CODE> or empty
    * @param src             corresponding state role context
    *                        cannot be <CODE>null</CODE>
    * @param request         the context of the request associated with
    *                        the extension from which the method is called.
    *                        cannot be <CODE>null</CODE>
    * @return                content adhoc users context specifying adhoc
    *                        cannot be <CODE>null</CODE>
    *                        users and roles. Calling method must commit
    *                        the changes to the data base
    *                        <CODE>null</CODE> if the adhocUserNames list is
    *                        <CODE>null</CODE>, empty or contains no user names
    *
    * @throws                PSRoleException if a user cannot be given an ad
    *                        hoc role
    * @throws                IllegalArgumentException if any of the input
    *                        parameters is not valid.
    */
   @SuppressWarnings("unchecked")
   static PSContentAdhocUsersContext classifyAdhocUsers(
      int contentID,
      String adhocUserNames,
      PSStateRolesContext src,
      IPSRequestContext request)
      throws PSRoleException
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method classifyAdhocUsers");
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      String userName = "";
      PSContentAdhocUsersContext cauc =
            new PSContentAdhocUsersContext(contentID);
      List unassignedUserList = new ArrayList();
      List anonymousStateRoleIDs = src.getAdhocAnonymousStateRoleIDs();
      boolean existsAnonymousStateRole =
            (!anonymousStateRoleIDs.isEmpty());
      List adhocNormalRoles = null;

      if ((null == adhocUserNames) || adhocUserNames.trim().length() == 0)
      {
         return null;
      }

      List adhocUserNameList =
            PSWorkFlowUtils.tokenizeString(
               adhocUserNames, PSWorkFlowUtils.ADHOC_USER_LIST_DELIMITER);

      if (null ==  adhocUserNameList || adhocUserNameList.isEmpty())
      {
         return null;
      }

      Iterator iter = adhocUserNameList.iterator();

      while (iter.hasNext())
      {
         userName = (String) iter.next();
         if (null == userName)
         {
            continue;
         }

         adhocNormalRoles = findAdhocNormalRoles(userName, src, request);
         PSAssignmentTypeHelper.filterAssignedRolesByCommunity(contentID,
            adhocNormalRoles);

         if (null == adhocNormalRoles || adhocNormalRoles.isEmpty())
         {
            /*
             * Unassigned users will become adhoc anonymous if such a role
             * is available, otherwise an exception will be thrown listing
             * the unassigned users.
             */
            unassignedUserList.add(userName);
         }
         else
         {
            cauc.addUserAdhocNormalRoleIDs(userName,
                                           adhocNormalRoles);
         }

      }

      if (!unassignedUserList.isEmpty())
      {
         if (existsAnonymousStateRole)
         {
            // The unassigned users become adhoc anonymous
             cauc.setAdhocAnonymousUsersAndRoles(unassignedUserList,
                                                 anonymousStateRoleIDs);
         }
         else
         {
            // Throw exception: the unassigned users have no home
            throw new PSRoleException(lang,
             IPSExtensionErrors.ADHOC_ASSIGNMENT_NOT_FOUND,
              PSWorkFlowUtils.listToDelimitedString(unassignedUserList,", "));
         }
      }
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method classifyAdhocUsers");
      return cauc;
   }

   /* Get information for notification */

   /**
    * Given a list of role IDs, returns a list of ones with notification
    * turned on.
    *
    * @param roleList    list of role IDs to check for notification enabled
    * @param src         context of state roles with assignment type of at
    *                    least assignee
    *
    * @return            List of roles from <CODE>roleList</CODE> for which
    *                    notification is turned on. Returns <CODE>null</CODE>
    *                    if the role list is null, or no roles have
    *                    notification enabled.
    */
   static List filterRolesNotificationEnabled(List roleList,
                                              PSStateRolesContext src)
   {
      Map notifyEnabledMap = src.getIsNotificationOnMap();
      List notificationEnabledRoles =
            PSWorkFlowUtils.filterList(roleList, notifyEnabledMap);
      return notificationEnabledRoles;
   }

   /**
    * Given a state role context, returns a list of IDs of non adhoc roles with
    * notification  turned  on.
    * @param src         context of state roles with assignment type of at
    *                    least assignee,  cannot be <CODE>null</CODE>
    * @param contentId The content id of the item being processed.
    *
    * @return            List of IDs of non adhoc state roles for which
    *                    notification is turned on.
    * @throws            IllegalArgumentException if any of the input
    *                    parameters is not valid.
    */
   @SuppressWarnings("unchecked")
   static List getStateRoleIDNotificationList(PSStateRolesContext src,
      int contentId)
   {
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }
      Map notifyEnabledMap = src.getIsNotificationOnMap();
      filterNotifyEnabledMapByCommunity(contentId, notifyEnabledMap);
      List nonAdhocStateRoleIDs = src.getNonAdhocStateRoleIDs();
      List notificationEnabledRoles =
            PSWorkFlowUtils.filterList(nonAdhocStateRoleIDs,
                                       notifyEnabledMap);
      return notificationEnabledRoles;
   }

   /**
    * Given a state role context, return a list of names of non adhoc roles
    * with notification turned  on.
    * @param src         context of state roles with assignment type of at
    *                    least assignee, cannot be <CODE>null</CODE>
    * @param contentId The content ID of the item being processed.
    *
    * @return            List of non adhoc state role names for which
    *                    notification is turned on.
    * @throws            IllegalArgumentException if any of the input
    *                    parameters is not valid.
    */
   @SuppressWarnings("unchecked")
   static List getStateRoleNameNotificationList(PSStateRolesContext src,
      int contentId)
   {
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }

      List roleNameList = PSWorkFlowUtils.applyMapList(
         getStateRoleIDNotificationList(src, contentId),
         src.getStateRoleNameMap());

      return roleNameList;
   }

   /**
    * Convenience method that constructs a list of all adhoc actors for all
    * state roles that have notification on, validating role membership for
    * adhoc normal roles. Calls {@link
    * #getStateAdhocActorNotificationList(PSContentAdhocUsersContext,
    * PSStateRolesContext, int, IPSRequestContext, boolean)
    * getStateAdhocActorNotificationList( cauc, src, contentId, request, true )}
    */
   static List getStateAdhocActorNotificationList(
      IPSContentAdhocUsersContext cauc,
      PSStateRolesContext src,
      int contentId, IPSRequestContext request)
   {
      return getStateAdhocActorNotificationList(cauc, src, contentId, request,
         true);
   }

   /**
    * Constructs list of all adhoc actors for all state roles that have
    * notification on.
    * @param cauc        content ad hoc user context for the state
    *                    may be <CODE>null</CODE>
    * @param src         context of state roles with assignment type of at
    *                    least assignee,cannot be <CODE>null</CODE>
    * @param contentId   Content ID of the item being processed
    * @param request     the context of the request associated with
    *                    the extension from which the method is called.
    *                    may not be <CODE>null</CODE>
    * @param validateRoleMembership  <CODE>true</CODE> if users role membership
    *       should be validated for adhoc normal roles, else <CODE>false</CODE>
    *
    * @return            List of adhoc actors that should be notified. ,
    *                    <CODE>null</CODE> if content ad hoc user context is
    *                    null.
    * @throws            IllegalArgumentException if any of the input
    *                    parameters is not valid.
    */
   @SuppressWarnings("unchecked")
   static List getStateAdhocActorNotificationList(
      IPSContentAdhocUsersContext cauc,
      PSStateRolesContext src,
      int contentId,
      IPSRequestContext request, boolean validateRoleMembership)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getStateAdhocActorNotificationList");
      Set stateAdhocActorSet = new HashSet();

      List userAdhocNormalRoles = null;
      Iterator adhocNormalUserNameIter = null;

      List stateNotificationEnabledAdhocNormalRoles = null;
      List notificationEnabledAdhocAnonymousRoles = null;
      List userRoleList = null;
      String userName = "";

      //validation
      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }
      List stateAdhocNormalRoles = src.getAdhocNormalStateRoleIDs();
      List stateAdhocAnonymousRoles = src.getAdhocAnonymousStateRoleIDs();
      Map notifyEnabledMap = src.getIsNotificationOnMap();

      filterNotifyEnabledMapByCommunity(contentId, notifyEnabledMap);

      if (null == request && validateRoleMembership)
      {
         throw new IllegalArgumentException(
            "Request context may not be null if role membership" +
            "must be validated.");
      }

      List userAdhocAnonymousRoles;
      List adhocAnonymousUserNames;
      List adhocNormalUserNames;

      if (null == cauc)
      {
         userAdhocAnonymousRoles = new ArrayList();
         adhocAnonymousUserNames = new ArrayList();
         adhocNormalUserNames = new ArrayList();
      }
      else
      {
         userAdhocAnonymousRoles = cauc.getAdhocAnonymousRoleIDs();
         adhocAnonymousUserNames = cauc.getAdhocAnonymousUserNames();
         adhocNormalUserNames = cauc.getAdhocNormalUserNames();
      }


      stateNotificationEnabledAdhocNormalRoles =
            PSWorkFlowUtils.filterList(stateAdhocNormalRoles,
                                       notifyEnabledMap);
      List unnotifiedAdhocRoleIds = new ArrayList();

      /*
       * All adhoc anonymous users will be added if at least one of the adhoc
       * anonymous roles is an adhoc anonymous state role, and notification
       * has been turned on for that role.
       */

      notificationEnabledAdhocAnonymousRoles =
            PSWorkFlowUtils.intersectLists(userAdhocAnonymousRoles,
                                           stateAdhocAnonymousRoles);
      notificationEnabledAdhocAnonymousRoles =
            PSWorkFlowUtils.filterList(notificationEnabledAdhocAnonymousRoles,
                                       notifyEnabledMap);
      if (null != notificationEnabledAdhocAnonymousRoles &&
          !notificationEnabledAdhocAnonymousRoles.isEmpty())
      {
         stateAdhocActorSet.addAll(adhocAnonymousUserNames);
      }
      else
      {
         // if no anonymous adhoc users were assigned for roles with notify on,
         // then notify all users for those roles
         unnotifiedAdhocRoleIds.addAll(PSWorkFlowUtils.filterList(
            stateAdhocAnonymousRoles, notifyEnabledMap));
      }

      if (null !=  adhocNormalUserNames)
      {
         Set notifiedAdhocRoleIds = new HashSet();
         adhocNormalUserNameIter = adhocNormalUserNames.iterator();
         while (adhocNormalUserNameIter.hasNext())
         {
            userName = (String)  adhocNormalUserNameIter.next();
            userAdhocNormalRoles = cauc.getUserAdhocNormalRoleIDs(userName);
            userAdhocNormalRoles = PSWorkFlowUtils.intersectLists(
               userAdhocNormalRoles, stateNotificationEnabledAdhocNormalRoles);
            if (validateRoleMembership &&
                null != userAdhocNormalRoles && !userAdhocNormalRoles.isEmpty())
            {
               userRoleList = getSubjectRoleIDs(userName, src, request);
               userRoleList = PSWorkFlowUtils.intersectLists(
                  userAdhocNormalRoles, userRoleList);
            }
            if (null != userRoleList && !userRoleList.isEmpty())
            {
               stateAdhocActorSet.add(userName);
               notifiedAdhocRoleIds.addAll(userRoleList);
            }
         }
         stateNotificationEnabledAdhocNormalRoles.removeAll(
            notifiedAdhocRoleIds);
         unnotifiedAdhocRoleIds.addAll(
            stateNotificationEnabledAdhocNormalRoles);
      }

      // add all usernames for unnotified roles
      stateAdhocActorSet.addAll(getRoleSubjectNames(src, request,
         unnotifiedAdhocRoleIds));

      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getStateAdhocActorNotificationList");
      return new ArrayList(stateAdhocActorSet);
   }

   /**
    * Determines if the "notify add-hoc users only" feature is enabled.
    * @return <code>true</code> if it is enabled.
    */
   static boolean isNotifyAddHocOnly()
   {
      return "true".equals(getProperty("notifyAddHocUsersOnly"));
   }

   /**
    * Filters the supplied map of role IDs to notification setting by community.
    * See {@link PSAssignmentTypeHelper#filterAssignedRolesByCommunity(int,
    * Collection)} for details.
    *
    * @param contentId The contentId of the item being processed.
    * @param notifyEnabledMap Map of workflow role id to its notification
    * setting value (value is not read by this method), assumed not
    * <code>null</code>.  Entries are removed from the map.
    */
   @SuppressWarnings("unchecked")
   private static void filterNotifyEnabledMapByCommunity(int contentId,
      Map notifyEnabledMap)
   {
      Set<Integer> notifyRoleIds =
         new HashSet<>(notifyEnabledMap.keySet());
      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(contentId,
         notifyRoleIds);
      Set<Integer> nonNotifyRoleIds =
         new HashSet<>(notifyEnabledMap.keySet());
      nonNotifyRoleIds.removeAll(notifyRoleIds);
      for (Integer roleId : nonNotifyRoleIds)
      {
         notifyEnabledMap.remove(roleId);
      }
   }

/* Methods that get subject and role information from the server */

   /**
    * Gets a list of IDs of a subject's state roles
    *
    * @param subject  name of subject for whom roles are requested
    *                 cannot be <CODE>null</CODE> or empty
    * @param src      corresponding state role context
    *                 cannot be <CODE>null</CODE>
    * @param request  the context of the request associated with
    *                 the extension from which the method is called
    *                 cannot be <CODE>null</CODE>
    * @return         list of subject's state role IDs, <CODE>null</CODE>
    *                 if they have no roles
    * @throws         IllegalArgumentException if any of the input
    *                 parameters is not valid.
    */
   public static List getSubjectRoleIDs(String subject,
                                        PSStateRolesContext src,
                                        IPSRequestContext request)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getSubjectRoleIDs");

      List subjectRoleNames = null;
      List subjectRoleIDs = null;

      if (null == subject || 0 == subject.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be null or empty.");
      }

      subject = subject.trim();

      if (0 == subject.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be empty after trimming.");
      }

      if (null == src)
      {
         throw new IllegalArgumentException(
            "State roles context may not be null.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

      subjectRoleNames = getSubjectRoles(subject, request);
      if (null == subjectRoleNames || subjectRoleNames.isEmpty())
      {
         return null;
      }
      subjectRoleIDs =  PSWorkFlowUtils.applyMapList(
         PSWorkFlowUtils.lowerCaseList(subjectRoleNames),
         src.getLowerCaseRoleNameToIDMap());

      if (null == subjectRoleIDs || subjectRoleIDs.isEmpty())
      {
         return null;
      }
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getSubjectRoleIDs");
      return subjectRoleIDs;
   }

   /**
    * Gets a list of names of a subject's state roles
    *
    * @param subject  name of subject for whom roles are requested,
    *                 cannot be <CODE>null</CODE> or empty
    * @param request  the context of the request associated with
    *                 the extension from which the method is called
    *                 cannot be <CODE>null</CODE>
    * @return         list of subject's state role names
    * @throws         IllegalArgumentException if any of the input
    *                 parameters is not valid.
    */
   public static List getSubjectRoles(String subject,
                                      IPSRequestContext request)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getSubjectRoles");
      List roleList = null;

      if (null == subject || 0 == subject.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be null or empty.");
      }

      subject = subject.trim();

      if (0 == subject.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be empty after trimming.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

      roleList = request.getSubjectRoles(subject);
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getSubjectRoles");
      return roleList;
   }


   /**
    * Gets (from the server) a list of subjects belonging to a role
    *
    * @param roleName role for which subjects are desired,
    *                 cannot be <CODE>null</CODE> or empty
    * @param request  the context of the request associated with
    *                 the extension from which the method is called.
    *                 cannot be <CODE>null</CODE>
    * @return         a list of subjects belonging to the role
    * @throws                   IllegalArgumentException if any of the input
    *                           parameters is not valid.
    */
   public static List getRoleSubjects(String roleName,
                                      IPSRequestContext request)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getRoleSubjects");
      List subjectList = null;

      if (null == roleName || 0 == roleName.length())
      {
         throw new IllegalArgumentException(
            "Role name may not be null or empty.");
      }

      roleName = roleName.trim();

      if (0 == roleName.length())
      {
         throw new IllegalArgumentException(
            "Role name may not be empty after trimming.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }
      subjectList = request.getRoleSubjects(roleName);
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getRoleSubjects");
      return subjectList;
   }

   /**
    * Gets a list of Strings containing the attribute text values for a
    * specified subject and attribute name; a list containing a specified
    * default string is returned if the subject's attribute list does not
    * contain a value for the attribute.
    *
    * @param subject        subject object for which attribute is desired
    * @param attributeName  name of the attribute to be found
    *                       cannot be <CODE>null</CODE> or empty
    * @param defaultValue   default value if an attribute value is not found
    *
    * @return               a list of subjects each of which has an attribute
    *                       list that is empty, or contains an attribute list
                            for the desired attribute.
    * @throws               IllegalArgumentException if the subject is
    *                       is <CODE>null</CODE> or the attribute name
    *                       is <CODE>null</CODE> or empty.
    */
   public static List getSubjectAttributeValueTextList(PSSubject subject,
                                                       String attributeName,
                                                       String defaultValue)
   {
      PSAttributeList attributeList = null;
      PSAttribute attribute = null;
      List attributeValueList = null;
      List defaultValueList = new ArrayList();

      defaultValueList.add(defaultValue);

      if ( null == subject )
      {
         throw new IllegalArgumentException(
               "subject  name can't be null or empty" );
      }

      if (null == attributeName || 0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be null or empty.");
      }

      attributeName = attributeName.trim();

      if (0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be empty after trimming.");
      }

      /*
       * Return a list containing the default value if
       * The attribute list is empty,
       * The attribute list does not contain the specified attribute, or
       * The specified attribute list is empty.
       */
      attributeList = subject.getAttributes();

      if (0 == attributeList.size())
      {
         return defaultValueList;
      }

      attribute = attributeList.getAttribute(attributeName);

      attributeValueList = attribute.getValues();

      if (attributeValueList.isEmpty())
      {
          return defaultValueList;
      }
      else
      {
         return attributeValueList;
      }
   }

   /**
    * Gets (from the server) a list of all subjects belonging to a role for
    * which each subject has an attribute list that is empty, or contains an
    * attribute list for a specified attribute.
    *
    * @param roleName       role for which the subjects are desired,
    *                       cannot be <CODE>null</CODE> or empty
    * @param attributeName  name of the attribute to be found
    *                       cannot be <CODE>null</CODE> or empty
    * @param request        the context of the request associated with
    *                       the extension from which the method is called.
    *                       cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the community filter.
    * @return               a list of subjects each of which has an attribute
    *                       list that is empty, or contains an attribute list
                            for the desired attribute.
    * @throws               IllegalArgumentException if any of the input
    *                       parameters is not valid.
    */
   public static List getRoleSubjectsGlobalAttribute(String roleName,
      String attributeName, IPSRequestContext request, String communityId)
   {
      List subjectsWithAttribute = null;

      if (null == roleName || 0 == roleName.length())
      {
         throw new IllegalArgumentException(
            "Role name may not be null or empty.");
      }

      roleName = roleName.trim();

      if (0 == roleName.length())
      {
         throw new IllegalArgumentException(
            "Role name may not be empty after trimming in " +
            "getRoleMemberEmailAddresses.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

     if (null == attributeName || 0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be null or empty.");
      }

      attributeName = attributeName.trim();
      if (0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be empty after trimming in " +
            "method getRoleSubjectsGlobalAttribute.");
      }

      subjectsWithAttribute = request.getSubjectGlobalAttributes(
         null,          // get all users
         0,             // get all subject types
         roleName,
         attributeName,
         true,          // include the attribute list even if it is empty
         communityId);  // filtered by the supplied community

      return subjectsWithAttribute;
   }

   /**
    * Gets (from the server) a list of all subjects belonging to a subject for
    * which each subject has an attribute list that is empty, or contains an
    * attribute list for a specified attribute.
    *
    * @param subjectName    subject for which the attribute is desired,
    *                       cannot be <CODE>null</CODE> or empty
    * @param attributeName  name of the attribute to be found
    *                       cannot be <CODE>null</CODE> or empty
    * @param request        the context of the request associated with
    *                       the extension from which the method is called.
    *                       cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the community filter.
    * @return               a list of subjects each of which has an attribute
    *                       list that is empty, or contains an attribute list
                            for the desired attribute.
    * @throws               IllegalArgumentException if any of the input
    *                       parameters is not valid.
    */
   public static List getSubjectGlobalAttribute(String subjectName,
      String attributeName, IPSRequestContext request, String communityId)
   {
      List subjectWithAttribute = null;

      if (null == subjectName || 0 == subjectName.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be null or empty.");
      }

      subjectName = subjectName.trim();

      if (0 == subjectName.length())
      {
         throw new IllegalArgumentException(
            "Subject name may not be empty after trimming in " +
            "getSubjectMemberEmailAddresses.");
      }

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

     if (null == attributeName || 0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be null or empty.");
      }

      attributeName = attributeName.trim();

      if (0 == attributeName.length())
      {
         throw new IllegalArgumentException(
            "Attribute name may not be empty after trimming in " +
            "method getSubjectSubjectsGlobalAttribute.");
      }

      subjectWithAttribute = request.getSubjectGlobalAttributes(
         subjectName,
         0,             // get all subject types
         null,          // get any role
         attributeName,
         true,          // include the attribute list even if it is empty
         communityId);  // filtered by the supplied community

      return subjectWithAttribute;
   }

   /**
    * Gets a list of the email addresses of all subjects belonging to any role
    * in a role list, using the subject name for role members that do not have
    * the email attribute.
    *
    * @param roleList list of roles for which  email addresses of subjects are
    *                 desired, can be <CODE>null</CODE> or empty
    * @param request  the context of the request associated with
    *                 the extension from which the method is called.
    *                 cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the community filter.
    * @return         a list (never <CODE>null</CODE> may be empty) containing
    *                 for each role subject, either
    *                 <ul><li>their email address(es) given by the system
    *                 global email address attribute, or </<li>
    *                 <li>their subject name</li> </ul>
    *                 All redundant (considered case insensitively) addresses
    *                 are removed.
    * @throws         IllegalArgumentException if any of the input
    *                 parameters is not valid.
    */
   public static List<String> getRolesEmailAddresses(List roleList,
      IPSRequestContext request, String communityId)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getRolesEmailAddresses");
      List<String> membersEmail = new ArrayList<>();
      String roleName = "";
      List<String> roleEmailAddresses = new ArrayList<>();
      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

      if (null == roleList || roleList.isEmpty())
      {
         return membersEmail;
      }

      roleList = PSWorkFlowUtils.caseInsensitiveUniqueList(roleList);

      if (null == roleList || roleList.isEmpty())
      {
         return membersEmail;
      }
      Iterator iter = roleList.iterator();

      while (iter.hasNext())
      {
         roleName = (String) iter.next();
         if (null == roleName || 0 == roleName.length())
         {
            continue;
         }
         roleName = roleName.trim();
         if (0 == roleName.length())
         {
            continue;
         }
         roleEmailAddresses = getRoleEmailAddresses(roleName, request,
            communityId);
         if (null !=  roleEmailAddresses && !roleEmailAddresses.isEmpty())
         {
            membersEmail.addAll(roleEmailAddresses);
         }
      }

      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getRolesEmailAddresses");
      return PSWorkFlowUtils.caseInsensitiveUniqueList(membersEmail);

   }

   /**
    * Gets  a list of email addresses of subjects belonging to
    * a role, using the subject name for role members that do not have the
    * email attribute.
    *
    * @param roleName role for which  email addresses of subjects are desired,
    *                 cannot be <CODE>null</CODE> or empty
    * @param request  the context of the request associated with
    *                 the extension from which the method is called.
    *                 cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the community filter.
    * @return         a list containing for each role subject, either
    *                 <ul><li>their email address(es) given by the system
    *                 global email address attribute, or </<li>
    *                 <li>their subject name</li> </ul>
    * @throws         IllegalArgumentException if any of the input
    *                 parameters is not valid.
    */
   public static List getRoleEmailAddresses(String roleName,
      IPSRequestContext request, String communityId)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getRoleEmailAddresses");

      if (null == roleName || 0 == roleName.length())
         throw new IllegalArgumentException(
            "Role name may not be null or empty.");

      roleName = roleName.trim();

      if (0 == roleName.length())
         throw new IllegalArgumentException(
            "Role name may not be empty after trimming.");

      if (null == request)
         throw new IllegalArgumentException(
            "Request context may not be null.");

      List membersEmail = new ArrayList();
      String emailAttributeName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE_PROPERTY,
            PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE);
      membersEmail.addAll(request.getRoleEmailAddresses(roleName,
         emailAttributeName, communityId));

      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getRoleEmailAddresses");

      return membersEmail;
   }

   /**
    * Just like {@link #getRolesEmailAddresses(List, IPSRequestContext, String)}.
    * @param princes not null maybe empty.
    * @param communityId maybe null.
    * @return not null maybe empty.
    */
   protected static List<String> getRoleEmailAddresses(
         Collection<IPSTypedPrincipal> princes, String communityId)
   {
      List membersEmail = new ArrayList();
      String emailAttributeName = PSWorkFlowUtils.properties.getProperty(
            PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE_PROPERTY,
            PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE);

      List<String> emails = new ArrayList<>();
      emails.addAll(PSRoleManager.getInstance().getSubjectEmailAddresses(princes, emailAttributeName, null));

      return emails;
   }

   /**
    *
    * @param princes
    * @param contentid
    * @param revisionid
    * @return
    * @throws Exception
    */
   protected static Set<IPSTypedPrincipal> filterPrincipalsByCommunityAndFolderSecurity(
         final Collection<IPSTypedPrincipal> princes, final int contentid, int revisionid)
         throws Exception
   {

      final int itemCommunityId = getCommunityId(contentid);

      Set<IPSTypedPrincipal> commFiltered = PSRoleManager.getInstance().filterByCommunity(princes, "" + itemCommunityId, true);
      if ( ! PSCms.isFolderSecurityOverridesWorkflowSecurity()) {
         return commFiltered;
      }
      final Set<IPSTypedPrincipal> filteredPrinces = new HashSet<>();

      PSRunAsUser<IPSTypedPrincipal> runAsUser = new PSRunAsUser<IPSTypedPrincipal>()
      {
         @Override
         protected void run(IPSTypedPrincipal p, PSRequest newRequest) throws Exception
         {
            IPSRequestContext rc = new PSRequestContext(newRequest);
            if (PSCms.canWriteInFolders(contentid)) {
               filteredPrinces.add(p);
            }
         }
      };

      for (IPSTypedPrincipal p : commFiltered) {
         if (p.getPrincipalType() == PrincipalTypes.SUBJECT) {
            runAsUser.run(p.getName(), p);
         }
         else {
            filteredPrinces.add(p);
         }
      }
      return filteredPrinces;
   }

   /**
    * Gets the CM system from the list of roles.
    * This does not expand groups.
    * @param fromStateRoles not null maybe empty.
    * @return not null maybe empty.
    */
   protected static Set<IPSTypedPrincipal> getPrincipals(
         List<String> fromStateRoles)
   {
      try
      {
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         Set<IPSTypedPrincipal> fromStatePrinces = new HashSet<>();
         for (String roleName : fromStateRoles) {
            fromStatePrinces.addAll(roleMgr.getRoleMembers(roleName));
         }
         return fromStatePrinces;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Converts a list of user names to a list of principals
    * @param userNames the list of user names, not <code>null</code>, may be empty.
    * @return the list of converted principals, never <code>null</code>, may be empty.
    */
   public static Set<IPSTypedPrincipal> getPricipalsFromNames(List<String> userNames)
   {
      notNull(userNames);
      Set<IPSTypedPrincipal> principals = new HashSet<>();
      for (String user : userNames)
         principals.add(PSTypedPrincipal.createSubject(user));

      return principals;
   }

   /**
    * Gets the community id from an item.
    * @param contentid not null, valid content id.
    * @return community id.
    */
   private static int getCommunityId(int contentid)
   {
      /*
       * TODO: Should we use the item cache here?
       */
      final PSComponentSummary summary =
         PSCmsObjectMgrLocator.getObjectManager().loadComponentSummaries(singletonList(contentid)).get(0);
      final int itemCommunityId = summary.getCommunityId();
      return itemCommunityId;
   }

  /**
    * Gets a list of the email addresses of all subjects
    * in a list, using the subject name for list members that do not have
    * the email attribute.
    *
    * @param subjectList list of subjects for which  email addresses are
    *                    desired, can be <CODE>null</CODE> or empty
    * @param request     the context of the request associated with
    *                    the extension from which the method is called.
    *                    cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the ccommunity filter.
    * @return            a list (never <CODE>null</CODE> may be empty)
    *                    containing for each subject, either
    *                    <ul><li>their email address(es) given by the system
    *                    global email address attribute, or </<li>
    *                    <li>their subject name</li> </ul>
    *                    All redundant (considered case insensitively)
    *                    addresses are removed.
    * @throws            IllegalArgumentException if any of the input
    *                    parameters is not valid.
    */
   public static List getSubjectsEmailAddresses(List subjectList,
      IPSRequestContext request, String communityId)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getSubjectsEmailAddresses");
      List subjectsEmail = new ArrayList();
      String subjectName = "";

      if (null == request)
      {
         throw new IllegalArgumentException(
            "Request context may not be null.");
      }

      if (null == subjectList || subjectList.isEmpty())
      {
         return subjectsEmail;
      }

      subjectList = PSWorkFlowUtils.caseInsensitiveUniqueList(subjectList);

      if (null == subjectList || subjectList.isEmpty())
      {
         return subjectsEmail;
      }

      Iterator iter = subjectList.iterator();

      while (iter.hasNext())
      {
         subjectName = (String) iter.next();
         if (null == subjectName || 0 == subjectName.length())
         {
            continue;
         }
         subjectName = subjectName.trim();
         if (0 == subjectName.length())
         {
            continue;
         }
         subjectsEmail.addAll(getSubjectEmailAddresses(subjectName, request,
            communityId));
      }

      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getSubjectsEmailAddresses");
      return PSWorkFlowUtils.caseInsensitiveUniqueList(subjectsEmail);

   }

   /**
    * Gets  a list of email addresses of subjects belonging to
    * a subject, using the subject name if the subject does not have the
    * email attribute.
    *
    * @param subjectName subject for which  email addresses are desired,
    *                    cannot be <CODE>null</CODE> or empty
    * @param request     the context of the request associated with
    *                    the extension from which the method is called.
    *                    cannot be <CODE>null</CODE>
    * @param communityId the community id by which to filter the subjects to
    *    which the notifications are sent, may be <code>null</code> to
    *    ignore the community filter.
    * @return            a list either
    *                    <ul><li>the email address(es) given by the system
    *                    global email address attribute, or </<li>
    *                    <li>the subject name</li> </ul>
    * @throws            IllegalArgumentException if any of the input
    *                    parameters is not valid.
    */
   public static List getSubjectEmailAddresses(String subjectName,
      IPSRequestContext request, String communityId)
   {
      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Entering Method getSubjectEmailAddresses");

      if (null == subjectName || 0 == subjectName.length())
         throw new IllegalArgumentException(
            "Subject name may not be null or empty.");

      subjectName = subjectName.trim();
      if (0 == subjectName.length())
         throw new IllegalArgumentException(
            "Subject name may not be empty after trimming.");

      if (null == request)
         throw new IllegalArgumentException(
            "Request context may not be null.");

      List subjectsEmail = new ArrayList();
      String emailAttributeName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE_PROPERTY,
            PSWorkFlowUtils.USER_EMAIL_ATTRIBUTE);
      subjectsEmail.addAll(request.getSubjectEmailAddresses(subjectName,
            emailAttributeName, communityId));

      PSWorkFlowUtils.printWorkflowMessage(
         request, "    Exiting Method getSubjectEmailAddresses");
      return subjectsEmail;
   }

   /**
    * Get the list of subject names that are members of the supplied list of
    * role ids.
    *
    * @param src Used to convert the role ids to role names, may not be
    * <code>null</code>.
    * @param request The request used to get the role membership from the
    * server, may not be <code>null</code>.
    * @param roleIds The list of role ids as <code>Integer</code> objects,
    * may not be <code>null</code>, may be empty.
    *
    * @return The role memeber subject names as <code>String</code> objects,
    * never <code>null</code>, may be empty.
    */
   public static List getRoleSubjectNames(
      PSStateRolesContext src,
      IPSRequestContext request,
      List roleIds)
   {
      Set subjectNames = new HashSet();
      if (!roleIds.isEmpty())
      {
         Set subjects = new HashSet();
         List roleNameList = roleIDListToRoleNameList(roleIds,
            src);
         if (roleNameList != null)
         {
            Iterator roleNames = roleNameList.iterator();
            while (roleNames.hasNext())
            {
               String roleName = (String)roleNames.next();
               subjects.addAll(request.getRoleSubjects(roleName));
            }
         }

         Iterator subjectIter = subjects.iterator();
         while (subjectIter.hasNext())
         {
            PSSubject subject = (PSSubject)subjectIter.next();
            subjectNames.add(subject.getName());
         }
      }

      return new ArrayList(subjectNames);
   }


   /**
    * Logger for this class.
    */
   public static final Logger log = LogManager.getLogger(PSWorkflowRoleInfoStatic.class);

   /**
    * Key used to set and obtain the <CODE>PSWorkflowRoleInfo</CODE>
    * private object created by <CODE>IPSRequestContext.setPrivateObject</CODE>
    */
   public static final String WORKFLOW_ROLE_INFO_PRIVATE_OBJECT=
                              "workflowroleinfoprivateobject";
}


