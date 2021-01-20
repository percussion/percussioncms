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

import java.io.Serializable;

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
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
         ToStringStyle.MULTI_LINE_STYLE);
   }
}
