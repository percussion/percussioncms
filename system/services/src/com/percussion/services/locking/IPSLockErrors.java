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
package com.percussion.services.locking;

/**
 * Error numbers for use with the bundle <code>PSLockErrorStringBundle</code>.
 */
public interface IPSLockErrors
{
   /**
    * Object is already locked.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object.</TD></TR>
    * <TR><TD>1</TD><TD>The name of the locker.</TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_ALREADY_LOCKED = 1;
   
   /**
    * Object is not locked for a valid extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object.</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_EXTENSION_NOT_LOCKED = 2;
   
   /**
    * Object is locked by somebody else and cannot be entended.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object.</TD></TR>
    * <TR><TD>1</TD><TD>The name of the locker.</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_EXTENSION_LOCKED_BY_SOMEBODY_ELSE = 3;
   
   /**
    * An invalid session was supplied for the lock extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object.</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_EXTENSION_INVALID_SESSION = 4;
   
   /**
    * The object for the supplied id is not locked.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object.</TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_NOT_LOCKED = 5;
   
   /**
    * The operation was attempted with more than 1 object. Therefore, there
    * may be successful results and errors mixed together in the exception.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int MULTI_OPERATION = 6;
}

