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

package com.percussion.search;

/**
 * This interface contains the error codes for all exceptions thrown by
 * classes in this pkg.
 * The search error code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>16001 - 16050</TD><TD>general errors</TD></TR>
 * <TR><TD>16051 - 16100</TD><TD>Init and config errors</TD></TR>
 * <TR><TD>16101 - 16300</TD><TD>-unassigned-</TD></TR>
 * <TR><TD>16301 - 16700</TD><TD>Engine implementation specific errors</TD></TR>
 * <TR><TD>16701 - 17000</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 * The message strings for search messages are stored in the i18n resource 
 * bundle, not the error string bundle.
 * 
 * @author paulhoward
 */
public interface IPSSearchErrors
{
   /**
    * Unimplemented operation. This problem is raised if an operation is 
    * executed that is not implemented in the server's current mode.
    * This error takes no arguments.
    */
   public static final int SEARCH_ENGINE_UNIMPLEMENTED_OPERATION = 16001;
   
   /**
    * Specified object not found
    * <p>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the library being referenced, if available.
    * </TD></TR>
    * </TABLE>
    */
   public static final int SEARCH_ENGINE_OBJECT_NOT_FOUND = 16002;
   
   /**
    * The search terms for the query are missing. 
    * This error does not take arguments.
    * TODO - this should take at least 1 param - the name of the method,
    * and maybe a second param that is all the supplied args (or how many were
    * supplied, or something to give one a hint of what is going on)
    */
   public static final int SEARCH_ENGINE_NO_SEARCH_TERMS = 16003;
   
   /**
    * Fatal error. This error does not take arguments.
    */
   public static final int SEARCH_ENGINE_FATAL_ERROR = 16004;
   
   /**
    * Bad or missing parameters in API call.
    * This error takes no arguments.
    */
   public static final int SEARCH_ENGINE_BAD_PARAMETERS = 16005;
   
   /**
    * The search engine ran out of wildcards.
    * This error takes no arguments.
    */
   public static final int SEARCH_ENGINE_WILDCARD_LIMIT = 16006;
   
   /**
    * The search engine hit an error that wasn't mapped. Ask the
    * user to call tech support so we can augment the mapping list.
    * <p>
    * The arguments passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error code from the search system</TD></TR>
    * <TR><TD>1</TD><TD>The name of the search system</TD></TR>
    * </TABLE>
    */
   public static final int SEARCH_ENGINE_UNEXPECTED_ERROR = 16007;
   
   /**
    * The query string had one or more parsing errors.
    * <P>
    * The argument passed in for this message is:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the search engine</TD></TR>
    * <TR><TD>1</TD><TD>A description of the parsing problem encountered</TD>
    * </TR>
    * </TABLE>
    * 
    */
   public static final int SEARCH_ENGINE_QUERY_PARSE_ERROR = 16008;

   /**   
    * The object used to manage the search engine configuration is currently
    * being used by someone else. 
    * <p>
    * This message has no arguments.
    */
   public static final int ADMIN_HANDLER_LOCKED = 16009;

   /**
    * One or more objects that require releaseXXX to be called are still 
    * allocated. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The number of admin objects still allocated.</TD></TR>
    * <TR><TD>1</TD><TD>The number of query objects still allocated.</TD></TR>
    * <TR><TD>2</TD><TD>The number of indexer objects still allocated.</TD></TR>
    * </TABLE>
    */
   public static final int UNRELEASED_OBJECTS = 16010;
   
   /**
    * A search request has been made against the external search engine, but
    * one is not available.
    * <p>
    * This message has no arguments.
    */
   public static final int SEARCH_ENGINE_REQUIRED = 16011;

   /**
    * An index event is processed for an item whose content type is not 
    * currently valid for indexing. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The content id of the item.</TD></TR>
    * <TR><TD>1</TD><TD>The content type id</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_INDEX_CONTENTTYPE = 16012;
   
   /**
    * The configured pluggable engine failed to initialize. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the class being loaded.</TD></TR>
    * <TR><TD>1</TD><TD>The text from the underlying exception</TD></TR>
    * </TABLE>
    */
   public static final int SEARCH_ENGINE_FAILED_INIT = 16051;
   
   /**
    * Authentication failed.
    * This error takes no arguments.
    */
   public static final int SEARCH_ENGINE_AUTHENTICATION_FAILED = 16052;
   
   /**
    * A required parameter is missing.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The parameter name.</TD></TR>
    * <TR><TD>1</TD><TD>The parameter type (HTML or extension).</TD></TR>
    * </TABLE>
    */
   public static final int HTML_SEARCH_MISSING_PARAMETER = 16053;

   /**
    * Use getInstance() to instead of new instance on this class
    */
   public static final int USE_GET_INSTANCE = 16054;

}
