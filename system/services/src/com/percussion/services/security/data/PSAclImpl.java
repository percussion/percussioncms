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

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.*;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.xml.sax.SAXException;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.*;

/**
 * Implementation of the interface
 * {@link com.percussion.services.security.IPSAcl}.
 * 
 * @created 08-Aug-2005 3:09:34 PM
 * @version 6.0
 */
@Entity
@Table(name="PSX_ACLS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSAclImpl")
public class PSAclImpl implements IPSAcl, IPSCatalogItem, IPSCatalogSummary
{
   
   private static final Logger log = LogManager.getLogger(PSAclImpl.class);
   
   static final long BIT32 = 0xFFFFFFFFL;
   
   /**
    * Default ctor. Added to keep serializers happy. Should not be used.
    */
   public PSAclImpl()
   {

   }

   /**
    * Ctor taking the owner of the ACL. ACL needs at least one owner during
    * construction time.
    * 
    * @param aclName name of the ACL, must not be <code>null</code> or empty.
    * @param owner the acl owner, must not be <code>null</code>.
    */
   public PSAclImpl(String aclName, AclEntry owner)
   {
      if (aclName == null || aclName.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      if (owner == null)
      {
         throw new IllegalArgumentException("owner must not be null"); 
      }
      name = aclName;
      ((PSAclEntryImpl) owner).setOwner(true);
      
      PSAclEntryImpl ownerEnt = (PSAclEntryImpl) owner;
      ownerEnt.setAcl(this);;
      entries.add(ownerEnt);
   }
   
   public PSAclImpl(PSAclImpl source)
   {
      this.name = source.name;
      this.objectId = source.objectId;
      this.objectType = source.objectType;
      this.description = source.description;
      this.m_version = source.m_version;
      for (IPSAclEntry entry : source.entries)
      {
         this.addEntry(new PSAclEntryImpl(this,(PSAclEntryImpl)entry));
      }
   }

   /**
    * Get ACL id.
    * 
    * @return Returns the id.
    */
   @IPSXmlSerialization(suppress = true) 
   public long getId()
   {
      updateGuid();
      return this.id;
   }


   /**
    * Set method only used for serialization, not part of public interface.
    * 
    * @param aclid The id to set.
    */
   public void setId(long aclid)
   {
      this.id = aclid;
      updateGuid();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Strictly for serialization
    * 
    * @param aclName name of the ACL to set
    */
   public void setName(String aclName)
   {
      name = aclName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#setName(java.security.Principal,
    * java.lang.String)
    */
   public void setName(Principal caller, String aclName) throws NotOwnerException
   {
      if (caller == null)
      {
         throw new IllegalArgumentException("caller must not be null"); 
      }
      if (aclName == null || aclName.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty"); 
      }
      if (!isOwner(caller))
      {
         throw new NotOwnerException();
      }
      name = aclName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAcl#getObjectId()
    */
   public long getObjectId()
   {
      return objectId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAcl#setObjectId(com.percussion.utils.guid.IPSGuid)
    */
   public void setObjectId(long objId)
   {
      // fix object ids set as guids to return only uuid part
      objectId = objId & BIT32;
   }

   /**
    * The returned description is internationalized based on the locale set for
    * this session.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return description == null ? "" : description; 
   }

   /**
    * Set the description of the Acl.
    * 
    * @param desc to set, May be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      description = desc == null ? "" : desc; 
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Owner#isOwner(java.security.Principal)
    */
   public boolean isOwner(Principal owner)
   {
      PSAclEntryImpl entry = (PSAclEntryImpl) findEntry(owner);
      if (entry == null)
      {
         throw new SecurityException("ACL does not contain the user " 
            + owner.getName());
      }
      return entry.isOwner();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Owner#addOwner(java.security.Principal,
    * java.security.Principal)
    */
   public boolean addOwner(Principal caller, Principal owner)
      throws NotOwnerException
   {
      if (!isOwner(caller))
      {
         throw new NotOwnerException();
      }
      PSAclEntryImpl entry = (PSAclEntryImpl) findEntry(owner);
      if (entry == null)
      {
         PrincipalTypes t = PrincipalTypes.USER;
         if(owner instanceof IPSTypedPrincipal)
            t = ((IPSTypedPrincipal)owner).getPrincipalType();

         // Entry with this name does not exist, create one
         entry = new PSAclEntryImpl(owner, t);
      }
      else if (isOwner(owner))
      {
         // Entry exists and is owner, so return false by contract
         return false;
      }
      entry.setOwner(true);
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Owner#deleteOwner(java.security.Principal,
    * java.security.Principal)
    */
   public boolean deleteOwner(Principal caller, Principal owner)
      throws NotOwnerException, LastOwnerException
   {
      if (caller == null)
      {
         throw new IllegalArgumentException("caller must not be null"); 
      }
      if (owner == null)
      {
         throw new IllegalArgumentException("owner must not be null"); 
      }
      if (!isOwner(caller))
      {
         throw new NotOwnerException();
      }
      if (!isOwner(owner))
      {
         return false;
      }
      int ownerCount = getOwnerCount();
      if (ownerCount == 1)
      {
         throw new LastOwnerException();
      }
      PSAclEntryImpl entry = (PSAclEntryImpl) findEntry(owner);
      if (entry == null)
         return false; // ? is it correct contract is not clear.
      entry.setOwner(false);
      return true;
   }
   

   /**
    * Resets the acl owner in the error case that acl has lost owner.
    * The code should be checked to see where it is being removed.
    */
   public void fixOwner()
   {
      IPSAclEntry defaultUser = null;

      // User config to set this,  may want to make "Admin" user be owner if no other in CMS
      String defaultOwnerUser = PSTypedPrincipal.DEFAULT_USER_ENTRY;

      for (IPSAclEntry entry : entries)
      {
         PSTypedPrincipal principal = (PSTypedPrincipal) entry.getPrincipal();
         
         if (principal.isUser())
         {
            if (principal.getName().equals(defaultOwnerUser))
            {
               defaultUser = entry;
            }
            if ( entry.isOwner())
            {
             
               if (!PSAclUtils.entryHasPermission(entry, 
                     PSPermissions.READ))
               {
                  entry.addPermission(PSPermissions.READ);
               }
           
               return;
            }
         }
      }
      if (defaultUser == null)
      {
          PSTypedPrincipal principal = new PSTypedPrincipal(defaultOwnerUser, PrincipalTypes.USER);
          defaultUser = new PSAclEntryImpl(principal);
          addEntry((PSAclEntryImpl)defaultUser);

      }

       defaultUser.addPermissions(new PSPermissions[]
       {
               PSPermissions.READ, PSPermissions.UPDATE,PSPermissions.DELETE,PSPermissions.OWNER
       });

      
      log.debug("Reset owner entry for acl " + this.id+ "for object "+this.getObjectGuid().toString());

      if (log.isDebugEnabled())
      {
          log.debug("stacktrace for owner reset ", new Throwable());

          String xml = "error";
         try
         {
            xml = this.toXML();
         }
         catch (IOException | SAXException e)
         {
            log.debug("Cannot create acl xml for debug ", e);
         }
         log.debug("stacktrace for owner xml " + xml);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#addEntry(java.security.Principal,
    * java.security.acl.AclEntry)
    */
   public boolean addEntry(Principal caller, AclEntry entry)
      throws NotOwnerException
   {
      if (caller == null)
      {
         throw new IllegalArgumentException("caller must not be null"); 
      }
      if (entry == null)
      {
         throw new IllegalArgumentException("entry must not be null"); 
      }
      if (!isOwner(caller))
      {
         throw new NotOwnerException();
      }
      if (findEntry(entry.getPrincipal()) != null)
         return false;
      PSAclEntryImpl psEntry = (PSAclEntryImpl) entry;
      psEntry.setAcl(this);
      entries.add(psEntry);
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#removeEntry(java.security.Principal,
    * java.security.acl.AclEntry)
    */
   public boolean removeEntry(Principal caller, AclEntry entry)
      throws NotOwnerException
   {
      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");
      
      if (!isOwner(caller))
      {
         throw new NotOwnerException();
      }
      if (isOwner(entry.getPrincipal()) && getOwnerCount() < 2)
      {
         throw new SecurityException(
            "This entry is the last owner of the ACL and cannot be removed"); 
      }
      AclEntry cur = findEntry(entry.getPrincipal());
      if (cur == null)
         return false;
      entries.remove(cur);
      return true;
   }
   
   /**
    * Remove the specified entry
    * 
    * @param entry The entry to remove, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it was removed, <code>false</code> if not
    * found.
    */
   public boolean removeEntry(PSAclEntryImpl entry)
   {
      
      AclEntry cur = findEntry(entry.getPrincipal());
      if (cur == null)
         return false;
      entries.remove(cur);
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#getPermissions(java.security.Principal)
    */
   public Enumeration<Permission> getPermissions(Principal user)
   {
      if (user == null)
      {
         throw new IllegalArgumentException("user must not be null"); 
      }
      AclEntry entry = findEntry(user);
      if (entry == null)
      {
         throw new SecurityException("ACL does not contain the user " 
            + user.getName());
      }
      return entry.permissions();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#entries()
    */
   public Enumeration<AclEntry> entries()
   {
      return new Enumeration<AclEntry>()
      {
         private Iterator<? extends AclEntry> it = entries.iterator();

         public boolean hasMoreElements()
         {
            return it.hasNext();
         }

         public AclEntry nextElement()
         {
            return it.next();
         }
      };
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Acl#checkPermission(java.security.Principal,
    * java.security.acl.Permission)
    */
   public boolean checkPermission(Principal principal, Permission permission)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null"); 
      }
      if (permission == null)
      {
         throw new IllegalArgumentException("permission must not be null"); 
      }
      AclEntry entry = findEntry(principal);
      if (entry == null)
      {
         throw new SecurityException("ACL does not contain the user " 
            + principal.getName());
      }
      return entry.checkPermission(permission);
   }

   /**
    * Finds and returns the acl entry whose principal matches with the supplied
    * principal. The match is found using
    * {@link Principal#equals(java.lang.Object)} method.
    * 
    * @param principal principal to find in the Access Control List, must not be
    * <code>null</code>.
    * @return acl matching acl entry if found, <code>null</code> otherwise.
    */
   public AclEntry findEntry(Principal principal)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null"); 
      }
      for (IPSAclEntry entry : entries)
      {
         if (entry.getPrincipal().equals(principal))
            return entry;
      }
      return null;
   }

   /**
    * @return the int value of the design object type.
    */
   public int getObjectType()
   {
      return objectType;
   }
   
   /**
    * Create a guid from the object type and id.
    * 
    * @return The guid, never <code>null</code>.
    */
   
   public IPSGuid getObjectGuid()
   {  
      return objectType > 0 ? new PSGuid(PSTypeEnum.valueOf(objectType), objectId): null;
   }      

   /**
    * @param objType the int value of the design object type to set.
    */
   public void setObjectType(int objType)
   {
      objectType = objType;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAclImpl)) return false;
      PSAclImpl psAcl = (PSAclImpl) o;
      return objectId == psAcl.objectId &&
              objectType == psAcl.objectType;
   }

   @Override
   public int hashCode() {
      return Objects.hash(objectId, objectType);
   }

   /**
    * Returns a string representation of the ACL contents.
    * 
    * @return a string representation of the ACL contents.
    */
   public String toString()
   {
      
      StringBuilder sb = new StringBuilder();
      sb.append("ACL id=").append(this.getId()).append(" guid=").append(this.getGUID())
      .append(" objectGuid=").append(this.getObjectGuid())
      .append(" objectType=").append(PSTypeEnum.valueOf(this.getObjectType()).toString())
      .append(" name=").append(this.name);
      
      for (IPSAclEntry entry : entries)
      {
         sb.append("\n   >");
         sb.append(entry.toString());
      }
      return sb.toString();
   } 

   /**
    * Finds the number of owners in the access control list.
    * 
    * @return owner count greater than or equal to 0.
    */
   private int getOwnerCount()
   {
      int count = 0;
      Iterator<? extends AclEntry> iter = entries.iterator();
      while (iter.hasNext())
      {
         AclEntry entry = iter.next();
         if (isOwner(entry.getPrincipal()))
            count++;

      }
      return count;
   }

   /**
    * Strictly for serialization. Do not use.
    * 
    * @param entry entry to set, must not be <code>null</code>.
    */
   public void addEntry(PSAclEntryImpl entry)
   {
      if (entry == null)
      {
         throw new IllegalArgumentException("entry must not be null"); 
      }
      entry.setAcl(this);
      entries.add(entry);
   }

   /**
    * Strictly for serialization. Do not use.
    * 
    * @return Acl entries, never <code>null</code> may be empty.
    */
   public Collection<IPSAclEntry> getEntries()
   {
      return entries;
   }
   
   /**
    * Performs deep merging maintaining existing objects and ids if
    * they already exist and removing entries that do not exist by name.
    * 
    * @param acl The source acl, may not be <code>null</code>.
    */
   public void merge(IPSAcl acl)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");
      PSAclImpl aclImpl = (PSAclImpl) acl;
      
      // first copy top level properties
      description = aclImpl.description;
      name = aclImpl.name;
      objectId = aclImpl.objectId;
      objectType = aclImpl.objectType;
      m_version = aclImpl.m_version;
      
      
      // now handle adds, removes, and updates of entries
      Set<String> matches = new HashSet<String>();
      Enumeration<AclEntry> entryEnum = acl.entries();
      while (entryEnum.hasMoreElements())
      {
         PSAclEntryImpl entryImpl = (PSAclEntryImpl) entryEnum.nextElement();
         matches.add(entryImpl.getName());
         IPSAclEntry tgtEntry = getEntry(entryImpl.getName());
         if (tgtEntry == null)
         {
            // treat as insert clone removes ids
            addEntry(new PSAclEntryImpl(this,entryImpl));
         }
         else
         {
            ((PSAclEntryImpl) tgtEntry).merge(entryImpl);
         }
      }
      
      // process deletes
      Iterator<IPSAclEntry> tgtEntries = entries.iterator();
      while (tgtEntries.hasNext())
      {
         IPSAclEntry tgtEntry = tgtEntries.next();
         if (!matches.contains(((PSAclEntryImpl)tgtEntry).getName()))
            tgtEntries.remove();
      }
   }   

