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

package com.percussion.rest.users;


import com.percussion.rest.MainTest;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertNotNull;

@Category(IntegrationTest.class)
public class UsersTest extends MainTest{
    
	
	@Test
	public void testNeverNul(){
		User u = new User();
		
		assertNotNull("Should never be null",u.getBookmarkedPages());
		assertNotNull("Should never be null",u.getEmailAddress());
		assertNotNull("Should never be null",u.getFirstName());
		assertNotNull("Should never be null",u.getLastName());
		assertNotNull("Should never be null",u.getPersonalPage());
		assertNotNull("Should never be null",u.getPersonAssets());
		assertNotNull("Should never be null",u.getRecentAssetFolders());
		assertNotNull("Should never be null",u.getRecentAssetTypes());
		assertNotNull("Should never be null",u.getRecentPages());
		assertNotNull("Should never be null",u.getRecentSiteFolders());
		assertNotNull("Should never be null",u.getRoles());
		assertNotNull("Should never be null",u.getRecentTemplates());
		assertNotNull("Should never be null",u.getUserName());
		assertNotNull("Should never be null",u.getUserType());
		
	}

}
