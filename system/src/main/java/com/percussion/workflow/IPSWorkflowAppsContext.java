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
