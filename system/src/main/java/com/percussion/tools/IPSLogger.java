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

package com.percussion.tools;


/**
 * This interface defines a few methods to log the messages so that any plugin
 * can use.
 */
public interface IPSLogger
{
   /**
    * Logs the message to whatever output the Logger object was initialized with
    * no matter whether the Logger is in debug mode or not. This method is
    * normally used to log a mandatory message.
    *
    * @param msg - the message to be logged, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the msg is <code>null</code> or
    * <code>empty</code>.
    *
    * @deprecated use log4j methods.
    */
   void logMessage(String msg);

   /**
    * Logs the message to whatever output the Logger object was initialized with
    * only if Logger is in debug mode. This method is normally used to log a
    * extra information for debug purpose.
    *
    * @param msg - the message to be logged, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the msg is <code>null</code> or
    * <code>empty</code>.
    *
    * @deprecated use log4j methods.
    */
   void logDebugMessage(String msg);

   /**
    * If an <code>IOException</code> occurs while logging, the exception will
    * be available through this method.
    * @return the exception message or <code>null</code> if no exception
    * occurred.
    *
    * @deprecated use log4j methods.
    */
   String getLoggerError();

   /**
    * Resets the logger exception string to <code>null</code>.
    * @deprecated use log4j methods.
    */
   void resetLoggerError();
}
