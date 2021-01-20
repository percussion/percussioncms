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
package com.percussion.share.dao;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Access;
import com.percussion.pathmanagement.data.PSFolderPermission.Principal;
import com.percussion.pathmanagement.data.PSFolderPermission.PrincipalType;
import com.percussion.utils.types.PSPair;

import static com.percussion.role.service.IPSRoleService.ADMINISTRATOR_ROLE;
import static com.percussion.role.service.IPSRoleService.DESIGNER_ROLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class used to retrieve and store {@link PSFolderPermission} from {@link PSFolder}
 * 
 * @author yubingchen
 */
public class PSFolderPermissionUtils
{
    /**
     * The equivalent of ADMIN permission in legacy access term, ADMIN, WRITE & READ access
     */
    public static int ADMIN_ACCESS = PSObjectAclEntry.ACCESS_ADMIN | PSObjectAclEntry.ACCESS_WRITE | PSObjectAclEntry.ACCESS_READ;
    
    /**
     * The equivalent of WRITE permission in legacy access term, WRITE & READ access
     */
    public static int WRITE_ACCESS = PSObjectAclEntry.ACCESS_WRITE | PSObjectAclEntry.ACCESS_READ;
    
    /**
     * The equivalent of READ permission in legacy access term, READ access only
     */
    public static int READ_ACCESS = PSObjectAclEntry.ACCESS_READ;

    /**
     * Constant for role name Editor
     */
    public static String EDITOR_ROLE_NAME = "Editor";

    /**
     * The logger
     */
    public static Log log = LogFactory.getLog(PSFolderPermissionUtils.class);

    /**
     * Gets the permission for the specified folder.
     * 
     * @param folder the folder in question, not <code>null</code>.
     * 
     * @return the permission of the folder, never <code>null</code>.
     */
    public static PSFolderPermission getFolderPermission(PSFolder folder)
    {
        notNull(folder);
        
        PSFolderPermission permission = new PSFolderPermission();
        PSObjectAcl acl = folder.getAcl();
        setAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, DESIGNER_ROLE, ADMIN_ACCESS);
        PSObjectAclEntry aclEntry =  getVirtualAcl(acl);
        if (aclEntry != null)
        {
            if (aclEntry.hasAdminAccess())
                permission.setAccessLevel(PSFolderPermission.Access.ADMIN);
            else if (aclEntry.hasWriteAccess())
                permission.setAccessLevel(PSFolderPermission.Access.WRITE);
            else if (aclEntry.hasReadAccess())
                permission.setAccessLevel(PSFolderPermission.Access.READ);
        }
        
        List<Principal> adminPrincipals = new ArrayList<Principal>();
        List<Principal> readPrincipals = new ArrayList<Principal>();
        List<Principal> writePrincipals = new ArrayList<Principal>();
        
        // Walk the folder acl, adding users to the appropriate list
        Iterator<?> acls = acl.iterator();
        while (acls.hasNext())
        {
            PSObjectAclEntry entry = (PSObjectAclEntry) acls.next();
            
            if (entry.isUser())
            {
                Principal p = new Principal();
                p.setName(entry.getName());
                p.setType(PrincipalType.USER);
                if (entry.hasAdminAccess())
                {
                    adminPrincipals.add(p);                    
                }
                else if (entry.hasWriteAccess())
                {
                    writePrincipals.add(p);
                }
                else if (entry.hasReadAccess())
                {
                    readPrincipals.add(p);
                }
            }
        }
        
        if (!adminPrincipals.isEmpty())
        {
            // Add admin users
            permission.setAdminPrincipals(adminPrincipals);
        }
        
        if (!writePrincipals.isEmpty())
        {
            // Add write users
            permission.setWritePrincipals(writePrincipals);
        }
        
        if (!readPrincipals.isEmpty())
        {
            // Add read users
            permission.setReadPrincipals(readPrincipals);
        }
        
