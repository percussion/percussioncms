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


package com.percussion.installerbot;

/**
 * Indicates an error during driving a console application. 
 * 
 * @see IPSConsoleAppDriver 
 * @author Andriy Palamarchuk
 */
public class PSConsoleAppDriverException extends Exception
{
   /**
    * @see #setTimeOut(boolean)
    */
   private boolean m_timeOut;

   /**
    * Creates exception with specified message.
    */
   public PSConsoleAppDriverException(final String message) {
      super(message);
   }

   /**
    * Creates exception with specified root cause.
    */
   public PSConsoleAppDriverException(final Throwable rootCause) {
      super(rootCause);
   }

   /**
    * @see #setTimeOut(boolean)
    */
   public boolean isTimeOut()
   {
      return m_timeOut;
   }

   /**
    * Indicates whether exception was thrown because an expected pattern was not
    * found during specified period of time.
    * Is not set by the exceptions thrown by {@link IPSConsoleAppDriver}.
    * Instead can be used in upper-level logic constructing the exception to
    * indicate timeout/pattern mismatch.  
    */
   public void setTimeOut(boolean timeOut)
   {
      this.m_timeOut = timeOut;
   }
}
