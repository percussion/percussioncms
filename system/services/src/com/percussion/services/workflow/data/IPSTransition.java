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

package com.percussion.services.workflow.data;

import com.percussion.services.workflow.data.PSTransitionRole;

import java.util.List;

/**
 * The interface for (non-aging) transition
 *  
 * @author YuBingChen
 */
public interface IPSTransition extends IPSTransitionBase
{
   /**
    * Are all state roles allowed to use this transition?
    * 
    * @return <code>true</code> if they are, <code>false</code> otherwise.
    */
   boolean isAllowAllRoles();

   /**
    * Set if all state roles are allowed to use this transition.
    * 
    * @param allowAll <code>true</code> if they are, <code>false</code> if a 
    * list of transition roles is to be specified (see {@link #getTransitionRoles()})
    */
   void setAllowAllRoles(boolean allowAll);  
   
   /**
    * Get the number of approvals required for this transition.
    * 
    * @return the number of approvals required for this transition.
    */
   int getApprovals();

   /**
    * Set the number of approvals required for this transition.
    * 
    * @param number The number of approvals.
    */
   void setApprovals(int number);

   /**
    * Get the comment requirements for this workflow.
    * 
    * @return the comment requirement for this transition, never 
    * <code>null</code>.
    */
   PSTransition.PSWorkflowCommentEnum getRequiresComment();
   
   /**
    * Set the comment requirements for this workflow.
    * 
    * @param requirement The comment requirement, may not be <code>null</code>.
    */
   void setRequiresComment(PSTransition.PSWorkflowCommentEnum requirement);
   
   /**
    * Is this the default transition for the from state?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   boolean isDefaultTransition();
   
   /**
    * Set if this is the default transition for the from state.
    * 
    * @param isDefault <code>true</code> if it is the default, 
    * <code>false</code> if not.
    */
   void setDefaultTransition(boolean isDefault);
   
   /**
    * Get the roles allowed to use this transition. Is only used if 
    * {@link #isAllowAllRoles()} returns <code>false</code>.
    * 
    * @return a list with all allowed roles which may use this transition,
    *    never <code>null</code>, may be empty.
    */
   List<PSTransitionRole> getTransitionRoles();
   
   /**
    *  Set toles roles allowed to use this transition. Is only used if 
    * {@link #isAllowAllRoles()} returns <code>false</code>.
    * 
    * @param roleList The list of roles, may be <code>null</code> or empty.
    */
   void setTransitionRoles(List<PSTransitionRole> roleList);
   
   
}
