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
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.*;
import org.xml.sax.SAXException;

/**
 * Persist a single community definition including all role 
 * associations.
 */
@Entity
@DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSCommunity")
@Table(name = "RXCOMMUNITY")
public class PSCommunity implements Serializable, IPSCatalogSummary, 
   IPSCatalogItem, IPSCloneTuner
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -7790532918814938767L;

   /**
    * The unique community id, can only be initialized once.
    */
   @Id
   @Column(name = "COMMUNITYID", nullable = false)
   private long id = UNINITIALIZED_ID;

   /**
    * The object version.
    */
   @Version
   @Column(name = "VERSION")
   private Integer version;

   /**
    * The community name, never <code>null</code> or empty in correctly
    * initialized objects. Unique accross all other defined communities.
    */
   @Basic
   @Column(name = "NAME", nullable = false, unique = true, length = 50)
   private String name;

   /**
    * The community description, may be <code>null</code> or empty.
    */
   @Basic
   @Column(name = "DESCRITPION", nullable = true, length = 255)
   private String description;

   /**
    * A set with all associated roles, never <code>null</code>, may be empty.
    */
   @OneToMany(targetEntity = PSCommunityRoleAssociation.class, 
      cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
   @JoinColumn(name = "COMMUNITYID", insertable = false, updatable = false)
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "Community_Roles")
   @Fetch(FetchMode.SUBSELECT)
   private Set<PSCommunityRoleAssociation> roleAssociations = 
      new HashSet<PSCommunityRoleAssociation>();

   /**
    * Default constructor should only be used for serialization.
    */
   public PSCommunity()
   {
   }

   /**
    * Construct a new community for the supplied parameters.
    * 
    * @param name the name of the new community, not <code>null</code> or
    *    empty.
    * @param description the description for the new community, may be
    *    <code>null</code> or empty.
    */
   public PSCommunity(String name, String description)
   {
      setId(PSGuidHelper.generateNextLong(PSTypeEnum.COMMUNITY_DEF));
      setName(name);
      setDescription(description);
   }
   
   /**
    * Performs a shallow copy, merging entries from the supplied source, 
    * ignores id and version.
    * 
    * @param source the community to merge with, not <code>null</code>.
    */
   public void merge(PSCommunity source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      setName(source.getName());
      setDescription(source.getDescription());
      setRoleAssociations(source.getRoleAssociations());
   }

   /**
    * Get the uniqe id of this community.
    * 
    * @return the unique id.
    */
   @IPSXmlSerialization(suppress=true)
   public long getId()
   {
      return id;
   }

   /**
    * Set a new unique id for this community.
    * 
    * @param id the new unique id, can only be set once, not changeable 
    *    afterwards.
    */
   public void setId(long id)
   {
      if (this.id != UNINITIALIZED_ID)
         throw new IllegalStateException("cannot change the unique id"); 

      this.id = id;
   }

   /**
    * Get the object version.
    * 
    * @return the object version, <code>null</code> if not initialized yet.
    */
   public Integer getVersion()
   {
      return version;
   }

   /**
    * Set the object version. The version can only be set once in the life cycle
    * of this object.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("version can only be initialized once");

      if (version != null && version.intValue() < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.version = version;
   }

   /**
    * Get the community name.
    * 
    * @return the community name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set a new community name.
    * 
    * @param name the new name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      this.name = name;
   }

   /**
    * Get the community description.
    * 
    * @return the community description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set a new community description.
    * 
    * @param description the new community description, may be 
    *    <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSCommunity))
         return false;
      
      if (this == b)
         return true;
      
      PSCommunity other = (PSCommunity) b;
      boolean isEquals = new EqualsBuilder()
         .append(id, other.id)
         .append(name, other.name)
         .append(description, other.description)
         .isEquals();
      
      // need to test entries "by hand" due to issues with proxied objects not
      // working as expected (hibnerate)
      if (isEquals)
      {
         Set<PSCommunityRoleAssociation> roles = 
            new HashSet<PSCommunityRoleAssociation>(roleAssociations);
         Set<PSCommunityRoleAssociation> otherRoles = 
            new HashSet<PSCommunityRoleAssociation>(other.roleAssociations);
         isEquals = roles.equals(otherRoles);
      }
      
      return isEquals;
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
         .append(id)
         .append(name)
         .append(description)
         .append(roleAssociations)
         .toHashCode();
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.COMMUNITY_DEF, id);
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getType()
    */
   public PSTypeEnum getType()
   {
      return PSTypeEnum.COMMUNITY_DEF;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (id != UNINITIALIZED_ID)
         throw new IllegalStateException("cannot change the unique guid"); 

      id = newguid.longValue();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /**
    * Get all community role associations definied for this community. The
    * returned collection may be modified but no change to the underlying
    * association will be made until the corresponding
    * {@link #setRoleAssociations(Collection) method} is called with the new
    * data.
    * 
    * @return a collection with all defined community role associations, never
    *    <code>null</code>, may be empty.
    */
   public Collection<IPSGuid> getRoleAssociations()
   {
      Collection<IPSGuid> associations = new ArrayList<IPSGuid>();

      for (PSCommunityRoleAssociation a : roleAssociations)
         associations.add(new PSGuid(PSTypeEnum.ROLE, a.getRoleId()));

      return associations;
   }

   /**
    * Set the new role associations.
    * 
    * @param associations the new role associations for this community, not
    *    <code>null</code>, may be empty.
    */
   public void setRoleAssociations(Collection<IPSGuid> associations)
   {
      if (associations == null)
         throw new IllegalArgumentException("associations cannot be null");
      
      roleAssociations.clear();
      for (IPSGuid association : associations)
         addRoleAssociation(association);
   }

   /**
    * Add a single role association to the set, if the association already
    * exists this call will have no effect.
    * 
    * @param roleId the id of the role to associate with this community, not
    *    <code>null</code>.
    */
   public void addRoleAssociation(IPSGuid roleId)
   {
      if (roleId == null)
         throw new IllegalArgumentException("roleId cannot be null");

      roleAssociations.add(new PSCommunityRoleAssociation(
         new PSGuid(PSTypeEnum.COMMUNITY_DEF, id), roleId));
   }
   
   /**
    * Add a role for the serialization
    * @param rid an id that corresponds to a guid
    */
   public void addRole(long rid)
   {
      addRoleAssociation(new PSGuid(rid));
   } 
   
   /**
    * Get the roles for serialization
    * @return the roles for serialization, never <code>null</code> but may be
    * empty
    */
   public Collection<Long> getRoles()
   {
      Collection<IPSGuid> ras = getRoleAssociations();
      Collection<Long> rval = new ArrayList<Long>();
      for(IPSGuid ra : ras)
      {
         rval.add(ra.longValue());
      }
      return rval;
   }
   
   /**
    * Remove the association to the identified role. Does nothing if no
    * association exists for the supplied role id.
    * 
    * @param roleId the id of the role for which to remove the association,
    *    not <code>null</code>.
    */
   public void removeRoleAssociation(IPSGuid roleId)
   {
      if (roleId == null)
         throw new IllegalArgumentException("roleId cannot be null");
      
      for (PSCommunityRoleAssociation association : roleAssociations)
      {
         if (association.getRoleId() == roleId.longValue())
         {
            roleAssociations.remove(association);
            break;
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(java.lang.Object,
    * long)
    */
   public Object tuneClone(long newId)
   {
      id = newId;
      Iterator roleAssocs = roleAssociations.iterator();
      while (roleAssocs.hasNext())
      {
         PSCommunityRoleAssociation assoc = (PSCommunityRoleAssociation) roleAssocs
            .next();
         assoc.setCommunityId(id);
      }
      return this;
   }

   /**
    * Constant to indicate an id is not initialized or invalid.
    */
   public static final long UNINITIALIZED_ID = -1;
}
