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
package com.percussion.services.contentmgr.impl;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Perform a variety of tests to ensure that the JCR query engine is
 * working correctly.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSContentMgrQueryTest extends ServletTestCase
{
   /**
    * @throws Exception
    */
   public void testCreateXmlQuery() throws Exception
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      String q = "//foo/bar[@rx:a > 3]";

      Query xpathq = mgr.createQuery(q, Query.XPATH);

      String q2 = "select * from nt:base where jcr:path like '//foo/bar/%' "
            + "and rx:a > 3";

      Query sqlq = mgr.createQuery(q2, Query.SQL);

      assertNotNull(xpathq);
      assertNotNull(sqlq);
   }

   /**
    * @throws Exception
    */
   public void testInvalidQuery() throws Exception
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      String q = "//foo/bar[@rx:a & 3]";

      try
      {
         mgr.createQuery(q, Query.XPATH);
         assertTrue("No exception was thrown", false);
      }
      catch (InvalidQueryException e)
      {
         System.out.println(e.getLocalizedMessage());
      }

      try
      {
         String q2 = "select * from nt:base where jcr:path ^ '//foo/bar/%' "
               + "and rx:a > 3";
         mgr.createQuery(q2, Query.SQL);
         assertTrue("No exception was thrown", false);
      }
      catch (InvalidQueryException e)
      {
         System.out.println(e.getLocalizedMessage());
      }
   }
   
   /**
    * @throws Exception
    */
   public void testBothStyles() throws Exception
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      String xpathq = "element(*,rx:rffgeneric)(@rx:sys_title)";
      String sqlq = "select rx:sys_title from rx:rffgeneric";
      
      Query xpathquery = mgr.createQuery(xpathq, Query.XPATH);
      Query sqlquery = mgr.createQuery(sqlq, Query.SQL);
      
      QueryResult xpathres = xpathquery.execute();
      QueryResult sqlres = sqlquery.execute();
      
      assertTrue(xpathres.getRows().getSize() > 1);
      assertEquals(xpathres.getRows().getSize(), sqlres.getRows().getSize());
      
      Row r = xpathres.getRows().nextRow();
      
      assertEquals(r.getValues().length, 1);
   }
   
   /**
    * Test that queries that hit simple children are working. This also tests
    * that numeric parameters are correctly set as long values for parameters.
    * @throws Exception
    */
   public void testSimpleChildQuery() throws Exception
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      String sqlq = "select rx:sys_contentid from rx:rffevent " +
            "where rx:event_type = 1";
      Query sqlquery = mgr.createQuery(sqlq, Query.SQL);
      QueryResult result = sqlquery.execute();
      assertTrue(result.getRows().getSize() > 0);
   }
   
   /**
    * Test that queries that use sys_contentid, a content status field
    * work properly. 
    * @throws Exception
    */
   public void testContentIdQuery() throws Exception
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      String sqlq = "select rx:sys_contentid from rx:rffevent " +
            "where rx:sys_contentid = 519";
      Query sqlquery = mgr.createQuery(sqlq, Query.SQL);
      QueryResult result = sqlquery.execute();
      assertTrue(result.getRows().getSize() > 0);
   }
}
