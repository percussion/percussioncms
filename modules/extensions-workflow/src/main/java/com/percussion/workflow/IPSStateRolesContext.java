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

/**
 * An interface that defines methods for State-Roles Context. The table joins
 * are hidden from the user.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
import java.util.List;
import java.util.Map;

@Deprecated
public interface IPSStateRolesContext
{
   /**
    * Gets a list of all state role IDs
    *
    * @return list of all state role IDs
    */
   public List getStateRoleIDs();
   
   /**
    * Gets a list of non adhoc state role IDs
    *
    * @return list of non adhoc state role IDs
    */
   public List getNonAdhocStateRoleIDs();

   /**
    * Gets a list of adhoc normal state role IDs
    *
    * @return list of adhoc normal state role IDs
    */
   public List getAdhocNormalStateRoleIDs();
   
   /**
    * Gets a list of adhoc anonymous state role IDs
    *
    * @return list of adhoc anonymous state role IDs
    */
   public List getAdhocAnonymousStateRoleIDs();
   
   /**
    * Gets map with role ID as key and value = assignment type, which can take
    * values defined in  {@link PSWorkFlowUtils}
    *
    * @return role ID to assignment type map
    */
   public Map getStateRoleAssignmentTypeMap();
   
   /**
    * Gets map with role ID as key and value = role name
    *
    * @return map with role ID as key and value = role name
    */
   public Map getStateRoleNameMap();
   
   /**
    * Gets map with role ID as key and value <CODE>true</CODE> if notification
    * for the role is on, else <CODE>false</CODE>
    *
    * @return map from role ID to <CODE>true</CODE>/<CODE>false</CODE> for
    *         notification on/off.
    */
   public Map getIsNotificationOnMap();
   
   /**
    * Gets map of role IDs for nonadhoc roles with trimmed lower case role
    * names as key
    *
    * @return map from lower case nonadhoc role names to role IDs
    */
   public Map getNonAdhocStateRoleNameToRoleIDMap();

   /**
    * Gets map of role IDs for adhoc normal roles with trimmed lower case role
    * names as key
    *
    * @return map from lower case adhoc normal role names to role IDs
    */
   public Map getAdhocNormalStateRoleNameToRoleIDMap();

   /**
    * Gets map of role IDs for all state roles with trimmed lower case
    * role names as key
    *
    * @return map from lower case state role names to role IDs
    */
   public Map getLowerCaseRoleNameToIDMap();

   /**
    * Gets the number of roles assgined for the current state. This is more 
    * than 1 formultiple assignments.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @param   none
    *
    * @return  role count as int
    */
   public int getStateRoleCount();
   
   /**
    * Indicates whether the context has any entries.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @param   none
    *
    * @return  <CODE>true</CODE> if empty, else <CODE>false</CODE>
    */
      public boolean isEmpty();
}
