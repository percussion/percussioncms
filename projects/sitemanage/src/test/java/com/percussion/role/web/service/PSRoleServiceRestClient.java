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
