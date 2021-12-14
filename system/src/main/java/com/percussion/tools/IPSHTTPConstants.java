/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
