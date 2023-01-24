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
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSCommunityRoleAssociation{");
      sb.append("id=").append(id);
      sb.append(", roleName='").append(roleName).append('\'');
      sb.append('}');
      return sb.toString();
   }
}

