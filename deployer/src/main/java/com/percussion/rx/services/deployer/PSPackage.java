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
package com.percussion.rx.services.deployer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Class that represents one package entry in the package mgt ui
 * list table.
 * @author erikserating
 *
 */
@XmlRootElement(name = "Package")
public class PSPackage
{   
   
   /**
    * ctor
    */
   public PSPackage()
   {
      super();
   }   
   
   /**
    * @return the installed
    */
   public String getPackageStatus()
   {
      return m_packageStatus;
   }
   /**
    * @param installed the installed to set
    */
   public void setPackageStatus(String status)
   {
      m_packageStatus = status;
   }
   /**
    * @return the configured
    */
   public String getConfigStatus()
   {
      return m_configStatus;
   }
   /**
    * @param configured the configured to set
    */
   public void setConfigStatus(String status)
   {
      m_configStatus = status;
   }
   /**
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }
   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      m_name = name;
   }
   /**
    * @return the publisher
    */
   public String getPublisher()
   {
      return m_publisher;
   }
   /**
    * @param publisher the publisher to set
    */
   public void setPublisher(String publisher)
   {
      m_publisher = publisher;
   }
   /**
    * @return the version
    */
   public String getVersion()
   {
      return m_version;
   }
   /**
    * @param version the version to set
    */
   public void setVersion(String version)
   {
      m_version = version;
   }
   /**
    * @return the desc
    */
   public String getDesc()
   {
      return m_desc;
   }
   /**
    * @param desc the desc to set
    */
   public void setDesc(String desc)
   {
      m_desc = desc;
   }
   /**
    * @return the installdate
    */
   public Date getInstalldate()
   {
      return m_installdate;
   }
   /**
    * @param installdate the installdate to set
    */
   public void setInstalldate(Date installdate)
   {
      m_installdate = installdate;
   }
   /**
    * @return the installer
    */
   public String getInstaller()
   {
      return m_installer;
   }
   /**
    * @param installer the installer to set
    */
   public void setInstaller(String installer)
   {
      m_installer = installer;
   }
   /**
    * @return the package category
    */
   public String getCategory()
   {
      return m_category;
   }
   /**
    * @param category the package category to set
    */
   public void setCategory(String category)
   {
      m_category = category;
   }
   
   /**
    * @return the package category
    */
   public String getLockStatus()
   {
      return m_lockstatus;
   }
   /**
    * @param category the package category to set
    */
   public void setLockStatus(String lockStatus)
   {
      m_lockstatus = lockStatus;
   }
   private String m_packageStatus;
   private String m_configStatus;
   private String m_name;
   private String m_publisher;
   private String m_version;
   private String m_desc;
   private Date m_installdate;
   private String m_installer;
   private String m_category;
   private String m_lockstatus;
}
