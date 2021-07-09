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
package com.percussion.search;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.data.PSIdGenerator;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSRequestHandlerTestSuite;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;

/**
 * JUnit test that validates the indexing capability of the RW search engine.
 * The query handler is used to validate the indexing.
 * <p>This test must be run in the server context and requires the 
 * rxs_generic_ce content type to be present and running. This method clears 
 * the index, therefore don't run the test if you want to keep the currently 
 * indexed data.
 *
 * @author paulhoward
 */
@Category(IntegrationTest.class)
public class PSSearchIndexerTest extends TestCase 
   implements IPSServerBasedJunitTest
{
   /**
    * Only ctor. 
    */
   public PSSearchIndexerTest(String name)
   {
      super(name);
   }

   /**
    * Validates the contract of the <code>update</code> method.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    */
   public void testUpdateValidation()
      throws PSSearchException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = eng.getSearchIndexer();
      Map frags = new HashMap();
      try
      {
         si.update(null, frags, true);
         assertTrue("Search key contract validation failed", false);
      }
      catch (IllegalArgumentException iae)
      {
         //expected
      }
      
      PSKey cTypeKey = createContentTypeKey();

      //create fake items for searching
      PSLocator itemKey = new PSLocator(10, 1);
      PSSearchKey id = new PSSearchKey(cTypeKey, itemKey, null);
      try
      {
         si.update(id, null, true);
         assertTrue(
               "Fragment map contract validation failed (accepted null arg)", 
               false);
      }
      catch (IllegalArgumentException iae)
      {
         //expected
      }
      
      eng.releaseSearchIndexer(si);
   }
   
   /**
    * Adds some docs, then removes all content from the library under test.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated.
    */
   public void testClearIndex()
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = null;
      PSSearchIndexer si = null;
      try
      {
         eng = PSSearchEngine.getInstance();
         si = eng.getSearchIndexer();
         PSKey cTypeKey = createContentTypeKey();
         //add a couple in case the lib is empty
         String testWord = "yyyx";
         addDocs(si, 3, testWord, true);
         //TODO need way to count all docs (query = "*" didn't work) how about ~*
         int startDocs = PSSearchQueryTest.getDocCount(eng, cTypeKey, testWord);
         assertTrue("clear index test cannot run w/o docs in db", startDocs > 0);
      
         si.clearIndex(cTypeKey);
      
         //TODO - not a great test
         assertEquals("Library not empty after clear index", 0, 
               PSSearchQueryTest.getDocCount(eng, cTypeKey, testWord));
      }
      finally
      {
         if (null != si)
            eng.releaseSearchIndexer(si);
      }
   }

   /**
    * Checks how many docs in library, inserts a new one, checks count again,
    * commits the insert, then checks one final time.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    */
   public void testUpdateNoCommit()
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = eng.getSearchIndexer();
      try
      {
         PSKey cTypeKey = createContentTypeKey();

         //make sure there are no docs waiting to be commited
         si.commitAll();
         //determine how many docs in db now    
         String query = "score"; 
         int originalDocCount = 
               PSSearchQueryTest.getDocCount(eng, cTypeKey, query);     

         //create fake items for searching
         addDocs(si, 1, query, false);
               
         int newCt = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);
         assertEquals("Wrong number of query results before commit. ", 
               originalDocCount, newCt);
         
         System.out.println("Committing index.");
         si.commit();

         assertEquals("Wrong number of query results after commit. ", 
               originalDocCount+1, 
               PSSearchQueryTest.getDocCount(eng, cTypeKey, query));
      }
      finally
      {
         if (null != si)
            eng.releaseSearchIndexer(si);
      }
            
   }

   /**
    * Adds a new doc, then submits the same doc w/ changes.
    * 
    * @throws PSSearchException If any unexpected problems.
    */
   public void testModifyDoc()
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = eng.getSearchIndexer();
      try
      {
         PSKey cTypeKey = createContentTypeKey();
         //start clean
         si.clearIndex(cTypeKey);

         String specialWord = "hubrusxx";
         //create fake items for searching
         Collection keys = addDocs(si, 1, specialWord, true);
         assertEquals("Wrong number of query results after doc add. ", 
               1, PSSearchQueryTest.getDocCount(eng, cTypeKey, specialWord));
         
         String modifiedWord = "exactayy";
         Iterator it = keys.iterator();
         while (it.hasNext())
         {
            PSSearchKey skey = (PSSearchKey) it.next();
            Map data = getDocData(modifiedWord);
            si.update(skey, data, true);
         }

         assertEquals("Original doc still present. ", 
               0, PSSearchQueryTest.getDocCount(eng, cTypeKey, specialWord));
         assertEquals("Wrong number of query results after doc mod. ", 
               1, PSSearchQueryTest.getDocCount(eng, cTypeKey, modifiedWord));
      }
      finally
      {
         if (null != si)
            eng.releaseSearchIndexer(si);
      }
            
   }

   /**
    * Performs a sequence of adding N docs several times, where n varies 
    * between 1 and 10.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated.
    */
   public void testMultiUpdate()
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = eng.getSearchIndexer();
      try
      {
         PSKey cTypeKey = createContentTypeKey();

         //make sure there are no docs waiting to be commited
         si.commitAll();
         //determine how many docs in db now    
         String query = "score"; 
         int i=1;
         int originalDocCount = 
               PSSearchQueryTest.getDocCount(eng, cTypeKey, query);     
         addDocs(si, i, "", true);
         int newCt = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);
         assertEquals("Wrong number of query results:", 
               originalDocCount+i, newCt);

         i=5;
         originalDocCount = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);     
         addDocs(si, i, "", true);
         newCt = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);
         assertEquals("Wrong number of query results:", 
               originalDocCount+i, newCt);

         i=10;
         originalDocCount = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);     
         addDocs(si, i, "", true);
         newCt = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);
         assertEquals("Wrong number of query results:", 
               originalDocCount+i, newCt);

      }
      finally
      {
         if (null != si)
            eng.releaseSearchIndexer(si);
      }
            
   }

   /**
    * Add docs, supplying <code>true</code> for the commit flag and verify 
    * the docs were really added w/o having to call commit directly.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated.
    */
   public void testUpdateCommit()
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = null;
      try
      {
         PSKey cTypeKey = createContentTypeKey();

         //determine how many docs in db now    
         String query = "score"; 
         int originalDocCount = 
               PSSearchQueryTest.getDocCount(eng, cTypeKey, query);     

         //create fake items for searching
         addDocs(si, 1, query, true);
      
         int newCt = PSSearchQueryTest.getDocCount(eng, cTypeKey, query);
         assertEquals("Wrong number of query results after doc added:", 
               originalDocCount+1, newCt);
      }
      finally
      {
         if (null != si)
            eng.releaseSearchIndexer(si);
      }
            
   }

   /**
    * Adds 3 docs to the test library, then attempts to delete them.
    * 
    * @throws PSSearchException If any problems w/ search engine.
    * @throws SQLException If next number generator fails.
    */
   public void testDelete()
      throws PSSearchException, SQLException
   {
      boolean success = false;
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = null;
      PSSearchQuery  sq = null;
      
      try
      {
         si = eng.getSearchIndexer();
         sq = eng.getSearchQuery();
         String testWord = "xxxxyy";
         int originalCount = PSSearchQueryTest.getDocCount(eng, 
               createContentTypeKey(), testWord);
         Collection keys = addDocs(si, 3, testWord, true);         
         assertEquals("Added docs not found.", originalCount+3, 
               PSSearchQueryTest.getDocCount(eng, createContentTypeKey(), 
               testWord));
         si.delete(keys);        
         assertEquals("Wrong doc count after delete.", originalCount, 
               PSSearchQueryTest.getDocCount(eng, createContentTypeKey(), 
               testWord));
               
         
         //validate the contract 'end' conditions
         Collection emptyKeys = new ArrayList();
         si.delete(emptyKeys);
         emptyKeys.add(null);
         si.delete(emptyKeys);
         PSSearchKey fakeKey = new PSSearchKey(createContentTypeKey(), 
               new PSLocator(999999, 1), null);
         emptyKeys.add(fakeKey);
         si.delete(emptyKeys);
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
    * Convenience method that calls {@link #addDocs(PSSearchIndexer, Map, int, 
    * int,boolean) addDocs(suppliedSi, getDocData(specialWord), count, -1, 
    * commit)}.
    * <p>Available for use by other unit tests.
    * 
    * @param specialWord This word is prefixed on the standard body text. May
    * be <code>null</code>, in which case "" is used.
    * 
    * @throws PSSearchException If any unexpected problems occur.
    * @throws SQLException If content id can't be generated.
    */
   static Collection addDocs(PSSearchIndexer suppliedSi, int count, 
         String specialWord, boolean commit)
      throws PSSearchException, SQLException
   {
      System.out.println("Inserting " + count + " docs w/ special word: " 
            + specialWord);
      if (null == specialWord)
         specialWord = "";

      return addDocs(suppliedSi, getDocData(specialWord), count, -1, commit);
   }

   /**
    * Adds several fields to a map for submission to the indexer.
    * 
    * @param specialWord This word is prefixed on the standard body text. May
    * be <code>null</code>.
    * 
    * @return A partial rx_generic_ce item. Never <code>null</code>.
    */
   static Map getDocData(String specialWord)
   {
      Map data = null;
      try
      {
         data = new HashMap();
         data.put("sys_title", "First Doc");
         data.put("description", "This is George Bush's final hurrah.");
         data.put("body", (specialWord 
               + " Four score and seven years ago our forefathers brought "
               + "forth on this great land.").getBytes("UTF8"));
      }
      catch (UnsupportedEncodingException e)
      {
         fail("UTF8 not supported by String any more.");
      }
      return data;      
   }

   /**
    * Adds docs to the library associated with the content type returned by
    * {@link #createContentTypeKey()}. 
    * 
    * @param suppliedSi The indexer to use. If <code>null</code>, an indexer 
    * will be obtained from the search engine. The caller is responsible for
    * releasing it.
    * 
    * @param data The data for the doc. Each entry has a <code>String</code> 
    * key that is the field name and a value that depends on the field type. 
    * See the {@link PSSearchIndexer#update(PSSearchKey,Map,boolean)} for 
    * allowed types.
    * 
    * @param count How many docs to add. Each will have a unique id.
    * 
    * @param revision If -1, use a revision of 1 for all docs. Otherwise, 
    * create all docs w/ the same content id and an incrementing revision, 
    * starting at this value. (Note, this functionality was removed from the
    * engine, so anything other than -1 will error. Left for future use.)
    * 
    * @param commit A flag that indicates whether to actually write the docs
    * to the index.
    * 
    * @return A bag of 0 or more PSSearchKey, 1 for each doc added.
    */
   static Collection addDocs(PSSearchIndexer suppliedSi, Map data, int count, 
         int revision, boolean commit)
      throws PSSearchException, SQLException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      PSSearchIndexer si = 
            null == suppliedSi ? eng.getSearchIndexer() : suppliedSi;
      try
      {
         PSKey cTypeKey = createContentTypeKey();
         Collection keys = new ArrayList();
         int rev = revision;
         int nextId = -1;
         Map submitData = new HashMap();
         for (int i=0; i < count; i++)
         {
            //create fake items for searching
            if (revision == -1 || nextId < 0)
               nextId = PSIdGenerator.getNextId("CONTENT");
            PSLocator itemKey = new PSLocator(nextId, revision == -1 ? 1 : rev++);
            PSSearchKey id = new PSSearchKey(cTypeKey, itemKey, null);
            //we need to recopy the data into the map because the udpate 
            // method may modify it
            submitData.clear();
            Iterator iter = data.keySet().iterator();
            while (iter.hasNext())
            {
               Object o = iter.next();
               submitData.put(o, data.get(o));
            }
            si.update(id, submitData, false);
            keys.add(id);
         }
         return keys;
      }
      finally
      {
         if (commit)
            si.commit();
         if (null == suppliedSi)
            eng.releaseSearchIndexer(si);
      }
   }

   /**
    * Creates a key for the generic (fast-forward, #311) content type.
    * <p>Made available for other unit tests.
    * 
    * @return Never <code>null</code>.
    */
   static PSKey createContentTypeKey()
   {
      PSKey id = PSContentType.createKey(311);
      /* we must make it persisted or it won't match corresponding ids in the 
       * server
       */
      id.setPersisted(true);
      return id;            
   }


   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new PSRequestHandlerTestSuite();
      //test indexer and query handlers, use one to test other
      suite.addTest(new PSSearchIndexerTest("testUpdateValidation"));
      suite.addTest(new PSSearchIndexerTest("testMultiUpdate"));
      suite.addTest(new PSSearchIndexerTest("testUpdateNoCommit"));
      suite.addTest(new PSSearchIndexerTest("testClearIndex"));
      suite.addTest(new PSSearchIndexerTest("testUpdateCommit"));
      suite.addTest(new PSSearchIndexerTest("testDelete"));
      suite.addTest(new PSSearchIndexerTest("testModifyDoc"));
      
      /*
      suite.addTest(new PSSearchIndexerTest("testChildUpdate"));
      suite.addTest(new PSSearchIndexerTest("test"));
      */
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
}
