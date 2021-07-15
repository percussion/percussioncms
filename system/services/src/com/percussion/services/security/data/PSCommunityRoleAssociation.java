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
package com.percussion.services.security.data;

import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Persists an association between a community and a role.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSCommunityRoleAssociation")
@Table(name = "RXCOMMUNITYROLE")
public class PSCommunityRoleAssociation implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -7962199858797607290L;
   
   /**
    * The associations primary key, never <code>null</code>.
    */
   @EmbeddedId
   PSCommunityRoleAssociationPK id = new PSCommunityRoleAssociationPK();
   
   /**
    * The role name, may be <code>null</code> or empty, is not persisted.
    */
   @Transient
   private String roleName;

   /**
    * Default constructor should only be used for serialization.
    */
   public PSCommunityRoleAssociation()
   {
   }

   /**
    * Construct a new community role association for the supplied parameters.
    * 
    * @param communityId the community id of the new association, not
    *    <code>null</code>.
    * @param roleId the role id of the new association, not
    *    <code>null</code>.
    */
   public PSCommunityRoleAssociation(IPSGuid communityId, IPSGuid roleId)
   {
      if (communityId == null)
         throw new IllegalArgumentException("communityId cannot be null");
      
      if (roleId == null)
         throw new IllegalArgumentException("roleId cannot be null");
      
      id.setCommunityId(communityId.longValue());
      id.setRoleId(roleId.longValue());
   }
   
   /**
    * Get the community id.
    * 
    * @return the community id.
    */
   public long getCommunityId()
   {
      return id.getCommunityId();
   }
   
   /**
    * Set a new community id.
    * 
    * @param communityId the new community id.
    */
   public void setCommunityId(long communityId)
   {
      id.setCommunityId(communityId);
   }

   /**
    * Get the role id.
    * 
    * @return the role id.
    */
   public long getRoleId()
   {
      return id.getRoleId();
   }
   
   /**
    * Set a new role id.
    * 
    * @param roleId the new role id.
    */
   public void setRoleId(long roleId)
   {
      id.setRoleId(roleId);
   }
   
   /**
    * Get the role name.
    * 
    * @return the role name, may be <code>null</code> or empty.
    */
   public String getRoleName()
   {
      return roleName;
   }
   
   /**
    * Set a new role name.
    * 
    * @param roleName the new role name, may be <code>null</code> or empty.
    */
   public void setRoleName(String roleName)
   {
      this.roleName = roleName;
   }

   @Override
   public boolean equals(Object b)
   {
      EqualsBuilder eq = new EqualsBuilder();
      if (!(b instanceof PSCommunityRoleAssociation))
         return false;
      
      PSCommunityRoleAssociation other = (PSCommunityRoleAssociation) b;

      return eq.append(id, other.id).isEquals();
   }

   @Override
   public int hashCode()
   {
      if (id != null)
         return id.hashCode();
      else
         return 0;
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
         ToStringStyle.MULTI_LINE_STYLE);
   }
}

