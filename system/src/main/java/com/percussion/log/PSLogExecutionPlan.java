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

