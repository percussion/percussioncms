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

package com.percussion.conn;

import com.percussion.error.PSException;
import com.percussion.utils.exceptions.PSExceptionHelper;

import java.lang.reflect.Constructor;

/**
 * PSServerException is thrown to indicate the server cannot be reached. This
 * may be due to the server being down, a configuration error or a path may not
 * exist between the client and the server.
 * 
 * @author Tas Giakouminakis
 * @version 1.0
 * @since 1.0
 */
public class PSServerException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    * 
    * @param msgCode the error string to load
    * 
    * @param singleArg the argument to use as the sole argument in the error
    *           message
    */
   public PSServerException(int msgCode, Object singleArg) {
      super(msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of arguments. Be sure
    * to store the arguments in the correct order in the array, where {0} in the
    * string is array element 0, etc.
    * 
    * @param msgCode the error string to load
    * 
    * @param arrayArgs the array of arguments to use as the arguments in the
    *           error message
    */
   public PSServerException(int msgCode, Object[] arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    * 
    * @param msgCode the error string to load
    */
   public PSServerException(int msgCode) {
      super(msgCode);
   }

   /**
    * Construct a server exception when an unknown exception occurs while
    * communicating with the server.
    * 
    * @param e the exception
    */
   public PSServerException(Exception e) {
       super(IPSConnectionErrors.UNKNOWN_SERVER_EXCEPTION, PSExceptionHelper
            .findRootCause(e, true));
   }

   /**
    * Construct an exception for the specified class which uses the specified
    * message code and arguments. This class is used primarily on the client
    * side when the server sends an exception. If it's one of our exceptions, it
    * is wrapped into this class. The recipient can then determine if the
    * exception is of a type it is interested in reconstructing and throwing. If
    * so, it can do so by using the class name, msg code and msg args.
    * 
    * @param exceptionClass the originator class
    * 
    * @param msgCode the message code thrown
    * 
    * @param msgArgs the array of arguments thrown
    */
   public PSServerException(String className, int msgCode, Object[] msgArgs) {
      super(msgCode, msgArgs);
      m_className = className;
   }

   /**
    * Get the underlying PSException class associated with this exception. If
    * the exception is not one of ours, null is returned.
    */
   public PSException getOriginatingException()
   {
      if (m_className == null)
         return null;

      try
      {
         Class excClass = Class.forName(m_className);
         Class[] classParams =
         {Integer.TYPE, Class.forName("[Ljava.lang.Object;")};
         Constructor constr = excClass.getConstructor(classParams);
         PSException e = (PSException) constr.newInstance(new Object[]
         {new Integer(m_code), m_args});
         return e;
      }
      catch (Exception e)
      {
         // guess it's not one of ours
         return null;
      }
   }

   /**
    * Get the underlying PSException class name associated with this exception.
    * If the exception is not one of ours, null is returned.
    */
   public String getOriginatingExceptionClass()
   {
      return m_className;
   }

   protected String m_className = null;
}
