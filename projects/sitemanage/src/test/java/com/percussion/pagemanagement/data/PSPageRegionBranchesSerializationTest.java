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
package com.percussion.pagemanagement.data;

import static com.percussion.share.test.PSDataObjectTestUtils.assertEqualsMethod;
import static com.percussion.share.test.PSDataObjectTestUtils.assertXmlSerialization;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.percussion.share.dao.PSSerializerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class PSPageRegionBranchesSerializationTest
{
    PSRegionBranches branches = new PSRegionBranches();

    @Test
    public void testSerialization() throws Exception
    {
        PSWidgetItem item =  new PSWidgetItem();
        item.setName("JB");
        branches.setRegionWidgets("test", asList(item));
        String s = PSSerializerUtils.marshal(branches);
        PSRegionBranches unmarshal = PSSerializerUtils.unmarshal(s, PSRegionBranches.class);
        assertNotNull(unmarshal);
        assertFalse(unmarshal.getRegionWidgetAssociations().isEmpty());
        log.debug(s);
    }
    
    @Test
    public void testSetRegionWidgets() throws Exception {
        String rid = "rid";
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("BLAH");
        
        branches.setRegionWidgets(rid, asList(wi));
        
        wi = new PSWidgetItem();
        wi.setDefinitionId("STUFF");
        
        branches.setRegionWidgets(rid, asList(wi));
        assertEquals(1, branches.getRegionWidgetAssociations().size());
    }
    
    
    
    @Test
    public void testPageSerialization() throws Exception {
        testSetRegionWidgets();
        PSPage page = new PSPage();
        page.setId("1000");
        page.setFolderPath("//folderpath");
        page.setName("Page Name");
        page.setTemplateId("2000");
        page.setLinkTitle("dummy");
        
        PSRegion overrideRegion = new PSRegion();
        overrideRegion.setRegionId("templateRegion");
        
        PSRegionCode code = new PSRegionCode();
        code.setTemplateCode("#region('' '' '' '' '')");
        PSRegion pageSubRegion = new PSRegion();
        
        pageSubRegion.setRegionId("rid");
        List<PSRegionNode> regionNodes = new ArrayList<PSRegionNode>();
        regionNodes.add(code);
        pageSubRegion.setChildren(regionNodes);
        
        overrideRegion.setChildren(Arrays.<PSRegionNode>asList(pageSubRegion));
        
        List<PSRegion> pageRegions = asList(overrideRegion);
        branches.setRegions(pageRegions);
        page.setRegionBranches(branches);
        String s = PSSerializerUtils.marshal(page);
        assertXmlSerialization(page);
        assertEqualsMethod(page);
        
        log.debug("\n" + s);
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPageRegionBranchesSerializationTest.class);
}
