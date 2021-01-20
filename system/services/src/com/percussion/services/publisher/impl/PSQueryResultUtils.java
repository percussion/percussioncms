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
package com.percussion.services.publisher.impl;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.List;

import javax.help.UnsupportedOperationException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;

/**
 * JCR 170 Query Results utils similar to 
 * jackrabbit commons.
 * @adamgent
 */
public class PSQueryResultUtils
{

   /**
    * Splits a {@link QueryResult} into multiple query results.
    * The returned iterator is not backed by a collection and loads each
    * element on demand.
    *
    * @param qr original query result.
    * @param chunkSize the maximum number of items for each result set returned.
    * @return not null.
    * @throws RepositoryException
    */
   public static Iterator<QueryResult> splitQueryResults(QueryResult qr, int chunkSize) throws RepositoryException {
      final String[] columnNames = qr.getColumnNames();
      return Iterators.transform(Iterators.partition((Iterator<Row>)qr.getRows(), chunkSize), rows -> new RowQueryResult(columnNames,  rows));
   }
   
   
   private static class RowQueryResult implements QueryResult {

      private String[] columnNames;
      private List<Row> rows;
      
      public RowQueryResult(String[] columnNames, List<Row> rows)
      {
         super();
         this.columnNames = columnNames;
         this.rows = rows;
      }

      public String[] getColumnNames() throws RepositoryException
      {
         return columnNames;
      }

      public RowIterator getRows() throws RepositoryException
      {
         return new RowIteratorAdapter(rows);
      }

      public NodeIterator getNodes() throws RepositoryException
      {
         throw new UnsupportedOperationException("Not supported");
      }
   
   }

}
