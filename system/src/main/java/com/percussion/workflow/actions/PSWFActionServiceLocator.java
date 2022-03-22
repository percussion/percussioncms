/******************************************************************************
 *
 * [ PSWFActionServiceLocator.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workflow.actions;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Locator for the IPSWFActionService bean. 
 *
 * @author DavidBenua
 * @see IPSWFActionService
 * @see PSSpringWorkflowActionDispatcher
 */
public class PSWFActionServiceLocator extends PSBaseServiceLocator
{
   /**
    * Gets the Workflow Action Service bean. 
    * @return the Workflow Action Service bean. 
    */
   public static IPSWFActionService getPSWFActionService()
   {
      return (IPSWFActionService) PSBaseServiceLocator.getBean(PS_WF_ACTION_SERVICE_BEAN); 
   }
   
   public static final String PS_WF_ACTION_SERVICE_BEAN = "sys_WorkflowActionService";
}
