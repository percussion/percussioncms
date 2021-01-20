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

package com.percussion.services.pkginfo.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Class representing an Package Element dependency object, 
 * used to hold information dependencies between design objects in 
 * a "solution" package created or installed on a server.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSPkgElementDependency")
@Table(name = "PSX_PKG_ELEMENT_DEPEND")
public class PSPkgElementDependency implements Serializable
{

   /**
    * Get the GUID of the PSPkgElementDependency object
    * 
    * @return the guid. Never <code>null</code>.
    */
   public IPSGuid getGuid()
   {
      return new PSGuid(PSTypeEnum.PACKAGE_ELEMENT_DEPENDENCY, guid);
   }

   /**
    * Get the GUID to the Package that has the dependency
    * 
    * @return the ownerPackageGuid. Never <code>null</code>.
    */
   public IPSGuid getOwnerPackageGuid()
   {
      return new PSGuid(PSTypeEnum.PACKAGE_INFO, ownerPackageGuid);
   }

   /**
    * Get the GUID to the Package to which there is a dependency
    * 
    * @return the dependentPackageGuid. May be <code>null</code>.
    */
   public IPSGuid getDependentPackageGuid()
   {
      if(dependentPackageGuid == 0)
         return null;
      return new PSGuid(PSTypeEnum.PACKAGE_INFO, dependentPackageGuid);
   }

   /**
    * Set the GUID to the Element (Design Object) that has the dependency
    * 
    * @return the ownerElementGuid. Never <code>null</code>.
    */
   public IPSGuid getOwnerElementGuid()
   {
      return new PSGuid(ownerElementGuid);
   }

   /**
    * Set the GUID to the Element (Design Object) to which there is a dependency
    * 
    * @return the dependentElementGuid. May be <code>null</code>.
    */
   public PSGuid getDependentElementGuid()
   {
      return new PSGuid(dependentElementGuid);
   }

   //------------------------------------------------------------------------------  

   /**
    * Set the GUID of the PSPkgElementDependency object
    * 
    * @param guid the guid to set. Never <code>null</code>.
    */
   public void setGuid(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException(
               "GUID may not be null");
      this.guid = guid.longValue();
   }

   /**
    * Set the GUID to the Package that has the dependency
    * 
    * @param ownerPackageGuid the ownerPackageGuid to set. 
    * Never <code>null</code>.
    */
   public void setOwnerPackageGuid(IPSGuid ownerPackageGuid)
   {
      if (ownerPackageGuid == null)
         throw new IllegalArgumentException(
               "Owner Package GUID may not be null");

      this.ownerPackageGuid = ownerPackageGuid.longValue();
   }

   /**
    * Set the GUID to the Package to which there is a dependency
    * 
    * @param dependentPackageGuid the dependentPackageGuid to set.
    * Never <code>null</code>.
    */
   public void setDependentPackageGuid(IPSGuid dependentPackageGuid)
   {
      if (dependentPackageGuid == null)
         throw new IllegalArgumentException(
               "Dependent Package GUID may not be null");

      this.dependentPackageGuid = dependentPackageGuid.longValue();
   }

   /**
    * Set the GUID to the Element (Design Object) that has the dependency
    * 
    * @param ownerGuid the ownerElementGuid to set.
    * Never <code>null</code>.
    */
   public void setOwnerElementGuid(IPSGuid ownerGuid)
   {
      if (ownerGuid == null)
         throw new IllegalArgumentException("Owner GUID may not be null");

      this.ownerElementGuid = ownerGuid.toString();
   }

   /**
    * Set the GUID to the Element (Design Object) to which there is a dependency
    * 
    * @param dependentGuid the dependentElementGuid to set.
    * Never <code>null</code>.
    */
   public void setDependentElementGuid(IPSGuid dependentGuid)
   {
      if (dependentGuid == null)
         throw new IllegalArgumentException("Dependent GUID may not be null");

      this.dependentElementGuid = dependentGuid.toString();
   }
   
//------------------------------------------------------------------------------  

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSPkgElementDependency))
         return false;
      PSPkgElementDependency second = (PSPkgElementDependency) b;
      return new EqualsBuilder()
         .append(guid, second.guid)
         .append(ownerPackageGuid, second.ownerPackageGuid)
         .append(dependentPackageGuid,  second.dependentPackageGuid)
         .append(ownerElementGuid,  second.ownerElementGuid)
         .append(dependentElementGuid,  second.dependentElementGuid)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {

      return new HashCodeBuilder()
         .append(guid)
         .append(ownerPackageGuid)
         .append(dependentPackageGuid)
         .append(ownerElementGuid)
         .append(dependentElementGuid)
         .toHashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE).toString();
   }
   
//------------------------------------------------------------------------------  

   /**
    * This PSPkgElementDependency object's GUID
    */
   @Id
   @Column(name = "GUID", nullable = false)
   private long guid;
   
   /**
    * GUID of the Package who has the dependency.
    */
   @Column(name = "OWNER_PACKAGE_GUID", nullable = false)
   private long ownerPackageGuid;
   
   /**
    * GUID of the Package upon which there is a dependency.
    */
   @Column(name = "DEPEND_PACKAGE_GUID", nullable = true)
   private long dependentPackageGuid;
   
   /**
    * GUID of the Element (Design Object) who has the dependency.
    */
   @Column(name = "OWNER_ELEMENT_GUID", nullable = false)
   private String ownerElementGuid;
   
   /**
    * GUID of the Element (Design Object) upon which there is a dependency.
    */
   @Column(name = "DEPEND_ELEMENT_GUID", nullable = true)
   private String dependentElementGuid;


}