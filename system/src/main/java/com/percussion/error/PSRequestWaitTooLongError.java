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
