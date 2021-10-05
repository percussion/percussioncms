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
 * The PSApplicationAuthorizationError class is used to report a failed
 * attempt to login to an application.
 * <p>
 * An error message containing the host address and login id is logged
 * when this error is encountered.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSApplicationAuthorizationError extends PSLogError {
   
   /**
    * Report an authorization failure.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()} or
    * {@link com.percussion.server.PSApplicationHandler#getId PSApplicationHandler.getId()}.
    *
    * @param      applId      the id of the application that generated
    *                           the error
    *
    * @param      ipAddress   the IP address of the host causing the
    *                           authorization failure
    *
    * @param      loginId      the login id used which caused the error
    *
    * @param      errorCode   the error code provided by the driver
    *                           when attempting the connection
    *
    * @param      errorString   the error string provided by the driver
    *                           when attempting the connection
    */
   public PSApplicationAuthorizationError(int applId,
                                          java.lang.String ipAddress,
                                          java.lang.String loginId,
                                          int errorCode,
                                          java.lang.String errorString)
   {
      super(applId);

      if (ipAddress == null)
         m_host      = "";
      else
         m_host      = ipAddress;

      if (loginId == null)
         m_uid         = "";
      else
         m_uid         = loginId;

      m_errorCode      = errorCode;

      if (errorString == null)
         m_errorString = "";
      else
         m_errorString = errorString;
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

      /* the generic submessage first */
      Object[] args = { m_host, m_uid };
      msgs[0]   = new PSLogSubMessage(
                                 IPSServerErrors.AUTHORIZATION_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.AUTHORIZATION_ERROR,
                                       args,
                                       loc));

      /* use the errorCode/errorString to format the second submessage */
      Object[] nativeArgs = { new Integer(m_errorCode), m_errorString };
      msgs[1]   = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.NATIVE_ERROR,
                                       nativeArgs,
                                       loc));

      return msgs;
   }

   private String   m_host;
   private String   m_uid;
   private int      m_errorCode;
   private String   m_errorString;
}
