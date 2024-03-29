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

package com.percussion.conn;


/**
 * The IPSConnectionErrors inteface is provided as a convenient mechanism
 * for accessing the various connectivity related error codes. Connectivity
 * errors are in the range 3001 - 4000. Within this range, errors are
 * further broken down as follows:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>3001 - 3100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>3101 - 3200</TD><TD>connection object errors</TD></TR>
 * <TR><TD>3201 - 3500</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSConnectionErrors {

   /**
    * The specified port number is invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the port number</TD></TR>
    * </TABLE>
    */
   public static final int PORT_NUMBER_INVALID         = 3001;

   /**
    * used when throwing PSIllegalArgumentException for missing port
    * <p>
    * No arguments are used in this message.
    */
   public static final int   PORT_NUMBER_REQD            = 3002;

   /**
    * used when throwing PSIllegalArgumentException for bad host
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the host address specified</TD></TR>
    * </TABLE>
    */
   public static final int   HOST_ADDRESS_INVALID         = 3003;

   /**
    * used when throwing PSIllegalArgumentException for missing host
    * <p>
    * No arguments are used in this message.
    */
   public static final int   HOST_ADDRESS_REQD            = 3004;

   /**
    * used when throwing PSIllegalArgumentException for bad queue limit
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the queue limit specified</TD></TR>
    * </TABLE>
    */
   public static final int   QUEUE_LIMIT_INVALID          = 3005;

   /**
    * used when throwing IllegalStateException due to socket already
    * being closed
    * <p>
    * No arguments are used in this message.
    */
   public static final int   CONN_ALREADY_CLOSED         = 3006;

   /**
    * used when throwing PSIllegalArgumentException for missing connInfo
    * <p>
    * No arguments are used in this message.
    */
   public static final int   CONN_PROPS_REQD            = 3007;

   /**
    * An unknown exception occurred while communicating with the server.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the URL used to access the server</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_NOT_RESPONDING      = 3008;

   /**
    * used when throwing IllegalStateException due to socket already
    * being opened
    * <p>
    * No arguments are used in this message.
    */
   public static final int   CONN_ALREADY_OPENED         = 3009;

   /**
    * A required connection property is not defined
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the property name</TD></TR>
    * </TABLE>
    */
   public static final int CONN_PROP_MISSING = 3010;

   /**
    * An unsupported SSL cipher has been specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cipher</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_SSL_CIPHER = 3011;

   /**
    * Invalid value specified for socket timeout.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the socket timeout specified</TD></TR>
    * </TABLE>
    */
   public static final int SOCKET_TIMEOUT_INVALID = 3012;

   /**
    * An unknown exception occurred while communicating with the server.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_SERVER_EXCEPTION   = 3101;

   /**
    * An exception occurred on the server while processing a request.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception class</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_GENERATED_EXCEPTION   = 3102;

   /**
    * data returned by a request is not of the expected type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected type</TD></TR>
    * <TR><TD>1</TD><TD>the type received</TD></TR>
    * </TABLE>
    */
   public static final int RESPONSE_INVALID_MIME_TYPE   = 3103;

   /**
    * an error occurred parsing the response document
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SAXParseException.getMessage()</TD></TR>
    * <TR><TD>1</TD><TD>SAXParseException.getLineNumber()</TD></TR>
    * <TR><TD>2</TD><TD>SAXParseException.getColumnNumber()</TD></TR>
    * </TABLE>
    */
   public static final int RESPONSE_PARSE_EXCEPTION   = 3104;

   /**
    * an error occurred parsing the response document
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SAXParseException.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int RESPONSE_PARSE_EXCEPTION_NOLINEINFO   = 3105;

   /**
    * a PSConnection object cannot be constructed with a null socket
    * <p>
    * No arguments are used in this message.
    */
   public static final int NULL_SOCKET                  = 3106;

   public static final int UNAUTHORIZED                  = 3107;

}

