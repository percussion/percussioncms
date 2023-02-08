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
package com.percussion.services.catalog;


/**
 * Errors in cataloging 
 * 
 * @author dougrand
 *
 */
public interface IPSCatalogErrors
{
   /**
    * Error while enumerating summaries
    */
   public static final int SUMMARY_ERROR = 1;
   
   /**
    * Unknown type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the type</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_TYPE = 2;

   /**
    * Database error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object being saved or loaded</TD></TR>
    * </TABLE>
    */
   public static final int REPOSITORY = 3;

   /**
    * XML error reading an XML representation of an object
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The xml source of the object being loaded</TD></TR>
    * </TABLE>
    */
   public static final int XML = 4;

   /**
    * An io error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object being saved or loaded</TD></TR>
    * </TABLE>
    */
   public static final int IO = 5;

   /**
    * Error while serializing an object to XML
    */
   public static final int TOXML = 6;
}
