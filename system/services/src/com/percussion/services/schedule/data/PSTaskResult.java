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
