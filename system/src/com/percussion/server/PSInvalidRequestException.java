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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server;


/**
 * PSInvalidRequestException is thrown to indicate that a request was
 * received which could not be parsed properly. This usually occurs when
 * the request did not have the sufficient information so that a reply
 * could be sent.
 */
public class PSInvalidRequestException extends PSRequestParsingException
{
   /**
    * Default constructor. This exception is created when the
    * request has insufficient information and hence the default error code.
    */
   public PSInvalidRequestException()
   {
      super(IPSServerErrors.INVALID_REQUEST_LINE, null);
   }

   /**
    * Constructs a request parsing exception with the specified error
    * contents. The error string is formatted by loading the string
    * associated with the error code and passing it the array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   errorCode      the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *  in the error message, may be <code>null</code> or empty
    */
   public PSInvalidRequestException(int errorCode,
                                    Object[] arrayArgs)
   {
      super(errorCode, arrayArgs);
   }

   /**
    * Constructs a request parsing exception with the specified error
    * string.
    *
    * @param   msg            the error string describing the error,
    * may be <code>null</code> or empty
    */
   public PSInvalidRequestException(String msg)
   {
      super(msg);
   }
}

