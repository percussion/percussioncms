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
import java.util.ListIterator;

/**
 * A set of steps that may be executed in sequence.
 */
public class PSJdbcExecutionPlan
{
   /**
    * Adds the step to this plan.
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
    * Adds all the steps of a plan to this plan.
    *
    * @param plan The plan whose steps are added to this plan.
    *
    * @throws IllegalArgumentException if plan is <code>null</code>.
    */
   public void addPlan(PSJdbcExecutionPlan plan)
   {
      if (plan == null)
         throw new IllegalArgumentException("plan may not be null");

      m_steps.addAll(plan.m_steps);
   }

   /**
    * Executes each step in the plan sequentially in reverse order.
    *
    * @param conn A valid connection to use, may not be <code>null</code>.
    *
    * @throws SQLException if any errors occur.
    */
   public void reverseExecute(Connection conn) throws SQLException
   {
      execute(conn, false);
   }

   /**
    * Executes each step in the plan sequentially in forward order.
    *
    * @param conn A valid connection to use, may not be <code>null</code>.
    *
    * @throws SQLException if any errors occur.
    */
   public void execute(Connection conn) throws SQLException
   {
      execute(conn, true);
   }

   /**
    * Executes each step in the plan sequentially in forward or reverse order.
    *
    * @param conn A valid connection to use, may not be <code>null</code>.
    * @param forward executes the steps in forward order if <code>true</code>
    * else in reverse order.
    *
    * @throws IllegalArgumentException if conn is <code>null</code>.
    * @throws SQLException if any errors occur and {@link
    * PSJdbcExecutionStep#stopOnError() is <code>true</code>}.
    */
   public void execute(Connection conn, boolean forward) throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      int index = 0;
      if (!forward)
         index = m_steps.size();
      ListIterator<PSJdbcExecutionStep> steps = m_steps.listIterator(index);

      boolean continueLoop = false;
      if (forward)
         continueLoop = steps.hasNext();
      else
         continueLoop = steps.hasPrevious();

      while (continueLoop)
      {
         PSJdbcExecutionStep step = null;
         if (forward)
            step = steps.next();
         else
            step = steps.previous();

         m_planLogData.addStepLogData(step.getStepLogData());

         try
         {
            step.execute(conn);
            step.notifyChangeListeners();
         }
         catch (SQLException e)
         {
            /* if there is an error step, log the error we got and execute the
             * error step.
             */
            String errMsg = PSJdbcTableFactoryException.formatSqlException(e);
            PSJdbcExecutionStep errorStep = step.getErrorStep();
            if (errorStep != null)
            {
               // log the error and execute the error step
               PSJdbcTableFactory.logMessage("step failed: " + errMsg);
               PSJdbcTableFactory.logMessage("executing error step");

               try
               {
                  errorStep.execute(conn);
               }
               catch (SQLException e2)
               {
                  PSJdbcTableFactory.logMessage("error step failed: " +
                     PSJdbcTableFactoryException.formatSqlException(e2));
                  e.setNextException(e2);
               }
            }
            if (step.stopOnError())
               throw e;
         }
         if (forward)
            continueLoop = steps.hasNext();
         else
            continueLoop = steps.hasPrevious();
      }
   }

   /**
    * Returns the <code>PSJdbcExecutionPlanLog</code> object which stores
    * the result of the execution of this plan.
    *
    * @return the <code>PSJdbcExecutionStepLog</code> object which stores
    * the result of the execution of this plan, never <code>null</code>
    */
   public PSJdbcExecutionPlanLog getPlanLogData()
   {
      return m_planLogData;
   }

   /**
    * List of steps, never <code>null</code>, may be empty.
    */
   private List<PSJdbcExecutionStep> m_steps = new ArrayList<>();

   /**
    * Stores the log of execution of this plan, never <code>null</code>
    */
   private PSJdbcExecutionPlanLog m_planLogData = new PSJdbcExecutionPlanLog();

}

