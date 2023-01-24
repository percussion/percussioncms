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

import java.util.Locale;


/**
 * The PSLargeBackEndRequestQueueError class is used to report large
 * requests queues for a back-end (database) driver. This may signify
 * that the number of connections permitted is insufficient to handle
 * the load, or the amount of time to process a request is taking too long.
 * <p>
 * An error message containing the user's session id and the number of
 * requests in the queue is logged when this error is encountered.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLargeBackEndRequestQueueError extends PSLargeRequestQueueError
{
   /**
    * Report a large back-end request queue.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()} or
    * {@link com.percussion.server.PSApplicationHandler#getId PSApplicationHandler.getId()}.
    * <p>
    * The session id can be obtained from the
    * {@link com.percussion.server.PSUserSession PSUserSession} object
    * contained in the
    * {@link com.percussion.server.PSRequest PSRequest} object.
    *
    * @param      applId         the id of the application that generated
    *                            the error
    *
    * @param      sessionId      the session id of the user making the
    *                            request
    *
    * @param      driver         the back-end driver with the large
    *                            request queue
    *
    * @param      server         the back-end server with the large
    *                            request queue
    *
    * @param      size           the current size of the request queue
    */
   public PSLargeBackEndRequestQueueError(int applId,
                                          java.lang.String sessionId,
                                          java.lang.String driver,
                                          java.lang.String server,
                                          int size)
   {
      super(applId, sessionId, size);

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
      PSLogSubMessage[] msgs = new PSLogSubMessage[1];

      /* use IPSBackEndErrors.REQUEST_QUEUE_FULL along with:
       *    [0] = m_sessId
       *    [1] = m_size
       *    [2] = m_driver
       *    [3] = m_server
       * to format the the submessage text
       */
      Object[] args = { m_sessId, new Integer(m_size), m_driver, m_server };
      msgs[0]   = new PSLogSubMessage(
                              IPSBackEndErrors.REQUEST_QUEUE_FULL,
                              PSErrorManager.createMessage(
                                    IPSBackEndErrors.REQUEST_QUEUE_FULL,
                                    args, loc));

      return msgs;
   }


   private String   m_driver;
   private String   m_server;
}

