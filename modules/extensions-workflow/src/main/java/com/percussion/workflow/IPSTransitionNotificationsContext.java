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


/**
 * An interface that defines methods for the transition notifications context,
 * which contains information about notifications that should be performed
 * after a transition is completed, as well as recipient information.<BR>
 * The "get" methods will return data for the "current" notification. When
 * the context is created, the current notification is the first one. The
 * current notification can be advanced via calls to  {@link #moveNext} 
 */

public interface IPSTransitionNotificationsContext
{ 
   /**
    * Gets the value of the workflowID
    *
    * @return the value of the workflowID
    */
   public int getWorkflowID();

   /**
    * Gets the value of the transition ID
    *
    * @return the value of the transition D
    */
   public int getTransitionID();

   /**
    * Gets the value of the notification ID for the current notification data
    * set. 
    *
    * @return the value of the notification ID
    */
   public int getNotificationID() ;


   /**
    * Return value indicating which state role recipients should receive
    * notification: none, from-state, to-state or both
    * (for the current notification data set).    
    * @return 
    * <ul>
    * <li>NO_STATE_ROLE_RECIPIENTS for No State Role recipients</li>
    * <li>ONLY_NEW_STATE_ROLE_RECIPIENTS for To State Role recipients only</li>
    * <li>ONLY_OLD_STATE_ROLE_RECIPIENTS for From State Role recipients
    * only</li> 
    * <li>OLD_AND_NEW_STATE_ROLE_RECIPIENTS for Both To and From State Role
    * recipients</li>  
    * </ul> 
    */
   public int getStateRoleRecipientTypes();

   /**
    * Return value indicating whether from-state role recipients should receive
    * at least one notification
    * @return  <CODE>true</CODE> if from-state role recipients should receive
    *           at least one notification, else <CODE>false</CODE> 
    */
   public boolean requireFromStateRoles();

   /**
    * Return value indicating whether to-state role recipients should receive
    * at least one notification
    * @return  <CODE>true</CODE> if to-state role recipients should receive
    *           at least one notification, else <CODE>false</CODE> 
    */
   public boolean requireToStateRoles();

   /**
    * Return value indicating whether from-state role recipients should receive
    * this notification 
    * @return  <CODE>true</CODE> if from-state role recipients should receive
    *           this notification, else <CODE>false</CODE> 
    */
   public boolean notifyFromStateRoles();

   /**
    * Return value indicating whether to-state role recipients should receive
    * this notification 
    * @return  <CODE>true</CODE> if to-state role recipients should receive
    *           this notification, else <CODE>false</CODE> 
    */
   public boolean notifyToStateRoles();
   
   /**
    * Returns comma-delimited list of additional notification recipients
    * for the current notification data set.
    *
    * @return comma-delimited list of additional notification recipients
    */
   public String getAdditionalRecipientList();

   /**
    * Returns comma-delimited list of CC notification recipients
    * for the current notification data set.
    *
    * @return comma-delimited list of CC notification recipients
    */
   public String getCCList();

   /**
    * Makes the data for the next notification in the list available via the
    * "get" commands.
    *
    * @return  <CODE>true</CODE> if there is another notification,
    *          else <CODE>false</CODE>.
    */
   public boolean moveNext();
      
   /** Indicates no state role notification recipients */
   public static final int NO_STATE_ROLE_RECIPIENTS = 0;
   
   /** Indicates only to-state role notification recipients */
   public static final int ONLY_NEW_STATE_ROLE_RECIPIENTS = 1;
   
   /** Indicates only from-state role notification recipients */
   public static final int ONLY_OLD_STATE_ROLE_RECIPIENTS = 2;
   
   /** Indicates both to-state and from-state role notification recipients */
   public static final int OLD_AND_NEW_STATE_ROLE_RECIPIENTS = 3;   
   
}
