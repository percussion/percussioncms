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
