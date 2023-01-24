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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single back-end subject
 * 
 * @author dougrand
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, region="PSBackEndSubject")
@NaturalIdCache
@Table(name = "PSX_SUBJECTS")
public class PSBackEndSubject
{
   /**
    * The ID of this subject
    */
   @Id
   @Column(name="ID")
   private int m_id;
   
   /**
    * The name of this subject
    */
   @Basic
   @NaturalId(mutable=true)
   @Column(name = "NAME", unique=true)
   private String name;
   
   /**
    * The type of this subject
    */
   @Basic
   @Column(name="TYPE")
   @NaturalId
   private int type;
   
   /**
    * The componentid of this subject
    */
   @Basic
   @Column(name="COMPONENTID")
   private int m_componentid;
   
   /**
    * The set of roles this subject is a member of, <code>null</code> until
    * loaded thru hibernate.
    */
   @ManyToMany(targetEntity = 
      PSBackEndRole.class, 
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
   @JoinTable(name = "PSX_ROLE_SUBJECTS", 
         joinColumns = {@JoinColumn(name = "SUBJECTID")}, 
         inverseJoinColumns = {@JoinColumn(name = "ROLEID")})
   //@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "Role_Subjects")
   private Set roles = null;
   
   /**
    * Default ctor
    */
   PSBackEndSubject()
   {
      // Empty
   }
 
   /**
    * @return Returns the componentid.
    */
   public int getComponentid()
   {
      return m_componentid;
   }
   /**
    * @param componentid The componentid to set.
    */
   public void setComponentid(int componentid)
   {
      m_componentid = componentid;
   }
   /**
    * @return Returns the id. 
    */
   public int getId()
   {
      return m_id;
   }
   /**
    * @param id The id to set.
    */
   public void setId(int id)
   {
      m_id = id;
   }
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   /**
    * @param subjectName The name to set.
    */
   public void setName(String subjectName)
   {
      name = subjectName;
   }
   /**
    * @return Returns the type.
    */
   public int getType()
   {
      return type;
   }
   /**
    * @param subjectType The type to set.
    */
   public void setType(int subjectType)
   {
      type = subjectType;
   }   
   
   /**
    * @return The roles related to this subject
    */
   public Set getRoles()
   {
      return roles;
   }

   /**
    * @param subjectRoles The roles to set.
    */
   public void setRoles(Set subjectRoles)
   {
      roles = subjectRoles;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSBackEndSubject)) return false;
      PSBackEndSubject that = (PSBackEndSubject) o;
      return type == that.type &&
              Objects.equals(name, that.name);
   }
   
   @Override
   public int hashCode() {
      return Objects.hash(name, type);
   }
}

