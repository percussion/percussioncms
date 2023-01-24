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
 * The PSLogApplicationStart class is used to log an application
 * startup event.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogApplicationStart extends PSLogInformation {
   
   /**
    * Construct a log message identifying the application being started.
    *
    * @param   id       the id of the application being started
    * @param   name     the name of the application being started
    */
   public PSLogApplicationStart(int id, java.lang.String name)
   {
      super(LOG_TYPE, id);
      m_Subs = new PSLogSubMessage[1];
      m_Subs[0] = new PSLogSubMessage(SUBTYPE_APPNAME, name); 
   }

   /**
    * Get the sub-messages (type and text). A single sub-message is created
    * containing the name of the application being started.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_Subs;
   }

   /**
    * Application start is set as type 4.
    */
   private static final int LOG_TYPE = 4;
   
   /**
    *   Right now, there is only one sub-message type
    */
   private static final int SUBTYPE_APPNAME = 0;
   /**
    *   The array of sub-messages
    */
   private PSLogSubMessage[] m_Subs = null;
}
