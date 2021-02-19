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
package com.percussion.services.content.data;

import com.percussion.search.IPSSearchResultRow;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extends the PSItemSummary with additional search result fields.
 */
public class PSSearchSummary extends PSItemSummary
{
   /**
    * A map with additional search result fields, never <code>null</code>,
    * may be empty.
    */
   Map<String, String> fields = new HashMap<>();
   
   /**
    * Construct a search item summary for the supplied search row.
    * 
    * @param row the search row for which to construct this summary, not
    *    <code>null</code>.
    */
   public PSSearchSummary(IPSSearchResultRow row)
   {
      if (row == null)
         throw new IllegalArgumentException("row cannot be null");
      
      // always return legacy guids without the revision
      setGUID(new PSLegacyGuid(Integer.valueOf(row.getColumnValue(
         IPSHtmlParameters.SYS_CONTENTID)), -1));
      setName(row.getColumnValue(IPSHtmlParameters.SYS_TITLE));
      setContentTypeId(Integer.valueOf(row.getColumnValue(
         IPSHtmlParameters.SYS_CONTENTTYPEID)));
      setContentTypeName(row.getColumnDisplayValue(
         IPSHtmlParameters.SYS_CONTENTTYPEID));
      
      for (Object o : row.getColumnNames())
      {
         String colName = (String)o;
         fields.put(colName, row.getColumnValue(colName));
      }
   }

   /**
    * Should only be used by webservice converters. 
    */
   public PSSearchSummary()
   {
   }
   
   /**
    * Get the fields name value pairs.
    * 
    * @return a map with all search result fields, never <code>null</code>, 
    *    may be empty.
    */
   public Map<String, String> getFields()
   {
      return fields;
   }
   
   /**
    * Set new search result fields.
    * 
    * @param fields teh new fields, not <code>null</code>, may be empty.
    */
   public void setFields(Map<String, String> fields)
   {
      if (fields == null)
         throw new IllegalArgumentException("fields cannot be null");
      
      this.fields = fields;
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}

