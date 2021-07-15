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

import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSRequestHandlerTestSuite;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Unit tests for the full-text search engine. Tests very basic functionality,
 * that the server will start and stop and the standard objects (admin, query,
 * and indexer) can all be obtained. 
 *
 * @author paulhoward
 */
@Category(IntegrationTest.class)
public class PSSearchEngineTest extends TestCase 
   implements IPSServerBasedJunitTest
{
   /**
    * Ctor
    * 
    * @param name The name of the test.
    */
   public PSSearchEngineTest(String name)
   {
      super(name);
   }


   /**
    * Must be called once after all tests requiring the engine are complete.
    *  
    * @throws PSSearchException
    */
   public void testShutdown()
      throws PSSearchException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance();
      eng.shutdown(false);   
      assertTrue("Incorrect status.", 
            eng.getStateCode() == PSSearchEngine.STATUS_TERMINATED);
      assertTrue("Engine falsely claims available.", !eng.isAvailable());
      assertTrue("Status string is empty.", 
            eng.getStateString().trim().length() != 0);
      //leave in running state for other tests
      eng.start();
      assertTrue("Engine failed to restart", eng.isAvailable());
   }


   /**
    * Validates that a status element is returned. 
    * <p>Assumes the engine has already been initialized.
    * @throws PSSearchException
    */
   public void testGetStatus()
      throws PSSearchException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance(); 
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element e = eng.getStatus(doc);
      assertNotNull("Missing status.", e);
      String status = e.getAttribute("runningStatus");
      assertEquals("Incorrect status string", 
            eng.getStateString(), status);
      assertEquals("Incorrect root tag name.", "SearchStatus", e.getNodeName());
      // Get engine status
      NodeList nodes = e.getElementsByTagName("status");
      assertNotNull("Missing children", nodes);
      Element engine = (Element) nodes.item(0);
      assertNotNull("Missing engine status", engine);
      assertNotNull("Missing attribute for state", 
         engine.getAttribute("state"));
      Element indexer = (Element) nodes.item(1);
      assertNotNull("Missing indexer status", engine);
      assertNotNull("Missing attribute for state", 
         indexer.getAttribute("state"));  
      assertNotNull("Missing library count",
         indexer.getAttribute("uncommited-libs-count"));
      assertNotNull("Missing file count",
         indexer.getAttribute("file-delete-count"));             
   }

   /**
    * Validates that all getters return a valid object and don't throw.
    * <p>Assumes the engine has already been initialized.
    * 
    * @throws PSSearchException
    * @throws PSAdminLockedException
    */
   public void testGetters()
      throws PSSearchException, PSAdminLockedException
   {
      PSSearchEngine eng = PSSearchEngine.getInstance(); 
      
      PSSearchAdmin sa = eng.getSearchAdmin(true);
      assertNotNull("Missing admin.", sa);
      eng.releaseSearchAdmin(sa);
      
      sa = eng.getSearchAdmin(false);
      assertNotNull("Missing read-only admin.", sa);
      
      PSSearchIndexer si = eng.getSearchIndexer();
      assertNotNull("Missing indexer.", si);
      eng.releaseSearchIndexer(si);
      
      PSSearchQuery sq = eng.getSearchQuery();
      assertNotNull("Missing query.", sq);
      eng.releaseSearchQuery(sq);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new PSRequestHandlerTestSuite();
      suite.addTest(new PSSearchEngineTest("testGetStatus"));
      suite.addTest(new PSSearchEngineTest("testGetters"));
      //must be run last
      suite.addTest(new PSSearchEngineTest("testShutdown"));
      return suite;
   }

   /**
    * Runs the test suite.
    */
   public static void main(String args[]) 
   {
      TestRunner.run(suite());
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

   /**
    * Unused.
    * @param req unused
    */
   public void oneTimeTearDown() 
   {}
}
