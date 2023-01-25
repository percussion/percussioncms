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

package com.percussion.services.sitemgr;

/**
 * Erros in Site Manager. The messages are defined in 
 * PSSiteManagerErrorStringBundle.properties
 */
public interface IPSSiteManagerErrors
{
   /**
    * Cannot find a site with the specified site id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the site</TD></TR>
    * </TABLE>
    */
   public final static int SITE_ID_NOT_EXIT = 1;
   
   /**
    * Failed to find the root folder id for a specified site.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the site</TD></TR>
    * <TR><TD>1</TD><TD>The root folder path</TD></TR>
    * <TR><TD>2</TD><TD>The underlying error</TD></TR>
    * </TABLE>
    */
   public final static int FAILED_FIND_ROOT_FOLDER_ID = 2;

   /**
    * Cannot find the root folder id for a specified site.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the site</TD></TR>
    * <TR><TD>1</TD><TD>The root folder path</TD></TR>
    * </TABLE>
    */
   public final static int CANNOT_FIND_ROOT_FOLDER_ID = 3;
   
   
   /**
    * Failed to get a path for the specified folder id and the specified site.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The specified folder id</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error</TD></TR>
    * </TABLE>
    */
   public final static int FAILED_GET_FOLDER_PATH = 4;

   /**
    * The specified folder does not exist under the specified site.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The specified folder id</TD></TR>
    * <TR><TD>1</TD><TD>The id of the site</TD></TR>
    * <TR><TD>2</TD><TD>The root folder path</TD></TR>
    * </TABLE>
    */
   public final static int NOT_SITE_FOLDER = 5;
   
   /**
    * The caught an unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The unexpected error message</TD></TR>
    * </TABLE>
    */
   public final static int UNEXPECTED_ERROR = 6;

   /**
    * Cannot find a site with the specified site name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the site</TD></TR>
    * </TABLE>
    */
   public final static int SITE_NAME_NOT_EXIST = 7;
   
   /**
    * Cannot find a scheme with the specified id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the scheme</TD></TR>
    * </TABLE>
    */
   public final static int SCHEME_NOT_EXIST = 8;

   /**
    * Cannot find a context with the specified info.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>What kind of data was specified</TD></TR>
    * <TR><TD>1</TD><TD>What data specified the context, i.e. name, id, etc.</TD></TR>
    * </TABLE>
    */
   public static final int NO_SUCH_CONTEXT = 9;
}
