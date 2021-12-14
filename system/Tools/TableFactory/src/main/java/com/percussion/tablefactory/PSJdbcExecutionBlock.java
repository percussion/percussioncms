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
import java.util.Iterator;
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

      Iterator steps = m_steps.iterator();
      while (steps.hasNext())
      {
         PSJdbcExecutionStep step = (PSJdbcExecutionStep)steps.next();
         step.execute(conn);
      }
      return 0;
   }

   /**
    * List of steps, never <code>null</code>, may be empty.
    */
   private List m_steps = new ArrayList();

}

