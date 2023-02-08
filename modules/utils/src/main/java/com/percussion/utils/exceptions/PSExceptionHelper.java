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
package com.percussion.utils.exceptions;

import org.apache.velocity.exception.MethodInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Contains methods to aid in finding root causes
 * 
 * @author dougrand
 */
public class PSExceptionHelper
{
   /**
    * Exceptions can be nested in a variety of ways. The reflection API causes
    * exceptions that occur during method invocation to be wrapped in an
    * {@link InvocationTargetException}, other exceptions put the original
    * problem into a chain, etc. Sometimes it is handy to get the original
    * problem.
    * <p>
    * This method recurses until it finds a throwable that does not have a
    * different cause and is not one of the known exceptions that point to
    * another exception using a different mechanism.
    * 
    * @param ex original exception, never <code>null</code>
    * @param stopAtAppEx if <code>true</code> this will stop searching if it
    *           finds an exception that is derived from {@link PSBaseException}
    *           indicating one of <i>our</i> exceptions, which is presumed to
    *           be a business exception.
    * 
    * @return the root cause, which may be the passed argument. Never
    *         <code>null</code>
    */
   public static Throwable findRootCause(Throwable ex, boolean stopAtAppEx)
   {
      // ATTENTION! Be careful when adding checks for new exception classes
      // as these changes may add dependencies on libraries which are not
      // available in some usages of this class.
      // See below how check for
      // org.apache.velocity.exception.MethodInvocationException is done to
      // avoid the dependency
      if (ex == null)
      {
         throw new IllegalArgumentException("ex may not be null");
      }
      if (ex instanceof InvocationTargetException)
      {
         return findRootCause(((InvocationTargetException) ex)
               .getTargetException(), stopAtAppEx);
      }
      else if (ex instanceof UndeclaredThrowableException)
      {
         return findRootCause(((UndeclaredThrowableException) ex)
               .getUndeclaredThrowable(), stopAtAppEx);
      }
      else if (isVelocityException(ex))
      {
         MethodInvocationException mex = (MethodInvocationException) ex;
         return findRootCause(mex.getWrappedThrowable(), stopAtAppEx);
      }
      else if (stopAtAppEx && ex instanceof PSBaseException)
      {
         return ex;
      }
      else if (ex.getCause() != ex && ex.getCause() != null)
      {
         return findRootCause(ex.getCause(), stopAtAppEx);
      }
      else
      {
         return ex;
      }
   }

   /**
    * Returns <code>true</code> if velocity library is accessible and
    * the provided exception is instance of {@link MethodInvocationException}.
    * @param e the exception to check. Assumed not <code>null</code>.
    */
   private static boolean isVelocityException(Throwable e)
   {
      try
      {
         Class.forName("org.apache.velocity.exception.MethodInvocationException");
         return e instanceof MethodInvocationException;
      }
      catch (ClassNotFoundException ex)
      {
         return false;
      }
   }
}
