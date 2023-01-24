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
package com.percussion.rx.config;

/**
 * This is an unchecked (runtime) exception. It may be used in the Design Object 
 * configuration sub-system.
 *
 * @author YuBingChen
 */
public class PSConfigException extends RuntimeException
{
   /**
    * Constructs an exception with the specified detail message.
    * @param errorMsg the specified detail message.
    */
   public PSConfigException(String errorMsg)
   {
      super(errorMsg);
   }
   
   /**
    * Constructs an exception with the specified cause.
    * @param e the cause of the exception.
    */
   public PSConfigException(Throwable e)
   {
      super(e);
   }

   /**
    * Constructs an exception with the specified detail message and the cause.
    * @param errorMsg the specified detail message.
    * @param e the cause of the exception.
    */
   public PSConfigException(String errorMsg, Throwable e)
   {
      super(errorMsg, e);
   }
   
}
