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
package com.percussion.services.schedule.data;

import com.percussion.services.schedule.IPSTaskResult;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * It contains the result of an executed task 
 *
 * @author Yu-Bing Chen
 */
public class PSTaskResult implements IPSTaskResult
{
   /**
    * Constructor with all necessary info of a finished job. Note, the following
    * will be added to the context variables: <TABLE BORDER="1">
    * <TR>
    * <TH>Variable Name</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>$sys.completed</TD>
    * <TD>It is true if the job execution was completed; otherwise false.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.problemDesc</TD>
    * <TD>The problem description in case of execution failure.</TD>
    * </TR>
    * </TABLE>
    * 
    * @param wasCompleted it is <code>true</code> of the job execution was
    *    completed; otherwise it is <code>false</code>.
    * @param problemDesc the problem description of the finished job. It may be
    *    <code>null</code> or empty of the job was successful; otherwise it may
    *    not be <code>null</code> or empty.
    * @param variables the additional information of the finished job. It may be
    *    <code>null</code> if there is no additional info.
    */
   public PSTaskResult(boolean wasCompleted, String problemDesc,
         Map<String, Object> variables)
   {
      if ((!wasCompleted) && StringUtils.isBlank(problemDesc))
      {
         throw new IllegalArgumentException(
               "problemDesc may not be null or empty if wasSuccess is false.");
      }
        
      
      m_wasCompleted = wasCompleted;
      m_problemDesc = problemDesc;
      if (variables != null)
         m_variables.putAll(variables);
      
      m_variables.put("$sys.completed", wasCompleted);
      m_variables.put("$sys.problemDesc", problemDesc);
   }
   
   /*
    * //see base class method for details
    */
   public boolean wasSuccess()
   {
      return m_wasCompleted;
   }
   
   /*
    * //see base class method for details
    */
   public String getProblemDescription()
   {
      return m_problemDesc;
   }
   
   /*
    * //see base class method for details
    */
   public Map<String,Object> getNotificationVariables()
   {
      return m_variables;
   }
   
   /**
    * See ctor, default to <code>true</code>.
    */
   private boolean m_wasCompleted = true;
   
   /**
    * See ctor, default to <code>null</code>
    */
   private String m_problemDesc = null;
   
   /**
    * See ctor, default to empty.
    */
   private Map<String,Object> m_variables = new HashMap<>();
}