   /**
    * Find the specified entry by id.
    * 
    * @param entryId The id
    * 
    * @return The entry or <code>null</code> if no match found.
    */
   private IPSAclEntry getEntry(String name)
   {
      for (IPSAclEntry entry : entries)
      {
         if (((PSAclEntryImpl)entry).getName().equals(name))
            return entry;
      }
      
      return null;
   }

   /**
    * support for serialization: add the collection of entries 
    * @param newEntries the collection of PSAclEntryImpl may or may not 
    * be <code>null</code>
    */
   public void setEntries(Collection<PSAclEntryImpl> newEntries)
   {
      if(newEntries==null) {
         entries = null;
      } 
      else 
      {
         if (entries == null)
            entries = new HashSet<IPSAclEntry>();
         for (PSAclEntryImpl ent : newEntries)
            addEntry(ent);
      }
      
   }
   
   
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);   
   }

   public IPSGuid getGUID()
   {
      updateGuid();
      return guid;
   }

   @Override
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {    
      guid = newguid;
      id = guid.longValue();
   }

   /**
    * Method to create a system ACL entries
    * 
    * @param isCommunity specify <code>true</code> if you want to create the
    * special entry for
    * {@link PSTypedPrincipal#ANY_COMMUNITY_ENTRY special community} or
    * specify <code>false</code> to create special user
    * {@link PSTypedPrincipal#DEFAULT_USER_ENTRY special user}.
    * @return new special acl entry. Never <code>null</code>. No entry id or
    * permissions set for the entry.
    */
   public IPSAclEntry createDefaultEntry(boolean isCommunity)
   {
      PrincipalTypes type = PrincipalTypes.USER;
      String entryName = PSTypedPrincipal.DEFAULT_USER_ENTRY;
      if (isCommunity)
      {
         type = PrincipalTypes.COMMUNITY;
         entryName = PSTypedPrincipal.ANY_COMMUNITY_ENTRY;
      }

      PSTypedPrincipal principal = new PSTypedPrincipal(entryName, type);
      return new PSAclEntryImpl(principal);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#createDefaultEntry(boolean,
    * com.percussion.services.security.PSPermissions[])
    */
   public IPSAclEntry createDefaultEntry(boolean isCommunity,
      PSPermissions[] permissions)
   {
      IPSAclEntry entry = createDefaultEntry(isCommunity);
      if(permissions!=null)
         entry.addPermissions(permissions);

      return entry;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.security.IPSAcl#getFirstOwner()
    */
   public IPSTypedPrincipal getFirstOwner() throws SecurityException
   {
      for (IPSAclEntry entry : entries)
      {
         if(entry.isOwner())
         {
            return new PSTypedPrincipal(entry.getPrincipal().getName(), entry
               .getType());
         }
      }
     throw new SecurityException("ACL has no entry with owner permission");
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#createEntry(java.lang.String,
    * com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes)
    */
   public IPSAclEntry createEntry(String principalName, PrincipalTypes principalType)
   {
      if (principalName == null || principalName.length() == 0)
      {
         throw new IllegalArgumentException("principalName must not be null or empty");
      }
      if (principalType == null)
      {
         throw new IllegalArgumentException("principalType must not be null");
      }
      return createEntry(new PSTypedPrincipal(principalName, principalType));
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#createEntry(java.lang.String,
    * com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes,
    * com.percussion.services.security.PSPermissions[])
    */
   public IPSAclEntry createEntry(String principalName, PrincipalTypes principalType,
      PSPermissions[] permissions)
   {
      if (principalName == null || principalName.length() == 0)
      {
         throw new IllegalArgumentException("principalName must not be null or empty");
      }
      if (principalType == null)
      {
         throw new IllegalArgumentException("principalType must not be null");
      }
      IPSAclEntry entry = createEntry(principalName, principalType);
      if (permissions != null)
         entry.addPermissions(permissions);

      return entry;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#createEntry(com.percussion.utils.security.IPSTypedPrincipal)
    */
   public IPSAclEntry createEntry(IPSTypedPrincipal principal)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null");
      }
      return new PSAclEntryImpl(principal);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#createEntry(com.percussion.utils.security.IPSTypedPrincipal,
    * com.percussion.services.security.PSPermissions[])
    */
   public IPSAclEntry createEntry(IPSTypedPrincipal principal,
      PSPermissions[] permissions)
   {
      if (principal == null)
      {
         throw new IllegalArgumentException("principal must not be null");
      }
      IPSAclEntry entry = createEntry(principal);
      if (permissions != null)
         entry.addPermissions(permissions);

      return entry;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.IPSAcl#findEntry(com.percussion.utils.security.IPSTypedPrincipal)
    */
   public IPSAclEntry findEntry(IPSTypedPrincipal principal)
   {
      for (IPSAclEntry entry : entries)
      {
         if (entry.getPrincipal().equals(principal))
            return entry;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.security.IPSAcl#findDefaultEntry(boolean)
    */
   public IPSAclEntry findDefaultEntry(boolean isCommunity)
   {
      return findEntry(createDefaultEntry(isCommunity).getTypedPrincipal());
   }
   
   
   public Integer getVersion()
   {
      return m_version;
   }

   public void setVersion(Integer version)
   {
      m_version = version;
   }
   
   /**
    * Implemented to satisfy implemented interface {@link IPSCatalogSummary}.
    * @see com.percussion.services.catalog.IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }
   
   private void updateGuid()
   {
      if (id!=0 && (guid==null || guid.longValue()!=this.id))
         guid = new PSGuid(PSTypeEnum.ACL, this.id);
   }

   /**
    * The long version of the guid of the ACL.
    */
   @Id
   @Column(name = "ID", unique = true, nullable = false)
   private long id;


   /**
    * The GUID of the design object this ACL belongs to. Never <code>null</code>
    * in a fully initialized object.
    * 
    * @see #getObjectId()
    * @see #setObjectId(long)
    */
   @Basic
   @NaturalId
   @Column(name = "OBJECTID", nullable = false)
   private long objectId = UNINITIALIZED_ID;

   /**
    * Design object type this ACL belongs to.
    */
   @Basic
   @NaturalId
   @Column(name = "OBJECTTYPE", nullable = false)
   private int objectType;
   
   /**
    * Name of the ACL, never <code>null</code> or empty in a valid or fully
    * initialized ACL object.
    * 
    * @see #setName(Principal, String)
    * @see #getName()
    */
   @Basic
   @Column(name = "NAME", nullable = false)
   private String name;



   /**
    * Optional description for the object.
    */
   @Basic
   @Column(name = "DESCRIPTION")
   private String description;

   /**
    * ACL entries in ACL object, never <code>null</code> may be empty.
    * 
    * @see #addEntry(Principal, AclEntry)
    * @see #entries()
    */


   @OneToMany(targetEntity = PSAclEntryImpl.class,
           cascade= CascadeType.ALL,
           fetch = FetchType.EAGER,
           orphanRemoval=true, mappedBy="acl")
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSAclImpl_Entries")
   @Fetch(FetchMode.SUBSELECT)
   private Set<IPSAclEntry> entries = new HashSet<IPSAclEntry>();

   /**
    * The hibernate object version
    */
   @Version
   @Column(name = "VERSION", nullable = false)
   private Integer m_version;

   /*
    * Used for local persistence of id and support of old code.  
    * Changing this does not change hibernate id.  Once hibernate persists
    * this item the guid will be updated with new value  Object Id and type
    * should be used to identify record.
    */
   @Transient
   private IPSGuid guid;

   /**
    * Constant to indicate an id is not initialized or invalid.
    */
   public static final long UNINITIALIZED_ID = -1;
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("entry", PSAclEntryImpl.class);
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
      PSAclImpl clone = new PSAclImpl(this);  
      return clone;
   }
   
   private static final long serialVersionUID = 42L;


}
