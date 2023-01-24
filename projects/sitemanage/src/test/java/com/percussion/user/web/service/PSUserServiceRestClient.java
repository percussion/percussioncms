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
package com.percussion.user.web.service;

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.user.data.*;
import com.percussion.user.service.IPSUserService;

import java.util.List;

public class PSUserServiceRestClient extends PSObjectRestClient implements IPSUserService
{

    private String path = "/Rhythmyx/services/user/user";
    
    
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public PSUser create(PSUser user) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(), "create"), user, PSUser.class);
    }

    @Override
    public void delete(String name)
    {
        super.delete(concatPath(getPath(), "delete", name));
    }

    public PSUser find(String name) throws PSDataServiceException
    {
        return getObjectFromPath(concatPath(getPath(), "find", name), PSUser.class);
    }
    
    public List<PSExternalUser> findUsersFromDirectoryService(String query) throws PSDirectoryServiceException
    {
        return getObjectsFromPath(concatPath(getPath(), "external/find", query), PSExternalUser.class);
    }

    public List<PSImportedUser> importDirectoryUsers(PSImportUsers importUsers) throws PSDirectoryServiceException
    {
        return postObjectToPathAndGetObjects(concatPath(getPath(), "import"), importUsers, PSImportedUser.class);
    }

    public PSDirectoryServiceStatus checkDirectoryService()
    {
        return getObjectFromPath(concatPath(getPath(), "external/status"), PSDirectoryServiceStatus.class);
    }

    public PSRoleList getRoles()
    {
        return getObjectFromPath(concatPath(getPath(), "roles"), PSRoleList.class);
    }

    public PSUserList getUsers() throws PSDataServiceException
    {
        return getObjectFromPath(concatPath(getPath(), "users"), PSUserList.class);
    }
    
    public PSUserList getUsersByRole(String roleName)
    {
        return getObjectFromPath(concatPath(getPath(), "usersByRole", roleName), PSUserList.class);
    }

    public PSUser update(PSUser user) throws PSDataServiceException
    {
        return postObjectToPath(concatPath(getPath(),"update"), user, PSUser.class);
    }
    
    public PSUser changePassword(PSUser user)
    {
        return putObjectToPath(concatPath(getPath(),"changepw"), user, PSUser.class);
    }

    public PSCurrentUser getCurrentUser()
    {
        return getObjectFromPath(concatPath(getPath(), "current"), PSCurrentUser.class);
    }

    public PSAccessLevel getAccessLevel(PSAccessLevelRequest request)
    {
        return postObjectToPath(concatPath(getPath(),"accessLevel"), request, PSAccessLevel.class);
    }
    
    /* (non-Javadoc)
     * @see com.percussion.user.service.IPSUserService#isAdminUser(java.lang.String)
     */
    @Override
    public boolean isAdminUser(String userName)
    {
        throw new UnsupportedOperationException("Checking if current user have Admin role is not yet supported");
    }

    @Override
    public PSUserList getUserNames(String nameFilter)
    {
        return getObjectFromPath(concatPath(getPath(), "users/names", nameFilter), PSUserList.class);
    }

    /* (non-Javadoc)
     * @see com.percussion.user.service.IPSUserService#isDesignUser(java.lang.String)
     */
    @Override
    public boolean isDesignUser(String userName)
    {
        throw new UnsupportedOperationException("Checking if current user has Design role is not yet supported");
    }

}
