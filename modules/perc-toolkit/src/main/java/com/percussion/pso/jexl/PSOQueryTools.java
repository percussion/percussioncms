/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.jexl PSOQueryTools.java
 *  
 * @author DavidBenua
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static Log log = LogFactory.getLog(PSOQueryTools.class); 
   
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
      
      initServices();
      List<Map<String, Value>> results = new ArrayList<Map<String, Value>>();
      QueryResult qres = performQuery(query, maxRows, params, locale);
      List<String> colNames = Arrays.<String>asList(qres.getColumnNames()); 
      RowIterator rows = qres.getRows();
      while(rows.hasNext())
      {
         Row row = rows.nextRow(); 
         Map<String, Value> rowValues = new HashMap<String, Value>(); 
         for(String colName : colNames)
         {
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
    * @throws InvalidQueryException
    * @throws RepositoryException
    */
   @IPSJexlMethod(description="executes a JCR Query and returns the Nodes", params={
         @IPSJexlParam(name="query", description="the query string"),
         @IPSJexlParam(name="maxRows", description="max number of rows to return"), 
         @IPSJexlParam(name="params", description="parameters for query"), 
         @IPSJexlParam(name="locale", description="locale for collating results")
      })
      public NodeIterator executeQueryNodes(String query, int maxRows, Map<String,? extends Object> params, String locale ) 
         throws InvalidQueryException, RepositoryException
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
      QueryResult qres = cmgr.executeQuery(q, maxRows, params, locale);
      return qres;
   }
         
   private static void initServices()
   {
      if(cmgr == null)
      {
         cmgr = PSContentMgrLocator.getContentMgr(); 
      }
   }
}
