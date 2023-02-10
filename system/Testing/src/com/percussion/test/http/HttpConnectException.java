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
package com.percussion.test.http;

/**
 * Exception to signal that an attempt to establish an HTTP connction failed.
 */
public class HttpConnectException extends Exception
{
   /**
    * A constructor taking an error message.
    *
    * @param msg the error message, may be <code>null</code> or empty.
    */
   public HttpConnectException(String msg)
   {
      super(msg);
   }
}
