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


/**
 * The PSLogServerWarning class is used to log a warning (information)
 * message for the server.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogServerWarning extends PSLogInformation {

   /**
    * Construct a log message for a server warning and optionally
    * display the message on the server console.
    *
    * @param   msgCode         the code describing the type of warning
    *
    * @param   msgParams      if the string associated with the message
    *                           code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    *
    * @param   toConsole      <code>true</code> to display the message
    *                         on the server console
    *
    * @param   origin         if <code>toConsole</code> is <code>true</code>
    *                           the name to use in the console message.
    *                           if <code>null<code>, "Server" is used.
    */
   public PSLogServerWarning(
      int msgCode, Object[] msgParams, boolean toConsole, String origin)
   {
      super(LOG_TYPE, 0);

      m_msgCode = msgCode;
      m_msgArgs = msgParams;

      if (toConsole) {
         getSubMessages();   // create the message text
         if (origin == null) origin = "Server";
         com.percussion.server.PSConsole.printMsg(
            origin, m_Subs[0].getText(), null);
      }
   }

   /**
    * Construct a log message for a server warning.
    *
    * @param   msgCode         the code describing the type of warning
    *
    * @param   msgParams      if the string associated with the message
    *                           code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    */
   public PSLogServerWarning(int msgCode, Object[] msgParams)
   {
      this(msgCode, msgParams, false, null);
   }

   /**
    * Get the sub-messages (type and text). A single sub-message is created
    * containing the name of the application being started.
    *
    * @return   an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      if (m_Subs == null) {
         m_Subs = new PSLogSubMessage[1];

         /* use the msgCode/msgArgs to format the submessage */
         m_Subs[0] = new PSLogSubMessage( m_msgCode, 
            com.percussion.error.PSErrorManager.createMessage(
            m_msgCode, m_msgArgs));
      }

      return m_Subs;
   }


   /**
    * server warning is set as type 10.
    */
   private static final int LOG_TYPE = 10;

   /**
    *   The array of sub-messages
    */
   protected   PSLogSubMessage[]      m_Subs = null;

   protected int         m_msgCode;
   protected Object[]   m_msgArgs;
}

