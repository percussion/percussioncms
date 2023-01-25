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

package com.percussion.i18n;

/**
 * The IPSDataErrors inteface is provided as a convenient mechanism
 * for accessing the various locale related error codes. The data error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1801 - 1850</TD><TD>All locale specific errors</TD></TR>
 * </TABLE>
 * The text of the error messages is stored in the properties file
 * <code>com.percussion.error.PSErrorStringBundle</code>
 */
public interface IPSLocaleErrors
{
   /**
    * Invalid column value provided to restore a locale object from the 
    * database.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The column name</TD></TR>
    * <TR><TD>1</TD><TD>The column value</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_COLUMN_VALUE = 1801;

   /**
    * Missing column in row data provided to restore locale object from the
    * database.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The column name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_COLUMN = 1802;
   
   /**
    * Error while initializing the locale manager.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int LOCALE_MGR_INIT = 1803;
   
   /**
    * Unexpected error encountered by the locale manager.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int LOCALE_MGR_UNEXPECTED_ERROR = 1804;
}
