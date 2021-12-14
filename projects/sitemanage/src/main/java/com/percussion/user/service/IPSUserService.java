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
package com.percussion.user.service;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.web.service.PSExceptionJaxbXmlAdapter;
import com.percussion.user.data.PSAccessLevel;
import com.percussion.user.data.PSAccessLevelRequest;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.data.PSExternalUser;
import com.percussion.user.data.PSImportedUser;
import com.percussion.user.data.PSRoleList;
import com.percussion.user.data.PSUser;
import com.percussion.user.data.PSUserList;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * The user data service is responsible for managing users and 
 * there role associations.
 * <p>
 * A User is required to be in at least one role.
 * 
 * @author DavidBenua
 * @author adamgent
 */
public interface IPSUserService
{

    /**
     * All the roles.
     * @return maybe empty never <code>null</code>
     * @throws PSDataServiceException
     */
    public PSRoleList getRoles() throws PSDataServiceException; 
    
    /**
     * All the users.
     * @return never <code>null</code>, maybe empty.
     * @throws PSDataServiceException
     */
    public PSUserList getUsers() throws PSDataServiceException;
    
    /**
     * All the users which are members of the specified role.
     * @param roleName never <code>null</code> or empty.
     * @return never <code>null</code>, maybe empty.
     * @throws PSDataServiceException
     */
    public PSUserList getUsersByRole(String roleName) throws PSDataServiceException;
    
    /**
     * Finds a user.
     * If the user is not found an exception is thrown.
     * @param name never <code>null</code> or empty.
     * @return never <code>null</code>.
     * @throws PSDataServiceException
     */
    public PSUser find(String name) throws PSDataServiceException;
    
    /**
     * Gets the current user from the callers request
     * that is in thread-local meta data.
     * @return never <code>null</code>.
     * @throws PSNoCurrentUserException if the callers thread-local does not have the user name available.
     */
    public PSCurrentUser getCurrentUser() throws PSNoCurrentUserException, PSDataServiceException;
    
    /**
     * Check if user has admin role.
     * @param userName
     * @return
     */
    public boolean isAdminUser(String userName);
    
    /**
     * Check if user has the Designer role.
     * 
     * @param userName The name of the user
     * 
     * @return <code>true</code> if a design user, <code>false</code> if not or if username is  <code>null<code/> or empty.
     */
    public boolean isDesignUser(String userName);
    
    /**
     * Creates the user.
     * {@link PSUser#getPassword()} must not be empty or <code>null</code>.
     * @param user
     * @return never <code>null</code>. The password property will always be <code>null</code> on the return object.
     * @throws PSDataServiceException
     */
    public PSUser create(PSUser user) throws PSDataServiceException; 
    
    /**
     * Updates the user with the given object.
     * @param user the {@link PSUser#getName()} is used to determine which user to update.
     * @return never <code>null</code>. The user object should have null on the password property.
     * @throws PSDataServiceException
     */
    public PSUser update(PSUser user) throws PSDataServiceException;

    /**
     * Change the user's password object.
     * @param user the {@link PSUser#getName()} is used to determine which user to update password for.
     * @return never <code>null</code>. The user object should have null on the password property.
     * @throws PSDataServiceException
     */
    public PSUser changePassword(PSUser user) throws PSDataServiceException;
    
    /**
     * Deletes the user with the given user name.
     * @param name never <code>null</code> or empty.
     * @throws PSDataServiceException if unable to delete the user.
     */
    public void delete(String name) throws PSDataServiceException;
    
    /**
     * Find external users from a directory service.
     * 
     * @param query similar to SQL LIKE expression where '%' is a wild card.
     * @return never <code>null</code>.
     * @throws PSDirectoryServiceException if the directory service is not enabled or is not available.
     */
    public List<PSExternalUser> findUsersFromDirectoryService(String query) throws PSDirectoryServiceException;
    
    /**
     * Imports external users usually from a directory service.
     * <p>
     * The users to import can usually be found by using: {@link #findUsersFromDirectoryService(String)}.
     * 
     * @param importUsers never <code>null</code>, maybe empty.
     * @return Imported users successful or not. The number of users returned should be the same number
     * of external users {@link PSImportUsers#getExternalUsers() passed in} even if some of the users failed to import.
     * See {@link PSImportedUser#getStatus()}.
     * @throws PSDirectoryServiceException if the directory service is not enabled or is not available.
     * @see #findUsersFromDirectoryService(String)
     */
    public List<PSImportedUser> importDirectoryUsers(PSImportUsers importUsers) throws PSDirectoryServiceException, PSSpringValidationException;
    
    
    /**
     * Checks the external directory service to see if its enabled, disabled, or error.
     * <p>
     * An exception should never be thrown from this method. Instead the
     * {@link PSDirectoryServiceStatus#getError()} will contain the exception.
     * @return never <code>null</code>.
     */
    public PSDirectoryServiceStatus checkDirectoryService();
    
    /**
     * Finds the access level of the current user.  The access level is the assignment type of the user for the initial
     * state of the workflow specified in the request.
     * 
     * @param request never <code>null</code>.
     * 
     * @return the access level, never <code>null</code>.
     */
    public PSAccessLevel getAccessLevel(PSAccessLevelRequest request);
    
    /**
     * 
     * A group of external users to import.
     * @author adamgent
     *
     */
    @XmlRootElement(name = "ImportUsers")
    @JsonRootName("ImportUsers")
    public static class PSImportUsers {
        
        @NotNull
        private List<PSExternalUser> externalUsers;

        public List<PSExternalUser> getExternalUsers()
        {
            return externalUsers;
        }

        public void setExternalUsers(List<PSExternalUser> externalUser)
        {
            this.externalUsers = externalUser;
        }
    }

