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
package com.percussion.cms.handlers;

import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.relationship.PSRelationshipException;

/**
 * Implementations of this interface can be passed to clone handlers to
 * create relationships if needed.
 */
public interface IPSRelationshipHandlerCallback
{
   /**
    * Creates a new relationship of the supplied type between the provided
    * owner and dependent.
    *
    * @param relationshipType the relationship type to create, not 
    *    <code>null</code>.
    * @param owner the relationship owner locator, not <code>null</code>.
    * @param dependent the relationship dependent locator, not <code>null</code>.
    * @param data the execution context to operate on, not <code>null</code>.
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    * @throws PSRelationshipException if anything goes wrong creating the 
    *    requested relationship.
    */
   public void relate(String relationshipType, PSLocator owner, 
      PSLocator dependent, PSExecutionData data)
         throws PSRelationshipException;
}
