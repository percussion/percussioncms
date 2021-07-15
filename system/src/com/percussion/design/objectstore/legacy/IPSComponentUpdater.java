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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

/**
 * Interface implemented by a class that handles the updating of a type of
 * objectstore component where the old object needs to be updated to the new
 * model. The new component class ctor that takes an element should get the
 * updater by passing the class name.
 */
public interface IPSComponentUpdater
{
   /**
    * This should be called fromXml methods of the component class by passing
    * the just created object. The implementation of this method should check
    * the object and update if needed.
    * 
    */
   public void updateComponent(PSComponent comp);

   /**
    * Determines if this updater supports updating elements to the specified
    * class. Each updater should handle a single class, and it is expected that
    * it will know the legacy XML format of that class.
    * 
    * @param type The type for which the serialized XML requires conversion, may
    *           not be <code>null</code>.
    * 
    * @return <code>true</code> if this converter supports the supplied type,
    *         <code>false</code> if not.
    */
   public boolean canUpdateComponent(Class type);

}
