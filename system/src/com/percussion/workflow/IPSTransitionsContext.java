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

import java.sql.SQLException;
import java.util.List;
/**
 * An interface that defines methods for transitions context. 
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */

public interface IPSTransitionsContext
{
   public static final int NORMAL_TRANSITION = 0;
   public static final int AGING_TRANSITION = 1;
   public static final int ABSOLUTE_INTERVAL_AGING_TRANSITION = 1;
   public static final int REPEATED_INTERVAL_AGING_TRANSITION = 2;
   public static final int SYSTEM_FIELD_AGING_TRANSITION = 3;

   /*
    * String indicating that no additional restriction is placed on roles
    * allowed perform a given transition. This is required because the splitter
    * does not support empty attributes.
    */
   public static final String NO_TRANSITION_ROLE_RESTRICTION = "*ALL*";
   /*
    * String indicating that an additional restriction is placed on roles
    * allowed perform a given transition.
    */
   public static final String SPECIFIED_ROLE_TRANSITION_RESTRICTION =
      "*Specified*";
   /**
    * Gets the TransitionID
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  TransitionID 
    */
   public int getTransitionID();

   /**
    * Gets the Transition Label
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition Label 
    */
   public String getTransitionLabel();

   /**
    * Gets the Transition Prompt
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition Prompt 
    */
   public String getTransitionPrompt();

   /**
    * Gets the Transition Description
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition Description 
    */
   public String getTransitionDescription();

   /**
    * Gets the Transition Trigger
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition Trigger 
    */
   public String getTransitionActionTrigger();

   /**
    * Gets the Transition Approvals Required
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition Approvals Required 
    */
   public int getTransitionApprovalsRequired();

   /**
    * Gets the Transition From StateID
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition  From StateID 
    */
   public int getTransitionFromStateID();

   /**
    * Gets the Transition  To StateID
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  Transition To StateID 
    */
   public int getTransitionToStateID();

   /**
    * Indicates whether the transition is to initial state of the workflow
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if the transition is to initial state,
    *          else <CODE>false</CODE>.
    */
   public boolean isTransitionToInitialState();
   
   /**
    * Indicates whether the transition is to a different state.
    * @author   Aaron Brandes
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if the transition is to a different state.
    *          else <CODE>false</CODE>.
    */
   public boolean isTransitionToDifferentState();
      
   /**
    * Indicates whether the transition is to the same state
    *
    * @return  <CODE>true</CODE> if the transition is to the same state,
    *          else <CODE>false</CODE>.
    */
   public boolean isSelfTransition();

   /**
    * Indicates whether this is an aging transition
    *
    * @return  <CODE>true</CODE> if the transition is an aging transition,
    *          else <CODE>false</CODE>.
    */
   public boolean isAgingTransition();

   /**
    * Gets the value of the aging type
    *
    * @return the value of the aging type
    */
   public int getAgingType(); 

   /**
    * Gets the value of the aging interval
    *
    * @return the value of the aging interval
    */
   public int getAgingInterval(); 

   /**
    * Gets the value of the aging system field
    *
    * @return the value of the aging system field
    */
   public String getSystemField();
   
   /**
    * Gets the number of transitions in the current workflow
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  number of transitions 
    */
   public int getTransitionCount();
      
   /**
    * Indicates whether the transition will be performed only if comments 
    * have been specified.
    * @author  Aaron Brandes
    *
    * @return  <CODE>true</CODE> if the transition requires comments,
    *          else <CODE>false</CODE>.
    */
   public boolean isTransitionCommentRequired();

   /**
    * Gets a list of the names of the workflow action extensions to be run 
    * for this transition.
    * @author   Aaron Brandes
    *
    * @return   <ul><li>list of full names of workflow action extension to be
    *           run for this  transition</li>
    *           <li><CODE>null</CODE> if there are no workflow actions for this
    *           transition</li> 
    *           </ul>
    */
   public List getTransitionActions();

   /**
    * Gets a list of the roles that allowed to perform this transition.
    * @author   Aaron Brandes
    *
    * @return   <ul><li>list of the roles that allowed to perform this
    *           transition</li>
    *           <li><CODE>null</CODE> there are no additional role
    *            restrictions</li>  
    *           </ul>
    */
   public List getTransitionRoles();
   
   /**
    * Moves the cursor to the next transition in the list.
    *
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if cursor movement is successful,
    *          else <CODE>false</CODE>.
    */
   public boolean moveNext() throws SQLException;

   /**
    * Indicates whether the number of transitions in the context is empty
    * @author   Ram
    *
    * @version 1.0
    *
    * @return  <CODE>true</CODE> if empty else <CODE>false</CODE>
    */
   public boolean isEmpty();

   /**
    * Closes the transition context freeing all JDBC resources.
    * @author   Ram
    *
    * @version 1.0
    *
    */
   public void close();
}
