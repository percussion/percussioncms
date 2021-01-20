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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.error;

import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;


/**
 * The PSPoorResponseTimeError class is used to report poor
 * response time for the processing of a request through the application.
 * <p>
 * An error message containing the user's session id and the amount of
 * time to handle the request is logged when this error is encountered. If
 * detailed user activity logging is also enabled, the request can be
 * tracked back to help determine why it took so long.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSPoorResponseTimeError extends PSLogError {
   
   /**
    * Report poor response time for the processing of a request.
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
    * @param      timeMS         the response time (in milliseconds)
    */
   public PSPoorResponseTimeError(  int applId,
                                    java.lang.String sessionId,
                                    int timeMS)
   {
      super(applId);
      
      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      m_timeMS = timeMS;
   }

   /**
    * Get the response time in milliseconds.
    */
   public int getResponseTimeMS()
   {
      return m_timeMS;
   }

   /**
    * Get the session id of the user making the request.
    */
   public String getSessionId()
   {
      return m_sessId;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[1];

      /* use IPSServerErrors.POOR_RESPONSE_TIME along with:
       *    [0] = m_sessId
       *    [1] = (m_timeMS / 1000) in 0.00 format (so we have some ms info)
       * to format the the submessage text
       */
      Object[] args = { m_sessId,
                        new Double((double)((double)m_timeMS / 1000.0)) };
      msgs[0]   = new PSLogSubMessage(
                              IPSServerErrors.POOR_RESPONSE_TIME,
                              PSErrorManager.createMessage(
                                    IPSServerErrors.POOR_RESPONSE_TIME,
                                    args, loc));

      return msgs;
   }


   private String   m_sessId;
   private int    m_timeMS;
}

