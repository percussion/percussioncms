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
package com.percussion.services.workflow.data;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Primary key for adhoc user info
 * 
 * @author dougrand
 */
@Embeddable
public class PSContentAdhocUserPK implements Serializable
{
   /**
    * The serial id, used for serialization to detect version changes
    */
   private static final long serialVersionUID = 1L;

   /**
    * The content id of the item
    */
   @Column(name = "CONTENTID", nullable = false)
   private int contentId;
   
   /**
    * The role id for the adhoc assignment
    */
   @Column(name = "ROLEID", nullable = false)
   private int roleId;
   
   /**
    * The user name, never <code>null</code> or empty after ctor
    */
   @Column(name = "USERNAME", nullable = false)
   private String user;
   
   /**
    * Empty ctor
    */
   public PSContentAdhocUserPK()
   {
      // Empty
   }
   
   /**
    * Ctor
    * @param contentid the content item's id. 
    * @param roleid the role id.
    * @param user the user name, never <code>null</code> or empty.
    */
   public PSContentAdhocUserPK(int contentid, int roleid, String user)
   {
      if (user == null || StringUtils.isBlank(user))
      {
         throw new IllegalArgumentException("user may not be null or empty");
      }
      this.user = user;
      this.roleId = roleid;
      this.contentId = contentid;
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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentAdhocUserPK)) return false;
      PSContentAdhocUserPK that = (PSContentAdhocUserPK) o;
      return getContentId() == that.getContentId() && getRoleId() == that.getRoleId() && Objects.equals(getUser(), that.getUser());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getContentId(), getRoleId(), getUser());
   }
}
