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
