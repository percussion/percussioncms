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
