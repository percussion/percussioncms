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
