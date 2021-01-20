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
package com.percussion.sitemanage.data;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import com.percussion.share.data.PSDataObjectTestCase;
import com.percussion.share.test.PSDataObjectTestUtils;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;

import org.junit.Test;

public class PSSiteDataObjectTests
{
    public static class PSSitePropertiesTest extends PSDataObjectTestCase<PSSiteProperties>
    {
        @Override
        public PSSiteProperties getObject() throws Exception
        {
            PSSiteProperties props = new PSSiteProperties();
            props.setId("0");
            props.setName("Percussion");
            props.setDescription("Percussion");
            props.setHomePageLinkText("Percussion Site");
            
            return props;
        }
    }

    public static class PSSectionNodeTest extends PSDataObjectTestCase<PSSectionNode> {

        @Override
        public PSSectionNode getObject() throws Exception
        {
            PSSectionNode node = new PSSectionNode();
            node.setId("0");
            PSSectionNode node1 = new PSSectionNode();
            PSSectionNode node2 = new PSSectionNode();
            node1.setId("1");
            node1.setTitle("node 1");
            node2.setId("2");
            node2.setTitle("node 2");
            node.setChildNodes(asList(node1,node2));
            node.setTitle("title root");
            return node;
        }
    
    }
    
    public static class PSSiteTest extends PSDataObjectTestCase<PSSite> {

        @Override
        public PSSite getObject() throws Exception
        {
            PSSite site = new PSSite();
            PSDataObjectTestUtils.fillObject(site);
            site.setFolderPath("blah");
            return site;
            
        }
        
        @Test
        public void testGetFolderPath() throws Exception
        {
            assertEquals("blah", object.getFolderPath());
        }
        
    }
    
    public static class PSSiteSectionTest extends PSDataObjectTestCase<PSSiteSection> {

        @Override
        public PSSiteSection getObject() throws Exception
        {
            PSSiteSection section = new PSSiteSection();
            PSDataObjectTestUtils.fillObject(section);
            section.setChildIds(asList("a","b"));
            return section;
        }
    
    }

    
    public static class PSSitePublishJobTest extends PSDataObjectTestCase<PSSitePublishJob> {

		@Override
		public PSSitePublishJob getObject() throws Exception {
			PSSitePublishJob job = new PSSitePublishJob();
			PSDataObjectTestUtils.fillObject(job); 
			job.setElapsedTime(42L); 
			job.setElapsedTime(3147L);
			job.setCompletedItems(0L);
			job.setTotalItems(487L); 
			job.setRemovedItems(57L);
			job.setFailedItems(13L); 
			return job;
		}
       	    	
    }
    
    public static class PSSitePublishItemTest extends PSDataObjectTestCase<PSSitePublishItem> {

		@Override
		public PSSitePublishItem getObject() throws Exception {
			PSSitePublishItem item = new PSSitePublishItem();
			PSDataObjectTestUtils.fillObject(item);
			item.setContentid(42L);
			item.setElapsedTime(487L);
			item.setItemStatusId(6783L);
			return item;
		}
    	
    }
    
    public static class PSSitePublishLogRequestTest extends PSDataObjectTestCase<PSSitePublishLogRequest> {

		/* (non-Javadoc)
		 * @see com.percussion.share.data.PSDataObjectTestCase#getObject()
		 */
		@Override
		public PSSitePublishLogRequest getObject() throws Exception {
			PSSitePublishLogRequest request = new PSSitePublishLogRequest(); 
			request.setDays(3);
			request.setMaxcount(42);
			request.setShowOnlyFailures(true);
			request.setSkipCount(14);
			return request; 
		}
    	
    }
    
    public static class PSSitePublishLogDetailsRequestTest extends PSDataObjectTestCase<PSSitePublishLogDetailsRequest> {

		/* (non-Javadoc)
		 * @see com.percussion.share.data.PSDataObjectTestCase#getObject()
		 */
		@Override
		public PSSitePublishLogDetailsRequest getObject() throws Exception {
			PSSitePublishLogDetailsRequest request = new PSSitePublishLogDetailsRequest(); 
			request.setJobid(487L);
			request.setShowOnlyFailures(false);
			request.setSkipCount(14);
			return request; 
		}
    	
    }
    
    public static class PSUserTest extends PSDataObjectTestCase<PSUser>{

        @Override
        public PSUser getObject() throws Exception
        {
            PSUser user = new PSUser(); 
            user.setName("admin");
            user.setPassword("foo");
            user.setRoles(Collections.singletonList("bar")); 
            return user;
        }
        
    }
    
    public static class PSUserListTest extends PSDataObjectTestCase<PSUserList> {

        /* (non-Javadoc)
         * @see com.percussion.share.data.PSDataObjectTestCase#getObject()
         */
        @Override
        public PSUserList getObject() throws Exception
        {
            PSUserList userList = new PSUserList(); 
            List<String> users = userList.getUsers();
            users.add("a");
            users.add("b");
            users.add("c");
            return userList; 
        }        
    }
    
    public static class PSRoleListTest extends PSDataObjectTestCase<PSRoleList> {

        /* (non-Javadoc)
         * @see com.percussion.share.data.PSDataObjectTestCase#getObject()
         */
        @Override
        public PSRoleList getObject() throws Exception
        {
            PSRoleList roleList = new PSRoleList(); 
            List<String> roles = roleList.getRoles(); 
            roles.add("admin");
            roles.add("contributor"); 
            roles.add("editor"); 
            return roleList;
        }
      
        
    }
}
