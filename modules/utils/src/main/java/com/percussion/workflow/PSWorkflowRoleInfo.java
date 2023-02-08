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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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
public class PSWorkflowRoleInfo implements IWorkflowRoleInfo
{

   /* Getters & setters for state variables */

   /**
    * Gets the list of IDs of state roles in which a user is acting
    *
    * @return the list of IDs of state roles in which a user is acting
    */
   public List getUserActingRoleIDs()
   {
      return m_userActingRoleIDs;
   }

   /**
    * Gets the list of names of state roles in which a user is acting
    *
    * @return the list of names of state roles in which a user is acting
    */
   public List getUserActingRoleNames()
   {
      return m_userActingRoleNames;
   }


   /**
    * Gets the value of the content adhoc users context for the transition "to"
    * state
    *
    * @return Content adhoc users context for the transition "to" state
    */
   public IPSContentAdhocUsersContext getToStateCauc()
   {
      return m_toStateCauc;
   }

   /**
    * Gets the value of the content adhoc users context for the transition
    * "from" state
    *
    * @return Content adhoc users context for the transition "from" state
    */
   public IPSContentAdhocUsersContext getFromStateCauc()
   {
      return m_fromStateCauc;
   }

   /**
    * Sets the list of IDs of state roles in which a user is acting
    *
    * @param userActingRoleIDs  list of IDs of state roles in which a user is
    *                           acting
    */
   public void setUserActingRoleIDs(List userActingRoleIDs)
   {
      m_userActingRoleIDs = userActingRoleIDs;
   }

   /**
    * Sets the list of names of state roles in which a user is acting
    *
    * @param userActingRoleNames  list of names of state roles in which a user
    *                             is acting
    */
   public void setUserActingRoleNames(List userActingRoleNames)
   {
      m_userActingRoleNames = userActingRoleNames;
   }

   /**
    * Gets the value of the content adhoc users context for the transition "to"
    * state
    *
    * @param toStateCauc  content adhoc users context for the transition
    *                     "to" state
    */
   public void setToStateCauc(IPSContentAdhocUsersContext toStateCauc)
   {
      m_toStateCauc = toStateCauc;
   }

   /**
    * Sets the value of the content adhoc users context for the transition
    * "from" state
    *
    * @param fromStateCauc  content adhoc users context for the transition
    *                       "from" state
    */
   public void setFromStateCauc(IPSContentAdhocUsersContext fromStateCauc)
   {
      m_fromStateCauc = fromStateCauc;
   }


   /** list of IDs of state roles in which a user is acting */
   private List m_userActingRoleIDs = null;

    /** list of names of state roles in which a user is acting */
   private List m_userActingRoleNames = null;

   /** Content adhoc users context for the transition "to" state */
   private IPSContentAdhocUsersContext m_toStateCauc = null;

   /** Content adhoc users context for the transition "from" state */
   private IPSContentAdhocUsersContext m_fromStateCauc = null;
   
   
   /**
    * Logger for this class.
    */
   public static final Logger log = LogManager.getLogger(PSWorkflowRoleInfo.class);


}


