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
package com.percussion.content.ui.aa.actions;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * An exception that is used when a error or exception occurs
 * when executing an AA client action.
 * If constructed with a nested {@link InvocationTargetException} extracts the
 * nested exception from it.
 */
public class PSAAClientActionException extends Exception
{

   // see base class for detail
   public PSAAClientActionException()
   {
      super();
   }

   // see base class for detail
   public PSAAClientActionException(String message, Throwable cause)
   {
      super(message, maybeGetNestedException(cause));
   }

   // see base class for detail
   public PSAAClientActionException(String message)
   {
      super(message);
   }

   // see base class for detail
   public PSAAClientActionException(Throwable cause)
   {
      super(maybeGetNestedException(cause));
   }

   /**
    * If the passed exception is {@link InvocationTargetException},
    * returns the nested exception, otherwise returns the specified exception.
    * Is static, so it can be called from a constructor.
    * @param t the exception to extract cause exception from.
    * If <code>null</code>, the method returns null.
    */
   private static Throwable maybeGetNestedException(Throwable t)
   {
      return t instanceof InvocationTargetException ? t.getCause() : t;
   }

   /**
    * Creates a message from this exception message and the cause.
    * @param message this exception message. Can be <code>null</code> or empty.
    * @param causeMessage the message of the cause exception.
    * Can be <code>null</code> or empty.
    */
   private String composeMessageFromCause(
         final String message, final String causeMessage)
   {
      if (getCause() == null)
      {
         return message;
      }
      else
      {
         return StringUtils.isBlank(message) ? causeMessage : message;  
      }
   }

   /**
    * {@inheritDoc}
    * This implementation excludes cause exception class name if cause is
    * specified.
    */
   @Override
   public String getMessage()
   {
      return composeMessageFromCause(super.getMessage(),
            getCause() == null ? null : getCause().getMessage());
   }

   /**
    * {@inheritDoc}
    * This implementation excludes cause exception class name if cause is
    * specified.
    */
   @Override
   public String getLocalizedMessage()
   {
      return composeMessageFromCause(super.getLocalizedMessage(),
            getCause() == null ? null : getCause().getLocalizedMessage());
   }
}
