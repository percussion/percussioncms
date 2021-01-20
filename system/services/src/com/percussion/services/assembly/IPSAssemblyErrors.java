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
package com.percussion.services.assembly;

/**
 * Error numbers for use with the bundle PSAssemblyErrorStringBundle
 * 
 * @author dougrand
 */
public interface IPSAssemblyErrors
{
   /**
    * Missing template
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name or id of the template</TD></TR>
    * </TABLE>
    */
   public static final int TEMPLATE_MISSING = 1;
   
   /**
    * Missing assembler
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the assembler</TD></TR>
    * </TABLE>
    */
   public static final int ASSEMBLER_MISSING = 2;   
   
   /**
    * Assembler can't be instantiated
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the assembler</TD></TR>
    * </TABLE>
    */
   public static final int ASSEMBLER_INST = 3; 
   
   /**
    * The variant or the template must be specified - no params
    */
   public static final int PARAMS_VARIANT_OR_TEMPLATE = 4;
   
   /**
    * Unknown error while processing assembly item
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The original error message</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_ERROR = 5;
   
   /**
    * The filter or authtype parameters must be specified - no params
    */
   public static final int PARAMS_AUTHTYPE_OR_FILTER = 6;
   
   /**
    * The content info or path must be specified - no params
    */
   public static final int PARAMS_ITEM_SPEC = 7;
   
   /**
    * The given path is invalid 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_PATH = 8; 
   
   /**
    * The given path is missing 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing path</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_PATH = 9;
   
   /**
    * Unknown error while performing CRUD operation - no params
    */
   public static final int UNKNOWN_CRUD_ERROR = 10;
   
   /**
    * The given slot is missing 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing slot</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_SLOT = 11;
   
   /**
    * The given finder is missing 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing finder</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_FINDER = 12;   
   
   /**
    * Problem creating assembly item
    * <p>
    * No arguments
    */
   public static final int ITEM_CREATION = 13;  

   /**
    * Couldn't build landing page url
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the landing page item</TD></TR>
    * </TABLE>
    */
   public static final int LANDING_PAGE_URL_1 = 14;

   /**
    * Missing pagelink binding
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the template</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_PAGELINK = 15;   
   
   /**
    * Missing template
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name or id of the template</TD></TR>
    * <TR><TD>1</TD><TD>The id of the content type</TD></TR>
    * </TABLE>
    */
   public static final int TEMPLATE_BY_ID_MISSING = 16;
   
   /**
    * Could not locate the default template.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The content id</TD></TR>
    * <TR><TD>1</TD><TD>The id of the content type</TD></TR>
    * <TR><TD>2</TD><TD>The reason</TD></TR>
    * </TABLE>
    */
   public static final int NO_DEFAULT_TEMPLATE = 17;
   
   /**
    * Not unique name for object
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the object (template or slot)</TD></TR>
    * <TR><TD>1</TD><TD>The name of the type</TD></TR>
    * </TABLE>
    */
   public static final int NAME_NOT_UNIQUE = 18;
 
   /**
    * A problem occurred while running the given slot content finder
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the slot content finder</TD></TR>
    * <TR><TD>1</TD><TD>The exception message</TD></TR>
    * </TABLE>
    */   
   public static final int FINDER_ERROR = 19;
   
   /**
    * Item's id field does not match its sys_contentid parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value of the id field</TD></TR>
    * <TR><TD>1</TD><TD>The value of the sys_contentid parameter</TD></TR>
    * </TABLE>
    */
   public static final int PARAMS_ITEM_ID_MISMATCH = 20;
 
   /**
    * Item's folder field does not match its sys_folderid parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value of the folder field</TD></TR>
    * <TR><TD>1</TD><TD>The value of the sys_folderid parameter</TD></TR>
    * </TABLE>
    */
   public static final int PARAMS_ITEM_FOLDER_MISMATCH = 21;

   /**
    * A hash is being used for a binary resource but the binary
    * for this hash cannot be found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The hash value specified</TD></TR>
    * </TABLE>
    */
   public static final int HASHED_BINARY_NOT_FOUND = 22;
   
   /**
    * A binary is being accessed by its hash value, but no
    * hash was sent. no params
    *
    */
   public static final int HASHED_BINARY_NO_HASH = 23;
   
   /**
    * A binary is being accessed by its hash value, 
    * an Error was thrown.
    * <TR><TD>0</TD><TD>The hash value specified</TD></TR>
    * <TR><TD>1</TD><TD>The exception message</TD></TR>
    */
   public static final int HASHED_BINARY_ERROR = 24;

}


