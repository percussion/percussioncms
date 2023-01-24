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
package com.percussion.pagemanagement.data;

import com.percussion.share.dao.PSSerializerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.percussion.share.test.PSDataObjectTestUtils.assertEqualsMethod;
import static com.percussion.share.test.PSDataObjectTestUtils.assertXmlSerialization;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class PSPageRegionBranchesSerializationTest
{


    @Test
    public void testSerialization() throws Exception
    {
        PSRegionBranches branches = new PSRegionBranches();
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
        PSRegionBranches branches = new PSRegionBranches();
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
        PSRegionBranches branches = new PSRegionBranches();
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
