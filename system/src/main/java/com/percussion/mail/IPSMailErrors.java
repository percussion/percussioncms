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
 
package com.percussion.mail;

/**
 * The IPSMailErrors interface is provided as a convenient mechanism
 * for accessing the various mail related error codes. Mail
 * errors are in the range 3501 - 4000. 
 */
public interface IPSMailErrors
{
   /**
    * An empty or null email address was specified.
    * <p>
    * No arguments are passed for this message.
    */
   public static final int MAIL_ADDRESS_EMPTY = 3501;

   /**
    * An invalid email address was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid email address</TD></TR>
    * </TABLE>
    */
   public static final int MAIL_ADDRESS_INVALID = 3502;

   /**
    * An invalid custom To: header was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the header</TD></TR>
    * </TABLE>
    */
   public static final int MAIL_CUSTOM_TO_HEADER_INVALID = 3503;

   /**
    * An empty or null custom To: header was specified.
    * <p>
    * No arguments are passed for this message.
    */
   public static final int MAIL_CUSTOM_TO_HEADER_EMPTY = 3504;

   /**
    * An unexpected exception was encountered while sending a
    * mail message.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int MAIL_SEND_UNEXPECTED_EXCEPTION = 3505;

   /**
    * An exception occurred when mail server is expected to be up.
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int MAIL_SERVER_UP_EXCEPTION = 3506;

   /**
    * An exception occurred when trying to communicate with the mail server.
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int MAIL_SERVER_CONNECTION_ERROR = 3507;

   /**
    * The attempted host is invalid.
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int HOST_NOT_VALID               = 3508;
}
