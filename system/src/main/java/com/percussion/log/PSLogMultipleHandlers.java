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


import java.util.concurrent.ConcurrentHashMap;

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
   public PSLogMultipleHandlers(int applId, ConcurrentHashMap info)
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
    * {@link #PSLogMultipleHandlers(int, java.util.concurrent.ConcurrentHashMap) constructor}
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

