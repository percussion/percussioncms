/*******************************************************************************
 * (c) 2005-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.workflow;
import java.util.List;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
/**
 * Interface for loading and dispatching Workflow Actions. This is intended to be 
 * used as a Spring based service.
 * <p>
 * <b>Note:</b> unlike the original PSOWFActionDispatcher which matched on workflow id
 * and transition id, this implementation matches on Workflow Name and 
 * Transition Label.  While the Workflow Name is unique, it is possible to have
 * more than one transition with the same label.  In this case, the workflow 
 * actions retrieved from this service will match ALL transitions with that label.
 * <p>
 * If this is a problem, make sure that you give your transitions unique labels. In 
 * many cases, you can use this functionality to your advantage. For example, You can 
 * define a single set of actions for all of your "return to public" transitions.  
 * <p>
 * The <code>transitionActions</code> property is a <code>Map</code> of 
 * <code>Maps</code> of <code>Lists</code> of <code>Strings</code>. The 
 * outer map is keyed by the Workflow Name, and the inner map is keyed 
 * by the Transition Label.  The Strings are the names of the Workflow 
 * Actions that you wish to run on that Transition. 
 * <p>
 * The bean configuration will look something like this: 
 * <pre>
   &lt;bean id="psoWFActionService" class="com.percussion.pso.workflow.PSOWFActionService"
      init-method="init"&gt;
       &lt;property name="transitionActions"&gt;
          &lt;map&gt;
              &lt;entry key="workflow1"&gt;
                  &lt;map&gt;
                     &lt;entry key="transition1"&gt;
                         &lt;list&gt;
                            &lt;value&gt;action1&lt;/value&gt;
                            &lt;value&gt;action2&lt;/value&gt;
                         &lt;/list&gt;
                     &lt;/entry&gt; 
                  &lt;/map&gt;
              &lt;/entry&gt;
          &lt;/map&gt;          
       &lt;/property&gt;
    &lt;/bean&gt;
 
 *</pre>
 * <p>
 * A sample file can be found at 
 * /WEB-INF/config/user/spring/PSOSpringWorkflowActionDispatcher-beans.xml
 * 
 * 
 * @author DavidBenua
 * @see PSOWFActionServiceLocator
 */
public interface IPSOWFActionService
{
   /**
    * Get the list of workflow actions configured for this workflow id and transition id.
    * @param workflowid  the workflow id.
    * @param transitionid the transition id.
    * @return a list of workflow actions.  The actions are already loaded and ready for execution.
    * @throws Exception
    */
   public List<IPSWorkflowAction> getActions(int workflowid, int transitionid)
         throws Exception;
   /**
    * Get and load the desired workflow action.
    * @param workflowActionName the name of the workflow action
    * @return the workflow action. May be <code>null</code> if the extension was not found. 
    * @throws PSExtensionException
    * @throws PSNotFoundException
    */
   @SuppressWarnings("unchecked")
   public IPSWorkflowAction getWorkflowAction(String workflowActionName)
         throws PSExtensionException, PSNotFoundException;
}