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
