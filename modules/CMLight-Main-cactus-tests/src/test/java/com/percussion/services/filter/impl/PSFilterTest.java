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
package com.percussion.services.filter.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test item filters
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSFilterTest extends ServletTestCase
{
   /**
    * 
    */
   private static final String TEST_FILTER_NAME = "junitTestFilter";

   
   /**
    * Not really a test, just cleanup anything left over from an earlier
    * test to make sure we're clean.
    * 
    * @throws Exception
    */
   public void testCleanup() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      
      try
      {
         while(true)
         {
            IPSItemFilter filter = fsvc.findFilterByName(TEST_FILTER_NAME);
            fsvc.deleteFilter(filter);
         }
      }
      catch(Exception e)
      {
         // Ignore
      }
   }
   
   /**
    * This test creates and mutates an item filter instance. It makes sure to
    * cover the following cases:
    * <ul>
    * <li>Adding a rule
    * <li>Removing a rule
    * <li>Modifying a rule
    * </ul>
    * 
    * @throws Exception
    */
   public void testFolderSavesAndMerges() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.createFilter(TEST_FILTER_NAME,
            "Item filter just to test");
      Map<String, String> params = new HashMap<String, String>();
      params.put("a", "1");
      IPSItemFilterRuleDef def = fsvc.createRuleDef("mytestrule1", params);
      filter.addRuleDef(def);
      fsvc.saveFilter(filter);

      Thread.sleep(200);
      
      // Add a new rule and add a new rule def
      filter = fsvc.findFilterByName(TEST_FILTER_NAME);
      params = new HashMap<String, String>();
      params.put("b", "2");
      params.put("c", "3");
      def = fsvc.createRuleDef("mytestrule2", params);
      filter.addRuleDef(def);
      fsvc.saveFilter(filter);

      // Remove the first rule and save
      filter = fsvc.findFilterByName(TEST_FILTER_NAME);
      Map<String, IPSItemFilterRuleDef> defs = 
         new HashMap<String, IPSItemFilterRuleDef>();
      for (IPSItemFilterRuleDef frdef : filter.getRuleDefs())
      {
         defs.put(frdef.getRuleName(), frdef);
      }
      // Do some testing
      assertNotNull(defs.get("mytestrule1"));
      assertNotNull(defs.get("mytestrule2"));
      IPSItemFilterRuleDef deftoTest = defs.get("mytestrule1");
      assertEquals("1", deftoTest.getParam("a"));
      deftoTest = defs.get("mytestrule2");
      assertEquals("2", deftoTest.getParam("b"));
      assertEquals("3", deftoTest.getParam("c"));
      filter.removeRuleDef(deftoTest);
      fsvc.saveFilter(filter);
      
      // Check that the rule is now missing, modify a parameter and save
      filter = fsvc.findFilterByName(TEST_FILTER_NAME);
      defs = new HashMap<String, IPSItemFilterRuleDef>();
      for (IPSItemFilterRuleDef frdef : filter.getRuleDefs())
      {
         defs.put(frdef.getRuleName(), frdef);
      }
      assertNull(defs.get("mytestrule2"));
      deftoTest = defs.get("mytestrule1");
      // Modify parameter
      deftoTest.setParam("a", "5");
      fsvc.saveFilter(filter);
      
      // Remove existing parameter and add a new parameter
      filter = fsvc.findFilterByName(TEST_FILTER_NAME);
      defs = new HashMap<String, IPSItemFilterRuleDef>();
      for (IPSItemFilterRuleDef frdef : filter.getRuleDefs())
      {
         defs.put(frdef.getRuleName(), frdef);
      }
      deftoTest = defs.get("mytestrule1");
      // Check modified parameter
      assertEquals("5", deftoTest.getParam("a"));
      // Modify parameter
      deftoTest.setParam("d", "6");
      deftoTest.removeParam("a");
      fsvc.saveFilter(filter);
   }

   /**
    * Test preview filter
    * 
    * @throws Exception
    */
   public void testPreviewFilter() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      PSStopwatch w = new PSStopwatch();
      List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
      items.addAll(getFolderItems("//Sites/EnterpriseInvestments", 1));
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices", 1));
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices/Funds", 1));

      IPSItemFilter filter = fsvc.findFilterByName("preview");

      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_USER, "doug");
      w.start();
      List<IPSFilterItem> results = filter.filter(items, params);
      w.stop();
      assertNotNull(results);
      assertTrue(results.size() > 0);
      System.out.println("Preview filtering " + results.size() + " took " + w);

      // Make a map of the current revisions of all the items
      Map<Integer, Integer> currentmap = new HashMap<Integer, Integer>();
      for (IPSFilterItem item : results)
      {
         PSLegacyGuid lg = (PSLegacyGuid) item.getItemId();
         currentmap.put(lg.getContentId(), lg.getRevision());
      }
      // Now get the same information directly
      items.clear();
      items.addAll(getFolderItems("//Sites/EnterpriseInvestments", 0));
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices", 0));
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices/Funds", 0));
      // Make a map of the results and check some revisions
      Map<Integer, Integer> revisionmap = new HashMap<Integer, Integer>();
      for (IPSFilterItem item : results)
      {
         PSLegacyGuid lg = (PSLegacyGuid) item.getItemId();
         revisionmap.put(lg.getContentId(), lg.getRevision());
      }
      assertEquals(currentmap, revisionmap);
   }

   /**
    * Test folder filter
    * 
    * @throws Exception
    */
   public void testFolderFilter1() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.findFilterByName("sitefolder");
      List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
      items.addAll(getFolderItems("//Sites/EnterpriseInvestments", 0));
      items.addAll(getFolderItems("//Sites/CorporateInvestments", 0));

      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_SITEID, "301");
      List<IPSFilterItem> results = filter.filter(items, params);
      assertNotNull(results);
      assertTrue(results.size() > 0);
   }

   /**
    * Test folder filter and print out performance information, the prior test
    * will prime the pump with data.
    * 
    * @throws Exception
    */
   public void testFolderFilter2() throws Exception
   {
      PSStopwatch w = new PSStopwatch();
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.findFilterByName("sitefolder");
      List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices", 0));
      items.addAll(getFolderItems("//Sites/", 0));

      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_SITEID, "301");
      w.start();
      List<IPSFilterItem> results = filter.filter(items, params);
      w.stop();
      assertNotNull(results);
      assertTrue(results.size() > 0);
      System.out.println("Folder filtering " + results.size() + " took " + w);
   }

   /**
    * Test publishable filter
    * 
    * @throws Exception
    */
   public void testPublishableFilter() throws Exception
   {
      PSStopwatch w = new PSStopwatch();
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.findFilterByName("public");
      
      List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
      items.addAll(getFolderItems(
            "//Sites/EnterpriseInvestments/ProductsAndServices", 0));

      w.start();
      List<IPSFilterItem> results = filter.filter(items, null);
      w.stop();
      assertNotNull(results);
      assertTrue(results.size() > 0);
      //System.out.println("State filtering " + results.size() + " took " + w);
   }
   
   public void testSetLegacyAuthtypeId() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.findFilterByName("public");
      
      String xmlString = ((PSItemFilter) filter).toXML();
      PSItemFilter pubFilter = new PSItemFilter();
      pubFilter.fromXML(xmlString);
      assertTrue(pubFilter.getLegacyAuthtypeId() != null);
      
      pubFilter.setLegacyAuthtypeId(null);
      xmlString = pubFilter.toXML();
      PSItemFilter pubFilter_2 = new PSItemFilter();
      pubFilter_2.fromXML(xmlString);
      assertTrue(pubFilter_2.getLegacyAuthtypeId() == null);
   }
   
   /**
    * Test Find methods
    * @throws Exception
    */
   public void testFindFilter() throws Exception
   {
      String filterName = "junitTestFilterForFinder";
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter filter = fsvc.createFilter(filterName,
            "Item filter just to test");
      Map<String, String> params = new HashMap<String, String>();
      params.put("a", "1");
      IPSItemFilterRuleDef def = fsvc.createRuleDef("mytestrule1", params);
      filter.addRuleDef(def);
      fsvc.saveFilter(filter);

      Thread.sleep(200);
      
      filter = fsvc.findFilterByName(filterName);
      assertNotNull(filter);
      filter = fsvc.findFilterByID(filter.getGUID());
      assertNotNull(filter);
      //clean up
      fsvc.deleteFilter(filter);
   }

   /**
    * Get the folder items for the test
    * 
    * @param folderPath the path for the items
    * @param rev if more than zero, use this revision instead of the current
    *           revision
    * @return the filter items
    * @throws PSCmsException
    */
   private List<IPSFilterItem> getFolderItems(String folderPath, int rev)
         throws PSCmsException
   {
      PSRequest req = PSRequest.getContextForRequest();
      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      PSComponentSummary folder = proc.getSummary(folderPath);
      if (folder == null)
      {
         throw new RuntimeException("Couldn't find folder " + folderPath);
      }
      PSLocator folderLocator = folder.getCurrentLocator();
      PSComponentSummary children[] = proc.getChildSummaries(folderLocator);
      List<IPSFilterItem> rval = new ArrayList<IPSFilterItem>();
      for (PSComponentSummary child : children)
      {
         int contentid = child.getContentId();
         int revision = rev == 0
               ? child.getCurrentLocator().getRevision()
               : rev;
         PSFilterItem item = new PSFilterItem(new PSLegacyGuid(contentid,
               revision), new PSLegacyGuid(folderLocator), null);
         rval.add(item);
      }
      return rval;
   }
   
   
   
}
