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

