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
package com.percussion.services.security.data;

import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.security.Principal;
import java.security.acl.Permission;
import java.util.*;


/**
 * Implementation of the interface {@link IPSAclEntry}
 * 
 * @created 08-Aug-2005 3:09:34 PM
 * @version 6.0
 */
@Entity
/*
 * The cache is disabled because it doubles the time to load them the first time
 * and doubles the time to load them on successive times. See the comment with
 * the cache entry for PSAclImpl for more details.
 */
@Table(name = "PSX_ACLENTRIES")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSAclEntryImpl")
public class PSAclEntryImpl implements IPSAclEntry
{
   private static final Logger log = LogManager.getLogger(PSAclEntryImpl.class);
   
   /**
    * Default ctor. Added to keep the serializers happy. Not recommended but if
    * used the object will be in an invalid state until
    * {@link #setPrincipal(Principal)} is called.
    */
   public PSAclEntryImpl()
   {
   }

   /**
    * Ctor taking the pricipal.
    * 
    * @param principal principal of the entry, must not be <code>null</code>.
    */
   public PSAclEntryImpl(Principal principal, PrincipalTypes principalType)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null");
      }
      if (principalType == null)
      {
         throw new IllegalArgumentException("principalType must not be null");
      }
      name = principal.getName();
      type = principalType.getOrdinal();
   }

   /**
    * Ctor taking the principal.
    * 
    * @param principal principal of the entry, must not be <code>null</code>.
    */
   public PSAclEntryImpl(IPSTypedPrincipal principal)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null");
      }
      name = principal.getName();
      type = principal.getPrincipalType().getOrdinal();

   }

   public PSAclEntryImpl(PSAclImpl acl, PSAclEntryImpl entryImpl)
   {
      this.name=entryImpl.getName();
      this.type=entryImpl.getType().getOrdinal();

      this.setAcl(acl);
      for (PSAccessLevelImpl permission : entryImpl.psPermissions)
      {  
         this.addPermission(new PSAccessLevelImpl(this,permission.getPermission()));
      }
   }

   /**
    * Get method for ACL Id. Mainly for serialization.
    * 
    * @return ACL Id greater than 0 for a valid object.
    */
   public long getAclId()
   {
      return acl != null ? acl.getId() : aclId;
   }

   public void setAclId(long aclId)
   {
      long current = getAclId();
      if (current == aclId)
         return;
      
      if (current != 0)
         throw new IllegalStateException("Cannot change the ACLId");
      
      this.aclId = aclId;
   }

   public void setId(long id)
   {
      if (this.id == id)
         return;
      if (this.id != 0)
         throw new IllegalStateException("Cannot change the Id");
      
      this.id = (id == -1 )? 0 : id;
   }
   
   /**
    * Get id of the ACL entry.
    * 
    * @return unique ideentifier for the entry. Not equal to
    * {@link #UNINITIALIZED_ID} for a valid object.
    */
   public long getId()
   {
      return id;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.security.IPSAclEntry#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name for the acl entry.
    * 
    * @param entryName the name to set, must not be <code>null</code> or empty.
    */
   public void setName(String entryName)
   {
      if (StringUtils.isBlank(entryName))
         throw new IllegalArgumentException(
            "entryName must not be null or empty");

      name = entryName;
   }

   /**
    * @see java.security.acl.AclEntry#setPrincipal(java.security.Principal)
    */
   public boolean setPrincipal(Principal user)
   {
      if (user == null)
      {
         throw new IllegalArgumentException("user must not be null");
      }
      boolean result = name == null;
      if (result)
         name = user.getName();
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.AclEntry#getPrincipal()
    */
   @IPSXmlSerialization(suppress = true)
   public Principal getPrincipal()
   {
      return new PSTypedPrincipal(name, PrincipalTypes.valueOf(type));
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclEntry#isUser()
    */
   public boolean isUser()
   {
      return ((PSTypedPrincipal) getPrincipal()).isUser();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAclEntry#isGroup()
    */
   public boolean isGroup()
   {
      return ((PSTypedPrincipal) getPrincipal()).isGroup();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAclEntry#isSystemEntry()
    */
   public boolean isSystemEntry()
   {
      return ((PSTypedPrincipal) getPrincipal()).isSystemEntry();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAclEntry#isSystemCommunity()
    */
   public boolean isSystemCommunity()
   {
      return ((PSTypedPrincipal) getPrincipal()).isSystemCommunity();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclEntry#isType(
    * com.percussion.security.acl.IPSAclEntry.ENTRY_TYPE)
    */
   public boolean isType(PrincipalTypes entryType)
   {
      return ((PSTypedPrincipal) getPrincipal()).isType(entryType);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclEntry#getType()
    */
   public PrincipalTypes getType()
   {
      return ((PSTypedPrincipal) getPrincipal()).getPrincipalType();
   }

   /**
    * Set the entry type.
    * 
    * @param entryType must be one of the {@link PrincipalTypes} values.
    */
   public void setType(PrincipalTypes entryType)
   {
      type = entryType.getOrdinal();
   }

   /**
    * Not supported. Always throws
    * {@link java.lang.UnsupportedOperationException}.
    * 
    * @see java.security.acl.AclEntry#setNegativePermissions()
    */
   public void setNegativePermissions()
   {
      throw new UnsupportedOperationException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclEntry#isCommunity()
    */
   public boolean isCommunity()
   {
      return ((PSTypedPrincipal) getPrincipal()).isCommunity();
   }

   /**
    * @see java.security.acl.AclEntry#isNegative()
    * 
    * @return <code>false</code> always.
    */
   public boolean isNegative()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.AclEntry#addPermission(
    * java.security.acl.Permission)
    */
   public boolean addPermission(Permission permission)
   {
      if (permission == null)
      {
         throw new IllegalArgumentException("permission must not be null");
      }

      if (checkPermission(permission))
         return false;

      PSAccessLevelImpl access = new PSAccessLevelImpl((PSPermissions) permission);
      access.setAclEntry(this);
      psPermissions.add(access);

      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAclEntry#addPermissions(java.security.acl.Permission[])
    */
   public void addPermissions(Permission[] perms)
   {
      if (perms == null)
      {
         throw new IllegalArgumentException("permissions must not be null");
      }

      for (Permission permission : perms)
         addPermission(permission);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclEntry#isRole()
    */
   public boolean isRole()
   {
      return ((PSTypedPrincipal) getPrincipal()).isRole();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.AclEntry#removePermission(
    * java.security.acl.Permission)
    */
   public boolean removePermission(Permission permission)
   {
      if (permission == null)
      {
         throw new IllegalArgumentException("permission must not be null");
      }

      PSAccessLevelImpl cur = findPermission(permission);
      if (cur == null)
         return false;

      psPermissions.remove(cur);

      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.AclEntry#checkPermission(
    * java.security.acl.Permission)
    */
   public boolean checkPermission(Permission permission)
   {
      if (permission == null)
      {
         throw new IllegalArgumentException("permission must not be null");
      }

      return (findPermission(permission) != null);
   }

   /**
    * Find the access level with the specified permission
    * 
    * @param permission The permission to check for, assumed not
    * <code>null</code>.
    * 
    * @return The matching object, or <code>null</code> if not found.
    */
   private PSAccessLevelImpl findPermission(Permission permission)
   {
      for (PSAccessLevelImpl level : psPermissions)
      {
         if (level.getPermission().equals(permission))
            return level;
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.AclEntry#permissions()
    */
   @SuppressWarnings(value =
   {
      "unchecked"
   })
   public Enumeration<Permission> permissions()
   {
      return new Enumeration<Permission>()
      {
         private Iterator it = psPermissions.iterator();

         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         public Permission nextElement()
         {
            return ((PSAccessLevelImpl) it.next()).getPermission();
         }
      };
   }

   /**
    * Overridden to suit our requirement.
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      
      StringBuilder sb = new StringBuilder();
      sb.append("Entry id=").append(this.getId()).append(" type=").append(PrincipalTypes.valueOf(type).toString())
      .append(" name=").append(this.getName());
      
      int count = 0;
      sb.append(" perms=[");
      for (PSAccessLevelImpl perms : psPermissions)
      {
         if (count++>0)
            sb.append(", ");
         sb.append(perms.getPermission().toString());
      }
      sb.append("]");
      return sb.toString();
   }

   /**
    * Returns the deep clone of this object. Caller must set the unique id of
    * the object before using the object.
    * 
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSAclEntryImpl clone = new PSAclEntryImpl(this.getAcl(),this);
      return clone;
   }

   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + type;
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSAclEntryImpl other = (PSAclEntryImpl) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      if (type != other.type)
         return false;
      return true;
   }

   /**
    * Strictly for serialization
    * 
    * @param access permission to set must not be <code>null</code>.
    */
   public void addPsPermission(PSAccessLevelImpl access)
   {
      if (access == null)
      {
         throw new IllegalArgumentException("access must not be null");
      }
      access.setAclEntry(this);
      psPermissions.add(access);
   }

   /**
    * Add access permission
    * @param access permission to set must not be <code>null</code>.
    */

   @IPSXmlSerialization(suppress = true)
   public void addPermission(PSAccessLevelImpl access)
   {
      addPsPermission( access);
   }

   
   /**
    * Strictly for serialization
    * 
    * @return set of access levels, never <code>null</code> may be empty.
    */
   public Collection<PSAccessLevelImpl> getPsPermissions()
   {
      return psPermissions;
   }

   /**
    * Returns all the permissions as a collection
    * 
    * @return set of access levels, never <code>null</code> may be empty.
    */

   @IPSXmlSerialization(suppress = true)
   public Collection<PSAccessLevelImpl> getPermissions()
   {
      return getPsPermissions();
   }

   /**
    * Always throws <code>UnsupportedOperationException</code>.
    * @param negative ignored.
    */
   @SuppressWarnings("unused")
   public void setIsNegative(boolean negative)
   {
      throw new UnsupportedOperationException();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.security.IPSAclEntry#isOwner()
    */
   public boolean isOwner()
   {
      for (PSAccessLevelImpl access : psPermissions)
      {
         if (access.getPermission().equals(PSPermissions.OWNER))
            return true;
      }
      return false;
   }

   /**
    * Mark this entry as owner (who can modify the ACL) or not. Nothing happens
    * is the entry is aleardy an owner (or not) and tried to set as owner (or
    * not) again.
    * 
    * @param isOwner <code>true</code> to set as owner and <code>false</code>
    * to rmove owner permission.
    * 
    */
   void setOwner(boolean isOwner)
   {
      if (isOwner)
      {
         if (!isOwner())
         {
            addPermission(PSPermissions.OWNER);
         }
      }
      else
      {
         for (PSAccessLevelImpl access : psPermissions)
         {
            if (access.getPermission() == PSPermissions.OWNER)
            {
               removePermission(access.getPermission());
               return;
            }
         }
      }
   }

   @IPSXmlSerialization(suppress = true)
   public PSAclImpl getAcl()
   {
      return acl;
   }

   public void setAcl(PSAclImpl acl)
   {
      if (acl==null) return;
      // if we are assigning the object for hibernate want to remove aclId used in
      // client and testing.
      aclId = 0;
      this.acl = acl;
   }

   /**
    * Performs a shallow copy, merging permissions from the supplied entry,
    * handling additions and deletes, ignores id and aclid.
    * 
    * @param srcEntry The source entry, may not be <code>null</code>.
    */
   public void merge(PSAclEntryImpl srcEntry)
   {
      if (srcEntry == null)
         throw new IllegalArgumentException("srcEntry may not be null");

      log.debug("Merging aclEntry name="+name+" type="+type);
      name = srcEntry.name;
      type = srcEntry.type;

    
      Set<Short> updatePer = new HashSet<>();
      
      for (PSAccessLevelImpl updateAccess : srcEntry.getPsPermissions())
      {
         updatePer.add(updateAccess.getPermission().getOrdinal());
         }
      
      
      HashMap<Short,PSAccessLevelImpl> curPer = new HashMap<>();
      HashSet<Short> newPer = new HashSet<>();
      
      for (PSAccessLevelImpl currAccess : getPsPermissions())
         {
         curPer.put(currAccess.getPermission().getOrdinal(), currAccess);
         }

      for (PSAccessLevelImpl newAccess : srcEntry.getPsPermissions())
      {
         newPer.add(newAccess.getPermission().getOrdinal());
         if (!curPer.containsKey(newAccess.getPermission().getOrdinal()));
      {
            addPsPermission(newAccess);
      }
   }

      curPer.keySet().removeAll(newPer);
      
      psPermissions.removeAll(curPer.values());
     
      
      }

  

   /* (non-Javadoc)
    * @see com.percussion.services.security.IPSAclEntry#clearPermissions()
    */
   public void clearPermissions()
   {
      psPermissions.clear();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAclEntry#getTypedPrincipal()
    */
   public IPSTypedPrincipal getTypedPrincipal()
   {
      return new PSTypedPrincipal(getName(), getType());
   }
   
   /**
    * The long version of the guid of the ACL.
    */
   @Id
   @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSGuidHibernateGenerator")
   @GeneratedValue(generator = "id")
   @Column(name = "ID", nullable = false)
   private long id;

   /**
    * Name of the ACL entry.
    */
   @Basic
   @Column(name = "NAME", nullable = false)
   private String name;

   /**
    * ACL entry type. Default is {@link PrincipalTypes#USER}.
    * 
    * @see #setType(PrincipalTypes)
    */
   @Basic
   @Column(name = "TYPE", nullable = false)
   private int type = PrincipalTypes.USER.getOrdinal();

   
   @ManyToOne(targetEntity = PSAclImpl.class, optional = false, fetch = FetchType.EAGER)
   @JoinColumns({
     @JoinColumn(name="ACLID", referencedColumnName="ID")
   })
   private PSAclImpl acl;
   
   // Only use for testing
   @Transient
   private long aclId;
   
   /**
    * Set of permissions for this ACL entry. never <code>null</code> may be
    * empty.
    * 
    * @see #getPsPermissions()
    * @see #permissions()
    */
   @OneToMany(targetEntity = PSAccessLevelImpl.class, cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.EAGER)
   @Cascade(
           {
                   org.hibernate.annotations.CascadeType.DELETE_ORPHAN
           })
   @JoinColumn(name = "ENTRYID", referencedColumnName = "ID", insertable = false, updatable = false)
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSAclEntry_Perms")
   @Fetch(FetchMode.SUBSELECT)
   private Set<PSAccessLevelImpl> psPermissions = new HashSet<>();

   /**
    * Constant to indicate an id is not initialized or invalid.
    */
   public static final long UNINITIALIZED_ID = -1;

   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("ps-permission", PSAccessLevelImpl.class);
      PSXmlSerializationHelper.addType("typed-principal", PSTypedPrincipal.class);
   }
}
