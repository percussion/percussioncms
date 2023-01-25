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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

/**
 * Delivery type node.
 */
public class PSDeliveryTypeNode extends PSDesignNode
{
   /**
    * Constructs an instance.
    * 
    * @param dtype the delivery type of this node. Never <code>null</code>.
    */
   public PSDeliveryTypeNode(IPSDeliveryType dtype) 
   {
      super(dtype.getName(), dtype.getGUID());
      m_deliveryType = dtype;
      setProperties();
   }

   /**
    * Calculate the properties, called by the constructor and save;
    */
   private void setProperties()
   {
      getProperties().put("description", m_deliveryType.getDescription());
      setTitle(m_deliveryType.getName());
   }

   @Override
   public String delete()
   {
      IPSPublisherService svr = getPublisherService();
      svr.deleteDeliveryType(m_deliveryType);
      super.remove();
      
      return navigateToList();
   }

   @Override
   public String navigateToList()
   {
      return PSDeliveryTypeContainerNode.DELIVERY_TYPE_LIST;
   }

   @Override
   public String copy()
   {
      throw new UnsupportedOperationException("copy is not implemented.");
   }

   /**
    * Persist the node to the back-end repository.
    * 
    * @return outcome for navigating to its parent node.
    */
   public String save()
   {
      IPSPublisherService svr = getPublisherService();
      svr.saveDeliveryType(m_deliveryType);
      setProperties();
      
      return gotoParentNode();
   }
   
   /**
    * Reload content of the node from back-end repository.
    * 
    * @return outcome for navigating to its parent node.
    */
   @Override
   public String cancel() throws PSNotFoundException {
      reLoaded(); // reload the object if it has been modified.
      return gotoParentNode();
   }

   /**
    * @return the name of the delivery type. Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_deliveryType.getName();
   }
   
   /**
    * Set the name of the delivery type.
    * @param name the new name, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name==null || name.trim().length() ==0)
         throw new IllegalArgumentException("name may not be null or empty.");
      
      m_isDirty = true;
      m_deliveryType.setName(name);
   }
   
   /**
    * @return the description of the delivery type, maybe <code>null</code> or
    * empty.
    */
   public String getDescription()
   {
      return m_deliveryType.getDescription();
   }

   /**
    * Set the description of the delivery type.
    * @param desc the new description, may be <code>null</code> or empty.
    */
   public void setDescription(String desc)
   {
      m_isDirty = true;
      m_deliveryType.setDescription(desc);
   }
   
   /**
    * @return the Spring Bean Name of the delivery type, may be 
    * <code>null</code> or empty.
    */
   public String getBeanName()
   {
      return m_deliveryType.getBeanName();
   }
   
   /**
    * Set the Spring Bean Name of the delivery type.
    * @param beanName the new Spring Bean Name, may be <code>null</code> or empty.
    */
   public void setBeanName(String beanName)
   {
      m_isDirty = true;
      m_deliveryType.setBeanName(beanName);
   }
   
   /**
    * It determines if the item need to be unpublished.
    * 
    * @return <code>true</code> if the item must be assembled for the 
    * unpublishing case; otherwise return <code>false</code>.
    */
   public boolean isUnpublishingRequiresAssembly()
   {
      return m_deliveryType.isUnpublishingRequiresAssembly();
   }
   
   /**
    * Set the value, see {@link #isUnpublishingRequiresAssembly()}.
    * 
    * @param isUnpublishingRqsAssembly the unpublishingRequiresAssembly to set.
    */
   public void setUnpublishingRequiresAssembly(boolean isUnpublishingRqsAssembly)
   {
      m_isDirty = true;
      m_deliveryType.setUnpublishingRequiresAssembly(isUnpublishingRqsAssembly);
   }
   
   @Override
   public String getHelpTopic()
   {
      return "DeliveryTypeEditor";
   }

   /**
    * Convenience method to access publisher service.
    * @return the publisher service object. Not <code>null</code>.
    */
   private IPSPublisherService getPublisherService()
   {
      return PSPublisherServiceLocator.getPublisherService();
   }
   
   /**
    * Reload the Delivery Type to sync up the object with the persisted one.
    */
   private void reLoaded() throws PSNotFoundException {
      if (m_isDirty)
      {
         m_deliveryType = getPublisherService().loadDeliveryTypeModifiable(
               m_deliveryType.getGUID());
         m_isDirty = false;
      }
   }

   /**
    * The delivery type object, never <code>null</code> after ctor.
    */
   private IPSDeliveryType m_deliveryType;
   
   /**
    * Determines if the node has been modified. It is <code>true</code> if the
    * node has been modified; <code>false</code> otherwise. Defaults to 
    * <code>false</code>.
    */
   private boolean m_isDirty = false;
   
}
