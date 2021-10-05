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

