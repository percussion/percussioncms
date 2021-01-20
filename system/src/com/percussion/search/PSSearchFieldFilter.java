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
package com.percussion.search;

import com.percussion.design.objectstore.PSEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Search field filter is used for filtering the keywords list for the search
 * fields in search dialog box.
 */
public class PSSearchFieldFilter
{
   /**
    * ctor takes the search fieldname for which the filter needs to be done,
    * new list of keywords, filter type
    * @param searchFieldName name of the search field.
    * @param keywords <code>List</code> of the keywords which will be applied on
    * source list based on the filterType
    * @param filterType Three types are supported, default is replace the source
    * list with the new list SEARCH_FILTER_TYPE_REPLACE, take the intersection
    * of the new list and source list SEARCH_FILTER_TYPE_INTERSECTION, take the
    * union of the new list and source list SEARCH_FILTER_TYPE_UNION.
    */
   public PSSearchFieldFilter(String searchFieldName, List keywords,
      String filterType)
   {
      setKeywords(keywords);
      setSearchFieldName(searchFieldName);
      setFilterType(filterType);
   }
   /**
    * Returns the list of the keywords
    * @return list of keywords consisting of <code>PSEntry</code> objects.
    *    May be empty but never <code>null</code>.
    */
   public List getKeywords()
   {
      return m_keywords;
   }
   /**
    * Sets the list of the keywords.
    * @param keywords must not be <code>null</code> and must contain
    *    <code>PSEntry</code> objects only.
    * @throws IllegalArgumentException if keywords is null or if the objects
    *    in the list are not of type <code>PSEntry</code>.
    */
   public void setKeywords(List keywords)
   {
      if(keywords == null)
      {
          throw new IllegalArgumentException(
            "keywords must not be null");
      }
      Iterator kiter = keywords.iterator();
      while(kiter.hasNext())
      {
         Object obj = kiter.next();
         if(!(obj instanceof PSEntry))
         {
             throw new IllegalArgumentException(
               "keywords list should contain PSEntry objects only");
         }
      }
      m_keywords.addAll(keywords);
   }
   /**
    * Returns the type of the filter
    * @return String type of the filter, never <code>null</code> or
    *    <code>empty</code>
    */
   public String getFilterType()
   {
      return m_filterType;
   }
   /**
    * Sets the filter type
    * @param filterType Type of the filter that needs to be applied. One of the
    *    types defined as SEARCH_FILTER_TYPE_XXX in this class. Can be
    *    <code>null</code>. If <code>null</code> sets filtertype as
    *    SEARCH_FILTER_TYPE_REPLACE.
    */
   public void setFilterType(String filterType)
   {
      if(SEARCH_FILTER_TYPE_INTERSECTION.equalsIgnoreCase(filterType))
      {
         m_filterType = SEARCH_FILTER_TYPE_INTERSECTION;
      }
      else if(SEARCH_FILTER_TYPE_UNION.equalsIgnoreCase(filterType))
      {
         m_filterType = SEARCH_FILTER_TYPE_UNION;
      }
      else
      {
         m_filterType = SEARCH_FILTER_TYPE_REPLACE;
      }
   }
   /**
    * Returns the name of the search field.
    * @return string search field name, never <code>null</code> or
    *    <code>empty</code>
    */
   public String getSearchFieldName()
   {
      return m_searchFieldName;
   }
   /**
    * Sets the search field name.
    * @param searchFieldName must not be <code>null</code> or
    *    <code>empty</code>.
    * @throws IllegalArgumentException if searchFieldName is <code>null</code>
    *    or <code>empty</code>.
    */
   public void setSearchFieldName(String searchFieldName)
   {
      if(searchFieldName == null || searchFieldName.trim().length()==0)
      {
          throw new IllegalArgumentException(
            "Search Field Name must not be null or empty");
      }
      m_searchFieldName = searchFieldName;
   }
   /**
    * Filters the list based on the filter type
    * @param sourceList <code>List</code> of <code>PSEntry</code> objects must
    * not be <code>null</code>
    * @return List filtered list of the <code>PSEntry</code> objects may be
    *    <code>empty</code> but never <code>null</code>
    * @throws IllegalArgumentException if sourceList is <code>null</code>.
    */
   public List getFilteredList(List sourceList)
   {
      List keywords = new ArrayList();
      if(sourceList == null)
      {
          throw new IllegalArgumentException(
            "source keywords list must not be null");
      }
      Iterator iter = sourceList.iterator();
      if(m_filterType == SEARCH_FILTER_TYPE_INTERSECTION)
      {
         while(iter.hasNext())
         {
            Object obj = iter.next();
            if(m_keywords.contains(((PSEntry)obj)))
            {
               keywords.add(obj);
            }
         }
      }
      else if(m_filterType == SEARCH_FILTER_TYPE_UNION)
      {
         keywords.addAll(m_keywords);
         while(iter.hasNext())
         {
            Object obj = iter.next();
            if(!m_keywords.contains(((PSEntry)obj)))
            {
               keywords.add(obj);
            }
         }
      }
      else
         keywords.addAll(m_keywords);

      return keywords;
   }
   /**
    * List of the keywords that need to be used for filtering
    */
   private List m_keywords = new ArrayList();
   /**
    * Name of the search field for which the filtering is need to be done
    */
   private String m_searchFieldName;
   /**
    * Filter type, default is replace.
    */
   private String m_filterType = SEARCH_FILTER_TYPE_REPLACE;
   /**
    * Constant for Search Field Filter Type Replace, This is the default type.
    * m_keywords list will be returned unaltered.
    */
   public static final String SEARCH_FILTER_TYPE_REPLACE = "Replace";
   /**
    * Constant for Search Field Filter Type Intersection.
    * The intersection of m_keywords and source list will be returned
    */
   public static final String SEARCH_FILTER_TYPE_INTERSECTION = "Intersection";
   /**
    * Constant for Search Field Filter Type Union.
    * The union of m_keywords and source list will be returned
    */
   public static final String SEARCH_FILTER_TYPE_UNION = "Union";
}