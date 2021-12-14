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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;

/**
 * This interface is used to persist objects and retrieve objects from different
 * locations (repositories).
 */
public interface IPSPersister
{
   /**
    * Loads this item from the storage component.  This will populate the
    * item with definition and data.
    *
    * It is important to note you should only access this PSServerItem
    * with the same request.  Do not access this item with different PSRequest
    * objects.  This may cause exceptions and corrupted data.
    *
    * @param itemId the item locator.  May be <code>null</code> to load an items
    * defaults.
    * @param request - the original request that prompted this update/insert
    * loads the this item from the system.
    * @throws PSCmsException if error loading item occurs
    * @throws PSInvalidContentTypeException if content type is invalid.
    */
   public void load(PSLocator itemId, PSRequest request) throws PSCmsException,
      PSInvalidContentTypeException;

   /**
    * Persists the this item to the storage component.  Once an object has
    * been created use this function to update the data for that item on the
    * server.
    *
    * @param request the request that will be used to persist the data.
    * @throws PSCmsException if an error occurs during peristance.
    */
   public void save(PSRequest request) throws PSCmsException;
}
