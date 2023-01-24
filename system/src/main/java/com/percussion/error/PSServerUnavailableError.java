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
 * The PSUnavailableError class is used to report that the server
 * is currently unavailable.
 *
 * @author      David Gennaco
 * @version      1.1
 * @since      1.1
 */
public class PSServerUnavailableError extends PSLogError {
   
   /**
    * Report that the server is unavailable.
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
   public PSServerUnavailableError(   int errorCode,
                                    Object[] errorParams)
   {
      super(0);
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* the generic submessage first */
      msgs[0]   = new PSLogSubMessage(
                              IPSServerErrors.SERVER_UNAVAILABLE_ERROR_MSG,
                              PSErrorManager.getErrorText(
                                    IPSServerErrors.SERVER_UNAVAILABLE_ERROR_MSG,
                                    false,
                                    loc));

      /* use the errorCode/errorParams to format the second submessage */
      msgs[1]   = new PSLogSubMessage(
                              m_errorCode,
                              PSErrorManager.createMessage(   m_errorCode,
                                                            m_errorArgs,
                                                            loc));

      return msgs;
   }

   private int         m_errorCode;
   private Object[]   m_errorArgs;
}


