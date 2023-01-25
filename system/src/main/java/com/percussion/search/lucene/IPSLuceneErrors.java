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
package com.percussion.search.lucene;

/**
 * This interface contains the error codes for all exceptions thrown by classes
 * in this pkg. The search error code ranges are: <B>Avoid the following ranges
 * as these were used by convera.16301-16310, 16351-16365 and 16401.</B> <TABLE
 * BORDER="1">
 * <TR>
 * <TH>Range</TH>
 * <TH>Component</TH>
 * </TR>
 * <TR>
 * <TD>16301 - 16350</TD>
 * <TD>general errors and engine level services.</TD>
 * </TR>
 * <TR>
 * <TD>16351 - 16400</TD>
 * <TD>Administration services</TD>
 * </TR>
 * <TR>
 * <TD>16401 - 16450</TD>
 * <TD>Indexing services</TD>
 * </TR>
 * <TR>
 * <TD>16451 - 16500</TD>
 * <TD>Querying services</TD>
 * </TR>
 * <TR>
 * <TD>16501 - 16700</TD>
 * <TD>-unassigned-</TD>
 * </TR>
 * </TABLE> The message strings for search messages are stored in the i18n
 * resource bundle, not the error string bundle.
 * 
 * @author bjoginipally
 * 
 */

public interface IPSLuceneErrors
{
   /**
    * The search engine failed to start because a property that specifies the
    * location of the index files was missing or pointing to an 
    * invalid directory.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the parameter.</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_DIR_PARAM_INVALID_MISSING = 16311;
   
   
   /**
    * The search engine failed to start because a property that specifies the
    * location of the index files was missing or pointing to an 
    * invalid directory.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type id.</TD></TR>
    * <TR><TD>1</TD><TD>Directory path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_INDEX_DIRECTORY = 16366;
   
   /**
    * A CorruptIndexException is thrown by the system while accessing or
    * optimizing or closing during searching process by Lucene Search engine.
   * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type id</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_CURRUPTED_EXCEPTION_INDEXING = 16402;

   /**
    * A IOException is thrown by the system while accessing or
    * optimizing or closing during searching process by Lucene Search engine.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type id</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_IO_EXCEPTION_INDEXING = 16403;

   /**
    * An error is thrown by the system while optimizing the indexes. 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type ids</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_OPTIMIZATION_ERROR = 16404;

   /**
    * A CorruptIndexException is thrown by the system while accessing or
    * optimizing or closing during searching process by Lucene Search engine.
   * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type id</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_CURRUPTED_EXCEPTION_SEARCHING = 16451;

   /**
    * A IOException is thrown by the system while accessing or
    * optimizing or closing during searching process by Lucene Search engine.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type id</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_IO_EXCEPTION_SEARCHING = 16452;

   /**
    * A Repository Exception is thrown by the system while loading the content
    * types during the search process.
    */
   public static final int REPOSITORY_EXCEPTION = 16453;
   
   /**
    * An IOException is thrown by the system while extracting the results from
    * hits
    */
   public static final int HITS_IOEXCEPTION = 16454;

   /**
    * An IOException is thrown by the system while extracting the results from
    * hits
    */
   public static final int SEARCH_QUERY_PARSEEXCEPTION = 16455;

   /**
    * An IOException is thrown by the system while extracting the results from
    * hits
    */
   public static final int SEARCH_QUERY_MULTISEARCHER = 16456;
}
