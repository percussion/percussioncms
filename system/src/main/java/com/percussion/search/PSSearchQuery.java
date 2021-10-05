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

package com.percussion.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the pluggable, full text search engine architecture.
 * <p>Instances of this class must be obtained from the search engine interface.
 * <p>This class is responsible for providing access to the query engine of the
 * FTS framework.
 * 
 * @author paulhoward
 */
public abstract class PSSearchQuery
{
   /**
    * Used to control the maximum number of items allowed in the result set.
    * The default is -1, which means no limit. Pass this as a property in 
    * the <code>controlProps</code> parameter of the <code>performSearch</code>
    * method.
    */
   public static final String QUERYPROP_MAXRESULTS = "maxresults";
   
   /**
    * Specifies which language the query should be processed in (word 
    * expansion, etc.). The value should be of the form ll-cc, where ll is
    * the ISO 2 letter language code and cc is the 2 letter country code. 
    * -cc is optional. If not provided or not supported, "en" is used. 
    */
   public static final String QUERYPROP_LANGUAGE = "language";   
   
   /**
    * Convenience method that calls {@link #performSearch(Collection, String, 
    * Map, Map) performSearch(ctypeIds, globalQuery, fieldQueries, null)}.
    */
   public List performSearch(Collection ctypeIds, String globalQuery,
         Map fieldQueries)
      throws PSSearchException
   {
      return performSearch(ctypeIds, globalQuery, fieldQueries, null);
   }

   /**
    * This method executes a basic search against the current indexes. The 
    * <code>globalQuery</code> is a search string that is generally concept 
    * based and is applied to all fields in an indexed unit. The <code>
    * fieldQueries</code> are a set of query strings that are generally 
    * 'boolean' based and are applied only to specific fields. The <code>
    * globalQuery</code> and all field specific search strings are effectively 
    * AND'd together to determine the final results.
    * <p>The syntax for all query strings is dependent on the implementation.
    * <p>A concept based search is one that performs various expansions of the
    * supplied words (such as finding items w/ 'eye' when the search word was
    * 'vision') and performs various statistics such as how many of the 
    * search words were in the item, how close (physically or logically)
    * they were to each other to determine a relevancy ranking.
    * <p>A boolean search is one that looks for exact matches, no expansion
    * of the supplied words is done except for wildcard expansion.
    * 
    * @param ctypeIds One of the search query filters. Each entry is a 
    * PSKey. May be <code>null</code> or empty. The search is 
    * limited to item fragments whose type matches one of those supplied. If 
    * none are supplied, all content types are allowed.
    *  
    * @param globalQuery This search parameter is applied to all fields 
    * that have been indexed. May be empty or <code>null</code>. 
    * 
    * @param fieldQueries 0 or more field specific query strings. Each entry
    * has a <code>String</code> as the key, which is the field name, and a 
    * <code>String</code> as value, which is the query string. Only item 
    * fragments whose specific field match the query will be considered. May 
    * be <code>null</code>.
    * 
    * @param controlProps A set of name-value pairs that control how the 
    * search is executed and how big the result set is. See the QUERYPROP_xxx
    * properties for descriptions of general properties. May be <code>null
    * </code> or empty to use default values as indicated for each 
    * property. Other allowed properties are determined by the implementation.
    * Property names are case-insensitive. If invalid values are supplied, the
    * default will be used. Keys and values are all <code>String</code> objects.
    * 
    * @return A list w/ 0 or more entries, up to a maximum of maxResults
    * entries, never <code>null</code>. Each entry is a PSSearchResult. The
    * locator in the result has the revision set to -1.
    * 
    * @throws PSSearchException If the query cannot be successfully completed
    * for any reason.
    */
   public abstract List performSearch(Collection ctypeIds, String globalQuery,
         Map fieldQueries, Map controlProps)
      throws PSSearchException;

}
