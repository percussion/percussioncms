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
