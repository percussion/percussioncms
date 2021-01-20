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

package com.percussion.apibridge;

import com.percussion.rest.roles.IRoleAdaptor;
import com.percussion.rest.roles.Role;
import com.percussion.role.data.PSRole;
import com.percussion.role.service.impl.PSRoleService;
import com.percussion.share.data.PSStringWrapper;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
@Lazy
public class RoleAdaptor implements IRoleAdaptor{

   @Autowired
   PSRoleService roleService;

	@Override
	public Role getRole(URI baseURI, String roleName) {
		Role ret = null;
        PSStringWrapper wrap = new PSStringWrapper();
        wrap.setValue(roleName);

        PSRole p_role = roleService.find(wrap);
		ret = ApiUtils.convertRole(p_role);

		return ret;
	}

	@Override
	public Role updateRole(URI baseURI, Role role) {
	    return ApiUtils.convertRole(roleService.update(ApiUtils.convertRole(role)));
	}

    @Override
    public Role createRole(URI baseURI, Role role) {
        return ApiUtils.convertRole(roleService.create(ApiUtils.convertRole(role)));
    }


    @Override
	public void deleteRole(URI baseURI, String roleName) {
		PSStringWrapper wrap = new PSStringWrapper(roleName);
	    roleService.delete(wrap);
	}

    @Override
    public List<Role> findRoles(URI baseURI, String pattern) {
	    List<String> roleList = roleService.getRoleMgr().getDefinedRoles();

	    ArrayList<Role> roles = new ArrayList();
        for(String s : roleList){
            roles.add(ApiUtils.convertRole(roleService.find(new PSStringWrapper(s))));
        }
        return roles;
	}


    public RoleAdaptor(){

	}

}
