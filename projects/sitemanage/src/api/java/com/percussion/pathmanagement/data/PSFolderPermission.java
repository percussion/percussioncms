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
package com.percussion.pathmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.List;

/**
 * This class contains the permissions of a folder.
 *
 * @author yubingchen
 */
public class PSFolderPermission extends PSAbstractDataObject
{

    private static final long serialVersionUID = 1L;

    /**
     * A list of available access level. 
     *
     * @author yubingchen
     */
    public enum Access
    {
        /**
         * The ADMIN is the least restrictive access level. This also have READ and WRITE
         */
        ADMIN,
        
        /**
         * This is more restrictive than ADMIN, but less restrictive than READ. 
         * It also have READ permission. 
         */
        WRITE,
        
        /**
         * This is the most restrictive access level.
         */
        READ,

        /**
         * This is the most restrictive access level.
         */
        VIEW
    }
    
    /**
     * A list of principal types. 
     *
     * @author yubingchen
     */
    public enum PrincipalType
    {
        USER,
        ROLE
    }
    
    /**
     * A user or role that has ADMIN, READ or WRITE permission.
     *
     * @author yubingchen
     */
    public static class Principal extends PSAbstractDataObject
    {
        private static final long serialVersionUID = 1L;
        private PrincipalType type;
        private String name;
        
        public String getName()
        {
            return name;        
        }
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public PrincipalType getType()
        {
            return type;
        }
        
        public void setType(PrincipalType type)
        {
            this.type = type;
        }
    }
    
    private Access accessLevel = Access.ADMIN;
    
    private List<Principal> adminPrincipals;
    private List<Principal> writePrincipals;
    private List<Principal> readPrincipals;

    public List<Principal> getViewPrincipals() {
        return viewPrincipals;
    }

    public void setViewPrincipals(List<Principal> viewPrincipals) {
        this.viewPrincipals = viewPrincipals;
    }

    private List<Principal> viewPrincipals;

    /**
     * Gets the access level that is applied to unspecified principals.
     * It returns {@link Access#ADMIN} if it has not been set by 
     * {@link #setAccessLevel(Access)}.
     * 
     * @return the access level, never <code>null</code>.
     */
    public Access getAccessLevel()
    {
        return accessLevel;
    }
    
    /**
     * Sets the access level, that is applied to unspecified principals.
     * 
     * @param access the new access level
     */
    public void setAccessLevel(Access access)
    {
        accessLevel = access;
    }
    
    /**
     * Gets a list of principals that have ADMIN access. 
     * 
     * @return the list of principals, may be <code>null</code> or empty if
     * the list of principals that have ADMIN access is unknown.
     */
    public List<Principal> getAdminPrincipals()
    {
        return adminPrincipals;
    }
    
    /**
     * Sets a list of principals that have ADMIN access.
     * 
     * @param principals the new list of principals, it may be <code>null</code> or
     * empty.
     */
    public void setAdminPrincipals(List<Principal> principals)
    {
        adminPrincipals = principals;
    }
    
    /**
     * Gets a list of principals that have WRITE access.
     * 
     * @return the list of principals, it may be <code>null</code> or empty if
     * the list of subjects is unknown.
     */
    public List<Principal> getWritePrincipals()
    {
        return writePrincipals;
    }
    
    /**
     * Sets a list of principals that have WRITE access.
     * 
     * @param principals the new list of principals, it may be <code>null</code> or
     * empty.
     */
    public void setWritePrincipals(List<Principal> principals)
    {
        writePrincipals = principals;
    }
    
    /**
     * Gets a list of principals that have READ access.
     * 
     * @return the list of principals, it may be <code>null</code> or empty if
     * the list of subjects is unknown.
     */
    public List<Principal> getReadPrincipals()
    {
        return readPrincipals;
    }
    
    /**
     * Sets a list of principals that have READ access.
     * 
     * @param principals the new list of principals, it may be <code>null</code> or
     * empty.
     */
    public void setReadPrincipals(List<Principal> principals)
    {
        readPrincipals = principals;
    }
    
}
