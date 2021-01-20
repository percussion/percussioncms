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
 * Class representing an Package Information object, used to save information 
 * regarding the "solution" package created or installed on a server.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSPkgElement")
@Table(name = "PSX_PKG_ELEMENT")
public class PSPkgElement implements Serializable
{
   public PSPkgElement()
   {
//      guid = 0;
//      packageGuid = 0;
//      objectGuid = 0;
 
   }
   
   //------------------------------------------------------------------------------  

   /**
    * Get the Unique Identifier for this Package Element (PSPkgElement) object.
    * 
    *  @returns The GUID for the Package Element object.
    */
   public IPSGuid getGuid()
   {
      return new PSGuid(PSTypeEnum.PACKAGE_ELEMENT,guid);
   }

   /**
    * Get the GUID for Package to which this Element belongs.
    * 
    *  @returns The GUID for the Package Info object.
    */
   public IPSGuid getPackageGuid()
   {
      return new PSGuid(PSTypeEnum.PACKAGE_INFO,packageGuid);
   }

   /**
    * Get the GUID for this Element (Design Object) contained in the Package.
    * 
    *  @returns The GUID for this Element of the Package.
    */
   public PSGuid getObjectGuid()
   {
      return new PSGuid(PSTypeEnum.valueOf(getObjectType()), objectGuid);
   }

   /**
    * Get the UUID for this Element (Design Object) contained in the Package.
    * 
    *  @returns The UUID for this Element of the Package.
    */
   public int getObjectUuid()
   {
      return objectUuid;
   }

   /**
    * Get the type (PSTypeEnum) for this Element (Design Object) contained in 
    * the Package.
    * 
    *  @returns The type for this Element of the Package.
    */
   public int getObjectType()
   {
      return objectType;
   }

   /**
    * Get the version for this Element (Design Object) contained in 
    * the Package.
    * 
    *  @returns The version for this Element of the Package.
    */
   public long getVersion()
   {
      return version;
   }
   
   //------------------------------------------------------------------------------  
   
   /**
    * Set the Package Element object's GUID.  See {@link #getGuid()}.
    * 
    * @param theGuid The GUID, may not be <code>null</code>.
    */
   public void setGuid(IPSGuid theGuid)
   {
      if (theGuid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      guid = theGuid.longValue();
   }
   
   /**
    * Set the GUID for Package to which this Element belongs.  
    *   See {@link #getPackageGuid()}.
    * 
    * @param theGuid The GUID, may not be <code>null</code>.
    */
   public void setPackageGuid(IPSGuid theGuid)
   {
      if (theGuid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      packageGuid = theGuid.longValue();
   }
   
   /**
    * Set the GUID for this Element (Design Object) contained in this Package.  
    *   See {@link #getObjectGuid()}.
    * 
    * @param theGuid The GUID, may not be <code>null</code>.
    */
   public void setObjectGuid(IPSGuid theGuid)
   {
      if (theGuid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      objectGuid = theGuid.toString();
      objectUuid = theGuid.getUUID();
      objectType = theGuid.getType();
   }
   
   /**
    * Set the UUID for this Element (Design Object) contained in this Package.  
    *   See {@link #getObjectUuid()}.
    * 
    * @param theGuid The GUID, may not be <code>null</code>.
    */
   @SuppressWarnings("unused")
   private void setObjectUuid(int theUuid)
   {
      objectUuid = theUuid;
   }
   
   /**
    * Set the Type for this Element (Design Object) contained in this Package.  
    *   See {@link #getObjectGuid()}.
    * 
    * @param theType The Type of the Design Object, may not be <code>null</code>.
    */
   @SuppressWarnings("unused")
   private void setObjectType(int theType)
   {
      objectType = theType;
   }
   
   /**
    * Set the version for this Element.  
    *   See {@link #getVersion()}.
    * 
    * @param theVersion The version.
    */
   public void setVersion(long theVersion)
   {
      version = theVersion;
   }
   
//------------------------------------------------------------------------------  

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSPkgElement))
         return false;
      PSPkgElement second = (PSPkgElement) b;
      return new EqualsBuilder()
         .append(guid, second.guid)
         .append(packageGuid, second.packageGuid)
         .append(objectGuid,  second.objectGuid)
         .append(objectUuid,  second.objectUuid)
         .append(objectType,  second.objectType)
         .append(version, second.version)
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
         .append(packageGuid)
         .append(objectGuid)
         .append(objectUuid)
         .append(objectType)
         .append(version)
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
   * GUID for this Package Element (PSPkgElement) object
   */
  @Id
  @Column(name = "GUID", nullable = false)
  private long guid;

  /**
   * Package (PSPkgInfo) GUID to which this Element (PSPkgElement) belongs.
   */
  @Column(name = "PACKAGE_GUID", nullable = false)
  private long packageGuid;

  /**
   * The Package Element object GUID. That is, the GUID of the Design Object 
   * * contained in the Package.
   */
 
  @Column(name = "OBJECT_GUID", nullable = false)
  private String objectGuid;

  /**
   * The Package Element UUID. That is, the UUID of the Design Object 
   * * contained in the Package.
   */
 
  @Column(name = "OBJECT_UUID", nullable = false)
  private int objectUuid;

  /**
   * The Package Element Type. That is, the object type (PSTypeEnum)
   *  of the Design Object contained in the Package.
   */
 
  @Column(name = "OBJECT_TYPE", nullable = false)
  private int objectType;

  /**
   * The Package Element Version. That is, the version of the Design Object
   * contained in the Package after last update via installation or
   * configuration.
   */
 
  @Column(name = "VERSION", nullable = false)
  private long version;
}
