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
package com.percussion.services.workflow;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Locator for workflow service
 * 
 * @author dougrand
 * 
 */
public class PSWorkflowServiceLocator extends PSBaseServiceLocator
{
   public static volatile IPSWorkflowService wfService = null;
   /**
    * Get the workflow service
    * 
    * @return the workflow service, never <code>null</code> if the services
    *         are correctly configured
    */
   public static IPSWorkflowService getWorkflowService()
   {
      if (wfService == null)
      {
         synchronized (PSWorkflowServiceLocator.class)
         {
            if (wfService==null)
            {
               wfService = (IPSWorkflowService) getBean("sys_workflowService");
            }
         }
      }
      return wfService;
   }
}
