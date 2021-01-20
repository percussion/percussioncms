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
