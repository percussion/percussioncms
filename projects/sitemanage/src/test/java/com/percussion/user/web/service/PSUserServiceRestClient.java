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
