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

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
@Component
@Lazy
public class RoleTestAdaptor implements IRoleAdaptor {

	@Override
	public Role getRole(URI baseURI, String roleName) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public Role updateRole(URI baseURI, Role role) {
        return null;
    }

    @Override
    public Role createRole(URI baseURI, Role role) {
        return null;
    }

	@Override
	public void deleteRole(URI baseURI, String roleName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Role> findRoles(URI baseURI, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

}
