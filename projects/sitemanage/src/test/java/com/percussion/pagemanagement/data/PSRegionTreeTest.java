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
