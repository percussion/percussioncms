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
 * The PSLogMultipleHandlers class is used to log requests matching the
 * request criteria of multiple applications/datasets.
 * <p>
 * When processing requests, the first data set matching the request 
 * criteria handles it. If other data sets exist which are also
 * interested in the request, they will be ignored. This may cause
 * confusion to end users and application designers. By enabling logging,
 * E2 will check subsequent applications and log a message for each
 * application also matching the request criteria. This should only be
 * used for debugging purposes as it may impact performance.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the session id of the user making this request. This can be used to
 *     map back to the request when logging detailed user activity is
 *     enabled.</li>
 * <li>the name of each application/dataset matching the request criteria</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogMultipleHandlers extends PSLogInformation {
   
   /**
    * Construct a log message identifying that multiple handlers attempted
    * to act upon the same request.
    * <p>
    * The following list contains the supported keys and values:
    * <ul>
    * <li>sessionId - (subtype 1) the session id of the user making this
    *     request. This can be used to map back to the request when logging
    *     detailed user activity is enabled.</li>
    * <li>dataSetNames - (subtype 2) a comma delimited String containing
    *     the name of each dataset matching the request criteria</li>
    * </ul>
    *
    * @param   applId   the application containing the handlers
    *
    * @param   info     the information to be reported
    */
   public PSLogMultipleHandlers(int applId, java.util.Hashtable info)
   {
      super(LOG_TYPE, applId);

      String sessId = "";
      String dataSetNames = "";

      if (info != null)
      {
         String sTemp = (String)info.get(PROP_SESS_ID);
         if (sTemp != null)
            sessId = sTemp;

         sTemp = (String)info.get(PROP_DATASET_NAMES);
         if (sTemp != null)
            dataSetNames = sTemp;
      }

      m_Subs = new PSLogSubMessage[2];
      m_Subs[0] = new PSLogSubMessage(0, sessId); 
      m_Subs[1] = new PSLogSubMessage(1, dataSetNames); 
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each piece of information reported when this object was created.
    * See the
    * {@link #PSLogMultipleHandlers(int, java.util.Hashtable) constructor}
    * for more details.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return m_Subs;
   }


   public static final String      PROP_SESS_ID         = "sessionId";
   public static final String      PROP_DATASET_NAMES   = "dataSetNames";


   /**
    * Multiple handler detection is set as type 11.
    */
   private static final int LOG_TYPE = 11;

   /**
    *   The array of sub-messages
    */
   private PSLogSubMessage[] m_Subs = null;
}

