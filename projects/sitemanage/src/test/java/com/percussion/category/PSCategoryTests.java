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

package com.percussion.category;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PSCategoryTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public PSCategoryTests(){}

    @Test
    @Ignore("TODO: Fix me.  This test will always fail as written.")
    public void testToJson(){
        PSCategory cat = new PSCategory();

        cat.setTitle("TEST");
        cat.setAllowedSites("TEST");

        List<PSCategoryNode> topnodes = new ArrayList<PSCategoryNode>();
        List<PSCategoryNode> childnodes = new ArrayList<PSCategoryNode>();

        PSCategoryNode childnode = new PSCategoryNode();
        childnode.setId("2");
        childnode.setCreatedBy("L2TEST_CREATED_BY");
        childnode.setSelectable(true);
        childnode.setChildNodes(null);
        childnode.setCreationDate(LocalDateTime.now());
        childnode.setTitle("L2TEST_TITLE");
        childnode.setDeleted(false);
        childnode.setOldId(null);
        childnode.setAllowedSites(null);
        childnode.setInitialViewCollapsed(true);
        childnode.setLastModifiedBy("L2TEST_MODIFIED_BY");
        childnode.setLastModifiedDate(LocalDateTime.now());
        childnodes.add(childnode);

        PSCategoryNode node = new PSCategoryNode();
        node.setId("1");
        node.setCreatedBy("L1TEST_CREATED_BY");
        node.setSelectable(true);
        node.setChildNodes(childnodes);
        node.setCreationDate(LocalDateTime.now());
        node.setTitle("L1TEST_TITLE");
        node.setDeleted(false);
        node.setOldId(null);
        node.setAllowedSites(null);
        node.setInitialViewCollapsed(true);
        node.setLastModifiedBy("L1TEST_MODIFIED_BY");
        node.setLastModifiedDate(LocalDateTime.now());
        topnodes.add(node);

        cat.setTopLevelNodes(topnodes);

        assertEquals("{}",cat.toJSON());
    }
}
