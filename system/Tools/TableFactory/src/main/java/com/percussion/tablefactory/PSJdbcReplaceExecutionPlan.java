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

package com.percussion.tablefactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A set of steps that may be executed in sequence. First executes the first
 * step and if the update count equals zero then executes the corresponding
 * second step
 */
public class PSJdbcReplaceExecutionPlan
{
   /**
    * Adds the steps to this plan.
    *
    * @param firstStep The step to execute, may not be <code>null</code>.
    * @param secondStep The step to execute if the update count of the firstStep
    * execution equals <code>0</code>, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if firstStep or secondStep is <code>null</code>.
    */
   public void addStep(PSJdbcExecutionStep firstStep, PSJdbcExecutionStep secondStep)
   {
      if (firstStep == null)
         throw new IllegalArgumentException("first step may not be null");

      if (secondStep == null)
         throw new IllegalArgumentException("second step may not be null");

      m_firstSteps.add(firstStep);
      m_secondSteps.add(secondStep);
   }

   /**
    * Adds all the steps of a plan to this plan.
    *
    * @param plan The plan whose stpes are added to this plan.
    *
    * @throws IllegalArgumentException if plan is <code>null</code>.
    */
   public void addPlan(PSJdbcReplaceExecutionPlan plan)
   {
      if (plan == null)
         throw new IllegalArgumentException("plan may not be null");

      m_firstSteps.addAll(plan.m_firstSteps);
      m_secondSteps.addAll(plan.m_secondSteps);
   }

   /**
    * Executes each step in the plan sequentially in reverse order. First
    * executes the first step and if the update count equals <code>0</code>
    * then executes the corresponding second step.
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
    * Executes each step in the plan sequentially in forward order. First
    * executes the first step and if the update count equals <code>0</code>
    * then executes the corresponding second step.
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
    * Executes each step in the plan sequentially.  First executes the first
    * step and if the update count equals zero then executes the corresponding
    * second step.
    *
    * @param conn A valid connection to use, may not be <code>null</code>.
    * @param forward if true then executes the steps in the plan from the
    * beginning to the end of the list, else executes the steps in reverse
    * order.
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
         index = m_firstSteps.size();

      ListIterator firstList = m_firstSteps.listIterator(index);
      ListIterator secondList = m_secondSteps.listIterator(index);

      boolean continueLoop = false;
      if (forward)
         continueLoop = firstList.hasNext();
      else
         continueLoop = firstList.hasPrevious();

      while (continueLoop)
      {
         PSJdbcExecutionStep firstStep = null;
         PSJdbcExecutionStep secondStep = null;

         if (forward)
         {
            firstStep = (PSJdbcExecutionStep)firstList.next();
            secondStep = (PSJdbcExecutionStep)secondList.next();
         }
         else
         {
            firstStep = (PSJdbcExecutionStep)firstList.previous();
            secondStep = (PSJdbcExecutionStep)secondList.previous();
         }

         m_planLogData.addStepLogData(firstStep.getStepLogData());

         boolean isFirstStep = true;
         try
         {
            if(firstStep.execute(conn) == 0)
            {
               isFirstStep = false;
               m_planLogData.addStepLogData(secondStep.getStepLogData());

               secondStep.execute(conn);
            }
         }
         catch (SQLException e)
         {
            /* if there is an error step, log the error we got and execute the
             * error step.
             */

            String errMsg = PSJdbcTableFactoryException.formatSqlException(e);
            PSJdbcExecutionStep errorStep = null;

            if (isFirstStep)
            {
               errorStep = firstStep.getErrorStep();
            }
            else
            {
               errorStep = secondStep.getErrorStep();
            }

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
            if (isFirstStep)
            {
               if (firstStep.stopOnError())
                  throw e;
            }
            else
            {
               if (secondStep.stopOnError())
                  throw e;
            }
         }
         if (forward)
            continueLoop = firstList.hasNext();
         else
            continueLoop = secondList.hasPrevious();
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
   private List m_firstSteps = new ArrayList();

   /**
    * List of steps executed if the corresponding firstStep update count
    * equals <code>0</code>, never <code>null</code>, may be empty.
    */
   private List m_secondSteps = new ArrayList();

   /**
    * Stores the log of execution of this plan, never <code>null</code>
    */
   private PSJdbcExecutionPlanLog m_planLogData = new PSJdbcExecutionPlanLog();

}

