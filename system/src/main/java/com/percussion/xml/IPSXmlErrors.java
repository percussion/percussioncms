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
