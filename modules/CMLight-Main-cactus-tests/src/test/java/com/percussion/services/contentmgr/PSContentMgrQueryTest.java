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
package com.percussion.services.contentmgr;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Ignore;

import com.percussion.utils.jsr170.PSLongValue;
import com.percussion.utils.jsr170.PSStringValue;
import org.junit.experimental.categories.Category;

/**
 * Test jcr query facilities
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSContentMgrQueryTest extends ServletTestCase
{
   /**
    * Content manager instance
    */
   private static IPSContentMgr mgr = null;

   /**
    * Do the test, the rest of the test methods provide inputs
    * 
    * @param testquery the query, assumed never <code>null</code> or empty
    * @param lang the query language, either "sql" or "xpath"
    * 
    * @return the rows of the query result. Not <code>null</code> or empty. 
    * 
    * @throws Exception if there's a problem with the query
    */
   public RowIterator performTest(String testquery, String lang) throws Exception
   {
      mgr = PSContentMgrLocator.getContentMgr();
      Query q = mgr.createQuery(testquery, lang);
      QueryResult r = q.execute();
      RowIterator riter = r.getRows();
      
      // System.out.println("size = " + riter.getSize());
      return riter;
   }
   
   /**
    * This is the same as {@link #performTest(String, String)}, in additional,
    * it will validate the number of rows from the query result.
    *
    * @param testquery the query, assumed never <code>null</code> or empty
    * @param lang the query language, either "sql" or "xpath"
    * @param numResult the expected number of rows in the query result.
    * If it is ZERO, then the result set must be empty (or zero row); otherwise
    * the number of rows of the result set must be equal or more than the 
    * given number.
    * 
    * @return the rows of the query result. Not <code>null</code> or empty. 
    * 
    * @throws Exception if there's a problem with the query
    */
   public RowIterator performTest(String testquery, String lang, int numResult) 
      throws Exception
   {
      RowIterator riter = performTest(testquery, lang);
      if (numResult == 0 )
         assertTrue(riter.getSize() == 0);
      else
         assertTrue(riter.getSize() >= numResult);
      
      return riter;
   }
   
   /**
    * Do the test, the rest of the test methods provide inputs.
    * It expects the result set contain ZERO row.
    * 
    * @param testquery the query, assumed never <code>null</code> or empty
    * @param lang the query language, either "sql" or "xpath"
    * @throws Exception if there's a problem with the query
    */
   public void performTestZeroResults(String testquery, String lang) 
   throws Exception
   {
      performTest(testquery, lang, 0);
   }   
   
   /**
    * Basic test checks for some results for rffFile, and makes sure that the
    * requested projection is present.
    * 
    * @throws Exception
    */
   public void testSimpleQuery1() throws Exception
   {
      mgr = PSContentMgrLocator.getContentMgr();
      Query q = mgr.createQuery("SELECT rx:sys_title FROM rx:rffFile "
            + "WHERE rx:filename like '%.pdf'", Query.SQL);

      QueryResult r = q.execute();
      RowIterator riter = r.getRows();
      assertTrue(riter.getSize() > 0);
      
      // Grab a row and make sure there's a title
      Row row = riter.nextRow();
      assertNotNull(row.getValue("rx:sys_title"));
      
      // Test grabbing a node
      NodeIterator niter = r.getNodes();
      Node node = niter.nextNode();
      assertNotNull(node);
   }

   /**
    * Test items belongs to EI community, but it also under CI site folder
    * 
    * @throws Exception if an error occurs.
    */
   public void testCrossSiteItems() throws Exception
   {
      mgr = PSContentMgrLocator.getContentMgr();
      
      // get the items under CI site folder, but belongs to EI community
      Query q = mgr.createQuery("SELECT rx:sys_contentid, rx:sys_folderid, " +
            "jcr:path, rx:sys_communityid " + 
            "FROM nt:base " + 
            "WHERE jcr:path like '//Sites/CorporateInvestments/%' AND " + 
            "rx:sys_communityid = 1002 ORDER BY rx:sys_contentid", Query.SQL);
      
      QueryResult r = q.execute();
      RowIterator riter = r.getRows();
      assertTrue(riter.getSize() == 7);

      // validate the 1st and 2nd items
      Row row = riter.nextRow();
      validateCrossSiteRow(row, 442, 538, 1002,
            "//Sites/CorporateInvestments/Images/Icons");
      row = riter.nextRow();
      validateCrossSiteRow(row, 449, 537, 1002,
            "//Sites/CorporateInvestments/Images/Housing");
   }

   /**
    * Test items have more than one parent folders.
    * 
    * @throws Exception if an error occurs.
    */
   @Ignore("junit.framework.AssertionFailedError: null")
   public void testMultiFolderPaths() throws Exception
   {
      mgr = PSContentMgrLocator.getContentMgr();
      
      // get the items under CI site folder, but belongs to EI community
      Query q = mgr.createQuery("SELECT rx:sys_contentid, rx:sys_folderid, " +
            "jcr:path, rx:sys_communityid " + 
            "FROM nt:base " + 
            "WHERE rx:sys_contentid = 460", Query.SQL);
      
      QueryResult r = q.execute();
      RowIterator riter = r.getRows();
      assertTrue(riter.getSize() == 2);

      // validate the 1st and 2nd items
      Row row = riter.nextRow();
      validateCrossSiteRow(row, 460, 446, 1002,
            "//Sites/EnterpriseInvestments/Images/People");
      row = riter.nextRow();
      validateCrossSiteRow(row, 460, 539, 1002,
            "//Sites/CorporateInvestments/Images/People");
   }


   /**
    * Validates the supplied row against the given parameters.
    * 
    * @param r the tested row, assumed not <code>null</code>.
    * @param contentID the expected sys_content id
    * @param folderID the expected folder id
    * @param communityID the expected community id
    * @param folderPath the expected folder path.
    * 
    * @throws Exception if error occurs.
    */
   private void validateCrossSiteRow(Row r, long contentID, long folderID,
         long communityID, String folderPath) throws Exception
   {
      PSLongValue id = (PSLongValue) r.getValue("rx:sys_contentid");
      assertTrue(id.getLong() == contentID);
      id = (PSLongValue) r.getValue("rx:sys_folderid");
      assertTrue(id.getLong() == folderID);
      id = (PSLongValue) r.getValue("rx:sys_communityid");
      assertTrue(id.getLong() == communityID);
      PSStringValue path = (PSStringValue) r.getValue("jcr:path");
      assertTrue(path.getString().equals(folderPath));
   }
   
   /**
    * Skipped test - not sure why
    * 
    * @throws Exception
    */
   public void disabled_testSimpleQuery2() throws Exception
   {
      performTest("SELECT rx:sys_title FROM nt:base "
            + "WHERE rx:displaytitle like '%fund%'", Query.SQL);
   }

   /**
    * Test date comparison and ordering
    * 
    * @throws Exception
    */
   @Ignore("org.hibernate.exception.SQLGrammarException: could not execute query on Derby")
   public void testSimpleQuery3() throws Exception
   {
      performTest("SELECT rx:sys_title FROM nt:base "
            + "WHERE rx:sys_contentstartdate > '2004/8/1' "
            + "ORDER BY rx:sys_title asc, rx:sys_folderid desc",
            Query.SQL, 250);
   }

   /**
    * Test path selector
    * 
    * @throws Exception
    */
   public void testSimpleQueryPath1() throws Exception
   {
      performTest("SELECT rx:sys_title FROM rx:rffBrief "
                  + "WHERE jcr:path like '/jcr:root/Sites/EnterpriseInvestments/%'",
                  Query.SQL, 1);
   }

   /**
    * Test another path selector
    *  
    * @throws Exception
    */
   public void testSimpleQueryPath2() throws Exception
   {
      performTest("SELECT rx:sys_title FROM rx:rfffile "
            + "WHERE jcr:path like '/jcr:root/Sites/EnterpriseInvestments/Files/%'",
            Query.SQL, 2);
   }
   
   /**
    * Test using a missing property for some content types
    * 
    * @throws Exception
    */
   @Ignore("org.hibernate.exception.SQLGrammarException: could not execute query")
   public void testMissingProp() throws Exception
   {
      performTest("SELECT rx:sys_title, rx:filename FROM nt:base "
            + "WHERE rx:filename is not null",
            Query.SQL, 37);     
   }
   
   
   /**
    * Test various content status and calculated fields in the projection
    * 
    * @throws Exception
    */
   public void testProjections() throws Exception
   {
      performTest("SELECT jcr:path from rx:rffgeneric", Query.SQL, 108);
      performTest("SELECT rx:sys_title from rx:rffgeneric", Query.SQL, 108);
      performTest("SELECT rx:sys_contentmodifieddate, rx:sys_contenttypeid " +
            "from rx:rffgeneric", Query.SQL, 108);
   }
   
   /**
    * This test tries to query whole nodes from the repository
    * 
    * @throws Exception
    */
   public void fixme_testNodeResults() throws Exception
   {
      performTest("select * from rx:rffgeneric " +
            "where rx:displaytitle='EI Insurance'", Query.SQL, 4);
      performTest("select * from rx:rffevent", Query.SQL, 17);
      performTest("select * from rx:rffpressrelease", Query.SQL, 20);
   }
   
   /**
    * These are tests that grab lob fields for sorting, where clause 
    * and projection cases. This also tests that the joins are working since
    * the lob fields are in a separate object.
    * 
    * @throws Exception
    */
   public void fixme_testLobs() throws Exception
   {
      performTest("SELECT jcr:path from rx:rffgeneric " +
            "where rx:body is not null", Query.SQL, 108);
      performTest("SELECT jcr:path from rx:rffgeneric " +
            "where rx:body is not null order by rx:displaytitle", Query.SQL, 108);
      performTest("SELECT jcr:path, rx:body from rx:rffgeneric " +
            "order by rx:displaytitle", Query.SQL, 200);
      performTest("SELECT jcr:path from rx:rffimage " +
            "where rx:img1 is not null", Query.SQL, 31);
      
      performTestZeroResults("SELECT jcr:path from rx:rffimage " +
            "where rx:img1 is null", Query.SQL);
      performTestZeroResults("SELECT jcr:path from rx:rffgeneric " +
            "where rx:body is null", Query.SQL);
   }
}
