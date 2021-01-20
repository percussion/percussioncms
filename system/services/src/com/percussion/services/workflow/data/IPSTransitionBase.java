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

package com.percussion.services.workflow.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;

import java.io.Serializable;
import java.util.List;

/**
 * The base interface for transition types
 */
public interface IPSTransitionBase extends Serializable, 
IPSCatalogSummary, IPSCatalogItem
{
   /**
    * Get the workflow ID    * 
    * @return the workflow ID
    */
   long getWorkflowId();
   
   /**
    * Set the workflow ID
    * @param id the new workflow ID
    */
   void setWorkflowId(long id);
   
   /**
    * Get the from state id.
    * 
    * @return the id.
    */
   long getStateId();
   
   /**
    * Set the from state id.
    * 
    * @param state The id of the from state.
    */
   void setStateId(long state);

   /**
    * Set the label
    * 
    * @param lbl the label, may be <code>null</code> or empty.
    */
   void setLabel(String lbl);

   /**
    * Set the description
    * 
    * @param desc the description, may be <code>null</code> or empty.
    */
    void setDescription(String desc);
   
   /**
    * Get the name to trigger this transition.
    * 
    * @return the transitions trigger name, never <code>null</code> or empty.
    */
   String getTrigger();
   
   /**
    * Set the name to trigger this transaction.
    * 
    * @param triggerName The name.
    */
   void setTrigger(String triggerName);
   
   /**
    * Get the state to which this transition leads.
    * 
    * @return the state's id
    */
   long getToState();
   
   /**
    * Set the state to which this transition leads.
    * 
    * @param state The state id.
    */
   void setToState(long state);
   
   /**
    * Get the name of the workflow action to be executed after this transition.
    * 
    * @return the workflow action name, may be <code>null</code> or empty.
    */
   String getTransitionAction();
   
   /**
    * Set the transition action.
    * 
    * @param transAction
    */
   void setTransitionAction(String transAction);
   
   /**
    * Get all notifications to be sent with this transition.
    * 
    * @return all transition notifications, never <code>null</code>, may
    *    be empty.
    */
   List<PSNotification> getNotifications();
   
   /**
    * Set the list of notifications.
    * 
    * @param notificationList May be <code>null</code> or empty.
    */
   void setNotifications(List<PSNotification> notificationList);
}
