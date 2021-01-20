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