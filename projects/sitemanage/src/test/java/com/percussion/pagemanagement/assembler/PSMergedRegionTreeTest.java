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
package com.percussion.pagemanagement.assembler;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionNode;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSWidgetService;

import java.util.ArrayList;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description:
 * 
 * @author adamgent, Oct 29, 2009
 */
@RunWith(JMock.class)
public class PSMergedRegionTreeTest
{

    Mockery context = new JUnit4Mockery();

    private PSAbstractMergedRegionTree mergedTree;
    private PSRegionTree tree;
    private PSRegionBranches branches;  


    private IPSWidgetService widgetService;
    private PSWidgetItem widgetItem;
    private PSWidgetItem treeWidgetItem;

    @Before
    public void setUp() throws Exception
    {

        widgetService = context.mock(IPSWidgetService.class);
        mergedTree = new PSMergedRegionTree(widgetService);
        tree = new PSRegionTree();
        branches = new PSRegionBranches();
        PSRegion root = 
            r("A", asList(
                r("A/b", asList(
                        r("A/b/1", null),
                        c("test"),
                        r("A/b/2", null)
                        )),
                r("A/c", null)));
        PSRegion branch = 
            r("A/c", asList(
                    r("A/d", null),
                    r("A/e", null)));
        
        tree.setRootRegion(root);
        branches.setRegions(asList(branch));
        widgetItem = new PSWidgetItem();
        treeWidgetItem = new PSWidgetItem();
        widgetItem.setDefinitionId("page");
        treeWidgetItem.setDefinitionId("tree");
        
        
    }

    @Test
    public void shouldOverrideTemplateRegionsWithPageWidgets()
    {
        
        branches.setRegionWidgets("A/c", asList(widgetItem));

        context.checking(new Expectations() {{
            one(widgetService).load("page");
            will(returnValue(new PSWidgetDefinition()));
        }});


        mergedTree.merge(tree, branches);
        
        assertNull("Merged regions should not have subregions for widget regions", mergedTree.getMergedRegionMap().get("A/e"));
        assertNotNull("Should have merged region", mergedTree.getMergedRegionMap().get("A/c"));

    }
    
    
    @Test
    public void shouldOverrideTemplateRegionsWithPageRegions()
    {
        
        mergedTree.merge(tree, branches);
        

        assertNotNull("Merged regions should have subregions for widget regions", mergedTree.getMergedRegionMap().get("A/e"));
        assertNotNull("Should have merged region", mergedTree.getMergedRegionMap().get("A/c"));

    }
    
    
    @Test
    public void shouldOverridePageRegionsWithTemplateWidgets()
    {
        
        tree.setRegionWidgets("A/c", asList(treeWidgetItem));
        
        context.checking(new Expectations() {{
            one(widgetService).load("tree");
            will(returnValue(new PSWidgetDefinition()));
        }});
        
        mergedTree.merge(tree, branches);

        assertNull("Merged regions should not have subregions for widget regions", mergedTree.getMergedRegionMap().get("A/e"));
        assertNotNull("Should have merged region", mergedTree.getMergedRegionMap().get("A/c"));
        assertEquals(PSMergedRegionOwner.TEMPLATE, mergedTree.getMergedRegionMap().get("A/c").getOwner());
        
    }
    
    @Test
    public void shouldOverridePageWidgetsWithTemplateWidgets()
    {
        
        tree.setRegionWidgets("A/c", asList(treeWidgetItem));
        branches.setRegionWidgets("A/c", asList(widgetItem));
        
        widgetItem.setDefinitionId("tree");
        
        context.checking(new Expectations() {{
            one(widgetService).load("tree");
            will(returnValue(new PSWidgetDefinition()));
        }});
        

        mergedTree.merge(tree, branches);

        assertNull("Merged regions should not have subregions for widget regions", mergedTree.getMergedRegionMap().get("A/e"));
        assertNotNull("Should have merged region", mergedTree.getMergedRegionMap().get("A/c"));
        assertEquals(PSMergedRegionOwner.TEMPLATE, mergedTree.getMergedRegionMap().get("A/c").getOwner());
        
    }
    
    
    private PSRegionCode c(final String t) {
        PSRegionCode code = new PSRegionCode();
        code.setTemplateCode(t);
        return code;
    }
    
    @SuppressWarnings("serial")
    private PSRegion r(final String id, final Collection<? extends PSRegionNode> regions) {
   
        return new PSRegion() {{
            setRegionId(id);
            if (regions != null) {
                setChildren(new ArrayList<PSRegionNode>(regions));
            }
        }};
    }

}
