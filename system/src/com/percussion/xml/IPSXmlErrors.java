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

package com.percussion.xml;

/**
 * The IPSXmlErrors inteface is provided as a convenient mechanism
 * for accessing the various XML related error codes. The XML error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>6001 - 7000</TD><TD>XML errors</TD></TR>
 * </TABLE>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSXmlErrors
{

   /**
    * This is simply used to dump the raw XML data associated with an error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>XML data as a string</TD></TR>
    * </TABLE>
    */
   public static final int RAW_XML_DUMP            = 6001;

   /**
    * This is the generic error processing XML message.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int XML_PROCESSING_ERROR      = 6002;

   /**
    * An IO exception occurred processing the DTD.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception string</TD></TR>
    * </TABLE>
    */
   public static final int DTD_IO_ERROR = 6025;

   /**
    * A root element declaration can not be located in the DTD.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the root name expected</TD></TR>
    * </TABLE>
    */
   public static final int DTD_ROOTNOTFOUND_ERROR = 6026;

   /**
    * Multiple occurrence settings are not supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the first occurrence setting</TD></TR>
    * <TR><TD>1</TD><TD>the second occurrence setting</TD></TR>
    * </TABLE>
    */
   public static final int DTD_MULTIPLE_OCCURRENCE_NOTSUPPORTED_ERROR = 6027;

   /**
    * An element declaration can not be located in the DTD.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the element name expected</TD></TR>
    * </TABLE>
    */
   public static final int DTD_ELEMENT_NOTFOUND_ERROR = 6028;

}
