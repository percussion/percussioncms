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

import com.percussion.cms.objectstore.PSKey;
import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSRequestHandlerTestSuite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;


/**
 * JUnit test that validates the query capability of the RW search engine.
 * The indexer handler is used to submit test documents.
 * <p>This test must be run in the server context and requires the rx_generic_ce
 * content type to be present and running. This method adds data to the index.
 *
 * @author paulhoward
 */
@Category(IntegrationTest.class)
public class PSSearchQueryTest extends TestCase 
   implements IPSServerBasedJunitTest
{
   /**
    * Only ctor. 
    */
   public PSSearchQueryTest(String name)
   {
      super(name);
   }

   /**
    * Add some docs and perform a field based query.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated for test fragments.
    */
   public void testFieldQuery()
      throws PSSearchException, SQLException
   {
      boolean success = false;
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSKey ctypeKey = PSSearchIndexerTest.createContentTypeKey();
      PSSearchIndexer si = eng.getSearchIndexer();
      si.clearIndex(ctypeKey);
      String specialWord = "qqqyy";
      
      int originalDocCount = getDocCount(eng, ctypeKey, specialWord);

      // add test docs
      String testField = "sys_title";
      Map data = PSSearchIndexerTest.getDocData(specialWord);
      data.put(testField, "Red grEEn");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "Red BLUE");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "blue yellow");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "purple");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);

      assertEquals("Wrong number of docs after adding test docs. ", 
            originalDocCount+4, getDocCount(eng, ctypeKey, specialWord));
      
      PSSearchQuery sq = null;
      try
      {
         sq = eng.getSearchQuery();
         Collection ctypeIds = new ArrayList();
         ctypeIds.add(ctypeKey);
         
         Map fq = new HashMap();
         fq.put(testField, "blue");
         List results = sq.performSearch(ctypeIds, null, fq);
         assertEquals("Wrong doc count for field search on 'blue'", 2, 
               results.size());
         
         fq.put(testField, "purple");      
         results = sq.performSearch(ctypeIds, null, fq);
         assertEquals("Wrong doc count for field search on 'purple'", 1, 
               results.size());
         
         fq.put(testField, "red green");      
         results = sq.performSearch(ctypeIds, null, fq);
         assertEquals("Wrong doc count for field search on 'red green'", 1, 
               results.size());
         success = true;
      }
      finally
      {
         PSSearchException pse = null;
         try
         {
            if (null != sq)
               eng.releaseSearchQuery(sq);
         }
         catch (PSSearchException se)
         { 
            pse = se;
         }
         try
         {
            if (null != si)
               eng.releaseSearchIndexer(si);
         }
         catch (PSSearchException se)
         { 
            pse = se;
         }
         //we don't want to hide the exception if currently unwinding stack
         if (success && null != pse)
            throw pse;
      }
   }
   
   
   /**
    * Add some docs and perform a body based query. Limit the results using the
    * maxResults property.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated for test fragments.
    */
   public void testMaxResults()
      throws PSSearchException, SQLException
   {
      boolean success = false;
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSKey ctypeKey = PSSearchIndexerTest.createContentTypeKey();
      PSSearchIndexer si = eng.getSearchIndexer();
      si.clearIndex(ctypeKey);
      String specialWord = "qqqyy";
      
      String testField = BODY_FIELD_NAME;
      Map data = PSSearchIndexerTest.getDocData(specialWord);
      data.put(testField, "Red grEEn");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "Red blue");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "Red yellow");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);
      data.put(testField, "Red white");
      PSSearchIndexerTest.addDocs(null, data, 1, -1, true);

      PSSearchQuery sq = null;
      try
      {
         sq = eng.getSearchQuery();
         Collection ctypeIds = new ArrayList();
         ctypeIds.add(ctypeKey);
         
         List results = sq.performSearch(ctypeIds, "red", null);
         assertEquals("Wrong doc count for field search on 'red'", 4, 
               results.size());
         //now limit the result set size
         Map control = new HashMap();
         int limit = 2;
         control.put(PSSearchQuery.QUERYPROP_MAXRESULTS, new Integer(limit));
         results = sq.performSearch(ctypeIds, "red", null, control);
         assertEquals("Too many docs when maxResults set", limit, 
               results.size());
         
         success = true;
      }
      finally
      {
         PSSearchException pse = null;
         try
         {
            if (null != sq)
               eng.releaseSearchQuery(sq);
         }
         catch (PSSearchException se)
         { 
            pse = se;
         }
         try
         {
            if (null != si)
               eng.releaseSearchIndexer(si);
         }
         catch (PSSearchException se)
         { 
            pse = se;
         }
         //we don't want to hide the exception if currently unwinding stack
         if (success && null != pse)
            throw pse;
      }
   }

   /**
    * Performs a search against the library associated with the supplied key
    * and counts the resulting docs.
    * <p>Available for use by other unit tests.
    * 
    * @param eng Never <code>null</code>.
    * @param cTypeKey Never <code>null</code>.
    * @param query Never <code>null</code> or empty.
    * 
    * @return How many docs match the supplied query string.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    */
   static int getDocCount(PSSearchEngine eng, PSKey cTypeKey, String query)
      throws PSSearchException
   {
      if (null == eng)
      {
         throw new IllegalArgumentException("eng cannot be null");
      }
      if (null == cTypeKey)
      {
         throw new IllegalArgumentException("ctype key cannot be null");
      }
      if (null == query || query.trim().length() == 0)
      {
         throw new IllegalArgumentException("query cannot be null or empty");
      }
      PSSearchQuery sq = null;
      List results = new ArrayList();
      try
      {
         sq = eng.getSearchQuery();
         Collection cTypeIds = new ArrayList();
         cTypeIds.add(cTypeKey);
         Map fieldQueries = new HashMap();
         results = sq.performSearch(cTypeIds, query, fieldQueries);
         Iterator dump = results.iterator();
         while (dump.hasNext())
         {
            PSSearchResult rs = (PSSearchResult) dump.next(); 
            System.out.println("      Locator = " + rs.getKey().getId() 
                  + " : Rel = " + rs.getRelevancy());
         }
         
      }
      catch (PSSearchException se)
      {
         //if the library is empty, we get a 16003 code back
         assertEquals("Unexpected error code querying docs", 
            IPSSearchErrors.SEARCH_ENGINE_NO_SEARCH_TERMS, se.getErrorCode());
      }
      finally
      {
          if (null != sq)
            eng.releaseSearchQuery(sq);
      }
      System.out.println("Found " + results.size() + " docs for query: " 
            + query + "(ctype: " + cTypeKey.getPart(cTypeKey.getDefinition()[0]) 
            + ")");
      return results.size(); 
   }


   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new PSRequestHandlerTestSuite();
      suite.addTest(new PSSearchQueryTest("testFieldQuery"));
      suite.addTest(new PSSearchQueryTest("testMaxResults"));
      suite.addTest(new PSSearchQueryTest("testPatternSearch"));
      suite.addTest(new PSSearchQueryTest("testExpansionLevel"));
      suite.addTest(new PSSearchQueryTest("testBodyFilterQuery"));
      //TODO - add tests when we support more types
      //suite.addTest(new PSSearchQueryTest("testGlobalQuery"));
      //suite.addTest(new PSSearchQueryTest("testAllContentTypes"));

      //suite.addTest(new PSSearchQueryTest("test"));
      return suite;
   }

   /**
    * The loadable handler will call this method once before any test method.
    *
    * @param req The request that was passed to the loadable handler.
    *            Never <code>null</code>;
    */
   @Override
   public void oneTimeSetUp(Object req) {

   }

   /* (non-Javadoc)
    * @see IPSServerBasedJunitTest#oneTimeTearDown()
    */
   public void oneTimeTearDown()
   {
      // noop
   }
   
   /**
    * The name of a field in the rxs_generic_ce content type that will be used
    * for testing. The field should be the one that stores the main content of
    * the item.
    */
   private static final String BODY_FIELD_NAME = "body";
}
