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

package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

/**
 * Tools for JCR Queries. 
 *
 * @author DavidBenua
 *
 */
public class PSOQueryTools extends PSJexlUtilBase implements IPSJexlExpression
{
   private static final Logger log = LogManager.getLogger(PSOQueryTools.class);
   
   private static IPSContentMgr cmgr = null;
   
   /**
    * 
    */
   public PSOQueryTools()
   {
     
   }
   
   
   /**
    * Executes a JCR Query. The results are returned as a List.  Each element of the list is a Map
    * that contains name / value pairs representing the columns of the query.  
    * @param query the JCR Query to execute
    * @param maxRows the maximum number of rows. Set to -1 for unlimited rows.
    * @param params the parameters to pass to the query. 
    * @param locale the locale used for sorting results. 
    * @return a list of result rows. Each Row is a map of name and value pairs. 
    * never <code>null</code> but may be <code>empty</code>. 
    * @throws InvalidQueryException
    * @throws RepositoryException
    */
   @IPSJexlMethod(description="executes a JCR Query", params={
      @IPSJexlParam(name="query", description="the query string"),
      @IPSJexlParam(name="maxRows", description="max number of rows to return"), 
      @IPSJexlParam(name="params", description="parameters for query"), 
      @IPSJexlParam(name="locale", description="locale for collating results")
   })
   public List<Map<String, Value>> executeQuery(String query, int maxRows, Map<String,? extends Object> params, String locale ) 
      throws InvalidQueryException, RepositoryException
   {
      QueryResult qres;

         initServices();
         List<Map<String, Value>> results = new ArrayList<>();
         qres = performQuery(query, maxRows, params, locale);
         String[] colNames = qres.getColumnNames();
         RowIterator rows = qres.getRows();
         while (rows.hasNext()) {
            Row row = rows.nextRow();
            Map<String, Value> rowValues = new HashMap<>();
            for (String colName : colNames) {
               Value value = row.getValue(colName);
               rowValues.put(colName, value);
            }
            results.add(rowValues);
         }
         return results;

   }
   
   /**
    * Executes a JCR Query and returns the Nodes. The resulting NodeIterator can be used 
    * to load the individual Nodes rather than using the result values.  
    * @param query the JCR Query
    * @param maxRows the maximum number of rows. Set to -1 for unlimited rows.
    * @param params the the parameters to pass to the query. 
    * @param locale the locale. Defaults to the JVM system locale if not present. 
    * @return the Nodes from the query. Never <code>null</code> but may be <code>empty</code>.
    * @throws RepositoryException
    */
   @IPSJexlMethod(description="executes a JCR Query and returns the Nodes", params={
         @IPSJexlParam(name="query", description="the query string"),
         @IPSJexlParam(name="maxRows", description="max number of rows to return"), 
         @IPSJexlParam(name="params", description="parameters for query"), 
         @IPSJexlParam(name="locale", description="locale for collating results")
      })
      public NodeIterator executeQueryNodes(String query, int maxRows, Map<String,? extends Object> params, String locale ) 
         throws RepositoryException
      {
         
         initServices();
         QueryResult qres = performQuery(query, maxRows, params, locale);
         return qres.getNodes();
      }
   
   private QueryResult performQuery(String query, int maxRows, Map<String,? extends Object> params, String locale ) 
      throws InvalidQueryException, RepositoryException
   {
      initServices();
      if(StringUtils.isBlank(query))
      {
         String emsg = "The query must not be null or empty"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg); 
      }      
      Query q = cmgr.createQuery(query, Query.SQL);
      return cmgr.executeQuery(q, maxRows, params, locale);
   }
         
   private static void initServices()
   {
      if(cmgr == null)
      {
         cmgr = PSContentMgrLocator.getContentMgr(); 
      }
   }
}
