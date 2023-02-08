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

import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;

/**
 * The PSRequestWaitTooLongError class is used to report the removal
 * of user requests from the queue list due to the ending of the
 * waiting period inside the queue.
 * <p>
 * An error message containing the user's session id and the number
 * of requests in the queue is logged when this error is encountered.
 *
 * @author    Jian Huang
 * @version   1.0
 * @since     1.0
 */
public class PSRequestWaitTooLongError extends PSLogError
{
  /**
   * Report a request waiting period expiration in the queue
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
   * @param   applId     the ID of the application that generated the error
   * @param   sessionId  the session ID of the user making the request
   * @param   size       the current size of the request queue
   */
  public PSRequestWaitTooLongError(int applId, String sessionId, int size)
  {
    super(applId);

    if (sessionId == null)
      m_sessId = "";
    else
      m_sessId = sessionId;

    m_size = size;
  }

  /**
   * subclasses must override this to build the message in the
   * specified locale
   */
  protected PSLogSubMessage[] buildSubMessages(Locale loc)
  {
    PSLogSubMessage[] msgs = new PSLogSubMessage[1];

      /* use IPSServerErrors.REQUEST_WAIT_TOO_LONG along with:
       *     [0] = m_sessId
       *     [1] = m_size
       * to format the submessage text
       */
    Object[] args = { m_sessId, new Integer(m_size) };
    msgs[0] = new PSLogSubMessage(IPSServerErrors.REQUEST_WAIT_TOO_LONG,
                                  PSErrorManager.createMessage(
                                    IPSServerErrors.REQUEST_WAIT_TOO_LONG,
                                    args, loc));

    return msgs;
  }

  private String m_sessId;
  private int    m_size;
}
