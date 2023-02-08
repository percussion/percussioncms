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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The primary key class for the {@link PSCommunityRoleAssociation association}.
 */
@Embeddable
public class PSCommunityRoleAssociationPK implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1564678840850058923L;
   
   /**
    * The community id, part or the primary key.
    */
   @Column(name = "COMMUNITYID", nullable = false)
   private long communityId;
   
   /**
    * The role id, part of the primary key.
    */
   @Column(name = "ROLEID", nullable = false)
   private long roleId;
   
   /**
    * Get the community id.
    * 
    * @return the community id.
    */
   public long getCommunityId()
   {
      return communityId;
   }
   
   /**
    * Set a new community id.
    * 
    * @param communityId the new community id.
    */
   public void setCommunityId(long communityId)
   {
      this.communityId = communityId;
   }

   /**
    * Get the role id.
    * 
    * @return the role id.
    */
   public long getRoleId()
   {
      return roleId;
   }
   
   /**
    * Set a new role id.
    * 
    * @param roleId the new role id.
    */
   public void setRoleId(long roleId)
   {
      this.roleId = roleId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCommunityRoleAssociationPK)) return false;
      PSCommunityRoleAssociationPK that = (PSCommunityRoleAssociationPK) o;
      return getCommunityId() == that.getCommunityId() && getRoleId() == that.getRoleId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getCommunityId(), getRoleId());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSCommunityRoleAssociationPK{");
      sb.append("communityId=").append(communityId);
      sb.append(", roleId=").append(roleId);
      sb.append('}');
      return sb.toString();
   }
}
