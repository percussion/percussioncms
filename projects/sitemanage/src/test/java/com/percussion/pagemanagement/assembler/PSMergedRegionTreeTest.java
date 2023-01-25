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

import com.percussion.share.service.exception.PSDataServiceException;
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
    public void shouldOverrideTemplateRegionsWithPageWidgets() throws PSDataServiceException {
        
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
    public void shouldOverrideTemplateRegionsWithPageRegions() throws PSDataServiceException {
        
        mergedTree.merge(tree, branches);
        

        assertNotNull("Merged regions should have subregions for widget regions", mergedTree.getMergedRegionMap().get("A/e"));
        assertNotNull("Should have merged region", mergedTree.getMergedRegionMap().get("A/c"));

    }
    
    
    @Test
    public void shouldOverridePageRegionsWithTemplateWidgets() throws PSDataServiceException {
        
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
    public void shouldOverridePageWidgetsWithTemplateWidgets() throws PSDataServiceException {
        
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
