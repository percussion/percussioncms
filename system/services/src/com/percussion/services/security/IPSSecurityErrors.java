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
package com.percussion.services.security;

/**
 * Error numbers for use with the bundle 
 * <code>PSSecurityErrorStringBundle.properties</code>
 */
public interface IPSSecurityErrors
{
   /**
    * Missing community.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the missing Community</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_COMMUNITY = 1;
   
   /**
    * Acl not found for specified acl id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The acl id </TD></TR>
    * </TABLE>
    */
   public static final int ACL_NOT_FOUND = 2;   
   
   /**
    * Acl not found for specified object guid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object guid value </TD></TR>
    * <TR><TD>1</TD><TD>The object type name </TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_ACL_NOT_FOUND = 3;
   
   /**
    * Error saving an acl
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The acl guid </TD></TR>
    * <TR><TD>1</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int ACL_SAVE_ERROR = 4;
   
   /**
    * Error deleting an acl
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The acl guid </TD></TR>
    * <TR><TD>1</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int ACL_DELETE_ERROR = 5;
   
   /**
    * Access denied exception
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object guid </TD></TR>
    * <TR><TD>1</TD><TD>The action attempted</TD></TR>
    * </TABLE>
    */
   public static final int ACCCESS_DENIED_ERROR = 6;
}

