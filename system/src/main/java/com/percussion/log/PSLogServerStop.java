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


/**
 * The PSLogServerStop class is used to log server shutdown events.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogServerStop extends PSLogInformation {
   
   /**
    * Construct a log message identifying the Server being stopped.
    *
    * @param   name     the name of the user who stopped the server
    */
   public PSLogServerStop(java.lang.String name)
   {
      super(LOG_TYPE, 0);
      m_Subs = new PSLogSubMessage[1];
      m_Subs[0] = new PSLogSubMessage(0, name); 
   }

   /**
    * Get the sub-messages (type and text). A single sub-message is created
    * containing the name of the user who stopped the server.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_Subs;
   }

   /**
    * Server stop is set as type 3.
    */
   private static final int LOG_TYPE = 3;

   /**
    *   The array of sub-messages
    */
   private PSLogSubMessage[] m_Subs = null;
}

