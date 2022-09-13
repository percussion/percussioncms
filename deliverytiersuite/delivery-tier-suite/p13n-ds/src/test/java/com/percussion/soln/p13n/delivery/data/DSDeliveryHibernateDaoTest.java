/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.soln.p13n.delivery.data;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static integrationtest.spring.SpringSetup.getBean;
import static integrationtest.spring.SpringSetup.loadXmlBeanFiles;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DSDeliveryHibernateDaoTest {

    static IDeliveryDataService dao;
    
    @BeforeClass
    public static void setUp() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/spring/ds/*.xml");
        dao = getBean("deliveryDao", IDeliveryDataService.class);
    }

    @Test
    public void testSaveAndGetRuleItems() {
        DeliveryListItem ruleItem = new DeliveryListItem();
        ruleItem.setContentId(1000);
        ruleItem.setContentType("Blah");
        dao.saveListItems(Collections.singletonList(ruleItem));
        assertNotNull(dao.getListItems(Collections.singletonList(1000L)));
    }
    
    
    @Test
    public void testSaveRuleItemWitNullIds() {
        DeliveryListItem ruleItem = new DeliveryListItem();
        ruleItem.setContentId(2000);
        ruleItem.setContentType("Blah");
        ruleItem.setSnippetFilterIds(null);
        ruleItem.setSegmentIds(null);
        ruleItem.setSnippets(null);
        dao.saveListItems(Collections.singletonList(ruleItem));
        assertNotNull(dao.getListItems(Collections.singletonList(2000L)));
    }
    
    
    @Test
    public void testSaveRuleItemWithSnippets() {
        DeliveryListItem ruleItem = new DeliveryListItem();
        ruleItem.setContentId(2000);
        ruleItem.setContentType("Blah");
        ruleItem.setSnippetFilterIds(null);
        ruleItem.setSegmentIds(null);
        DeliverySnippetItem snip_a = new DeliverySnippetItem();
        snip_a.setContentId(958);
        HashSet<String> segIds = new HashSet<>();
        segIds.add("1110");
        segIds.add("1102");
        snip_a.setSegmentIds(segIds);
        snip_a.setRendering("\r\n" + 
        		"    <div class=\"titleParaThumbHome\">\r\n" + 
        		"                    <a href=\"/SiteTools/PersonalizedLists/item1107.html\">Best Practices for Upgrading Webcast</a>\r\n" + 
        		"                    <p>Percussion Software is pleased to invite you to a complimentary webinar on the 4th of October. The webinar will give you an overview of the benefits of Rhythmyx 6.x as well as how best to go about a successful upgrade path for your environment and learn how other users have successfully upgraded.</p>            </div>");
        
        Map<String, String> props = new HashMap<>();
        props.put("test_name", "test_value");
        props.put("test_name_a", "test_value");
        snip_a.setProperties(props);
        DeliverySnippetItem snip_b = new DeliverySnippetItem();
        snip_b.setContentId(968);
        segIds = new HashSet<>();
        segIds.add("1110");
        segIds.add("1102");
        snip_b.setSegmentIds(segIds);
        
        ruleItem.setSnippets(asList(snip_a,snip_b));
        dao.saveListItems(Collections.singletonList(ruleItem));
        DeliveryListItem savedItem = dao.getListItems(Collections.singletonList(2000L)).get(0);
        assertNotNull(savedItem);
        assertNotNull(savedItem.getSnippets().get(0));
        assertNotNull(savedItem.getSnippets().get(0).getProperties());
        assertEquals("test_value", savedItem.getSnippets().get(0).getProperties().get("test_name"));
    }
    
    @Test
    public void testSaveModifiedRuleItemWithSnippets() {
        doFirst();
        doSecond();
    }
    
    private void doFirst() {
        DeliveryListItem ruleItem = new DeliveryListItem();
        ruleItem.setContentId(3000);
        ruleItem.setContentType("Blah");
        ruleItem.setSnippetFilterIds(null);
        ruleItem.setSegmentIds(null);
        DeliverySnippetItem snip_a = new DeliverySnippetItem();
        snip_a.setContentId(958);
        HashSet<String> segIds = new HashSet<>();
        segIds.add("1110");
        segIds.add("1102");
        snip_a.setSegmentIds(segIds);
        
        DeliverySnippetItem snip_b = new DeliverySnippetItem();
        snip_b.setContentId(968);
        segIds = new HashSet<>();
        segIds.add("1110");
        segIds.add("1102");
        snip_b.setSegmentIds(segIds);
        
        ruleItem.setSnippets(asList(snip_a,snip_b));
        dao.saveListItems(Collections.singletonList(ruleItem));
        DeliveryListItem savedItem = dao.getListItems(Collections.singletonList(3000L)).get(0);
        assertNotNull(savedItem);
        assertNotNull(savedItem.getSnippets().get(0));
    }
    
    private void doSecond() {
        DeliveryListItem ruleItem = new DeliveryListItem();
        ruleItem.setContentId(3000);
        ruleItem.setContentType("Blah changed");
        ruleItem.setSnippetFilterIds(null);
        ruleItem.setSegmentIds(null);
        
        DeliverySnippetItem snip_b = new DeliverySnippetItem();
        snip_b.setContentId(968);
        HashSet<String> segIds = new HashSet<>();
        segIds.add("2220");
        segIds.add("2203");
        snip_b.setSegmentIds(segIds);
        
        ruleItem.setSnippets(Collections.singletonList(snip_b));
        dao.saveListItems(Collections.singletonList(ruleItem));
        DeliveryListItem savedItem = dao.getListItems(Collections.singletonList(3000L)).get(0);
        assertNotNull(savedItem);
        assertEquals(1, savedItem.getSnippets().size());
    }
    
    

}
