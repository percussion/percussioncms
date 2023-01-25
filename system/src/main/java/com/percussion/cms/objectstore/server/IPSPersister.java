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
