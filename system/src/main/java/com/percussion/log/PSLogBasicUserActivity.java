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
 * The PSLogBasicUserActivity class is used to log basic user activity.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the session id of the user making the request</li>
 * <li>the host address of the requestor</li>
 * <li>the user name of the requestor (if authenticated)</li>
 * <li>the requested URL</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogBasicUserActivity extends PSLogInformation {
   
   /**
    * Construct a log message containing the basic user activity
    * information.
    * <p>
    * The following list contains the supported keys and values:
    * <ul>
    *   <li>sessionId - (subtype 1) - the session id of the user making
    *       this request.</li>
    *   <li>host - (subtype 2) the host address of the requestor</li>
    *   <li>user - (subtype 3) the user name of the requestor (if authenticated)</li>
    *   <li>url - (subtype 4) the requested URL</li>
    * </ul>
    *
    * @param   info     the information to be reported
    */
   public PSLogBasicUserActivity(int applId, java.util.Map info)
   {
      super(LOG_TYPE, applId);

      String subSessionId = (String)info.get("sessionId");
      if (subSessionId == null)
         subSessionId = "";

      String subHost = (String)info.get("host");
      if (subHost == null)
         subHost = "";

      String subUser = (String)info.get("user");
      if (subUser == null)
         subUser = "";

      String subURL = (String)info.get("url");
      if (subURL == null)
         subURL = "";

      m_subs      = new PSLogSubMessage[4];
      m_subs[0]   = new PSLogSubMessage(1, subSessionId);
      m_subs[1]   = new PSLogSubMessage(2, subHost);
      m_subs[2]   = new PSLogSubMessage(3, subUser);
      m_subs[3]   = new PSLogSubMessage(4, subURL);
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each piece of information reported when this object was created.
    * See the
    * {@link #PSLogBasicUserActivity(int,java.util.Map) constructor}
    * for more details.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_subs;
   }

   /**
    * Basic user activity is set as type 7.
    */
   private static final int LOG_TYPE = 7;
   private PSLogSubMessage[] m_subs = null;
}

