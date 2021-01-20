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
package com.percussion.services.pubserver.data;

import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.xml.sax.SAXException;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Represents a publishing server related to a given site.
 * 
 * @author leonardohildt
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSPubServer")
@Table(name = "PSX_PUBSERVER")
public class PSPubServer extends PSAbstractDataObject implements Serializable, IPSCatalogIdentifier, IPSPubServer
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "PUBSERVERID")
   private long serverId;

   @Column(name = "SITEID")
   private long siteId;

   @Column(name = "NAME")
   private String name;

   @Column(name = "DESCRIPTION")
   private String description;

   @Column(name = "PUBLISHTYPE")
   private String publishType;
   
   @Column(name="SERVERTYPE")
   private String  serverType;
   
   @Basic
   @Column(name="HAS_FULL_PUBLISHED", nullable=true)
   private String hasFullPublished;

    @Basic
    @Column(name="SITERENAMED", nullable=true)
    private String siteRenamed;

   public static final String PRODUCTION= "PRODUCTION";
   public static final String STAGING = "STAGING";
   public static final String LICENSE = "LICENSE";
   
   /**
    * @return the serverType
    */
   public String getServerType()
   {
      if (StringUtils.isBlank(serverType))
        return PRODUCTION;

      return serverType;
   }

   /***
    *  Test validity of a publishing server type.
    * @param type A publishing server type
    * @return Returns true if the server type is valid.
    */
   private boolean isValidServerType(String type){
      if(isBlank(type))
         return false;
      
      
      if(type.toUpperCase().equals(PRODUCTION)||type.toUpperCase().equals(STAGING)||type.toUpperCase().equals(LICENSE))
         return true;
      
      return false;
   }
   
   /**
    * @param serverType the serverType to set
    */
   public void setServerType(String serverType)
   {
      // default to production
      String srvType = PRODUCTION;
      //if it is not blank make sure it is valid, otherwise throw illegal argument exception
      if (StringUtils.isNotBlank(serverType))
      {
         if(!isValidServerType(serverType))
            throw new IllegalArgumentException("serverType " + serverType + " is not a valid publishing server type");
         srvType = serverType;
      }
      this.serverType = srvType;
   }

   @OneToMany(targetEntity = PSPubServerProperty.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
   @JoinColumn(name = "PUBSERVERID", nullable = false, insertable = false, updatable = false)
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSPubServerProperty")
   @Fetch(FetchMode. SUBSELECT)
   private Set<PSPubServerProperty> properties = new HashSet<PSPubServerProperty>();  
   
   /**
    * The default constructor.
    */
   public PSPubServer()
   {
   }

   public void setProperties(Set<PSPubServerProperty> properties)
   {
      this.properties = properties;
   }

   public Set<PSPubServerProperty> getProperties()
   {
      return properties;
   }
   
   /**
    * @return the id
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.PUBLISHING_SERVER, serverId);
   }

   /**
    * @param guid the id to set, never <code>null</code>
    */
   public void setGUID(IPSGuid guid)
   {
      this.serverId = guid.getUUID();
   }

   /**
    * The site Id
    * 
    * @return Returns the site id, never <code>null</code>
    */
   public long getSiteId()
   {
      return siteId;
   }

   /**
    * @param siteId The site to set, may be <code>null</code> when disconnecting
    */
   public void setSiteId(long siteId)
   {
      this.siteId = siteId;
   }

   /*
    * //see base class method for details
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /*
    * //see base class method for details
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /**
    * Get the name of the publishing server.
    * 
    * @return the server name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      if (isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = name;

   }

   /**
    * Get the description that describes this server.
    * 
    * @return the description, can be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set the description.
    * 
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Get the server id for this server.
    * 
    * @return the server id, never <code>null</code> or empty.
    */
   public long getServerId()
   {
      return serverId;
   }

   /**
    * Set the server id.
    * 
    * @param serverId the server id to set
    */
   public void setServerId(long serverId)
   {
      this.serverId = serverId;
   }

   /**
    * Get the publish type for the server.
    * 
    * @return the publish type, never <code>null</code> or empty.
    */
   public String getPublishType()
   {
      return publishType;
   }

   /**
    * Set the publish type for this server.
    * 
    * @param publishType the publish type to set
    */
   public void setPublishType(String publishType)
   {
      this.publishType = publishType;
   }

   /**
    * Adds a specified property. If the property already exist, set the supplied value;
    * otherwise add the property to this publish server.
    * 
    * @param pname the property name, not blank.
    * @param pvalue the property value, may be <code>null</code>.
    */
   public void addProperty(String pname, String pvalue)
   {
      notEmpty(pname);
      
      PSPubServerProperty p = getProperty(pname);
      if (p != null)
      {
         p.setValue(pvalue);
         return;
      }
      
      p = new PSPubServerProperty();
      p.setServerId(serverId);
      p.setName(pname);
      p.setValue(pvalue);
      
      properties.add(p);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.pubserver.IPSPubServer#getProperty(java.lang.String)
    */
   public PSPubServerProperty getProperty(String propertyName)
   {
      if(isBlank(propertyName) || properties.isEmpty())
      {
         return null;
      }
      
      for(PSPubServerProperty property : properties)
      {
         if(equalsIgnoreCase(property.getName(), propertyName))
         {
            return property;
         }
      }      
      return null;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubserver.IPSPubServer#getPropertyValue(String)
    */
   public String getPropertyValue(String propertyName)
   {
      if (isBlank(propertyName) || properties.isEmpty())
      {
         return null;
      }

      for (PSPubServerProperty property : properties)
      {
         if (equalsIgnoreCase(property.getName(), propertyName))
         {
            return property.getValue();
         }
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubserver.IPSPubServer#getPropertyValue(String,
    * String)
    */
   public String getPropertyValue(String propertyName, String defaultValue)
   {
      String rawValue = getPropertyValue(propertyName);

      if (rawValue == null)
      {
         return defaultValue;
      }
      return rawValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.pubserver.IPSPubServer#isXmlFormat()
    */
   public boolean isXmlFormat()
   {
      return equalsIgnoreCase(
            getPropertyValue(IPSPubServerDao.PUBLISH_FORMAT_PROPERTY, "HTML"),
            "xml");
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.pubserver.IPSPubServer#isDatabaseType()
    */
   public boolean isDatabaseType()
   {
      return equalsIgnoreCase(publishType, PublishType.database.name());
   }

   /**
    * @return
    */
   public boolean isFtpType()
   {
      return publishType.toLowerCase().contains(PublishType.ftp.toString());
   }

   public boolean hasFullPublished()
   {
      return "y".equals(hasFullPublished);
   }

   public void setHasFullPublisehd(boolean hasPublished)
   {
      this.hasFullPublished = hasPublished ? "y" : "n";
   }

    /**
     * Returns whether or not the site has been renamed since the last full publish.
     * @return <code>true</code> if the site has been renamed since last full publish.
     */
    public boolean getSiteRenamed()
    {
        return "y".equals(siteRenamed);
    }

    /**
     * Sets whether or not the site has been renamed since the last full publish.
     * Should be updated once full publish has been completed to <code>false</code>.
     * @param siteRenamed <code>true</code> if the site has been renamed since last full publish.
     */
    public void setSiteRenamed(boolean siteRenamed)
    {
        this.siteRenamed = siteRenamed ? "y" : "n";
    }

   /**
    * Determine if this and another server publish the same format to the same location
    * 
    * @param otherServer The other server, not <code>null</code>.
    * 
    * @return <code>true</code> if the same, <code>false</code> if different
    */
   public boolean isSamePublish(PSPubServer otherServer)
   {
      Validate.notNull(otherServer);
      
       if (!otherServer.getPublishType().equals(this.getPublishType()))
           return false;
       else if (otherServer.isXmlFormat() != this.isXmlFormat())
           return false;
       else if (!otherServer.getProperties().equals(this.getProperties()))
           return false;
       
       return true;
   }
}
