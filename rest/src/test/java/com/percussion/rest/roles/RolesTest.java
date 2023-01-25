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

package com.percussion.rest.roles;

import com.percussion.rest.MainTest;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertNotNull;

@Category(IntegrationTest.class)
public class RolesTest extends MainTest {


	@Test
	public void testneverNull(){
		Role r = new Role();
	
		assertNotNull("Should never be null", r.getDescription());
		assertNotNull("Should never be null", r.getName());
		assertNotNull("Should never be null", r.getDescription());
		assertNotNull("Should never be null", r.getUsers());
		
	}

}
