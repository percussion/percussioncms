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

package com.percussion.publisher.runner;

/**
 * This exception is thrown by publisher runner if publish HTTP request fails
 * for any reason. This is just same as its base class except for the name.
 */
public class PSPublisherRunnerException extends Exception
{
   /**
    * Empty constructor
    */
   public PSPublisherRunnerException()
   {
      super();
   }

   /**
    * Constructor that takes the error message.
    */
   public PSPublisherRunnerException(String msg)
   {
      super(msg);
   }
}
