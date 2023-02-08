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

import java.util.Locale;

/**
 * The PSNonFatalError class is used to report non-fatal error conditions
 * encountered during processing. An alternative plan can be followed
 * to handle the request, but we will log the condition so we can look
 * into it (and know it occurred). This may expose a flaw in our logic
 * (eg, occurs often when we thought it never should).
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSNonFatalError extends PSLogError
{
   /**
    * Report a non-fatal error. The error code and parameters should
    * clearly define where the error occurred for easy debugging.
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
   public PSNonFatalError(int errorCode, Object[] errorParams)
   {
      super(0);
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * Report a non-fatal error. The error code and parameters should
    * clearly define where the error occurred for easy debugging.
    *
    * @param      errorCode   the error code describing the type of error
    *
    *
    * @param      singleArg   the argument to use as the sole argument in
    *                           the error message
    */
   public PSNonFatalError(int errorCode, Object singleArg)
   {
      this(errorCode, new Object[] { singleArg });
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[1];

      /* the generic submessage first */
      msgs[0] = new PSLogSubMessage(
         m_errorCode,
         PSErrorManager.createMessage(m_errorCode, m_errorArgs, loc));

      return msgs;
   }


   private int         m_errorCode;
   private Object[]   m_errorArgs;
}