    /**
     * 
     * A message object used to communicate the
     * status of an external directory service.
     * @author adamgent
     *
     */
    @XmlRootElement(name = "DirectoryServiceStatus")
    @JsonRootName("DirectoryServiceStatus")
    public static class PSDirectoryServiceStatus {
        
        private ServiceStatus status = ServiceStatus.DISABLED;
        private Throwable error;
        
        /**
         * The state of the directory service.
         * @return if not set the default is {@link ServiceStatus#DISABLED}, never <code>null</code>.
         */
        public ServiceStatus getStatus()
        {
            return status;
        }
        
        public void setStatus(ServiceStatus status)
        {
            this.status = status;
        }

        /**
         * The exception that was thrown either because
         * of {@link ServiceStatus#CONFIG_ERROR configuration error} or 
         * {@link ServiceStatus#CONNECTION_ERROR connection error}.
         * @return maybe <code>null</code>.
         */
        @XmlJavaTypeAdapter(PSExceptionJaxbXmlAdapter.class)
        public Throwable getError()
        {
            return error;
        }

        public void setError(Throwable error)
        {
            this.error = error;
        }

        /**
         * The varying status of a external directory service.
         * @author adamgent
         *
         */
        public static enum ServiceStatus {
            /**
             * The Directory service is not enabled
             * because either config is missing or it is disabled via config.
             */
            DISABLED,
            /**
             * Everything OK for directory service.
             */
            ENABLED,
            /**
             * Indicates a connection error with the directory service.
             */
            CONNECTION_ERROR,
            /**
             * Indicates that there is a configuration error.
             */
            CONFIG_ERROR,
            /**
             * Indicates something unpredictable.
             */
            UNKNOWN_ERROR;
        }
    }
    
    /**
     * Thrown if there is no current user in the executing thread.
     * @author adamgent
     *
     */
    public static class PSNoCurrentUserException extends PSDataServiceException
    {

        private static final long serialVersionUID = 1L;

        public PSNoCurrentUserException(String message)
        {
            super(message);
        }

        public PSNoCurrentUserException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSNoCurrentUserException(Throwable cause)
        {
            super(cause);
        }

    }
    
    /**
     * 
     * Indicates some failure with an external directory service
     * such as LDAP or Active Directory.
     * 
     * @author adamgent
     *
     */
    public static class PSDirectoryServiceException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;
        
        /**
         * Get a serializable object that indicates the status
         * of the directory service.
         * @return never <code>null</code>.
         */
        public PSDirectoryServiceStatus getStatus() {
            PSDirectoryServiceStatus d = new PSDirectoryServiceStatus();
            d.setError(this);
            d.setStatus(getStatusType());
            return d;
        }
        
        /**
         * The status of the directory service as a simple type.
         * <p>
         * This method is safe to override.
         * @return never <code>null</code>.
         */
        protected PSDirectoryServiceStatus.ServiceStatus getStatusType() {
            return PSDirectoryServiceStatus.ServiceStatus.UNKNOWN_ERROR;
        }

        public PSDirectoryServiceException(String message)
        {
            super(message);
        }

        public PSDirectoryServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSDirectoryServiceException(Throwable cause)
        {
            super(cause);
        }

    }
    
    /**
     * Indicates the directory service is disabled.
     * @author adamgent
     * @see PSDirectoryServiceStatus.ServiceStatus#DISABLED
     */
    public static class PSDirectoryServiceDisabledException extends PSDirectoryServiceException
    {

        private static final long serialVersionUID = 1L;

        @Override
        protected PSDirectoryServiceStatus.ServiceStatus getStatusType()
        {
            return PSDirectoryServiceStatus.ServiceStatus.DISABLED;
        }

        public PSDirectoryServiceDisabledException(String message)
        {
            super(message);
        }

        public PSDirectoryServiceDisabledException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSDirectoryServiceDisabledException(Throwable cause)
        {
            super(cause);
        }

    }
    
    /**
     * A configuration error with the directory service.
     * @author adamgent
     * @see PSDirectoryServiceStatus.ServiceStatus#CONFIG_ERROR
     */
    public static class PSDirectoryServiceConfigException extends PSDirectoryServiceException
    {

        private static final long serialVersionUID = 1L;

        
        @Override
        protected PSDirectoryServiceStatus.ServiceStatus getStatusType()
        {
            return PSDirectoryServiceStatus.ServiceStatus.CONFIG_ERROR;
        }

        public PSDirectoryServiceConfigException(String message)
        {
            super(message);
        }

        public PSDirectoryServiceConfigException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSDirectoryServiceConfigException(Throwable cause)
        {
            super(cause);
        }

    }
    
    /**
     * 
     * A connection error with the directory service.
     * 
     * @author adamgent
     * @see PSDirectoryServiceStatus.ServiceStatus#CONNECTION_ERROR
     */
    public static class PSDirectoryServiceConnectionException extends PSDirectoryServiceException
    {

        private static final long serialVersionUID = 1L;

        @Override
        protected PSDirectoryServiceStatus.ServiceStatus getStatusType()
        {
            return PSDirectoryServiceStatus.ServiceStatus.CONNECTION_ERROR;
        }

        public PSDirectoryServiceConnectionException(String message)
        {
            super(message);
        }

        public PSDirectoryServiceConnectionException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSDirectoryServiceConnectionException(Throwable cause)
        {
            super(cause);
        }

    }

    /**
     * Get the list of all user names matching the supplied filter
     * 
     * @param nameFilter The filter to match, <code>null</code> to find all user names, "%" and "_" sql wildcards are supported.
     * 
     * @return The list of names, not <code>null</code>, may be empty.
     * 
     * @throws PSDataServiceException
     */
    PSUserList getUserNames(String nameFilter) throws PSDataServiceException;
    
}
