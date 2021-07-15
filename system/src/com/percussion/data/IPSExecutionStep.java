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

package com.percussion.data;


/**
 * The IPSExecutionStep interface is implemented by classes defining
 * a step in query execution. The Query Optimizer (PSQueryOptimizer) chooses
 * an execution plan which will optimally access the data from each back-end.
 * It also defines the join requirements. It returns to the data manager an
 * array of objects implementing IPSExecutionStep. This allows the
 * Query Handler (PSQueryHandler) to execute the plan without regard for
 * what the underlying step is actually doing.
 * 
 * @see        PSQueryHandler
 * @see        PSQueryOptimizer
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSExecutionStep {
   /**
    * Execute this step in the execution plan.
    *
    * @param   data     execution data is a container for the input data
    *                   as well as a collection of result sets generated
    *                   by queries.
    *
    * @exception   SQLException
    *                     if a SQL error occurs
    */
   public void execute(PSExecutionData data)
      throws java.sql.SQLException, 
         com.percussion.error.PSIllegalArgumentException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException;
}

