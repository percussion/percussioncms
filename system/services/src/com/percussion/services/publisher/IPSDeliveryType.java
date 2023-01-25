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
package com.percussion.services.publisher;

import com.percussion.services.catalog.IPSCatalogItem;

/**
 * A delivery location has the representation for a single publisher plugin. The
 * delivery location determines what spring bean is looked up in the publisher
 * and dictates whether an item needs to be assembled for unpublishing.
 */
public interface IPSDeliveryType extends IPSCatalogItem
{
   /**
    * Get the name of the bean to be used when publishing. This name is used to
    * look up a spring bean on the publisher side of the delivery.
    * 
    * @return the beanName, never <code>null</code> or empty.
    */
   String getBeanName();

   /**
    * Set the bean name.
    * 
    * @param beanName the beanName to set, never <code>null</code> or empty.
    */
   void setBeanName(String beanName);

   /**
    * Get the description that describes this location.
    * 
    * @return the description, can be <code>null</code> or empty.
    */
   String getDescription();

   /**
    * Set the description.
    * 
    * @param description the description to set
    */
   void setDescription(String description);

   /**
    * Get the name of the delivery location.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   String getName();

   /**
    * Set the name of the delivery location.
    * 
    * @param name the name to set, never <code>null</code> or empty.
    */
   void setName(String name);

   /**
    * It determines if the item need to be unpublished.
    * 
    * @return <code>true</code> if the item must be assembled for the 
    * un-publishing case; otherwise return <code>false</code>.
    */
   boolean isUnpublishingRequiresAssembly();

   /**
    * Set the value, see {@link #isUnpublishingRequiresAssembly()}.
    * 
    * @param isUnpublishingRequiresAssembly the unpublishingRequiresAssembly to
    *           set.
    */
   void setUnpublishingRequiresAssembly(boolean isUnpublishingRequiresAssembly);

}
