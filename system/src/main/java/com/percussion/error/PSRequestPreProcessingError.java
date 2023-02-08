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
