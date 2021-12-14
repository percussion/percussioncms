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
import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSWorkFlowContext;

/**
 * This class provides a convenient way to bundle together information needed
 * to process a workflow content item. 
 */
public class PSWorkFlowContext implements IPSWorkFlowContext
{
    /**
     * Constructor specifying workflowID, contentID, revisionNum, transitionID,
     * stateID, and historyID.
     *
     * @param workflowID    database ID of workflow (must be > 0)           
     * @param contentID     database ID of content item (must be > 0)
     * @param revisionNum   current revision number or revision number of item
     *                       being checked out (must be > 0)
     * @param transitionID  database ID of transition (must be > 0)  or
     *                         = IPSConstants.TRANSITIONID_CHECKINOUT or
     *                         = IPSConstants.TRANSITIONID_NO_ACTION_TAKEN
     * @param stateID       database ID of current content state
     *                        (new state if a transition was performed)
     *                      (must be > 0)
     * @param historyID     database ID of content status history entry
     *                      (must be > 0) or 
     *               IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE
     *                      if the content status history entry has not yet
     *                      been created.
     * @throws              IllegalArgumentException if any of the input
     *                      parameters is not valid.
     */
    public PSWorkFlowContext(int workflowID, 
                             int contentID, 
                             int revisionNum, 
                             int transitionID, 
                             int stateID)
    {
        if ( workflowID > 0 )
        {
           m_workflowID = workflowID;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: workflowID value " + workflowID +
              " is not valid\n");
        }

        if ( contentID > 0 )
        {
           m_contentID = contentID;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: contentID value " + contentID +
              " is not valid\n");
        }
        
        if ( revisionNum > 0 )
        {
           m_revisionNum = revisionNum;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: revisionNum value " + revisionNum +
              " is not valid\n");
        }

        if ( transitionID >= 0 ||
             IPSConstants.TRANSITIONID_NO_ACTION_TAKEN == transitionID)
        {
           m_transitionID = transitionID;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: transitionID value " + transitionID +
              " is not valid\n");
        }

        if ( stateID >= 0 )
        {
           m_stateID = stateID;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: stateID value " + stateID +
              " is not valid\n");
        }
                
        return;
    }
   
    public int getWorkflowID()
    {
        return m_workflowID;
    }
     
    public int getContentID()
    {
        return m_contentID;
    }
   
    public int getBaseRevisionNum()
    {
        return m_revisionNum;
    }
   
    public int getTransitionID()
    {
        return m_transitionID;
    }
   
    public int getStateID()
    {
        return m_stateID;
    }
   
    public int getHistoryID()
    {
        return m_historyID;
    }
    
    /**
     * Sets the content status history entry database ID.
     *
     * @param contentStatusHistoryID  content status history entry database ID
     *
     * @throws IllegalArgumentException if the input is not valid.
     */
    public void setHistoryid(int contentStatusHistoryID)
    {
        if ( contentStatusHistoryID > 0 )
        {
           m_historyID = contentStatusHistoryID;
        }
        else
        {
           throw new IllegalArgumentException(
              "PSWorkFlowContext: contentStatusHistoryID value " +
              contentStatusHistoryID +  " is not valid\n");
        }        
    }


   public String toString()
   {
      return "PSWorkFlowContext: " + "\n" +
            " workflowID = " + m_workflowID +  "\n" +
            " contentID = " + m_contentID + "\n" +
            " revisionNum = " + m_revisionNum + "\n" +
            " transitionID = " +  m_transitionID + "\n" +
            " stateID = " + m_stateID + "\n" +
            " historyID = " + m_historyID + "\n";
   }
   

   /**
    * database ID of workflow (must be > 0)
    */     
   private int m_workflowID
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
   /**
    * database ID of content item (must be > 0)
    */ 
   private int m_contentID
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
   /**
    * database ID of content revision
    * current revision number or revision number of item being checked out
    * (must be > 0) 
    */
   private int m_revisionNum
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
   /**
    * database ID of transition (must be > 0)
    *              or  = IPSConstants.TRANSITIONID_CHECKINOUT 
    *              or  = IPSConstants.TRANSITIONID_NO_ACTION_TAKEN
    */
   private int m_transitionID
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
   /**
    * database ID of current/new content state (must be >= 0)
    * (new state if a transition was performed)
    *  = 0 for a checkin or checkout
    */
   private int m_stateID
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
   /**
    * database ID of  content status history entry for this transition  
    */
   private int m_historyID
               = IPSWorkFlowContext.WORKFLOW_CONTEXT_INITIAL_INTEGER_VALUE;
}

