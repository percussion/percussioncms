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

