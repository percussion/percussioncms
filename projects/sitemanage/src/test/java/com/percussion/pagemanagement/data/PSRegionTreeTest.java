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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.pagemanagement.parser.PSParsedRegionTree;
import com.percussion.pagemanagement.parser.PSTemplateRegionParser;
import com.percussion.share.test.PSTestUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSRegionTreeTest
{
    @Test
    public void testInvalidTree()
    {
        PSRegion root = createInvalidRegionRoot();
        
        PSRegionTree tree = new PSRegionTree();
        tree.setRootRegion(root);
        
        try
        {
            tree.getDescendentRegions();
            fail("Invalid tree root");
        }
        catch (IllegalStateException e)
        {
        }
    }

    private PSRegion createInvalidRegionRoot()
    {
        List<PSRegionNode> children = new ArrayList<PSRegionNode>();
        
        PSRegion region = new PSRegion();
        region.setRegionId("region-1");
        children.add(region);
        
        region = new PSRegion();
        region.setRegionId("region-2");
        children.add(region);
        
        PSRegion root = new PSRegion();
        root.setRegionId("percRoot");
        root.setChildren(children);
        
        return root;
    }

    @Test
    public void testEmptyTree() 
    {
        PSRegionTree tree = new PSRegionTree();

        assertNull(tree.getRootRegion());
        assertNotNull(tree.getDescendentRegions());
        assertTrue(tree.getDescendentRegions().isEmpty());
        
        tree.setRootRegion(new PSRegion());
        
        assertNotNull(tree.getRootRegion());
        assertNotNull(tree.getDescendentRegions());
        assertTrue(tree.getDescendentRegions().isEmpty());
    }
    
    @Test
    public void testNonEmptyTree() throws Exception
    {
        PSRegionTree tree = loadRegionTree();
        PSRegion root = tree.getRootRegion();
        
        List<String> names = new ArrayList<String>();
        names.add("percRoot");
        names.addAll(Arrays.asList(nameChildren));
        List<String> regionNames = getRegionIds(root.getAllRegions());
        assertEquals(names, regionNames);

        names = Arrays.asList(nameChildren);
        regionNames = getRegionIds(tree.getDescendentRegions());
        assertEquals(names, regionNames);
    }

    private String[] nameChildren = new String[]{"container", "header", "middle", "leftsidebar", "content", "rightsidebar", "footer"};
    
    public static List<String> getRegionIds(List<PSRegion> regions)
    {
        List<String> result = new ArrayList<String>();
        for (PSRegion r : regions)
        {
            result.add(r.getRegionId());
        }
        return result;
    }
    
    public static PSRegionTree loadRegionTree()
    {
        PSRegionTree tree = new PSRegionTree();
        String markup = getMarkupText("PlainTemplateMarkup.vm");
        PSTemplateRegionParser parser = createRegionParser();
        PSParsedRegionTree<PSRegion, PSRegionCode> pt = parser.parse(markup);
        tree.setRootRegion(pt.getRootNode());
        return tree;
    }
    
    private static PSTemplateRegionParser createRegionParser()
    {
        Map<String, PSRegion> regions = new HashMap<String, PSRegion>();
        return new PSTemplateRegionParser(regions);        
    }
        
    private static String getMarkupText(String name) 
    {
        return PSTestUtils.resourceToString(PSRegionTreeTest.class, name);
    }
}
