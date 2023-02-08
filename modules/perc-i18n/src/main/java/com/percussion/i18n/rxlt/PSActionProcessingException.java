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
package com.percussion.i18n.rxlt;

/**
 * Exception thrown by the action implementation methods. This wraps the message
 * from any other exception thrown during processing.
 */
public class PSActionProcessingException extends RuntimeException
{
   /**
    * Default constructor.
    */
   public PSActionProcessingException()
   {
      super();
   }

   /**
    * Constructor that takes the error message.
    * @param msg must not be <code>null</code>.
    */
   public PSActionProcessingException(String msg)
   {
      super(msg);
   }

   /**
    * Constructor that takes the error message.
    * @param msg must not be <code>null</code>.
    */
   public PSActionProcessingException(String msg, Throwable e)
   {
      super(msg, e);
   }
}
