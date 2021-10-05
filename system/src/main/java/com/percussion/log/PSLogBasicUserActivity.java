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

