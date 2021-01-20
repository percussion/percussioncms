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

import com.percussion.services.security.PSPermissions;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Implementation of the interface
 * {@link com.percussion.services.security.PSPermissions}
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
@Table(name = "PSX_ACLENTRYPERMISSIONS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSAccessLevelImpl")
public class PSAccessLevelImpl implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -2628619291732942498L;

   /**
    * Default ctor. The Acl entry id and permissions must be set using
    * {@link #setAclEntryId(long)} and {@link #setPermission(PSPermissions)}
    * methods for this object to be valid.
    */
   public PSAccessLevelImpl()
   {
   }

   /**
    * Ctor taking the entry id and permission.
    * 
    * @param perm permission value to set, must be one of the
    * {@link PSPermissions}.
    */
   public PSAccessLevelImpl(PSPermissions perm)
   {
      setPermission(perm);
   }

   public PSAccessLevelImpl(PSAclEntryImpl psAclEntryImpl, PSPermissions perm)
   {
         this.aclEntry = psAclEntryImpl;
         this.aclEntryId = this.aclEntry.getId();
         setPermission(perm);
   }

    @IPSXmlSerialization(suppress = true)
    public PSAclEntryImpl getAclEntry()
    {
        return aclEntry;
    }

   public void setAclEntry(PSAclEntryImpl aclEntry)
   {
      this.aclEntry = aclEntry;
   }

   @Transient
   private java.lang.Object __equalsCalc = null;

   @Override
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PSAccessLevelImpl)) return false;
        PSAccessLevelImpl other = (PSAccessLevelImpl) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = this.id == other.getId() &&
                this.getAclEntryId() == other.getAclEntryId() &&
                this.getPermission() == other.getPermission();
        __equalsCalc = null;
        return _equals;
    }

    @Transient
    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += new Long(getId()).hashCode();
        _hashCode += new Long(getAclEntryId()).hashCode();
        _hashCode += this.getPermission().hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    /**
    * Overridden to match our requirement.
    * 
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("Permission id=").append(this.getId()).append(" name=")
      
      .append(this.getPermission().toString())
      .append(" ordinal=").append(this.getPermission().getOrdinal());
     
      return sb.toString();
   }


   public void setId(long id)
   {

      this.id = id;
      
   }
   /**
    * Get the unique id of this object.
    * 
    * @return The id
    */
   public long getId()
   {
      return id;
   }


   /**
    * Get the id of the ACL entry this access level associated with.
    * 
    * @return always greater than 0 if the object is fully initialized.
    */
   public long getAclEntryId()
   {
       if(this.aclEntry != null) {
           this.aclEntryId = this.aclEntry.getId();
       }

        return this.aclEntryId;

   }


   /**
    * Set the ACL entry id for this access level. Throw exception if it was
    * already set previously.
    * 
    * @param entryid id to set must be greater than 0.
    */
   public void setAclEntryId(long entryid)
   {
      long current = getAclEntryId();
      if (current==entryid)
         return;
         
      if ( current != 0)
         throw new IllegalStateException("Cannot change the ACLEntryId");
  
      this.aclEntryId = entryid;
   }
   
   /**
    * Get the permission associated with this.
    */
   public PSPermissions getPermission()
   {
      return PSPermissions.valueOf(permission);
   }

   /**
    * Set the permission.
    * 
    * @param perm one of the {@link PSPermissions} values, must not be
    * <code>null</code>.
    */
   public void setPermission(PSPermissions perm)
   {
      if (perm == null)
      {
         throw new IllegalArgumentException("permission must not be null");
      }
      permission = perm.getOrdinal();
   }


   @Id
   @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSGuidHibernateGenerator")
   @GeneratedValue(generator = "id")
   @Column(name = "ID", nullable = false)
   private long id;


    @ManyToOne
    @JoinColumns({
            @JoinColumn(name="ENTRYID", referencedColumnName="ID")
    })
   private PSAclEntryImpl aclEntry;
  

   /**
    * Unique identifier of the ACL entry this object belongs to.
    */
   @Transient
   private long aclEntryId;
   
   /**
    * Permission value this object represents. Normally not <code>null</code>.
    * Initialized in ctor or in {@link #setPermission(PSPermissions)}.
    */
   @Basic
   @Column(name = "PERMISSION", nullable = false)
   protected int permission=PSPermissions.READ.getOrdinal();

}