        return permission;
    }
    
    /**
     * Gets the access level of the specified folder for the specified user and roles.
     * It returns the access level of the virtual ACL entry if the folder does 
     * not contain an ACL entry with the specified user name. 
     * It defaults to {@link PSFolderPermission.Access#ADMIN} if the ACL is not 
     * defined in the given folder.  
     * 
     * @param folder the folder in question, never <code>null</code>.
     * @param userName the user name, may be <code>null</code> or empty.
     * @param roles the roles that the user is a member of, not <code>null</code>, but may be empty.
     * 
     * @return the access level described above, never <code>null</code>.
     */
    public static PSPair<PSFolderPermission.Access, Boolean> getUserAcl(PSFolder folder, String userName, Collection<String> roles)
    {
        notNull(folder);
        notNull(roles);

        PSObjectAcl folderAcl = folder.getAcl();
        
        if (folderAcl == null)
            return new PSPair<PSFolderPermission.Access, Boolean>(Access.ADMIN, false);
        
        boolean didAdd = ensureAdminAccess(folder);
            
        if (roles.contains(ADMINISTRATOR_ROLE))
            return new PSPair<PSFolderPermission.Access, Boolean>(Access.ADMIN, didAdd);       
        
        PSFolderPermission.Access userAcl = getUserAcl(folderAcl, userName);
        for (String role : roles)
        {
            PSFolderPermission.Access roleAcl = getRoleAcl(folderAcl, role);
            userAcl =  comparePermissions(roleAcl, userAcl);
        }
        
        if (userAcl != null)
        {
            return new PSPair<PSFolderPermission.Access, Boolean>(userAcl, didAdd);
        }
        
        PSObjectAclEntry virtualAcl = getVirtualAcl(folderAcl);
        userAcl = (virtualAcl == null) ? PSFolderPermission.Access.ADMIN : convertAcl(virtualAcl);
        
        return new PSPair<PSFolderPermission.Access, Boolean>(userAcl, didAdd);
    }
    
    /**
     * Compare the two permissions and return the one with greater access.  <code>null</code> entries
     * are not considered.
     * 
     * @param acl1 May be <code>null</code>.
     * @param acl2 May be <code>null</code>.
     * 
     * @return The greater permission, or <code>null</code> if both are <code>null</code>.  If either are <code>null</code>, the
     * other is returned.
     */
    private static Access comparePermissions(Access acl1, Access acl2)
    {
        if (acl1 == null)
            return acl2;
        
        if (acl2 == null)
            return acl1;
        
        if (acl1.equals(Access.ADMIN) || acl2.equals(Access.ADMIN))
            return Access.ADMIN;
        
        if (acl1.equals(Access.WRITE) || acl2.equals(Access.WRITE))
            return Access.ADMIN;
        
        return Access.READ;        
    }

    /**
     * Make sure the ACL contains an "Admin" role entry with ADMIN access, add "Designer" role entry w/ADMIN access if not there
     * 
     * @param folder The folder to check, not <code>null</code>.
     */
    public static boolean ensureAdminAccess(PSFolder folder)
    {
        Validate.notNull(folder);
        
        boolean isAdd = setAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, DESIGNER_ROLE, ADMIN_ACCESS);
        PSObjectAclEntry adminEntry = getAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, ADMINISTRATOR_ROLE);
        if (adminEntry == null)
        {
            log.debug("Folder (id=" + folder.getLocator().getId() + ", name=" + folder.getName() + ") does not have ACL entry with Admin role.");
        }
        else
        {
            notNull(adminEntry);
            isTrue(adminEntry.hasAdminAccess());
        }
        
        return isAdd;
    }
    
    /**
     * Sets the specified access level to the given folder.
     * This will set the access level for the given folder only and remove all other
     * permission properties, such as user or role permissions of the folder.
     * <p>
     * Note, The folder will always has an entry with ADMIN permission for the "Admin" and "Designer" roles. However, the role ACL entry
     * is not exposed in {@link PSFolderPermission} yet.
     * 
     * @param folder the folder, never <code>null</code>.
     * @param acl the new access level of the folder, never <code>null</code>
     */
    public static void setFolderPermission(PSFolder folder, PSFolderPermission.Access acl)
    {
        notNull(folder);
        notNull(acl);

        setFolderAccessLevel(folder, acl);
        removeNonVirtualAclEntries(folder.getAcl());
        setAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, ADMINISTRATOR_ROLE, ADMIN_ACCESS);
        setAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, DESIGNER_ROLE, ADMIN_ACCESS);
    }
    
    /**
     * Sets the specified access level and user permissions to the given folder.  This will set the access level and
     * user permissions for the given folder only and remove all other permission properties of the folder.
     * <p>
     * Note, The folder will always has an entry with ADMIN permission for "Admin" role. However, the role ACL entry
     * is not exposed in {@link PSFolderPermission} yet.
     * 
     * @param folder the folder, never <code>null</code>.
     * @param perm the new permissions of the folder, never <code>null</code>
     */
    public static void setFolderPermission(PSFolder folder, PSFolderPermission perm)
    {
        notNull(folder);
        notNull(perm);

        // Set folder access level
        setFolderPermission(folder, perm.getAccessLevel());
        
        PSObjectAcl acl = folder.getAcl();
                
        // Set the access level for individual users, if specified.  The order in which this is done is important, as it
        // ensures that each user will get the highest permission possible.
        
        // Set read users first
        setUserAcls(acl, perm.getReadPrincipals(), READ_ACCESS);
        
        // Next set write users
        setUserAcls(acl, perm.getWritePrincipals(), WRITE_ACCESS);
        
        // Finally, set admin users
        setUserAcls(acl, perm.getAdminPrincipals(), ADMIN_ACCESS);
    }        
    
    /**
     * Sets the access level of an ACL entry within the specified ACL.
     * The ACL entry will be added into the specified ACL if it does not exist;
     * otherwise the ACL entry will be updated with the specified permission. 
     * 
     * @param acl the ACL in question, which may or may not contain the specified ACL entry, 
     * assumed not <code>null</code>.
     * @param type the type of the ACL entry.
     * @param name the name of the ACL entry, assumed not blank.
     * @param permission the new access level of the ACL entry.
     * 
     *  @return <code>true</code> if the entry was added, <code>false</code> if it was already there.
     */
    private static boolean setAclEntry(PSObjectAcl acl, int type, String name, int permission)
    {
        boolean isAdd = false;
        PSObjectAclEntry aclEntry = getAclEntry(acl, type, name);
        if (aclEntry == null)
        {
            isAdd = true;
            aclEntry = new PSObjectAclEntry(type, name, permission);
        }
        aclEntry.setPermissions(permission);
        
        if (isAdd)
        {
            acl.add(aclEntry);
        }
        
        return isAdd;
    }
    
    /**
     * Sets the access level of an ACL entry within the specified ACL, where the type of the entry is
     * {@link PSObjectAclEntry#ACL_ENTRY_TYPE_USER} and the name of the entry is the specified user name. 
     * 
     * @param acl the ACL in question, assumed not <code>null</code>.
     * @param userName the name of the user in question, it may be <code>null</code> or empty.
     * @param permission the new access level for the user. 
     */
    private static void setUserAcl(PSObjectAcl acl, String userName, int permission)
    {
        setAclEntry(acl, PSObjectAclEntry.ACL_ENTRY_TYPE_USER, userName, permission);
    }
    
    /**
     * Sets the access level of ACL entries within the specified ACL, where the type of the entry is
     * {@link PSObjectAclEntry#ACL_ENTRY_TYPE_USER} and the name of the entry is specified in the given principals. 
     * 
     * @param acl the ACL in question, assumed not <code>null</code>.
     * @param users the list of principals in question, it may be <code>null</code> or empty.
     * @param permission the new access level for the users. 
     */
    private static void setUserAcls(PSObjectAcl acl, List<Principal> users, int permission)
    {
        if (users != null)
        {
            for (Principal user : users)
            {
                setUserAcl(acl, user.getName(), permission);
            }
        }
    }
    
    /**
     * Sets the specified access level to the given folder.
     * 
     * @param folder the folder, assumed not <code>null</code>.
     * @param acl the new access level of the folder, assumed not <code>null</code>
     */
    private static void setFolderAccessLevel(PSFolder folder, PSFolderPermission.Access acl)
    {
        int legacyAcl = ADMIN_ACCESS;
        int permValue = acl.ordinal();
        if (permValue == PSFolderPermission.Access.READ.ordinal())
            legacyAcl = READ_ACCESS;
        else if (permValue == PSFolderPermission.Access.WRITE.ordinal())
            legacyAcl = WRITE_ACCESS;
        
        setAclEntry(folder.getAcl(), PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL, PSObjectAclEntry.ACL_ENTRY_EVERYONE,
                legacyAcl);
    }

    /**
     * Gets the virtual ACL entry contained in the specified ACL.
     * 
     * @param acl the ACL that may contain a virtual entry, it may be <code>null</code>.
     * 
     * @return the virtual ACL entry. It may be <code>null</code> if the virtual ACL entry does not exist.
     */
    private static PSObjectAclEntry getVirtualAcl(PSObjectAcl acl)
    {
        return getAclEntry(acl, PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL, null);
    }

    /**
     * Converts the (legacy) ACL entry to the folder access level.
     * 
     * @param entry the (legacy) ACL entry, assumed not <code>null</code>.
     * 
     * @return the converted access level, defaults to {@link PSFolderPermission.Access#ADMIN} 
     * if the folder has no "admin", "read" or "write" access.
     */
    private static PSFolderPermission.Access convertAcl(PSObjectAclEntry entry)
    {
        if (entry.hasAdminAccess())
            return PSFolderPermission.Access.ADMIN;
        else if (entry.hasWriteAccess())
            return PSFolderPermission.Access.WRITE;
        else  if (entry.hasReadAccess())
            return PSFolderPermission.Access.READ;
        else
            return PSFolderPermission.Access.ADMIN; 
    }
    
    /**
     * Gets the access level of an ACL entry within the specified ACL, 
     * where the type of the entry is {@link PSObjectAclEntry#ACL_ENTRY_TYPE_USER} 
     * and the name of the entry is the specified user name. 
     * 
     * @param acl the ACL in question, assumed not <code>null</code>.
     * @param userName the name of the user in question, it may be <code>null</code> or empty. 
     * 
     * @return the access level described above. It may be <code>null</code> 
     * if such ACL entry does not exist in the given ACL.  
     */
    private static PSFolderPermission.Access getUserAcl(PSObjectAcl acl, String userName)
    {
        if (acl == null)
        {
            return PSFolderPermission.Access.ADMIN;
        }
        
        PSObjectAclEntry aclEntry = getUserAclEntry(acl, userName);
        
        return (aclEntry != null) ? convertAcl(aclEntry) : null;
    }

    
    /**
     * Gets the access level of an ACL entry within the specified ACL, 
     * where the type of the entry is {@link PSObjectAclEntry#ACL_ENTRY_TYPE_ROLE} 
     * and the name of the entry is the specified role name. 
     * 
     * @param acl the ACL in question, assumed not <code>null</code>.
     * @param roleName the name of the role in question, it may be <code>null</code> or empty. 
     * 
     * @return the access level described above. It may be <code>null</code> 
     * if such ACL entry does not exist in the given ACL.  
     */
    private static PSFolderPermission.Access getRoleAcl(PSObjectAcl acl, String roleName)
    {
        if (acl == null)
        {
            return PSFolderPermission.Access.ADMIN;
        }
        
        PSObjectAclEntry aclEntry = getAclEntry(acl, PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE, roleName);
        
        return (aclEntry != null) ? convertAcl(aclEntry) : null;
    }
    
    /**
     * Gets the ACL entry within the specified ACL, where the type of the entry is
     * {@link PSObjectAclEntry#ACL_ENTRY_TYPE_USER} and the name of the entry is the specified user name. 
     * 
     * @param acl the ACL in question, assumed not <code>null</code>.
     * @param userName the name of the user in question, it may be <code>null</code> or empty. 
     * 
     * @return the ACL entry described above. It may be <code>null</code> if such ACL entry does not exist in the given
     * ACL.  
     */
    private static PSObjectAclEntry getUserAclEntry(PSObjectAcl acl, String userName)
    {
        return getAclEntry(acl, PSObjectAclEntry.ACL_ENTRY_TYPE_USER, userName);
    }
    
    /**
     * Gets the specified ACL entry within the specified ACL. 
     * 
     * @param acl the ACL in question, it may be <code>null</code>.
     * @param type the type of the ACL entry in question, assumed it is one of the valid types.
     * @param name the name of the ACL entry in question, it may be <code>null</code> if it is ignored. 
     * 
     * @return the ACL entry described above. It may be <code>null</code> if such ACL entry 
     * does not exist in the given ACL.  
     */
    private static PSObjectAclEntry getAclEntry(PSObjectAcl acl, int type, String name)
    {
        if (acl == null)
            return null;

        Iterator<?> acls = acl.iterator();
        while (acls.hasNext())
        {
            PSObjectAclEntry entry = (PSObjectAclEntry) acls.next();
            
            if (entry.getType() == type && (name == null || entry.getName().equalsIgnoreCase(name)))
            {
                return entry;
            }
        }                
        
        return null;        
    }
    
    /**
     * Removes all non-virtual ACL entries from the specified ACL.
     * @param acl the ACL in question, it may be <code>null</code>.
     */
    private static void removeNonVirtualAclEntries(PSObjectAcl acl)
    {
        if (acl == null)
            return;
        
        List<PSObjectAclEntry> entries = new ArrayList<PSObjectAclEntry>();
        Iterator<?> acls = acl.iterator();
        while (acls.hasNext())
        {
            PSObjectAclEntry entry = (PSObjectAclEntry) acls.next();
            
            if (!entry.isVirtual())
                entries.add(entry);
        }
        if (!entries.isEmpty())
        {
            for (PSObjectAclEntry entry : entries)
                acl.remove(entry);
        }
    }
}
