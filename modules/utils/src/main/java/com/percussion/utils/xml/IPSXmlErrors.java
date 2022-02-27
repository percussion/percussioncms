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
package com.percussion.utils.xml;

/**
 * Provides error codes for messages located in 
 * <code>PSXmlErrorStringBundle</code>
 */
public interface IPSXmlErrors
{
   /**
    * Missing element
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected XML Element tag name</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_MISSING = 1;
   
   /**
    * Missing element value
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag name</TD></TR>
    * <TR><TD>1</TD><TD>the value</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_INVALID_VALUE = 2;
   
   /**
    * Multiple root elements defined in mapping
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>root element 1</TD></TR>
    * <TR><TD>1</TD><TD>root element 2</TD></TR>
    * </TABLE>
    */
   public static final int XML_TWO_ROOT_ELEMENTS = 3;
   

   /**
    * a required attribute of the XML element is missing or invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag</TD></TR>
    * <TR><TD>1</TD><TD>the attribute name</TD></TR>
    * <TR><TD>2</TD><TD>the value specified</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_INVALID_ATTR   = 4;
   

   /**
    * An XML element identified by an attribute value contains an invalid value. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag</TD></TR>
    * <TR><TD>1</TD><TD>the attribute name</TD></TR>
    * <TR><TD>2</TD><TD>the attribute value</TD></TR>
    * <TR><TD>3</TD><TD>the element value</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_ATTR_INVALID_VAL   = 5;
   
   /**
    * An unexpected error occurred trying to restore an object from its XML 
    * representation. In this case the XML conformed to the expected DTD, but 
    * the specific values specified caused an unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class name</TD></TR>
    * <TR><TD>1</TD><TD>the error</TD></TR>
    * <TR><TD>2</TD><TD>the XML data as a string</TD></TR>  
    * </TABLE>
    */
   public static final int XML_RESTORE_ERROR   = 6;
}


