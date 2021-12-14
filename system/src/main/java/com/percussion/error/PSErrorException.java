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

