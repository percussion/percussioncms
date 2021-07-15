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
package com.percussion.pathmanagement.data;

import static java.util.Arrays.*;

import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.share.data.PSDataObjectTestCase;
import com.percussion.share.test.PSDataObjectTestUtils;

import java.util.ArrayList;
import java.util.Collections;

public class PSPathDataObjectsTests
{
    public static class PSPathItemTest extends PSDataObjectTestCase<PSPathItem> {

        @Override
        public PSPathItem getObject() throws Exception
        {
            PSPathItem pathItem = new PSPathItem();
            PSDataObjectTestUtils.fillObject(pathItem);
            pathItem.setCategory(IPSItemSummary.Category.PAGE);
            pathItem.setFolderPaths(asList("blah","stuff"));
            return pathItem;
        }
        
    }
    
    public static class PSItemSummaryTest extends PSDataObjectTestCase<PSDataItemSummary> {

        
        @Override
        public PSDataItemSummary getObject() throws Exception
        {
            PSDataItemSummary sum = new PSDataItemSummary();
            sum.setIcon("/Rx/image/icon.gif");
            sum.setFolderPaths(asList("//Sites/aaa"));
            sum.setCategory(IPSItemSummary.Category.PAGE);
            sum.setId("Adam-ID");
            sum.setName("percPage");
            sum.setType("percPage");
            return sum;
        }
    
    }
    
    public static class PSPathItemEmptyListTest extends PSDataObjectTestCase<PSPathItem> {

        @Override
        public PSPathItem getObject() throws Exception
        {
            PSPathItem pathItem = new PSPathItem();
            PSDataObjectTestUtils.fillObject(pathItem);
            pathItem.setFolderPaths(new ArrayList<String>());
            return pathItem;
        }
    }
    
    public static class PSFolderPropertiesTest extends PSDataObjectTestCase<PSFolderProperties> {
       
        @Override
        public PSFolderProperties getObject() throws Exception
        {
            PSFolderProperties props = new PSFolderProperties();
            props.setId("01-03-02");
            props.setName("Folder name");
            props.setPermission(createFolderPermission());
            
            return props;
        }
        
        private PSFolderPermission createFolderPermission()
        {
            PSFolderPermission permission = new PSFolderPermission();
            permission.setAccessLevel(PSFolderPermission.Access.WRITE);
            PSFolderPermission.Principal adminSub = new PSFolderPermission.Principal();
            adminSub.setName("editor1");
            adminSub.setType(PSFolderPermission.PrincipalType.USER);
            permission.setAdminPrincipals(Collections.singletonList(adminSub));
            return permission;
        }
    }
    
}
