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
package com.percussion.server;

import com.percussion.error.PSErrorManager;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;

import java.util.Locale;

/**
 * Use this error to report failed user authentications.
 */
public class PSAuthenticationError extends PSLogError
{
   /**
    * Constructs an authentication error for the supplied error code and
    * parameters.
    * 
    * @param errorCode the error code is a reference to the error message.
    * @param errorParams the message parameters formatted into the message
    *    at runtime, may be <code>null</code> but not empty.
    */
   public PSAuthenticationError(int errorCode, Object[] errorParams)
   {
      super(0);
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * Constructs an authentication error for the supplied error code and
    * parameter.
    * 
    * @param errorCode the error code is a reference to the error message.
    * @param singleArg the message parameter formatted into the message
    *    at runtime, not <code>null</code>.
    */
   public PSAuthenticationError(int errorCode, Object singleArg)
   {
      this(errorCode, new Object[] { singleArg });
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   /**
    * Builds the error messages for the supplied locale.
    * 
    * @param loc the locale to use to build the error messages, may be
    *    <code>null</code>.
    * @return an array of error messages, never <code>null</code> or empty.
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[1];

      /* the generic submessage first */
      msgs[0] = new PSLogSubMessage(m_errorCode,
         PSErrorManager.createMessage(m_errorCode, m_errorArgs, loc));

      return msgs;
   }

   /**
    * The error code, initialized in ctor, never changed after that.
    */
   private int m_errorCode = 0;
   
   /**
    * An array of error message arguments, initialized in ctor, never changed
    * after that. May be <code>null</code>.
    */
   private Object[] m_errorArgs = null;
}

