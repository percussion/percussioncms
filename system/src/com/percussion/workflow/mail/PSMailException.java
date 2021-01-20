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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow.mail;
import com.percussion.error.PSException;

import java.util.Locale;

/**
 * This exception is thrown when Mail Program plugin fails to send a mail
 * message for some reason.
 */
public class PSMailException extends PSException
{ 
   /* (non-Javadoc)
    * @see java.lang.Throwable#getLocalizedMessage()
    */
   public String getLocalizedMessage()
   {
      if(m_code == 0)
         return getCause().getLocalizedMessage();
      return super.getLocalizedMessage();
   }
   /* (non-Javadoc)
    * @see com.percussion.error.IPSException#getLocalizedMessage(java.util.Locale)
    */
   public String getLocalizedMessage(Locale locale)
   {
      if(m_code == 0)
         return getCause().getLocalizedMessage();
      return super.getLocalizedMessage(locale);
   }
   /* (non-Javadoc)
    * @see com.percussion.error.PSException#getLocalizedMessage(java.lang.String)
    */
   public String getLocalizedMessage(String language)
   {
      if(m_code == 0)
         return getCause().getLocalizedMessage();
      return super.getLocalizedMessage(language);
   }
   /* (non-Javadoc)
    * @see java.lang.Throwable#getMessage()
    */
   public String getMessage()
   {
      if(m_code == 0)
         return getCause().getMessage();
      return super.getMessage();
   }
   /**
    * Construct an exception for messages taking locale and msgCode arguments.
    *
    * @param language  language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    */

   public PSMailException(String language, int msgCode)
   {
      super(language, msgCode);
   }

   /**
    * Construct an exception for messages taking only message code.
    * @param msgCode    the error string to load
    */
   public PSMailException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking locale and message code
   * arguments and and a single argument.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode the error string to load
    *
    * @param singleArg the argument to use as the sole argument in
    * the error message. Can be <code>null</code>.
    */
   public PSMailException(String language, int msgCode,
         Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking language, message code 
    * and an array of arguments. Be sure to store the arguments in the 
    * correct order in the array, where {0} in the string is array 
    * element 0, etc.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param msgCode  the error string to load
    *
    * @param arrayArgs   the array of arguments to use as the arguments
    * in the error message. Can be <code>null</code>.
    */
   public PSMailException(String language, int msgCode,
         Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }

   /**
    * Constructor that takes another exception as an argument. The plugin can
    * throw an exception when the internal mail program throws its own
    * exception.
    *
    * @param e axception thrown by the mail program API as Exception.
    * Can be <code>null</code>.
    *
    */
   public PSMailException(Exception e)
   {
      super(e.getLocalizedMessage(), e);
   }

   /**
    * Method to return the member exception if object was constructed with
    * another exception as argument.
    * @return Can be <code>null</code>.
    */
   public Throwable getThrowable()
   {
      return getCause();
   }
}
