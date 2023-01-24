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
package com.percussion.services.contentmgr.data;

import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.impl.jsrdata.PSQueryNodeIterator;
import com.percussion.services.contentmgr.impl.jsrdata.PSRowIterator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

/**
 * Implementation of query result for JSR-170 queries
 * 
 * @author dougrand
 */
public class PSQueryResult implements QueryResult
{
   /**
    * The column names, may be <code>null</code> or empty
    */
   private String[] m_columnNames = null;

   /**
    * The rows from the result set
    */
   private Set<PSRow> m_rows = null;

   /**
    * Create a new result with the given comparator and columns
    * 
    * @param columnNames an array of column names, never <code>null</code> but
    *           may be zero length for a result that doesn't require anything
    *           but nodes for output
    * @param comparator the comparator, never <code>null</code>, used to
    *           order the rows
    */
   public PSQueryResult(String[] columnNames, Comparator<PSRow> comparator) {
      if (columnNames == null)
      {
         throw new IllegalArgumentException("columnNames may not be null");
      }
      if (comparator == null)
      {
         throw new IllegalArgumentException("comparator may not be null");
      }
      m_columnNames = columnNames;
      m_rows = new TreeSet<>(comparator);
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.query.QueryResult#getColumnNames()
    */
   public String[] getColumnNames()
   {
      return m_columnNames;
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.query.QueryResult#getRows()
    */
   public RowIterator getRows()
   {
      return new PSRowIterator(m_rows);
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.query.QueryResult#getNodes()
    */
   public NodeIterator getNodes() throws RepositoryException
   {
      List<IPSGuid> guids = new ArrayList<>();
      for (PSRow row : m_rows)
      {
         long cid = row.getRawValue(
               IPSContentPropertyConstants.RX_SYS_CONTENTID).getLong();
         long rev = row
               .getRawValue(IPSContentPropertyConstants.RX_SYS_REVISION)
               .getLong();
         guids.add(new PSLegacyGuid((int) cid, (int) rev));
      }
      return new PSQueryNodeIterator(guids);
   }

   /**
    * Add a row to the results
    * 
    * @param rowToAdd never <code>null</code>
    */
   public void addRow(PSRow rowToAdd)
   {
      if (rowToAdd == null)
      {
         throw new IllegalArgumentException("rowToAdd may not be null");
      }
      m_rows.add(rowToAdd);
      rowToAdd.setParent(this);
   }

   /**
    * Get the number of results present
    * 
    * @return the count of results
    */
   public long getCount()
   {
      return m_rows.size();
   }

}
