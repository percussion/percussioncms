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
package com.percussion.services.publisher.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * @see IPSDeliveryType
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSDeliveryType")
@Table(name = "PSX_DELIVERY_TYPE")
public class PSDeliveryType implements IPSDeliveryType
{
   @Id
   long id;
   
   @Basic
   String name;

   @Basic
   String description;

   @Basic
   @Column(name = "BEAN_NAME")
   String beanName;

   @Basic
   @Column(name = "UNPUBLISHING_REQUIRES_ASSEMBLY")
   int unpublishingRequiresAssembly;

   /**
    * The default constructor.
    */
   public PSDeliveryType() 
   {
   }

   /**
    * @return the id
    */
   public IPSGuid getGUID()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      return gmgr.makeGuid(id, PSTypeEnum.DELIVERY_TYPE);
   }

   /**
    * @param id the id to set, never <code>null</code>
    */
   public void setGUID(IPSGuid guid)
   {
      this.id = guid.getUUID();
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
    * Get the name of the bean to be used when publishing. This name is used to
    * look up a spring bean on the publisher side of the delivery.
    * 
    * @return the beanName, never <code>null</code> or empty.
    */
   public String getBeanName()
   {
      return beanName;
   }

   /**
    * Set the bean name.
    * 
    * @param beanName the beanName to set, never <code>null</code> or empty.
    */
   public void setBeanName(String beanName)
   {
      if (StringUtils.isBlank(beanName))
      {
         throw new IllegalArgumentException("beanName may not be null or empty");
      }
      this.beanName = beanName;
   }

   /**
    * Get the description that describes this location.
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
    * Get the name of the delivery location.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name of the delivery location.
    * 
    * @param name the name to set, never <code>null</code> or empty.
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
    * It determines if the item need to be unpublished.
    * 
    * @return <code>true</code> if the item must be assembled for the 
    * unpublishing case; otherwise return <code>false</code>.
    */
   public boolean isUnpublishingRequiresAssembly()
   {
      return unpublishingRequiresAssembly == 1;
   }

   /**
    * Set the value, see {@link #isUnpublishingRequiresAssembly()}.
    * 
    * @param isUnpublishingRequiresAssembly the unpublishingRequiresAssembly to
    *           set.
    */
   public void setUnpublishingRequiresAssembly(
         boolean isUnpublishingRequiresAssembly)
   {
      this.unpublishingRequiresAssembly = isUnpublishingRequiresAssembly ? 1 : 0;
   }
}
