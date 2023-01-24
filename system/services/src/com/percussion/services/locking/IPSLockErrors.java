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

