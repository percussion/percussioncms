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
package com.percussion.pagemanagement.data;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.percussion.pagemanagement.data.PSWidgetProperties.PSWidgetProperty;
import com.percussion.share.data.PSDataObjectTestCase;
import com.percussion.share.test.PSDataObjectTestUtils;

public class PSPageDataObjectsTests
{
    
    @Test
    public void test() throws Exception
    {
        
    }
    
    public static class PSRegionCodeTest extends PSDataObjectTestCase<PSRegionCode> {

        @Override
        public PSRegionCode getObject() throws Exception
        {
            PSRegionCode code = new PSRegionCode();
            code.setTemplateCode("#region('' '' '' '' '')");
            return code;
        }
    }
    
    public static class PSRegionTest extends PSDataObjectTestCase<PSRegion> {

        @Override
        public PSRegion getObject() throws Exception
        {
            PSRegion region = new PSRegion();
            PSRegion subRegion = new PSRegion();
            subRegion.setRegionId("sub");
            region.setRegionId("adam");
            subRegion.setChildren(Arrays.<PSRegionNode>asList(new PSRegionCodeTest().getObject()));
            region.setChildren(Arrays.<PSRegionNode>asList(subRegion));
            return region;
        }
    
    }
    
    public static class PSWidgetItemTest extends PSDataObjectTestCase<PSWidgetItem> {

        @Override
        public PSWidgetItem getObject()
        {
            PSWidgetItem wi = new PSWidgetItem();
            wi.setDefinitionId("test");
            wi.setName("test");
            wi.setId("test");
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("list", asList("a","b","c"));
            props.put("number", "42");
            wi.setProperties(props);
            return wi;
        }
    
    }
    
    public static class PSWidgetPropertyTest extends PSDataObjectTestCase<PSWidgetProperty> {

        @Override
        public PSWidgetProperty getObject()
        {
        	PSWidgetProperty wp = new PSWidgetProperty();
        	wp.setName("jose");
        	wp.setValue("the best");
            return wp;
        }
    
    }
    
    public static class PSRegionWidgetsTest extends PSDataObjectTestCase<PSRegionWidgets> {

        @Override
        public PSRegionWidgets getObject()
        {
            PSRegionWidgets rw = new PSRegionWidgets();
            rw.setRegionId("test");
            rw.setWidgetItems(asList(new PSWidgetItemTest().getObject()));
            return rw;
        }
    
    }
    
    
    public static class PSPageTest extends PSDataObjectTestCase<PSPage> {

        @Override
        public PSPage getObject() throws Exception
        {
            PSPage page = new PSPage();
            page.setRegionBranches(new PSRegionBranchesTest().getObject());
            return page;
        }
        
        @Test
		public void testLog() throws Exception {
            log.debug(PSDataObjectTestUtils.doXmlSerialization(object).actualXml);
		}
    
    }
    
    public static class PSPageFullTest extends PSDataObjectTestCase<PSPage> {

        @Override
        public PSPage getObject() throws Exception
        {
            PSPage page = new PSPage();
            PSDataObjectTestUtils.fillObject(page);
            page.setTemplateId("blah");
            return page;
        }
        
        @Test
        public void testLog() throws Exception {
            log.debug(PSDataObjectTestUtils.doXmlSerialization(object).actualXml);
        }
    
    }
    
    public static class PSTemplateTest extends PSDataObjectTestCase<PSTemplate> {

        @Override
        public PSTemplate getObject() throws Exception
        {
        	PSTemplate template = new PSTemplate();
        	template.setRegionTree(new PSRegionTreeTest().getObject());
        	return template;
        }
        
        @Test
		public void testLog() throws Exception {
            log.debug(PSDataObjectTestUtils.doXmlSerialization(object).actualXml);
		}
    
    }
    
    public static class PSRegionTreeTest extends PSDataObjectTestCase<PSRegionTree> {

        @Override
        public PSRegionTree getObject() throws Exception
        {
        	PSRegionTree tree = new PSRegionTree();
        	tree.setRootRegion(new PSRegionTest().getObject());
        	tree.setRegionWidgets("rid", asList(new PSWidgetItemTest().getObject()));
        	return tree;
        }
    
    }
    
    public static class PSRegionBranchesTest extends PSDataObjectTestCase<PSRegionBranches> {

        @Override
        public PSRegionBranches getObject() throws Exception
        {
            PSRegionBranches branches = new PSRegionBranches();
            branches.setRegions(asList(new PSRegionTest().getObject()));
            branches.setRegionWidgets("rid", asList(new PSWidgetItemTest().getObject()));
            return branches;
        }
        
        @Test
        public void testRegionWidgetsSet() throws Exception
        {
            PSRegionBranches copy = getCopy();
            Set<PSRegionWidgets> os = object.getRegionWidgetAssociations();
            Set<PSRegionWidgets> cs = copy.getRegionWidgetAssociations();
            assertEquals(os.size(), cs.size());
            PSRegionWidgets o = os.iterator().next();
            PSRegionWidgets c = cs.iterator().next();
            assertEquals(o,c);
            assertEquals(c,o);
            assertEquals("Widget associations should be equal 1", cs, os);
            assertEquals("Widget associations should be equal 2", os, cs);
        }
    
    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPageDataObjectsTests.class);

}
