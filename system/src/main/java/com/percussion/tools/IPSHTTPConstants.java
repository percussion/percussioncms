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
 * This interface defines a few constant strings and HTTP constants required
 * frequently. This is only a partial list. More may be added as required.
 */
public interface IPSHTTPConstants
{
   /**
   * String representing HTTP header Content-Type
   */
   public static final String HTTP_CONTENT_TYPE = "Content-Type";

   /**
   * String representing HTTP header Content-Length
   */
   public static final String HTTP_CONTENT_LENGTH = "Content-Length";

   /**
   * String representing CGI header HTTP_USERAGENT
   */
   public static final String HTTP_USERAGENT = "User-Agent";

   /**
   * String representing CGI header HOST
   */
   public static final String HTTP_HOST = "HOST";

   /**
   * default protocol
   */
   public static final String DEFAULT_PROTOCOL = "http";

   /**
    * https protocol.
    */
   public static final String HTTPS_PROTOCOL = "https";

   /**
   * HTTP status that is returned after sending a request to a URL successfully.
   */
   public static final int HTTP_STATUS_OK = 200;

   /**
   * HTTP status that is returned after sending a request to a URL indicating authentication failure.
   */
   public static final int HTTP_STATUS_BASIC_AUTHENTICATION_FAILED = 401;

   /**
   * HTTP status cutoff value. HTTP status values below 400 are actually not errors but warnings
   * Better we check for this value rather than full success value pf HTTP_STATUS_OK above.
   */
   public static final int HTTP_STATUS_OK_RANGE = 399;
}
