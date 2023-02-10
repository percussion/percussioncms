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
package com.percussion.hooks;

/**
 * The IPSServletErrors interface provides a convenient mechanism
 * for accessing servlet related error codes. The servlet error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>10151 - 10200</TD><TD>general errors</TD></TR>
 * </TABLE>
 *
 * @author      Jian Huang
 * @version      2.0
 * @since      2.0
 */
public interface IPSServletErrors
{
   /**
    * Unable to create connections for the supplied parameters.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The host name</TD></TR>
    * <TR><TD>1</TD><TD>The port number</TD></TR>
    * <TR><TD>2</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int CONNECTION_ERROR = 10151;

   /**
    * An invalid port number was supplied, must be a parsable integer.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The port number provided</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_PORT_NUMBER = 10152;

   /**
    * Invalid request parameters were supplied.
    * <p>
    * No parameters are expected
    */
   public static final int INVALID_REQUEST_PARAMETERS = 10153;

   /**
    * Servlet has already been destroyed.
    * <p>
    * No parameters are expected
    */
   public static final int SERVLET_DESTROYED = 10154;

   /**
    * Cannot process request because of a connection failure.
    * <p>
    * No parameters are expected
    */
   public static final int CONNECTION_FAILURE = 10155;

   /**
    * Received invalid status code.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The status code received</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_STATUS_CODE = 10156;

   /**
    * The servlet information string.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The version string</TD></TR>
    * </TABLE>
    */
   public static final int SERVLET_INFORMATION = 10157;

   /**
    * The version string.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The version prefix</TD></TR>
    * <TR><TD>1</TD><TD>The major version</TD></TR>
    * <TR><TD>2</TD><TD>The minor version</TD></TR>
    * <TR><TD>3</TD><TD>The build number</TD></TR>
    * <TR><TD>3</TD><TD>The build id</TD></TR>
    * </TABLE>
    */
   public static final int VERSION_STRING = 10158;
}
