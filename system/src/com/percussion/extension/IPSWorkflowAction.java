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

package com.percussion.extension;

import com.percussion.server.IPSRequestContext;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.service.exception.PSDataServiceException;

/**
 * The <CODE>IPSWorkflowAction</CODE> interface must be implemented by 
 * extensions which represent "workflow actions," which are actions that are
 * performed on documents in the workflow only under particular conditions
 * (e.g. when a certain transition occurs).
 * <P>
 * Extensions implementing IPSWorkflowAction should not be added directly to an
 * application; they can be added to a transition via the "new transition" or
 * "edit transition" pages in the Workflow editor. The content editor will
 * execute them via the post-exit <CODE>sys_wfExecuteActions</CODE>. Since
 * <CODE>sys_wfExecuteActions</CODE> is included in the content editor's
 * default local definition (ContentEditorSystemDef.xml file) it does not need
 * to be added.
 */
public interface IPSWorkflowAction extends IPSExtension
{
  /**
   * Key used to obtain the workflow actions private object 
   * created by <CODE>IPSRequestContext.setPrivateObject</CODE>
   */
   public static final String WORKFLOW_ACTIONS_PRIVATE_OBJECT=
                                  "wfactionsprivateobject";
   
   /**
    * Perform the workflow action.
    * <P>
    * <em>NOTE:</em> The implementation of this method must be
    * safe for multi-threaded use. One instance of the extension will
    * be defined for each usage in an application. For
    * example, if the application defines the same extension in five
    * different cases of different data as input), five instances
    * of this extension will be created. When processing requests, the
    * same instance may be accessed for several threads, each with its own
    * set of data. As such, any variables used during run-time execution
    * should be defined within the method (not the class). Another
    * alternative is to use variables of type java.lang.ThreadLocal to
    * define thread specific copies of the variable.
    *
    * @param   wfContext   the workflow context, specifying information such
    *                       as workflowID, contentID, revisionNum,
    *                       transitionID, stateID, historyID
    *
    * @param   request     the request context object
    *
    * @throws   PSExtensionProcessingException
    *           if an exception occurs which prevents the proper
    *           handling of this request. 
    */
    void performAction(IPSWorkFlowContext wfContext,
                             IPSRequestContext request)
           throws PSExtensionProcessingException, PSDataServiceException, PSNotFoundException;
}

