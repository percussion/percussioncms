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

package com.percussion.log;

import com.percussion.error.PSErrorManager;


/**
 * The PSLogExecutionPlan class is used to log more informative
 * information concerning the execution plan we choose for an application.
 * This information is not necessarily structured and is anything deemed
 * relevent by the developer for debug purposes.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogExecutionPlan extends PSLogInformation
{
   /**
    * Construct a log message containing the specified message information.
    * <P>
    * Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   applId         the id of the application associated with
    *                           this execution plan
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
   public PSLogExecutionPlan(int applId, int msgCode, Object[] arrayArgs)
   {
      super(LOG_TYPE, applId);

      m_subs      = new PSLogSubMessage[1];
      m_subs[0]   = new PSLogSubMessage( msgCode,
         PSErrorManager.createMessage(msgCode, arrayArgs));
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each piece of information reported when this object was created.
    * See the
    * {@link #PSLogExecutionPlan constructor}
    * for more details.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_subs;
   }

   /**
    * Execution plan is set as type 12.
    */
   private static final int LOG_TYPE = 12;
   private PSLogSubMessage[] m_subs = null;
}

