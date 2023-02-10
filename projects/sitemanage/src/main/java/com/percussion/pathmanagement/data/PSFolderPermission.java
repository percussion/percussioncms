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
package com.percussion.pathmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.List;
import java.util.Objects;

/**
 * This class contains the permissions of a folder.
 *
 * @author yubingchen
 */
public class PSFolderPermission extends PSAbstractDataObject
{
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSFolderPermission)) return false;
        PSFolderPermission that = (PSFolderPermission) o;
        return getAccessLevel() == that.getAccessLevel() && Objects.equals(getAdminPrincipals(), that.getAdminPrincipals()) && Objects.equals(getWritePrincipals(), that.getWritePrincipals()) && Objects.equals(getReadPrincipals(), that.getReadPrincipals()) && Objects.equals(getViewPrincipals(), that.getViewPrincipals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccessLevel(), getAdminPrincipals(), getWritePrincipals(), getReadPrincipals(), getViewPrincipals());
    }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Principal)) return false;
            Principal principal = (Principal) o;
            return getType() == principal.getType() && Objects.equals(getName(), principal.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), getName());
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
