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
package com.percussion.services.publisher;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception thrown by the publisher service
 * 
 * @author dougrand
 */
public class PSPublisherException extends PSBaseException
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Ctor
    * 
    * @param msgCode message code, used to lookup the correct text message that
    *           is listed in the corresponding properties
    * @param arrayArgs the arguments to the message code, may be empty
    */
   public PSPublisherException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Ctor
    * 
    * @param msgCode message code, used to lookup the correct text message that
    *           is listed in the corresponding properties
    * @param cause the original exception cause
    * @param arrayArgs the arguments to the message code, may be empty
    */
   public PSPublisherException(int msgCode, Throwable cause,
         Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode message code, used to lookup the correct text message that
    *           is listed in the corresponding properties
    */
   public PSPublisherException(int msgCode) {
      super(msgCode);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.publisher.PSPublisherErrorStringBundle";
   }

}
