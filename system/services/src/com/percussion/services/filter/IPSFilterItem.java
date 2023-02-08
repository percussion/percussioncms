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
