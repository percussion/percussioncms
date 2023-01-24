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
package com.percussion.cx.error;

/**
 * This inteface is provided as a convenient mechanism for accessing the
 * various ContentExplorer system related error codes. The error
 * code messages are defined in the
 * PSContentExplorerErrorStringBundle.properties file.
 *
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>20001 - 21000</TD><TD>All content explorer error</TD></TR>
 * </TABLE>
 */
public interface IPSContentExplorerErrors
{
   /**
    * An general error code with single argument alone to replace with complete
    * error message from the exception.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int GENERAL_ERROR = 20001;

   /**
    * While performing a fromXml an error occurred while trying to instantiate a
    * PS class from a PSX node.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the PS class.</TD></TR>
    * <TR><TD>1</TD><TD>The name of the PSX node.</TD></TR>
    * </TABLE>
    */
   public static final int PSCLASS_INSTANTIATION_ERROR = 20002;

   /**
    * A general exception occurred while processing Options.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught,</TD></TR>
    * </TABLE>
    */
   public static final int MISC_PROCESSING_OPTIONS_ERROR = 20003;

   /**
    * An error occurred while trying to load Options.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int OPTIONS_LOAD_ERROR = 20004;

   /**
    * An error occurred while trying to save Options.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int OPTIONS_SAVE_ERROR = 20005;

   /**
    * An error getting the sub-actions of an action.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int ACTION_GET_CHILDREN = 20006;

   /**
    * An error occurred while using search web service.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int SEARCH_ERROR = 20007;

   /**
    * An error occurred while executing a catalog request. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_ERROR = 20008;

   /**
    * A validation error for wizard pages.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A string with all validation error details.</TD></TR>
    * </TABLE>
    */
   public static final int WIZARD_VALIDATION_ERROR = 20009;

   /**
    * A valid path could not be made from the 2 supplied paths. The number of
    * ../ in rel path were more than the path parts in root path.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The root path.</TD></TR>
    * <TR><TD>1</TD><TD>The relative path.</TD></TR>
    * </TABLE>
    */
   public static final int INCOMPATIBLE_PATHS = 20010;

   /**
    * One or more exceptions occurred while updating 1 or more site definitions.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The exception text for all failures.</TD></TR>
    * </TABLE>
    */
   public static final int SITEDEF_UPDATE_FAILURES = 20011;
}
