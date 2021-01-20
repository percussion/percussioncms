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
package com.percussion.services.filter;

import com.percussion.services.data.IPSIdentifiableItem;
import com.percussion.utils.guid.IPSGuid;

/**
 * Represents an item to be filtered. Items are considered immutable in filter
 * rules. If a modification is required, clone the item and modify the clone.
 * 
 * @author dougrand
 */
public interface IPSFilterItem extends Cloneable, IPSIdentifiableItem
{
   
   /**
    * Get the item's identifying folder guid
    * 
    * @return the item's folder guid, may be <code>null</code>
    */
   IPSGuid getFolderId();
   
   /**
    * Get the item's containing site guid
    * 
    * @return the item's site guid, may be <code>null</code>
    */
   IPSGuid getSiteId();
   
   /**
    * A number of operations require deciding if a filter item is in a set or
    * map. But the items are mutable, which can render these maps and sets
    * invalid. This method returns an opaque key that can be used for this 
    * purpose. The returned key is not dependent on the specific revision
    * of the item, but will be dependent on the folder and site.
    * <p>
    * Note to implementers, do not use an array for this purpose. Java does not
    * hash arrays, it uses the default hash code calculation. This will not
    * work properly.
    * 
    * @return an opaque key, never <code>null</code>, that identifies the
    * tuple content item id, folder id and site id.
    */
   Object getKey();
   
   /**
    * Make a clone and replace the item id. This is provided to enable 
    * implementers who need to return a modified item in the result set of
    * a filter to create a clone to modify. The original item should be
    * considered immutable. The returned object will not be equals to the 
    * original object (unless the new id happens to be the same).
    * <p>
    * Note that the general clone method is not guaranteed to be present.
    * 
    * @param newItemId the new item id, never <code>null</code>
    * @return the cloned and modified object
    */
   IPSFilterItem clone(IPSGuid newItemId);
   
   /**
    * Set a new item id, this must only be called on a cloned object.
    * 
    * @param newId the new item id, never <code>null</code>.
    */
   void setItemId(IPSGuid newId);
   
   /**
    * Set a new folder id, this must only be called on a cloned object.
    * 
    * @param newId the new folder id, never <code>null</code>.
    */
   void setFolderId(IPSGuid newId);
   
   /**
    * Set a new site id, this must only be called on a cloned object.
    * 
    * @param newId the new item id, never <code>null</code>.
    */
   void setSiteId(IPSGuid newId);
}
