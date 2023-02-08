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

package com.percussion.error;

import com.percussion.data.IPSBackEndErrors;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;


/**
 * The PSBackEndServerDownError class is used to report that a
 * back-end (database) server is unavailable (down).
 * <p>
 * An error message containing the unavailable back-end driver/server is
 * logged when this error is encountered.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndServerDownError extends PSBackEndError
{
   /**
    * Report a back-end server is down (unavailable).
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()}
    *
    * @param      applId         the id of the application that encountered
    *                              the error
    *
    * @param      driver         the back-end driver
    *
    * @param      server         the back-end server
    *
    * @param      errorCode      the error code provided by the driver
    *                              when attempting the connection
    *
    * @param      errorString      the error string provided by the driver
    *                              when attempting the connection
    */
   public PSBackEndServerDownError( int applId,
                                    java.lang.String driver,
                                    java.lang.String server,
                                    int errorCode,
                                    java.lang.String errorString)
   {
      super(applId, errorCode, new Object[] { errorString });

      if (driver == null)
         m_driver = "";
      else
         m_driver = driver;

      if (server == null)
         m_server = "";
      else
         m_server = server;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* use IPSBackEndErrors.SERVER_DOWN_ERROR along with:
       *    [0] = driver
       *    [1] = server
       * to format the first submessage
       */
      msgs[0] = new PSLogSubMessage(
         IPSBackEndErrors.SERVER_DOWN_ERROR,
         PSErrorManager.createMessage(
            IPSBackEndErrors.SERVER_DOWN_ERROR, 
            new String[] { m_driver, m_server },
            loc ));

      /* use IPSServerErrors.NATIVE_ERROR along with
       *    [0] = errorCode
       *    [1] = errorString
       * to format the second submessage
       */
      msgs[1] = new PSLogSubMessage(
         m_errorCode,
         PSErrorManager.createMessage(
            IPSServerErrors.NATIVE_ERROR, 
            new Object[] { new Integer(m_errorCode), m_errorArgs[0] },
            loc ));

      return msgs;
   }


   private String   m_driver;
   private String   m_server;
}

