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
package com.percussion.role.service;

import com.percussion.role.data.PSRole;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSStringWrapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.data.PSUserList;

/**
 * The role service is responsible for managing roles and their user associations.
 */
public interface IPSRoleService
{

    public static final String META_DATA_HOMEPAGE_PREFIX = "perc.role.homepage.";
    public static final String HOMEPAGE_TYPE_DASHBOARD = "Dashboard";
    public static final String HOMEPAGE_TYPE_EDITOR = "Editor";
    public static final String HOMEPAGE_TYPE_HOME = "Home";

    /**
     * Finds a role.
     * @param name of the role to find, wrapped by a {@link PSStringWrapper} object.  Never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSDataServiceException if the role cannot be found.
     */
    public PSRole find(PSStringWrapper name) throws PSDataServiceException;
    
    /**
     * Creates (and saves) a new role.  Adds specified users to the role.  Also adds the role to each workflow and each
     * workflow state as a Reader.
     * @param role never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSDataServiceException if the role cannot be created.
     */
    public PSRole create(PSRole role) throws PSDataServiceException; 
    
    /**
     * Updates the role with the given object.  All properties are overwritten except for the role name.
     * @param role the {@link PSRole#getName()} is used to determine which role to update.  Never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSDataServiceException if the role cannot be updated.
     */
    public PSRole update(PSRole role) throws PSDataServiceException;
    
    /**
     * Deletes the specified role.
     * @param name of the role to find, wrapped by a {@link PSStringWrapper} object.  Never <code>null</code>.
     * @throws PSDataServiceException if unable to delete the role.
     */
    public void delete(PSStringWrapper name) throws PSDataServiceException;
    
    /**
     * Finds the users which are currently not assigned to the specified role.
     * 
     * @param role never <code>null</code>.
     * @return list of users, sorted alphabetically (case-insensitive), never <code>null</code>.
     * @throws PSDataServiceException if unable to find the role.
     */
    public PSUserList getAvailableUsers(PSRole role) throws PSDataServiceException;
    
    /**
     * Validates that the specified role meets the following for deletion:
     * <br>
     * <li>
     * All users assigned to the role are also assigned to at least one other role.
     * </li>
     * <li>
     * The role is not being used by a workflow, i.e., it is not assigned permissions (other than READ) in a workflow.
     * </li>
     * 
     * @param role never <code>null</code>.  A role object is used instead of a string in order to support non-ascii
     * characters in the role name.
     * @throws PSDataServiceException with an appropriate message if the role does not meet the requirements.
     */
    public void validateForDelete(PSRole role) throws PSDataServiceException;
    
    /**
     * Validates the specified users for deletion from a role.  This checks to see if the users are in more than one
     * role.
     * 
     * @param userList list of user names, never <code>null</code>.
     * @throws PSDataServiceException with an appropriate message if there are users which are only in one role.
     */
    public void validateDeleteUsersFromRole(PSUserList userList) throws PSDataServiceException;
    
    /**
     * Gets the homepage for the logged in user.
     * @return String never <code>null</code>, if it is not set for any of the user roles, returns "Dashboard".
     */
    public String getUserHomepage() throws IPSGenericDao.LoadException;
}
