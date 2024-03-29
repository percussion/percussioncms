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
package com.percussion.rx.config.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing an Package Information Configuration object, used to save
 * configuration information regarding the "solution" package created or
 * installed on a server.
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSConfigStatus")
@Table(name = "PSX_CONFIG_STATUS")
public class PSConfigStatus implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 6702475588603579446L;

   public PSConfigStatus()
   {
      statusId = 0;
      configName = "";
      dateApplied = null;
      status = ConfigStatus.FAILURE;
      defaultConfig = "";
      localConfig = "";
   }

   // ------------------------------------------------------------------------------

   /**
    * Get the Unique Identifier for this object
    * 
    * @return the guid
    */
   public long getStatusId()
   {
      return statusId;
   }

   /**
    * Get the GUID of the Package being configured
    * 
    * @return the packageGuid
    */
   public String getConfigName()
   {
      return configName;
   }

   /**
    * Get the Date the configuration was applied.
    * 
    * @return the dateApplied
    */
   public Date getDateApplied()
   {
      return dateApplied;
   }

   /**
    * Get the Result of configuration
    * 
    * @return the successful
    */
   public ConfigStatus getStatus()
   {
      return status;
   }

   /**
    * Gets the local configuration
    * 
    * @return the configuration content, may be <code>null</code>.
    */
   public String getLocalConfig()
   {
      return localConfig;
   }

   /**
    * Gets the default configuration
    * 
    * @return the configuration content, may be <code>null</code>.
    */
   public String getDefaultConfig()
   {
      return defaultConfig;
   }

   // ------------------------------------------------------------------------------

   /**
    * Set the Unique Identifier for this object
    * 
    * @param guid the guid to set
    */
   public void setStatusId(long statusId)
   {
      this.statusId = statusId;
   }

   /**
    * Set the GUID of the Package being configured
    * 
    * @param packageGuid the packageGuid to set
    */
   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   /**
    * Set the Date the configuration was applied.
    * 
    * @param dateApplied the dateApplied to set
    */
   public void setDateApplied(Date dateApplied)
   {
      this.dateApplied = dateApplied;
   }

   /**
    * Set the Result of configuration
    * 
    * @param successful the successful to set
    */
   public void setStatus(ConfigStatus status)
   {
      this.status = status;
   }

   /**
    * Set the Configuration Data for the local configure
    * 
    * @param configuration the configuration to set
    */
   public void setLocalConfig(String configuration)
   {
      this.localConfig = configuration;
   }

   /**
    * Set the Configuration Data for the default configure
    * 
    * @param configuration the configuration to set
    */
   public void setDefaultConfig(String configuration)
   {
      this.defaultConfig = configuration;
   }

   /**
    * Gets the configuration definition file content.
    * 
    * @return configuration definition, it may be <code>null</code> or empty.
    */
   public String getConfigDef()
   {
      return configDef;
   }
   
   /**
    * Sets the configuration definition file content.
    * 
    * @param config the new configuration definition, it may be
    * <code>null</code> or empty.
    */
   public void setConfigDef(String config)
   {
      configDef = config;
   }
   
   // ------------------------------------------------------------------------------

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSConfigStatus))
         return false;

      PSConfigStatus second = (PSConfigStatus) b;
      return new EqualsBuilder().append(statusId, second.statusId).append(
            configName, second.configName).append(dateApplied,
            second.dateApplied).append(status, second.status).append(
            localConfig, second.localConfig).append(defaultConfig,
            second.defaultConfig).append(configDef, second.configDef)
            .isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(statusId).append(configName).append(
            dateApplied).append(status).append(localConfig).append(
            defaultConfig).append(configDef).toHashCode();
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSConfigStatus{");
      sb.append("statusId=").append(statusId);
      sb.append(", configName='").append(configName).append('\'');
      sb.append(", defaultConfig='").append(defaultConfig).append('\'');
      sb.append(", localConfig='").append(localConfig).append('\'');
      sb.append(", configDef='").append(configDef).append('\'');
      sb.append(", dateApplied=").append(dateApplied);
      sb.append(", status=").append(status);
      sb.append('}');
      return sb.toString();
   }
// ------------------------------------------------------------------------------

   /**
    * Unique Identifier for this object
    */
   @Id
   @Column(name = "STATUS_ID", nullable = false)
   private long statusId;

   /**
    * Name of the configuration
    */
   @Column(name = "CONFIG_NAME", nullable = false)
   private String configName;

   /**
    * Default configuration file
    */
   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String defaultConfig;

   /**
    * Local configuration file
    */
   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String localConfig;

   /**
    * Configuration definition file
    */
   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String configDef;

   
   /**
    * Date the configuration was applied.
    */
   @Column(name = "DATE_APPLIED", nullable = false)
   private Date dateApplied;

   /**
    * Status of configuration
    */
   @Column(name = "STATUS", nullable = false)
   private ConfigStatus status;

   /**
    * Enumeration for configuration status.
    */
   public enum ConfigStatus
   {
      /**
       * Enum for failure to apply configuration status
       */
      FAILURE(0),
      /**
       * Enum for Successfully applied configuration status
       */
      SUCCESS(1);
      
      ConfigStatus(int ordinal)
      {
         mi_ordinal = ordinal;
      }
      
      public static ConfigStatus valueOf(int s) throws IllegalArgumentException
      {
         ConfigStatus types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].getOrdinal() == s)
               return types[i];
         }
         return null;
      }
      
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      private int mi_ordinal;
   }

}
