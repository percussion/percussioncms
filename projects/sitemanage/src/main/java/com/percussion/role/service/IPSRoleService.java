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
    /**
     * Administrator role.
     */
    public static final String ADMINISTRATOR_ROLE = "Admin";
    public static final String DESIGNER_ROLE = "Designer";
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
