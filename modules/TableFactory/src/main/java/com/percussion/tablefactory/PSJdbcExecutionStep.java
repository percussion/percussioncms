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

import com.percussion.util.PSEntrySet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a step in an execution plan.
 */
public abstract class PSJdbcExecutionStep
{
   /**
    * Executes the step against the provided Connection.
    *
    * @param conn The connection, may not be <code>null</code>.
    *
    * @return numbers of rows in the database which were updated
    * by execution of this step. If sql statement execution
    * returns false then returns the update count else returns 0.
    * This return value is useful in {@link PSJdbcReplaceExecutionPlan}
    * which needs to know whether the step execution resulted
    * in any updates to the database and accordingly execute the
    * corresponding insert step.
    *
    * @throws IllegalArgumentException if conn is <code>null</code>.
    * @throws SQLException if any errors occur.
    */
   public abstract int execute(Connection conn) throws SQLException;

   /**
    * Sets a step to execute if this step fails.  If this error step itself
    * has an error step, it is not processed if this error step fails.
    *
    * @param errorStep The step to execute.  May be <code>null</code>.
    */
   public void setErrorStep(PSJdbcExecutionStep errorStep)
   {
      m_errorStep = errorStep;
   }

   /**
    * Returns this step's error step.  Set {@link #setErrorStep(
    * PSJdbcExecutionStep)} for more info.
    *
    * @return The error step, may be <code>null</code>.
    */
   public PSJdbcExecutionStep getErrorStep()
   {
      return m_errorStep;
   }

   /**
    * Should failure of this step cause execution to stop? If not, any errors
    * will be logged and execution will continue.
    *
    * @param shouldStop If <code>true</code> if it should stop, if <code>false
    * </code> it should continue.
    */
   public void setStopOnError(boolean shouldStop)
   {
      m_stopOnError = shouldStop;
   }

   /**
    * Should failure of this step cause execution to stop?
    *
    * @return <code>true</code> if it should stop, <code>false</code> if not.
    */
   public boolean stopOnError()
   {
      return m_stopOnError;
   }

   /**
    * Returns the <code>PSJdbcExecutionStepLog</code> object which stores
    * the result of the execution of this step.
    *
    * @return the <code>PSJdbcExecutionStepLog</code> object which stores
    * the result of the execution of this step, never <code>null</code>
    */
   public PSJdbcExecutionStepLog getStepLogData()
   {
      return m_stepLogData;
   }

   /**
    * Adds an event and a corresponding set of listerners to be notified of that
    * event when this step successfully executes.  Callers of the
    * {@link #execute(Connection)} method must then call
    * {@link #notifyChangeListeners} for the notification to occur.
    *
    * @param e The event, may not be <code>null</code>.
    * @param listeners A list of {@link IPSJdbcTableChangeListener} objects to
    * notify with this event, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addTableChangeEvent(PSJdbcTableChangeEvent e, List<IPSJdbcTableChangeListener> listeners)
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null");

      if (listeners == null || listeners.isEmpty())
         throw new IllegalArgumentException(
            "listeners may not be null or empty");

      PSEntrySet entry = new PSEntrySet(e, listeners);
      if (m_listenerEvents == null)
         m_listenerEvents = new ArrayList<>();
      m_listenerEvents.add(entry);
   }

   /**
    * Notifies any listeners with any events that have been set on this step by
    * a call to {@link #addTableChangeEvent(PSJdbcTableChangeEvent, List)}.
    */
   public void notifyChangeListeners()
   {
      if (m_listenerEvents != null)
      {
         for (PSEntrySet entry : m_listenerEvents) {
            PSJdbcTableChangeEvent e = (PSJdbcTableChangeEvent) entry.getKey();
            List<?> listenerList = (List<?>) entry.getValue();
            for (Object o : listenerList) {
               IPSJdbcTableChangeListener listener =
                       (IPSJdbcTableChangeListener) o;
               listener.tableChanged(e);
            }
         }
      }
   }

   /**
    * Sets the sql states for sql exceptions that should be ignored when
    * <code>execute()</code> method is called.
    *
    * @param sqlStates sql states for sql exceptions that should be ignored when
    * <code>execute()</code> method is called, may be <code>null</code> or empty.
    * If <code>null</code> or empty, then the internal list of sql states for
    * sql exceptions that shold be ignored is cleared and no sql exception is
    * ignored in the <code>execute()</code> method.
    */
   public void setIgnoreSQLExceptions(String[] sqlStates)
   {
      if ((sqlStates == null) || (sqlStates.length == 0))
         return;
      if (m_sqlStates == null)
         m_sqlStates = new HashSet<>();
      m_sqlStates.clear();
      Collections.addAll(m_sqlStates, sqlStates);
   }

   /**
    * Throws the supplied sql exception if the sql state specified in the
    * exception does not exist in the list of sql states to ignore.
    * See {@link #setIgnoreSQLExceptions(String[])}
    * setIgnoreSQLException(String[])} for details.
    *
    * @param sqle the sql exception to throw, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sqle</code> is <code>null</code>
    * @throws SQLException if the sql state of the supplied sql exception does
    * not exist in the list of sql states to ignore
    */
   protected void handleSqlException(SQLException sqle)
      throws SQLException
   {
      if (sqle == null)
         throw new IllegalArgumentException("sqle may not null");
      String sqlState = sqle.getSQLState();
      if ((m_sqlStates != null) && (m_sqlStates.contains(sqlState)))
         return;
      throw sqle;
   }

   /**
    * The step to execute if this step fails.  May be <code>null</code>.
    */
   private PSJdbcExecutionStep m_errorStep = null;

   /**
    * Determines if failure of this step should cause execution to fail.
    * <code>true</code> (should stop) by default.
    */
   private boolean m_stopOnError = true;

   /**
    * Stores the log of execution of this step, never <code>null</code>
    */
   private PSJdbcExecutionStepLog m_stepLogData = new PSJdbcExecutionStepLog();

   /**
    * List of events and listeners to be notified of the event.  Each entry is
    * an {@link PSEntrySet} with a {@link PSJdbcTableChangeEvent} as the key
    * and a <code>List</code> of {@link IPSJdbcTableChangeListener} objects as
    * the value.  Modified by a call to
    * {@link #addTableChangeEvent(PSJdbcTableChangeEvent, List)}, may be
    * <code>null</code>.
    */
   private List<PSEntrySet> m_listenerEvents = null;

   /**
    * Set of sql states for sql exceptions that should be ignored when
    * <code>execute()</code> method is called, initially <code>null</code>,
    * initialized and modified in <code>setIgnoreSQLException</code> method,
    * may be empty in which case no sql exception is ignored.
    */
   protected Set<String> m_sqlStates = null;

}

