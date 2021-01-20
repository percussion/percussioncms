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

