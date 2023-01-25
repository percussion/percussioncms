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
    * @param status the installed to set
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
    * @param status the configured to set
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
    * @param lockStatus the package category to set
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
