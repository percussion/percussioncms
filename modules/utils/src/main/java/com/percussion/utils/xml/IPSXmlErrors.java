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


