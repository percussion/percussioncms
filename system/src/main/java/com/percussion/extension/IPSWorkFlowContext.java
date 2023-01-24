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

package com.percussion.extension;

/**
 * This interface provides a convenient way to bundle together information
 * needed to process a workflow content item. It is used by
 * <CODE>IPSWorkflowAction</CODE>.
 */
public interface IPSWorkFlowContext
{
   /**
    * Key used to obtain the <CODE>PSWorkFlowContext</CODE> private object 
    * created by <CODE>IPSRequestContext.setPrivateObject</CODE>
    */
   public static final String WORKFLOW_CONTEXT_PRIVATE_OBJECT=
                              "wfcontextprivateobject";
   
   /*
    * Value used to initialize integer values in PSWorkFlowContext.
    */
   public static final int WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE = -2;
       
    /**
     * Gets the workflow database ID.
     *
     * @return  database ID of workflow    
     */
   public int getWorkflowID();
   
    /**
     * Gets the content item database ID.
     *
     * @return   database ID of content item    
     */
   public int getContentID();
       
    /**
     * Gets a revision number for the content item
     * <ul><li>for transitions - current revision</li>
     * <li>for checkin - revision being checked in</li>
     * <li>for checkout - base revision for the item being checked out:
     *                      either 1, or the revision of the item copied to
     *                      create the revision checked out</li></ul> 
     *
     * @return revision number of content item    
     */
   public int getBaseRevisionNum();
   
    /**
     * Gets the transition database ID.
     *
     * @return   <ul><li>database ID of transition</li> 
     *         <li>0 for checkin or checkout</li>
     *             <li>IPSConstants.TRANSITIONID_NO_ACTION_TAKEN if no 
     *             action was taken</li>
     *             </li>
     *             </ul>     
     */
       public int getTransitionID();
   
    /**
     * Gets the current content state database ID. If a transition has 
     * occurred this will be the new state ID.
     *
     * @return database ID of current/new content state    
     */
       public int getStateID();
   
    /**
     * Gets the content status history entry database ID.
     *
     * @return     content status history entry database ID or
     *             WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE if the content status
     *             history entry has not yet been created.
     */
       public int getHistoryID();
   

}
