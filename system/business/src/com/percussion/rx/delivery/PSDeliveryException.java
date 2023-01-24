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
package com.percussion.rx.delivery;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception thrown due to problems in delivery.
 * 
 * @author dougrand
 */
public class PSDeliveryException extends PSBaseException
{
   /**
    * Ctor
    * @param msgCode
    * @param cause
    * @param args
    */
   public PSDeliveryException(int msgCode, Throwable cause, Object... args) 
   {
      super(msgCode, cause, args);
   }

   /**
    * Ctor
    * @param msgCode
    * @param arrayArgs
    */
   public PSDeliveryException(int msgCode, Object... args) 
   {
      super(msgCode, args);
   }

   /**
    * Ctor
    * @param msgCode
    */
   public PSDeliveryException(int msgCode) {
      super(msgCode);
   }

   /**
    * 
    */
   private static final long serialVersionUID = -1655303624680066236L;

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.rx.delivery.PSDeliveryErrorStringBundle";
   }

}
