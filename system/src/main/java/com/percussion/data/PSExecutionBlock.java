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

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;

import java.util.ArrayList;


/**
 * The PSExecutionBlock class defines a block of statements to process
 * as a group if the specified conditions are met.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExecutionBlock implements IPSExecutionStep
{
   /**
    * Construct an empty execution block with the specified conditions.
    *
    * @param      conds         the conditions this block will be executed
    *                           on (PSConditional objects)
    *
    * @exception   PSIllegalArgumentException
    *                           if conds does not contain PSConditional objects
    */
   public PSExecutionBlock(PSCollection conds)
      throws PSIllegalArgumentException
   {
      super();

      if ((conds != null) && (conds.size() != 0)) {
         // attempting to create the exit handler will cause the throw
         // if the collection does not contain conditionals
         m_ConditionsChecker = new PSConditionalEvaluator(conds);
      }
      else {
         m_ConditionsChecker = null;
      }
      m_Executables = new ArrayList();
   }

   /**
    * Add an executable object to this block.
    * <p>
    * Be sure to add components in the appropriate order. The run-time
    * construction uses the same ordering as the add calls.
    *
    * @param      exec         the executable object to add
    */
   public void add(IPSExecutionStep exec)
   {
      m_Executables.add(exec);
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */
      
   /**
    * Execute a set of execution steps if the specified condition is met.
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
         com.percussion.error.PSErrorException
   {
      // if this is null, we always exec the block
      if (   (m_ConditionsChecker != null) &&
            !m_ConditionsChecker.isMatch(data))
         return;   // not a match, skip this block

      // met the condition, execute the objects one by one
      int size = m_Executables.size();
      for (int i = 0; i < size; i++) {
         ((IPSExecutionStep)m_Executables.get(i)).execute(data);
      }
   }


   private PSConditionalEvaluator      m_ConditionsChecker;
   private ArrayList                     m_Executables;
}

