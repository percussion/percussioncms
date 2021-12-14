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
import com.percussion.server.PSRequestParsingException;

import java.net.InetAddress;
import java.util.Locale;


/**
 * The PSRequestPreProcessingError class is used to report an error
 * encountered during pre-processing of the request. This usually occurs
 * if the request is improperly formed, or the connection dies.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRequestPreProcessingError extends PSLogError {
   
   /**
    * Report an error during the pre-processing of a request. This usually
    * occurs due to an I/O error, such as the client terminating the
    * connection.
    *
    *   @param      host         the host address of the requestor
    *
    * @param      errorCode   the error code describing the type of error
    *
    * @param      errorParams   if the error string associated with the
    *                           error code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    */
   public PSRequestPreProcessingError(   InetAddress host,
                                       int errorCode,
                                       Object[] errorParams)
   {
      super(0);

      m_host = "-not available-";
      try {
         if (host != null)
            m_host = host.getHostAddress();
      } catch (Exception e) { /* ignore this */ }

      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * Report an error during the pre-processing of a request. This usually
    * occurs due to an I/O error, such as the client terminating the
    * connection.
    *
    *   @param      host         the host address of the requestor
    *
    * @param      x            the parsing exception containing the error
    *                           description
    */
   public PSRequestPreProcessingError(   InetAddress host,
                                       PSRequestParsingException x)
   {
      this(host, x.getErrorCode(), x.getErrorArguments());
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* the generic submessage first (contains host address) */
      msgs[0]   = new PSLogSubMessage(
                                 IPSServerErrors.REQUEST_PREPROC_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.REQUEST_PREPROC_ERROR,
                                       new Object[] { m_host },
                                       loc));

      /* the submessage containing m_errorCode/m_errorArgs */
      msgs[1]   = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(   m_errorCode,
                                                               m_errorArgs,
                                                               loc));

      return msgs;
   }


   private String      m_host;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
}
