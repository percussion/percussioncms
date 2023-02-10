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
