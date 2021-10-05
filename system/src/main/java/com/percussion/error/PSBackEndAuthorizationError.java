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

import com.percussion.data.IPSBackEndErrors;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;


/**
 * The PSBackEndAuthorizationError class is used to report a failed
 * attempt to login to a back-end (database) driver for authorization
 * reasons.
 * <p>
 * An error message containing the host address and login id is
 * logged when this error is encountered. The back-end driver/server and
 * any information provided by the back-end is also logged.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndAuthorizationError extends PSBackEndError
{
   /**
    * Report an authorization failure.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()}
    *
    * @param      applId      the id of the BackEnd that generated
    *                           the error
    *
    * @param      ipAddress   the IP address of the host causing the
    *                           authorization failure
    *
    * @param      driver      the back-end driver the login attempt was
    *                           for
    *
    * @param      server      the back-end server the login attempt was
    *                           for
    *
    * @param      loginId      the login id used which caused the error
    *
    * @param      errorCode   the error code provided by the driver
    *                           when attempting the connection
    *
    * @param      errorString   the error string provided by the driver
    *                           when attempting the connection
    */
   public PSBackEndAuthorizationError( int applId,
                                       java.lang.String ipAddress,
                                       java.lang.String driver,
                                       java.lang.String server,
                                       java.lang.String loginId,
                                       int errorCode,
                                       java.lang.String errorString)
   {
      super(applId, errorCode, new Object[] { errorString });

      if (ipAddress == null)
         m_host      = "";
      else
         m_host      = ipAddress;

      if (driver == null)
         m_driver      = "";
      else
         m_driver      = ipAddress;

      if (loginId == null)
         m_uid         = "";
      else
         m_uid         = loginId;

      m_errorCode      = errorCode;
   }

   /**
    * Get the host name (or specifically, the IP address of the host).
    */
   public String getHost(){
      return m_host;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* use ERROR_CODE along with:
       *    [0] = ipAddress
       *    [1] = loginId
       *    [2] = driver
       *    [3] = server
       * to format the first submessage
       */
      Object[] params = { m_host, m_uid, m_driver, m_server };
      msgs[0] = new PSLogSubMessage(
         IPSBackEndErrors.AUTHORIZATION_ERROR,
         PSErrorManager.createMessage(
            IPSBackEndErrors.AUTHORIZATION_ERROR, params, loc));

      /* use IPSServerErrors.NATIVE_ERROR along with
       *    [0] = errorCode
       *    [1] = errorString
       * to format the second submessage
       */
      Object[] nativeArgs = { new Integer(m_errorCode), m_errorArgs[0] };
      msgs[1] = new PSLogSubMessage(
         m_errorCode, 
         PSErrorManager.createMessage(
            IPSServerErrors.NATIVE_ERROR, nativeArgs, loc));

      return msgs;
   }

   private String   m_host;
   private String   m_driver;
   private String   m_server;
   private String   m_uid;
}
