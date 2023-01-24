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


/**
 * The PSErrorException class provides a wrapper to throw PSLogError objects
 * which contain more useful error information. The contained PSLogError
 * object can then be logged directly without figuring out what the context
 * was.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSErrorException extends PSException
{
   /**
    * Constructs a new <code>PSErrorException</code> to allow the specified
    * <code>PSLogError</code> to be thrown.  This exception will use the code 
    * {@link com.percussion.server.IPSServerErrors#WRAPPED_LOG_ERROR
    * WRAPPED_LOG_ERROR} and the exception text will consist of all the log
    * error's sub-messages concatenated together.
    *
    * @param err the error object to wrap; if <code>null</code>, the text of
    * this exception will be empty.
    */
   public PSErrorException(PSLogError err)
   {
      super( IPSServerErrors.WRAPPED_LOG_ERROR, new Object[] {
         getMessageText( err ), new Integer( getMessageCode( err ) ) } );
      m_errorObject = err;
   }
   
   /**
    * Same as {@link #PSErrorException(PSLogError)} but allows to specify the
    * cause of the exception.
    * 
    * @param err the error object to wrap; if <code>null</code>, the text of
    *           this exception will be empty.
    * @param cause The cause of the exception. May be <code>null</code>, in that
    *           case it means the cause is unknown.
    */
   public PSErrorException(PSLogError err, Throwable cause)
   {
      super( IPSServerErrors.WRAPPED_LOG_ERROR, new Object[] {
         getMessageText( err ), new Integer( getMessageCode( err ) ) }, cause);
      m_errorObject = err;
   }

   /**
    * Get the error object associated with this exception.
    *
    * @return the error object, may be <code>null</code>
    */
   public PSLogError getLogError()
   {
      return m_errorObject;
   }


   // this is lame, but...
   protected static int getMessageCode(PSLogError err)
   {
      if (err == null)
         return 0;

      PSLogSubMessage[] msgs = err.getSubMessages();
      if ((msgs == null) || (msgs.length == 0))
         return 0;

      return msgs[0].getType();
   }

   /**
    * Collects all of the log error's sub messages together into a single
    * string.
    *
    * @param err the log error, may be <code>null</code>.
    *
    * @return all of the log error's sub-message concatenated together, or
    * the empty string if err is <code>null</code> or contains no sub-messages.
    * Never <code>null</code>.
    */
   protected static String getMessageText(PSLogError err)
   {
      StringBuilder errorMsgs = new StringBuilder();
      if (err != null)
      {
         PSLogSubMessage[] msgs = err.getSubMessages();
         for (int i = 0; i < msgs.length; i++)
         {
            errorMsgs.append( msgs[i].getText() );
            errorMsgs.append( " " );
         }
      }
      return errorMsgs.toString();
   }


   protected PSLogError m_errorObject;
}

