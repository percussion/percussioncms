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
