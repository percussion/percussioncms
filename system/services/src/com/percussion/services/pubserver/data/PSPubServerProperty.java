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

import com.percussion.services.pubserver.impl.PSPubServerDao;
import com.percussion.share.data.PSAbstractDataObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import static com.percussion.util.PSBase64Decoder.decode;
import static com.percussion.util.PSBase64Encoder.encode;

/**
 * Represents a single property for the server.
 * 
 * @author leonardohildt
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSPubServerProperty")
@Table(name = "PSX_PUBSERVER_PROPERTIES")
public class PSPubServerProperty extends PSAbstractDataObject
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "SERVERPROPERTYID")
   private long propertyId = -1L;

   @Basic
   @Column(name = "PUBSERVERID")
   long serverId;

   @Basic
   @Column(name = "PROPERTYNAME")
   private String name;

   @Basic
   @Column(name = "PROPERTYVALUE")
   private String value;

   /**
    * The default constructor.
    */
   public PSPubServerProperty()
   {
   }

   @Override
   public boolean equals(Object obj)
   {
      if ( !(obj instanceof PSPubServerProperty) )
         return false;
      
      // use "name" & "value" should be enough to avoid same pair more than once to make sure the property names are unique within a PSPubServer
      PSPubServerProperty b = (PSPubServerProperty) obj;
      return new EqualsBuilder().append(name, b.name).append(value, b.value).isEquals();
   }
   
   @Override
   public int hashCode()
   {
      // use "name" should be enough to avoid same pair more than once to make sure the property names are unique within a PSPubServer
      return new HashCodeBuilder().append(name).toHashCode();
   }
   
   /**
    * The database id for this object
    * 
    * @return Returns the serverPropertyId, never <code>null</code> after
    *         persistence
    */
   public long getPropertyId()
   {
      return propertyId;
   }

   /**
    * @param propertyId The propertyId to set.
    */
   public void setPropertyId(long propertyId)
   {
      this.propertyId = propertyId;
   }

   /**
    * The server id for this object
    * 
    * @return Returns the serverId, never <code>null</code> after persistence
    */
   public long getServerId()
   {
      return serverId;
   }

   /**
    * @param serverId The server id to set.
    */
   public void setServerId(long serverId)
   {
      this.serverId = serverId;
   }

   /**
    * Get the property name
    * 
    * @return Returns the property name, never <code>null</code> or empty
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = name;
   }

   /**
    * Get the value
    * 
    * @return Returns the value, may be <code>null</code> or empty.
    */
   public String getValue()
   {
      if (isEncodedProperty())
         return StringUtils.isEmpty(value) ? value : decode(value);
      else
         return value;
   }

   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      if (isEncodedProperty())
         this.value = StringUtils.isEmpty(value) ? value : encode(value);
      else
         this.value = value;
   }

   /**
    * Determines if the property value is encrypted.
    * @return <code>true</code> if the value is encrypted; <code>false</code> otherwise.
    */
   private boolean isEncodedProperty()
   {
      return PSPubServerDao.PUBLISH_PASSWORD_PROPERTY.equals(name);
   }
}