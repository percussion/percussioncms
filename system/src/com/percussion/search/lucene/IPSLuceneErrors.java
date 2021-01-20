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
