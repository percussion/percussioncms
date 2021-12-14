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
