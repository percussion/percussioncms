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
