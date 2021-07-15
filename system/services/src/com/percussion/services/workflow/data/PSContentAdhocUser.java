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
package com.percussion.services.workflow.data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Hibernate data object for adhoc information related to a content item, a
 * workflow role and a given user.
 * 
 * @author dougrand
 */
@Entity
@Table(name = "CONTENTADHOCUSERS")
@IdClass(PSContentAdhocUserPK.class)
public class PSContentAdhocUser
{
   /**
    * The content id of the item
    */
   @Id
   @Column(name = "CONTENTID", nullable = false)
   private int contentId;

   /**
    * The role id for the adhoc assignment
    */
   @Id
   @Column(name = "ROLEID", nullable = false)
   private int roleId;

   /**
    * The user name, never <code>null</code> or empty after ctor
    */
   @Id
   @Column(name = "USERNAME", nullable = false)
   private String user;

   /**
    * The type of adhoc assignment
    */
   @Basic
   @Column(name = "ADHOCTYPE")
   private int adhocType;

   /**
    * Empty ctor
    */
   public PSContentAdhocUser() {
      // Empty
   }

   /**
    * Ctor
    * 
    * @param contentid the content item's id.
    * @param roleid the role id.
    * @param user the user name, never <code>null</code> or empty.
    * @param adhoctype the adhoc type
    */
   public PSContentAdhocUser(int contentid, int roleid, String user,
         int adhoctype) {
      if (user == null || StringUtils.isBlank(user))
      {
         throw new IllegalArgumentException("user may not be null or empty");
      }
      this.user = user;
      this.roleId = roleid;
      this.contentId = contentid;
      this.adhocType = adhoctype;
   }

   /**
    * @return the contentId
    */
   public int getContentId()
   {
      return contentId;
   }

   /**
    * @param contentId the contentId to set
    */
   public void setContentId(int contentId)
   {
      this.contentId = contentId;
   }

   /**
    * @return the roleId
    */
   public int getRoleId()
   {
      return roleId;
   }

   /**
    * @param roleId the roleId to set
    */
   public void setRoleId(int roleId)
   {
      this.roleId = roleId;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
   }

   /**
    * @return the adhocType
    */
   public int getAdhocType()
   {
      return adhocType;
   }

   /**
    * @param adhocType the adhocType to set
    */
   public void setAdhocType(int adhocType)
   {
      this.adhocType = adhocType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
}
