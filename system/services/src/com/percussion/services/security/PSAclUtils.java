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
package com.percussion.services.security;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class with various helper methods for working with
 * Acl's
 */
public class PSAclUtils
{

   /**
    * Enforce static access
    */
   private PSAclUtils()
   {
      
   }
   
   /**
    * Copies acl entries from a source acl to a target acl, will not
    * create duplicate entries. If the entry already exists the permissions
    * are changed to match the source.
    * @param source cannot be <code>null</code>.
    * @param target cannot be <code>null</code>.
    */
   public static void copyAclEntries(IPSAcl source, IPSAcl target)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null.");
      if (target == null)
         throw new IllegalArgumentException("target cannot be null.");
      Enumeration enumEntries = source.entries();
      while (enumEntries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
         IPSAclEntry tEntry = target.findEntry(new PSTypedPrincipal(entry
            .getName(), entry.getType()));
         if (tEntry != null)
         {
            // Already has the entry so just modify the
            // permissions
            tEntry.clearPermissions();
            Enumeration enumPerms = entry.permissions();
            while (enumPerms.hasMoreElements())
            {
               Permission perm = (Permission) enumPerms.nextElement();
               tEntry.addPermission(perm);
            }
         }
         else
         {
            // Need to add the entry
            IPSAclEntry newEntry = target
               .createEntry(entry.getTypedPrincipal());
            Enumeration enumPerms = entry.permissions();
            while (enumPerms.hasMoreElements())
            {
               Permission perm = (Permission) enumPerms.nextElement();
               newEntry.addPermission(perm);
            }
            try
            {
               target.addEntry(source.getFirstOwner(), newEntry);
            }
            catch (SecurityException e)
            {
               // Should not happen
            }
            catch (NotOwnerException e)
            {
               // Should not happen
            }
         }
      }
   }
   
   /**
    * Removes all but the system entries
    * @param acl cannot be <code>null</code>.
    * @param type the type of entry that should be removed
    * or <code>null</code> if all types should be removed. 
    */
   public static void removeAllEntries(IPSAcl acl, PrincipalTypes type)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl cannot be null.");
      List<IPSAclEntry> removeList = new ArrayList<>();
      Enumeration enumEntries = acl.entries();
      while (enumEntries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
         if (type != null && entry.getType() != type)
            continue;
         if (!entry.isSystemCommunity() && !entry.isSystemEntry())
            removeList.add(entry);
      }
      try
      {
         for (IPSAclEntry entry : removeList)
            acl.removeEntry(acl.getFirstOwner(), entry);
      }
      catch (SecurityException e)
      {
         //Should not happen
      }
      catch (NotOwnerException e)
      {
         //Should not happen
      }
   }
   
   /**
    * Determines if an acl entry contains the specified permission
    * 
    * @param entry the entry to check, cannot be <code>null</code>.
    * @param permission the permission to look for, cannot be <code>null</code>.
    * @return <code>true</code> if the permission exists on the entry.
    */
   public static boolean entryHasPermission(IPSAclEntry entry,
      PSPermissions permission)
   {
      if (null == entry)
      {
         throw new IllegalArgumentException("entry cannot be null");  
      }
      return entry.checkPermission(permission);
   }
   
   /**
    * Removes all community acl entries and set the runtime visible
    * permission on the SYSTEM_COMMUNITY entry.
    * @param acl the acl to which the entry modifications will
    * be made. Cannot be <code>null</code>.
    */
   public static void useSystemCommunityEntry(IPSAcl acl)
   {
      removeAllEntries(acl, PrincipalTypes.COMMUNITY);
      // Find the system community entry and set permission
      Enumeration enumEntries = acl.entries();
      while (enumEntries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
         if (entry.isSystemCommunity())
         {
            // Set permission
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
            break;
         }
      }
   }
   
   /**
    * Removes the runtime visible
    * permission on the SYSTEM_COMMUNITY entry.
    * @param acl the acl to which the entry modifications will
    * be made. Cannot be <code>null</code>.
    */
   public static void removeVisibilityOnSystemCommunityEntry(IPSAcl acl)
   {
      // Find the system community entry and remove permission
      Enumeration enumEntries = acl.entries();
      while (enumEntries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
         if (entry.isSystemCommunity())
         {
            // Set permission
            entry.removePermission(PSPermissions.RUNTIME_VISIBLE);
            break;
         }
      }
   }
   /**
    * Determine if the specified type may have an associated acl.
    * 
    * @param type The type to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it may have an acl, <code>false</code> if 
    * not.
    */
   public static boolean typeSupportsAcl(PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      return ms_aclTypes.contains(type);
   }
   
   /**
    * Determine if the specified type may have an associated acl with an entry
    * with {@link IPSAccessLevel.PERMISSION#RUNTIME_VISIBLE}. 
    * 
    * @param type The type to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it may have an acl with such a permission,
    * <code>false</code> if not.
    */
   public static boolean typeSupportsRuntimeVisble(PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      return ms_runtimeTypes.contains(type);
   }
   
   /**
    * Set of types that support acls, never <code>null</code>, or modified.
    */
   private static Set<PSTypeEnum> ms_aclTypes = new HashSet<>();
   
   /**
    * Set of types that support acls with an entry with 
    * {@link IPSAccessLevel.PERMISSION#RUNTIME_VISIBLE}, never 
    * <code>null</code>, or modified.
    */
   private static Set<PSTypeEnum> ms_runtimeTypes = new HashSet<>();
   
   static
   {
      ms_aclTypes.add(PSTypeEnum.ACTION);
      ms_aclTypes.add(PSTypeEnum.AUTO_TRANSLATIONS);
      ms_aclTypes.add(PSTypeEnum.COMMUNITY_DEF);
      ms_aclTypes.add(PSTypeEnum.CONFIGURATION);
      ms_aclTypes.add(PSTypeEnum.CONTENT_LIST);
      ms_aclTypes.add(PSTypeEnum.DISPLAY_FORMAT);
      ms_aclTypes.add(PSTypeEnum.ITEM_FILTER);
      ms_aclTypes.add(PSTypeEnum.ITEM_FILTER_RULE_DEF);
      ms_aclTypes.add(PSTypeEnum.KEYWORD_DEF);
      ms_aclTypes.add(PSTypeEnum.LOCALE);
      ms_aclTypes.add(PSTypeEnum.LOCATION_SCHEME);
      ms_aclTypes.add(PSTypeEnum.NODEDEF);
      ms_aclTypes.add(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
      ms_aclTypes.add(PSTypeEnum.ROLE);
      ms_aclTypes.add(PSTypeEnum.SEARCH_DEF);
      ms_aclTypes.add(PSTypeEnum.SHARED_PROPERTY);
      ms_aclTypes.add(PSTypeEnum.SITE);
      ms_aclTypes.add(PSTypeEnum.SLOT);
      ms_aclTypes.add(PSTypeEnum.TEMPLATE);
      ms_aclTypes.add(PSTypeEnum.VIEW_DEF);
      ms_aclTypes.add(PSTypeEnum.WORKFLOW);
      
      ms_runtimeTypes.add(PSTypeEnum.DISPLAY_FORMAT);
      ms_runtimeTypes.add(PSTypeEnum.SEARCH_DEF);
      ms_runtimeTypes.add(PSTypeEnum.VIEW_DEF);
      ms_runtimeTypes.add(PSTypeEnum.ACTION);
      ms_runtimeTypes.add(PSTypeEnum.NODEDEF);
      ms_runtimeTypes.add(PSTypeEnum.SITE);
      ms_runtimeTypes.add(PSTypeEnum.TEMPLATE);
      ms_runtimeTypes.add(PSTypeEnum.WORKFLOW);
   }
}
