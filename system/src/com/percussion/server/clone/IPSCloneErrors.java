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
package com.percussion.server.clone;

/**
 * This interface contains the error codes for all exceptions thrown by
 * classes in this pkg.
 * 
 * The search error code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>17501 - 18000</TD><TD>general errors</TD></TR>
 * </TABLE>
 * 
 * The message strings for clone messages are stored in the i18n resource 
 * bundle, not the error string bundle.
 */
public interface IPSCloneErrors
{
   /**
    * The specified clone source id could not be parsed into an integer.
    * <p>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The supplied clone source id</TD></TR>
    * <TR><TD>1</TD><TD>The actual error message</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CLONESOURCEID = 17501;

   /**
    * The current user could not be authenticated to perform a clone request.
    * <p>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The actual error message</TD></TR>
    * </TABLE>
    */
   public static final int NOT_AUTHENTICACATED = 17502;

   /**
    * The current user is not authorized to perform a clone request.
    * <p>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The actual error message</TD></TR>
    * </TABLE>
    */
   public static final int NOT_AUTHORIZED = 17503;

   /**
    * An internal request call failed.
    * <p>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The rhythmyx resource being called</TD></TR>
    * <TR><TD>1</TD><TD>The actual error message</TD></TR>
    * </TABLE>
    */
   public static final int INTERNAL_REQUEST_ERROR = 17504;

   /**
    * A required Rhythmyx resource could not be found. (Maybe it's not running?)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the resource, in form app/resource</TD></TR>
    * </TABLE>
    */
   public static final int REQUIRED_RESOURCE_MISSING = 17505;

   /**
    * Error occurred while creating the role for the specified community.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The role name to create</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int ROLE_CREATION_ERROR = 17506;
}
