/******************************************************************************
 *
 * [ IPSWFActionService.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workflow.actions;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;

import java.util.List;

/**
 * Interface for loading and dispatching Workflow Actions. This is intended to
 * be used as a Spring based service.
 * <p>
 * <b>Note:</b> unlike the original PSWFActionDispatcher which matched on
 * workflow id and transition id, this implementation matches on Workflow Name
 * and Transition Label. While the Workflow Name is unique, it is possible to
 * have more than one transition with the same label. In this case, the workflow
 * actions retrieved from this service will match ALL transitions with that
 * label.
 * <p>
 * If this is a problem, make sure that you give your transitions unique labels.
 * In many cases, you can use this functionality to your advantage. For example,
 * You can define a single set of actions for all of your "return to public"
 * transitions.
 * <p>
 * The <code>transitionActions</code> property is a <code>Map</code> of
 * <code>Maps</code> of <code>Lists</code> of <code>Strings</code>. The outer
 * map is keyed by the Workflow Name, and the inner map is keyed by the
 * Transition Label. The Strings are the names of the Workflow Actions that you
 * wish to run on that Transition.
 * <p>
 * The bean configuration will look something like this:
 * 
 * <pre>
 *    &lt;bean id="sys_WorkflowActionService" class="com.percussion.workflow.actions.PSWFActionService"
 *       init-method="init"&gt;
 *        &lt;property name="transitionActions"&gt;
 *           &lt;map&gt;
 *               &lt;entry key="workflow1"&gt;
 *                   &lt;map&gt;
 *                      &lt;entry key="transition1"&gt;
 *                          &lt;list&gt;
 *                             &lt;value&gt;action1&lt;/value&gt;
 *                             &lt;value&gt;action2&lt;/value&gt;
 *                          &lt;/list&gt;
 *                      &lt;/entry&gt; 
 *                   &lt;/map&gt;
 *               &lt;/entry&gt;
 *           &lt;/map&gt;          
 *        &lt;/property&gt;
 *     &lt;/bean&gt;
 * 
 * </pre>
 * <p>
 * A sample file can be found at
 * /WEB-INF/config/user/spring/WorkflowActionDispatcher-beans.xml
 * 
 * 
 * @author DavidBenua
 * @see PSWFActionServiceLocator
 */
public interface IPSWFActionService
{
   /**
    * Get the list of workflow actions configured for this workflow id and
    * transition id.
    * 
    * @param workflowid the workflow id.
    * @param transitionid the transition id.
    * @return a list of workflow actions. The actions are already loaded and
    * ready for execution. May be empty but never <code>null</null>
    * @throws Exception
    */
   public List<IPSWorkflowAction> getActions(int workflowid, int transitionid)
      throws Exception;

   /**
    * Get and load the desired workflow action.
    * 
    * @param workflowActionName the name of the workflow action
    * @return the workflow action. May be <code>null</code> if the extension was
    * not found.
    * @throws PSExtensionException
    * @throws PSNotFoundException
    */
   public IPSWorkflowAction getWorkflowAction(String workflowActionName)
      throws PSExtensionException, PSNotFoundException;
}
