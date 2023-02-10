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
package com.percussion.services.content.data;

import com.percussion.search.IPSSearchResultRow;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSearchSummary)) return false;
      if (!super.equals(o)) return false;
      PSSearchSummary that = (PSSearchSummary) o;
      return Objects.equals(getFields(), that.getFields());
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), getFields());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSSearchSummary{");
      sb.append("fields=").append(fields);
      sb.append('}');
      return sb.toString();
   }
}

