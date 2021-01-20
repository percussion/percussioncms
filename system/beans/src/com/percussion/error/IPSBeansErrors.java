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
package com.percussion.error;

/**
 * This inteface is provided as a convenient mechanism for accessing the
 * various beans related error codes. The error code messages are defined in the
 * PSBeansStringBundle.properties file.  This was created so that it would be
 * completely independent of the other elements in the system and can stand
 * on its own.  There should not be many messages herein.
 *
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1001 - 2000</TD><TD>MISC- Miscellaneous</TD></TR>
 * </TABLE>
 */
public interface IPSBeansErrors
{
   /**
    * An exception occurred while processing xml.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught,</TD></TR>
    * </TABLE>
    */
   public static final int XML_PROCESSING_ERROR = 1001;


}
