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
package com.percussion.role.web.service;

import com.percussion.role.data.PSRole;
import com.percussion.role.service.IPSRoleService;
import com.percussion.share.data.PSStringWrapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.user.data.PSUserList;

public class PSRoleServiceRestClient extends PSObjectRestClient implements IPSRoleService
{

    private String path = "/Rhythmyx/services/rolemanagement/role";
        
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public PSRole create(PSRole role) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(), "create"), role, PSRole.class);
    }

    public void delete(PSStringWrapper strWrapper) throws PSDataServiceException
    {
        postObjectToPath(concatPath(getPath(), "delete"), strWrapper);
    }

    public PSRole find(PSStringWrapper strWrapper) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(), "find"), strWrapper, PSRole.class);
    }
    
    public PSRole update(PSRole role) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(),"update"), role, PSRole.class);
    }

    public PSUserList getAvailableUsers(PSRole role) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(), "availableUsers"), role, PSUserList.class);       
    }
    
    public void validateForDelete(PSRole role)
    {
        postObjectToPath(concatPath(getPath(), "validateForDelete"), role);
    }
    
    public void validateDeleteUsersFromRole(PSUserList userList)
    {
        postObjectToPath(concatPath(getPath(), "validateDeleteUsers"), userList);
    }

    @Override
    public String getUserHomepage() {
        return getObjectFromPath(concatPath(getPath(), "userhomepage"), String.class);
    }
    
}
