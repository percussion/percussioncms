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

package com.percussion.workflow;

import java.sql.SQLException;
/**
 * An interface that defines methods for workflow applications context.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */

public interface IPSWorkflowAppsContext
{
   /**
    * Gets the Workflow application ID
    * @author   Ram
    *
    * @version 1.0
    *
    *
    * @param   none
    *
    * @return  WorkflowAppID
    */
   public int getWorkFlowAppID() throws SQLException;

   /**
    * Gets the Workflow application name
    * @author   Ram
    *
    * @version 1.0
    *
    *
    * @param   none
    *
    * @return  Workflow app name
    */
   public String getWorkFlowAppName() throws SQLException;

   /**
    * Gets the Workflow application description
    * @author   Ram
    *
    * @version 1.0
    *
    *
    * @param   none
    *
    * @return  Workflow app description
    *
    */
   public String getWorkFlowAppDescription() throws SQLException;

   /**
    * Gets the Workflow application's administrator name
    * @author   Ram
    *
    * @version 1.0
    *
    *
    * @param   none
    *
    * @return  name of the Workflow application's administrator
    */
   public String getWorkFlowAdministrator() throws SQLException;

   /**
    * Gets the Workflow application's initial stateID
    * @author   Ram
    *
    * @version 1.0
    *
    *
    * @param   none
    *
    * @return  Workflow's initial stateID
    */
   public int getWorkFlowInitialStateID() throws SQLException;
}
