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
import org.apache.commons.lang.StringUtils;
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
import java.util.Date;

/**
 * Class representing an Package Information object, used to save information 
 * regarding the "solution" package created or installed on a server.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSPkgInfo")
@Table(name = "PSX_PKG_INFO")
public class PSPkgInfo implements Serializable
{
   /**
    * The types of actions that can be taken with the associated package.
    * <p>
    * Implementation note - the enum names are stored in persistent storage,
    * so they cannot be changed.
    */
   public enum PackageAction
   {
      /**
       * A package is installed or a descriptor is saved.
       */
      INSTALL_CREATE, 
      
      /**
       * A package is uninstalled, meaning all the design objects in the package
       * are removed from the system (unless they have content dependencies.) If
       * a descriptor is removed, the pkgInfo entry is removed from persistent
       * storage.
       */
      UNINSTALL
   }
   
   /**
    * The results of the actions taken with the associated package.
    * <p>
    * Implementation note - the enum names are stored in persistent storage,
    * so they cannot be changed.
    * @see PSPkgInfo.PackageAction
    */
   public enum PackageActionStatus
   {
      /**
       * The action completed with no errors.
       */
      SUCCESS, 
      
      /**
       * An exception occurred during action processing.
       */
      FAIL
   }
   
   /**
    * The different categories of packages that can exist on a system. 
    * <p>
    * Implementation note - the enum names are stored in persistent storage,
    * so they cannot be changed.
    */
   public enum PackageType
   {
      /**
       * The entry identifies a package descriptor. There may or may not be an
       * actual package. In any case, this entry only describes the descriptor.
       */
      DESCRIPTOR,

      /**
       * This entry identifies a package that has been installed or uninstalled
       * on this server.
       */
      PACKAGE
   }
   
   /**
    * The different types of packages that can exist on a system. 
    * <p>
    * Implementation note - the enum names are stored in persistent storage,
    * so they cannot be changed.
    */
   public enum PackageCategory
   {
      /**
       * The entry identifies a system package. Packages of this type cannot be
       * uninstalled.
       */
      SYSTEM,
      
      /**
       * The entry identifies a user package. Packages of this type can be
       * uninstalled.
       */
      USER
   }

   /*
   * Constructor
   */
   public PSPkgInfo()
   {
      //
      guid = 0;
      descriptorName = "";
      descriptorGuid = 0;
      publisherName  = "";
      publisherUrl  = "";
      description = "";
      pkgVersion = "";
      shippedConfigDefinition = "";
      lastActionDate = new Date(0);
      origConfigDate = new Date(0);
      lastActionByUser = "";
      type = PackageType.DESCRIPTOR.name() + "_true";
      lastAction = PackageAction.INSTALL_CREATE.name();
      lastActionStatus = PackageActionStatus.FAIL.name();
      cmVersionMinimum = "";
      cmVersionMaximum = "";
      category = PackageCategory.USER.name();
   }

 //------------------------------------------------------------------------------  

   /**
    * Get the Unique Identifier for this object
    * 
    *  @returns The GUID for the Package Info object.
    */
   public IPSGuid getGuid()
   {
      return new PSGuid(PSTypeEnum.PACKAGE_INFO, guid);
   }

   
   /**
    * Get the Package Descriptor Name of this Package 
    * 
    *  @returns The Package Descriptor Name.
    */
   public String getPackageDescriptorName()
   {
      return descriptorName;
   }

   
   /**
    * Get the Package Descriptor's GUID 
    * 
    *  @returns The GUID for the Package's Descriptor.
    */
   public IPSGuid getPackageDescriptorGuid()
   {
      return new PSGuid(PSTypeEnum.DEPLOYER_DESCRIPTOR_ID, descriptorGuid);
   }

      
   /**
    * Get the name of the Package's Publisher
    * 
    *  @returns The name of the Package Publisher.
    */
   public String getPublisherName()
   {
      return publisherName;
   }
   

   /**
    * Get the URL of the Package Publisher
    * 
    *  @returns The URL of the Package Publisher.
    */
   public String getPublisherUrl()
   {
      return publisherUrl;
   }
   

   /**
    * Get the description of the Package.
    * 
    *  @returns The description of the Package.
    */
   public String getPackageDescription()
   {
      return description;
   }
   

   /**
    * Get the version of the Package.
    * 
    *  @returns The version of the Package.
    */
   public String getPackageVersion()
   {
      return pkgVersion;
   }
   

   /**
    * Get the Configuration Definition shipped with the Package.
    * 
    *  @returns The Configuration Definition of the Package.
    */
   public String getShippedConfigDefinition()
   {
      return shippedConfigDefinition;
   }
   

   /**
    * Get the Installation Date of the Package.
    * 
    *  @returns The Installation Date of the Package.
    */
   public Date getLastActionDate()
   {
      return lastActionDate;
   }
   

   /**
    * Get the Original Configuration Date of the Package.
    * 
    *  @returns The Original Configuration Date of the Package.
    */
   public Date getOriginalConfigDate()
   {
      return origConfigDate;
   }
   
   
   public PackageAction getLastAction()
   {
      return PackageAction.valueOf(lastAction);
   }
   
   public PackageActionStatus getLastActionStatus()
   {
      return PackageActionStatus.valueOf(lastActionStatus);
   }

   /**
    * Get the name of the Installer of the Package.
    * 
    * @returns The Installer of the Package.
    */
   public String getLastActionByUser()
   {
      return lastActionByUser;
   }
   
   /**
    * Convenience method that performs the following check: {@link #getType()}
    * equals {@link PackageType#DESCRIPTOR}.
    * 
    * @return <code>true</code> if the associate package is of the
    * <code>CREATED</code> type, otherwise, <code>false</code>.
    */
   public boolean isCreated()
   {
      return getTypeValue(true).equals(PackageType.DESCRIPTOR.name());
   }
   
   /**
    * Check if Package is editable
    * 
    * @return <code>true</code> if the associate package is
    * editable, otherwise, <code>false</code>.
    */
   public boolean isEditable()
   {
      return Boolean.valueOf(getTypeValue(false));
   }

   /**
    * Convenience method that performs the following check: 
    * {@link #getCategory()} equals {@link PackageCategory#SYSTEM}.
    * 
    * @return <code>true</code> if the associated package is of the
    * <code>SYSTEM</code> category, otherwise, <code>false</code>.
    */
   public boolean isSystem()
   {
      return category.equals(PackageCategory.SYSTEM.name());
   }
   
   /**
    * A convenience method that looks at the type, the last action and the
    * status of that action and calculates a status.
    * 
    * @returns <code>true</code> if this pkg type is
    * {@link PackageType#PACKAGE} and the last action is
    * {@link PackageAction#INSTALL} and the last action status is
    * {@link PackageActionStatus#SUCCESS}, otherwise <code>false</code>.
    */
   public boolean isSuccessfullyInstalled()
   {
      return getTypeValue(true).equals(PackageType.PACKAGE.name())
            && lastAction.equals(PackageAction.INSTALL_CREATE.name())
            && lastActionStatus.equals(PackageActionStatus.SUCCESS.name());
   }
   
   /**
    * Get the Type of the Package.
    * 
    *  @returns The Type of the Package. Either 
    *  {@link PackageType#DESCRIPTOR} or {@link PackageType#PACKAGE} 
    */
   public PackageType getType()
   {
      return PackageType.valueOf(getTypeValue(true));
   }
   

   /**
    * Get the Category of the Package.
    * 
    *  @returns The Category of the Package. Either 
    *  {@link PackageCategory#SYSTEM} or {@link PackageCategory#USER} 
    */
   public PackageCategory getCategory()
   {
      return PackageCategory.valueOf(category);
   }
   
   /**
    * Get the Content Management Version Minimum of this Package 
    * 
    *  @returns The Content Management Version Minimum of the Package.
    */
   public String getCmVersionMinimum()
   {
      return cmVersionMinimum;
   }


   /**
    * Get the Content Management Version Maximum of this Package 
    * 
    *  @returns The Content Management Version Maximum of the Package.
    */
   public String getCmVersionMaximum()
   {
      return cmVersionMaximum;
   }


  //------------------------------------------------------------------------------  
 
    /**
    * Set the Package Info GUID.  See {@link #getGuid()}.
    * 
    * @param theGuid The GUID, may not be <code>null</code>.
    */
   public void setGuid(IPSGuid theGuid)
   {
      if (theGuid == null)
         throw new IllegalArgumentException("Guid may not be null");
      
      guid = theGuid.longValue();
   }
   

   /**
    * Set the Package Descriptor Name of the Package. 
    * This is base name without the extension. 
    *    See {@link #getPackageDescriptorName()}.
    * 
    * @param thePackageDescriptorName The name of the Package Descriptor, 
    *    may not be <code>null</code>.
    */
   public void setPackageDescriptorName(String thePackageDescriptorName)
   {
      if (thePackageDescriptorName == null)
         throw new IllegalArgumentException("Package Name may not be null");
      
      descriptorName = thePackageDescriptorName;
   }
   

   /**
    * Set the Package Descriptor GUID.  See {@link #getPackageDescriptorGuid()}.
    * 
    * @param guid The GUID, may not be <code>null</code>.
    */
   public void setPackageDescriptorGuid(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("Descriptor Guid may not be null");
      
      descriptorGuid = guid.longValue();
   }
   

   /**
    * Set the name of the Package Publisher. See {@link #getPublisherName()}.
    * 
    * @param thePublisherName The name of the Package's Publisher, may not be
    * <code>null</code>.
    */
   public void setPublisherName(String thePublisherName)
   {
      if (thePublisherName == null)
         throw new IllegalArgumentException("Publisher Name may not be null");
      
      publisherName = thePublisherName;
   }
   

   /**
    * Set the URL of the Package Publisher.  See {@link #getPublisherUrl()}.
    * 
    * @param thePublisherUrl The URL of the Package Publisher, 
    *                       may not be <code>null</code>.
    */
   public void setPublisherUrl(String thePublisherUrl)
   {
      if (thePublisherUrl == null)
         throw new IllegalArgumentException("Publisher URL may not be null");
      
      publisherUrl = thePublisherUrl;
   }
   

   /**
    * Set the description of the Package.  See {@link #getPackageDescription()}.
    * 
    * @param thePackageDescription The description of the Package, may not be <code>null</code>.
    */
   public void setPackageDescription(String thePackageDescription)
   {
      if (thePackageDescription == null)
         throw new IllegalArgumentException("Package description may not be null");
      
      description = thePackageDescription;
   }
   

   /**
    * Set the version of the Package.  See {@link #getPackageVersion()}.
    * 
    * @param thePackageVersion The version of the Package, may not be <code>null</code>.
    */
   public void setPackageVersion(String thePackageVersion)
   {
      if (thePackageVersion == null)
         throw new IllegalArgumentException("Package version may not be null");
      
      pkgVersion = thePackageVersion;
   }
   

   /**
    * Set the Configuration Definition to be shipped with the Package.  
    *    See {@link #getShippedConfigDefinition()}.
    * 
    * @param theShippedConfigDefinition The Configuration Definition to be shipped with the Package, may not be <code>null</code>.
    */
   public void setShippedConfigDefinition(String theShippedConfigDefinition)
   {
      if (theShippedConfigDefinition == null)
         throw new IllegalArgumentException("The Configuration Definition may not be null");
      
      shippedConfigDefinition = theShippedConfigDefinition;
   }
   

   /**
    * Set the Date the when the action was done .  
    *    See {@link #getInstallationDate()}.
    * 
    * @param theInstallationDate The Installation Date of the Package, 
    *    may not be <code>null</code>.
    */
   public void setLastActionDate(Date actionDate)
   {
      if (actionDate == null)
         throw new IllegalArgumentException("The Installation Date may not be null");
      
      // eliminate millesecs.
      Long dateTime = actionDate.getTime();
      dateTime = dateTime - (dateTime % 1000);
      lastActionDate.setTime(dateTime); 
   }
   
   /**
    * Set the Original Configuration Date the Package.  
    *    See {@link #getOriginalConfigDate()}.
    * 
    * @param theOriginalConfigurationDate The Original Configuration Date of the Package, 
    *    may not be <code>null</code>.
    */
   public void setOriginalConfigDate(Date theOriginalConfigurationDate)
   {
      if (theOriginalConfigurationDate == null)
         throw new IllegalArgumentException("The Original Configuration Date may not be null");
      
      Long dateTime = theOriginalConfigurationDate.getTime();
      dateTime = dateTime - (dateTime % 1000);
      origConfigDate.setTime(dateTime); 
   }
   

   
   public void setLastAction(PackageAction action)
   {
      lastAction = action.name();
   }
   
   public void setLastActionStatus(PackageActionStatus status)
   {
      lastActionStatus = status.name();
   }

   /**
    * Set the name of the user that performed the last action on this package.  
    *    See {@link #getInstaller()}.
    * 
    * @param theInstaller The Installer of the Package, 
    *    may not be <code>null</code>.
    */
   public void setLastActionByUser(String user)
   {
      if (user == null)
         throw new IllegalArgumentException("The Installer may not be null");
      
      lastActionByUser = user;
   }
   
   /**
    * Set the Type of the Package.  See {@link #getType()}.
    * 
    * @param thePackageType The type of the Package, may not be
    *    <code>null</code>.  Must be either "descriptor" or "package".
    */
   public void setType(PackageType thePackageType)
   {
      if (thePackageType == null)
      {
         throw new IllegalArgumentException("Package type may not be null");
      }
      updateType(true,thePackageType.name());
   }
   
   /**
    * Method to update the type field with the type and editable values
    * 
    * @param isFirst type if true or editable if false
    * @param value Sting value to save
    */
   private void updateType(boolean isFirst, String value)
   {
      String[] items = StringUtils.split(type, "_");
      if(isFirst)
      {
         items[0] = value;
      }
      else
      {
         items[1] = value;
      }
      type = StringUtils.join(items, "_");
   }
   
   /**
    * Method to parse type field and get type or editable value
    * 
    * @param isFirst type if true or editable if false
    * @return Sting value of field
    */
   private String getTypeValue(boolean isFirst)
   {
      String[] items = StringUtils.split(type, "_");
      
      if(isFirst)
      {
         return items[0];
      }
      
      if(items.length < 2)
      {
         return "false";
      }
      
      return items[1];
   }
   
   /**
    * Set the Type to Package and set if editable or not  
    * 
    * @param thePackageType The type of the Package, may not be
    *    <code>null</code>.  Must be either "descriptor" or "package".
    */
   public void setEditable(boolean editmode)
   {
      updateType(false, Boolean.toString(editmode));
   }
   
   /**
    * Set the Category of the Package.  See {@link #getCategory()}.
    * 
    * @param thePackageCat The category of the Package, may not be
    *    <code>null</code>.  Must be either "system" or "user".
    */
   
   public void setCategory(PackageCategory thePackageCat)
   {
      if (thePackageCat == null)
         throw new IllegalArgumentException("Package category may not be null");
      category = thePackageCat.name();
   }

   /**
    * Set the Content Management Version Minimum of the Package.  
    *    See {@link #getCmVersionMinimum()}.
    * 
    * @param theCmVersionMinimum The Content Management Version Minimum of the
    *    Package, may not be <code>null</code>.
    */
   public void setCmVersionMinimum(String theCmVersionMinimum)
   {
      if (theCmVersionMinimum == null)
         throw new IllegalArgumentException("Package Name may not be null");
      
      cmVersionMinimum = theCmVersionMinimum;
   }
   

   /**
    * Set the Content Management Version Maximum of the Package.  
    *    See {@link #getCmVersionMaximum()}.
    * 
    * @param theCmVersionMaximum The Content Management Version Maximum of the Package, 
    *    may not be <code>null</code>.
    */
   public void setCmVersionMaximum(String theCmVersionMaximum)
   {
      if (theCmVersionMaximum == null)
         throw new IllegalArgumentException("Package Name may not be null");
      
      cmVersionMaximum = theCmVersionMaximum;
   }
   
   
