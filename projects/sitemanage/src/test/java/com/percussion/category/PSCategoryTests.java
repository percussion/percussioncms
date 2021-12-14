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
