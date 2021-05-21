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
package com.percussion.services.filter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

/**
 * @author dougrand
 * 
 */
@Category(IntegrationTest.class)
public class PSFilterServiceTest extends ServletTestCase
{
   private static final Logger log = LogManager.getLogger(PSFilterServiceTest.class);

   /**
    * Fixed authtype
    */
   private static final int AUTH = 10111;


   /**
    * Cleanup
    * 
    * @throws Exception
    */
   public void testCleanupFilters() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();

      List<IPSItemFilter> filters = fsvc.findAllFilters();
      for (IPSItemFilter f : filters)
      {
         if (f.getName().equals("f1") || f.getName().equals("vFilter"))
            fsvc.deleteFilter(f);
      }
   }
   
   /**
    * Test executing Content Lists.
    * @throws Exception if any error occurs.
    */
   public void testExecuteContentList() throws Exception
   {
      // rffEiFullBinary (310)
      IPSGuid clistId = new PSGuid(PSTypeEnum.CONTENT_LIST, 310); 
      IPSGuid siteId = new PSGuid(PSTypeEnum.SITE, 301); // EI site
      IPSGuid deliveryContextId = new PSGuid(PSTypeEnum.CONTEXT, 1); // publish
      IPSPublisherService ps = PSPublisherServiceLocator.getPublisherService();
      IPSContentList cList = ps.loadContentList(clistId);
      List<PSContentListItem> clistItems = ps.executeContentList(cList, null,
            true, deliveryContextId, siteId);
      
      assertTrue(clistItems != null);
      assertTrue(clistItems.size() > 0); // == 73 from a refresh installed FF
      
      // rffEiFullNonBinary (311)
      clistId = new PSGuid(PSTypeEnum.CONTENT_LIST, 311); 
      cList = ps.loadContentList(clistId);
      clistItems = ps.executeContentList(cList, null, true, deliveryContextId,
            siteId);
      assertTrue(clistItems != null);
      assertTrue(clistItems.size() > 0); // == 68 from a refresh installed FF
   }

   public void testFindLastPublishedItem() throws Exception
   {
      IPSPublisherService ps = PSPublisherServiceLocator.getPublisherService();
      PSLegacyGuid lgId = new PSLegacyGuid(1, 1);
      IPSPubItemStatus itemStatus = ps.findLastPublishedItemStatus(lgId);
      assertTrue(itemStatus == null);
   }
   
   /**
    * Util method for create, save filter, then load serialize and load and save
    * See {@link #testLoadAndDeserializeAndSaveFilter()} for more info.
    * 
    * @param fsvc the filter service never <code>null</code>
    * @return the actual saved filter
    * @throws Exception
    */
   private IPSItemFilter createAndSaveFilter(IPSFilterService fsvc)
         throws Exception
   {
      IPSItemFilter ifilter = fsvc.createFilter("vFilter", "vfilter Desc");
      ifilter.setLegacyAuthtypeId(new Integer(AUTH));
      Map<String, String> params = new HashMap<String, String>();
      params.put("sys_folderPaths_v", "//Sites/EnterpriseInvestments/v%");
      IPSItemFilterRuleDef rule = fsvc.createRuleDef(
            "sys_filterByFolderPaths_v", params);
      ifilter.addRuleDef(rule);
      fsvc.saveFilter(ifilter);
      return ifilter;
   }


   /**
    * Helper method to load and save. Can be loaded in two ways:
    * 1. provide filter name only OR
    * 2. provide the filter as a string
    * @param fsvc the filter service
    * @param name the name of the filter may be <code>null</code>
    * @param filterStr the serialized data for filter may be <code>null</code>
    * @param doSave to save or not to save a boolean
    * @throws PSFilterException
    * @throws IOException
    * @throws SAXException
    * @throws PSInvalidXmlException
    */
   private void loadAndSaveFilter(IPSFilterService fsvc, String name,
         String filterStr, boolean doSave) throws PSFilterException,
         IOException, SAXException, PSInvalidXmlException
   {
      IPSItemFilter ifilter = null;
      if (StringUtils.isNotBlank(name))
         ifilter = fsvc.findFilterByName(name);
      else if (StringUtils.isNotBlank(filterStr))
      {
         ifilter = new PSItemFilter();
         ifilter.fromXML(filterStr);
      }
      // after loading from either filterName or deserializing, save the version
      Integer ver = ((PSItemFilter) ifilter).getVersion();
      
      Set<IPSItemFilterRuleDef> rules = ifilter.getRuleDefs();
      // set version info on the rule defs to null, because new guids are
      // assigned
      for (IPSItemFilterRuleDef def : rules)
         ((PSItemFilterRuleDef) def).setVersion(null);

      // restore the version
      ((PSItemFilter) ifilter).setVersion(null);
      ((PSItemFilter) ifilter).setVersion(ver);

      if ( doSave )
      {
         try
         {
            fsvc.saveFilter(ifilter);
         }
         catch (Exception e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            assert (false);
         }
      }
      assert (true);
   }



   /**
    * Testing of msm functionality for: creating a filter archive
    *    1. create, save a filter 
    *    2. load that filter and serialize 
    * @throws Exception if there's a problem 
    */
   public void testSerializeFilter() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter ifilter = createAndSaveFilter(fsvc);
      loadAndSaveFilter(fsvc, ifilter.getName(), null, false);
      ifilter.toXML();
   }
   /**
    * Testing of msm functionality for: deployment of a filter archive: 
    *    1. Load an existing filter 
    *    2. add  a new rule 
    *    3. save the filter with the new rule
    * 
    * @throws Exception
    */
   public void testLoadAndDeserializeAndSaveFilter() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();

      // load an existing filter and save its version
      IPSItemFilter filter = createAndSaveFilter( fsvc);
      
      // the same filter loaded above as string, but a new rule is added
      // representing an msm operation
      String foo1 = 
         "<item-filter id=\"1\">" +
            "<guid>" + filter.getGUID().toString() + "</guid>" +
            "<description>vfilter Desc</description>" +
            "<label>vFilter</label>" +
            "<legacy-authtype-id>10111</legacy-authtype-id>" +
            "<name>vFilter</name>" +
            "<parent-filter-id/>" +
            "<rule-defs>"+
               "<rule-def id=\"2\">" +
                  "<filter idref=\"1\"/>" +
                  "<params>"+
                     "<entry id=\"3\">" +
                        "<key>sys_folderPaths_v</key>" +
                        "<value>//Sites/EnterpriseInvestments/v%</value>" +
                     "</entry>" +
                  "</params>" +
                  "<rule-name>sys_filterByFolderPaths_v</rule-name>" +
                "</rule-def>" +
                "<rule-def id=\"4\">" + 
                   "<filter idref=\"1\"/>" +
                   "<params>" +
                      "<entry id=\"5\">" + 
                      "<key>new_rule_arg1</key>" +
                      "<value>new_rule_arg1_value</value>" +
                      "</entry>" +
                   "</params>" +
                   "<rule-name>newRule</rule-name>" +
                "</rule-def>" +
             "</rule-defs>" +
          "</item-filter>";

      loadAndSaveFilter(fsvc, null, foo1, true);
   }

   /**
    * @throws Exception
    */
   public void testCreateFilter() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter ifilter = fsvc.createFilter("f1", "filter 1");
      ifilter.setLegacyAuthtypeId(new Integer(AUTH));
      Map<String, String> params = new HashMap<String, String>();
      params.put("sys_folderPaths", "//Sites/EnterpriseInvestments/%");
      IPSItemFilterRuleDef rule = fsvc
            .createRuleDef(
                  "Java/global/percussion/itemfilter/sys_filterByFolderPaths",
                  params);
      ifilter.addRuleDef(rule);
      params = new HashMap<String, String>();
      params.put("sys_flagValues", "y");
      rule = fsvc.createRuleDef(
            "Java/global/percussion/itemfilter/sys_filterByPublishableFlag",
            params);
      ifilter.addRuleDef(rule);
      fsvc.saveFilter(ifilter);
   }

   /**
    * @throws Exception
    */
   public void testFinders() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      // Verify good lookups
      assertNotNull(fsvc.findFilterByAuthType(AUTH));
      assertNotNull(fsvc.findFilterByName("f1"));

      // Verify exception behavior
      try
      {
         fsvc.findFilterByAuthType(111111111);
         assertTrue(false);
      }
      catch (PSFilterException f)
      {
         // Correct
      }

      try
      {
         fsvc.findFilterByName("non existant filter****");
         assertTrue(false);
      }
      catch (PSFilterException f)
      {
         // Correct
      }
   }

   /**
    * @throws Exception
    */
   public void testActiveRule() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter ifilter = fsvc.findFilterByName("f1");
      List<IPSFilterItem> itemsToFilter = new ArrayList<IPSFilterItem>();
      itemsToFilter.add(new PSFilterItem(new PSLegacyGuid(466, 1),
            new PSLegacyGuid(301, 1), null));
      itemsToFilter.add(new PSFilterItem(new PSLegacyGuid(504, 1),
            new PSLegacyGuid(302, 1), null));
      itemsToFilter.add(new PSFilterItem(new PSLegacyGuid(442, 1),
            new PSLegacyGuid(441, 1), null));
      List<IPSFilterItem> filteredIds = ifilter.filter(itemsToFilter, null);
      assertNotNull(filteredIds);
   }

   /**
    * @throws Exception
    */
   public void testMutateFilterData() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSItemFilter ifilter = fsvc.findFilterByName("f1");
      ifilter = fsvc.loadFilter(ifilter.getGUID());
      Set<IPSItemFilterRuleDef> defs = ifilter.getRuleDefs();
      assertNotNull(defs);
      assertNotNull(defs.iterator());
      IPSItemFilterRuleDef thedef = defs.iterator().next();
      assertNotNull(thedef);
      // Try adding a parameter then saving. Restore and check the param.
      thedef.setParam("test", "testvalue");
      fsvc.saveFilter(ifilter);
      // Now get a new copy
      ifilter = fsvc.findFilterByName("f1");
      ifilter = fsvc.loadFilter(ifilter.getGUID());
      defs = ifilter.getRuleDefs();
      thedef = defs.iterator().next();
      assertEquals(thedef.getParam("test"), "testvalue");
      // Remove, save and check
      thedef.removeParam("test");
      fsvc.saveFilter(ifilter);
      ifilter = fsvc.findFilterByName("f1");
      ifilter = fsvc.loadFilter(ifilter.getGUID());
      defs = ifilter.getRuleDefs();
      thedef = defs.iterator().next();
      assertNull(thedef.getParam("test"));
   }

   /**
    * @throws Exception
    */
   public void testDeleteFilter() throws Exception
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      // load filter
      IPSItemFilter ifilter = fsvc.findFilterByName("f1");
      ifilter = fsvc.loadFilter(ifilter.getGUID());
      assertNotNull(ifilter);

      // delete rule
      ifilter.setRuleDefs(new HashSet<IPSItemFilterRuleDef>());
      fsvc.saveFilter(ifilter);

      // verify deleted rule
      ifilter = fsvc.findFilterByName("f1");
      ifilter = fsvc.loadFilter(ifilter.getGUID());
      assertTrue(ifilter != null && ifilter.getRuleDefs().size() == 0);

      fsvc.deleteFilter(ifilter);
   }
}