//------------------------------------------------------------------------------  

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSPkgInfo))
         return false;
      PSPkgInfo second = (PSPkgInfo) b;
      return new EqualsBuilder()
         .append(guid, second.guid)
         .append(descriptorName, second.descriptorName)
         .append(descriptorGuid, second.descriptorGuid)
         .append(publisherName, second.publisherName)
         .append(publisherUrl, second.publisherUrl)
         .append(description, second.description)
         .append(pkgVersion, second.pkgVersion)
         .append(shippedConfigDefinition,second.shippedConfigDefinition)
         .append(origConfigDate, second.origConfigDate)
         .append(lastAction, second.lastAction)
         .append(lastActionDate, second.lastActionDate)
         .append(lastActionByUser,second.lastActionByUser)
         .append(lastActionStatus, second.lastActionStatus)
         .append(type, second.type)
         .append(cmVersionMinimum, second.cmVersionMinimum)
         .append(cmVersionMaximum, second.cmVersionMaximum)
         .append(category, second.category)
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
         .append(descriptorName)
         .append(descriptorGuid)
         .append(publisherName)
         .append(publisherUrl)
         .append(description)
         .append(pkgVersion)
         .append(shippedConfigDefinition)
         .append(origConfigDate)
         .append(lastAction)
         .append(lastActionDate)
         .append(lastActionByUser)
         .append(lastActionStatus)
         .append(type)
         .append(cmVersionMinimum)
         .append(cmVersionMaximum)
         .append(category)
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
    * Unique Identifier for this object
    */
   @Id
   @Column(name = "GUID", nullable = false)
   private long guid;

   
   /**
    * Package Descriptor Name - base name without extension
    */
   @Column(name = "DESCRIPTOR_NAME", nullable = false)
   private String descriptorName;

   
   /**
    * Unique Identifier for the Descriptor
    */
   @Column(name = "DESCRIPTOR_GUID", nullable = false)
   private long descriptorGuid;

   
    /**
    * The name of the Package Publisher
    */
   @Column(name = "PUBLISHER_NAME", nullable = false)
   private String publisherName;

   
   /**
    * The URL of the Package Publisher
    */
   @Column(name = "PUBLISHER_URL", nullable = true)
   private String publisherUrl;

   
   /**
    * Description of the Package 
    */
   @Column(name = "DESCRIPTION", nullable = true)
   private String description;

   
   /**
    * Version of the Package 
    */
   @Column(name = "PKG_VERSION", nullable = false)
   private String pkgVersion;

   
   /**
    * The Configuration Definition shipped with the package. 
    */
   @Column(name = "SHIPPED_CONFIG_DEF", nullable = true)
   private String shippedConfigDefinition;

   
   /**
    * Installation Date. 
    */
   @Column(name = "LAST_ACTION_DATE", nullable = false)
   private Date lastActionDate;
   
   /**
    * Original Configuration Date. 
    */
   @Column(name = "CONFIG_DATE_ORIG", nullable = false)
   private Date origConfigDate;

   
   /**
    * Name of Installer . 
    */
   @Column(name = "LAST_ACTION_BY_USER", nullable = false)
   private String lastActionByUser;
   
   @Column(name = "LAST_ACTION", nullable = false)
   private String lastAction;
   
   @Column(name = "LAST_ACTION_STATUS", nullable = false)
   private String lastActionStatus;
   
   /**
    * Type - field is split.  
    * First param type is "Package or Descriptor"
    * Second is boolean if Package is Editable 
    */
   @Column(name = "TYPE", nullable = false)
   private String type;
   
   /**
    * Category - "System" or "User".
    */
   @Column(name = "CATEGORY", nullable = false)
   private String category;
   
   /**
    * Content Management Version Minimum - 
    *   minimum version of Rx required for this Package
    */
   @Column(name = "CM_VERSION_MIN", nullable = true)
   private String cmVersionMinimum;

   
   /**
    * Content Management Version Maximum - 
    *   maximum version of Rx allowed for this Package
    */
   @Column(name = "CM_VERSION_MAX", nullable = true)
   private String cmVersionMaximum;
   
 //------------------------------------------------------------------------------  

}