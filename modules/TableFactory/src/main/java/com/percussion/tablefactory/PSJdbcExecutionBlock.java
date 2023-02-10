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

package com.percussion.tablefactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A step that is actually a list of steps.  When the block is executed, it
 * executes each step sequentially.
 */
public class PSJdbcExecutionBlock extends PSJdbcExecutionStep
{
   /**
    * Adds the step to this block.
    *
    * @param step The step to execute, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if step is <code>null</code>.
    */
   public void addStep(PSJdbcExecutionStep step)
   {
      if (step == null)
         throw new IllegalArgumentException("step may not be null");

      m_steps.add(step);
   }

   /**
    * Executes each step in the block sequentially.
    *
    * @param conn A valid connection to use, may not be <code>null</code>.
    *
    * @return always returns <code>0</code>
    *
    * @throws IllegalArgumentException if conn is <code>null</code>.
    * @throws SQLException if any errors occur.
    */
   public int execute(Connection conn) throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      for (PSJdbcExecutionStep step : m_steps) {
         try {
            step.execute(conn);
         } catch (SQLException e) {
            /* if there is an error step, log the error we got and execute the
             * error step.
             */
            String errMsg = PSJdbcTableFactoryException.formatSqlException(e);
            // log the error and execute the error step
            PSJdbcTableFactory.logMessage("step failed: " + errMsg);
            PSJdbcTableFactory.logMessage("executing error step");
            if (step.stopOnError())
               throw e;
         }
      }
      return 0;
   }

   /**
    * List of steps, never <code>null</code>, may be empty.
    */
   private List<PSJdbcExecutionStep> m_steps = new ArrayList<>();

}

