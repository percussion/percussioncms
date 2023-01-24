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
package com.percussion.cms.handlers;

import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSObjectException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * All clone handlers implement this interface. 
 */
public interface IPSCloneHandler
{
   /**
    * Convenience method, see
    * {#link clone(PSLocator, Iterator, PSExecutionData, PSCommandHandler, IPSRelationshipHandlerCallback)}
    * for description. This will pass in <code>null</code> for the callback.
    */
   public PSLocator clone(PSLocator source, Iterator relationships, 
      PSExecutionData data, PSCommandHandler ch)
      throws SQLException, PSObjectException, IOException;
   
   /**
    * Creates a clone for the supplied source object locator and returns the 
    * locator of the new object created.
    *
    * @param source the locator of the object to be cloned, not 
    *    <code>null</code>.
    * @param relationships a list of all current relationships of the source,
    *    not <code>null</code>, may be empty.
    * @param data the execution context to operate on, not <code>null</code>.
    * @param ch the command handler to use to create new relationships, not
    *    <code>null</code>.
    * @param cb a callback to the relationship handler, may be <code>null</code>.
    *    If provided, this will be called after the object has been cloned to 
    *    create a new relationship.
    *    
    * @return the locator of the new object created, never <code>null</null>.
    * 
    * @throws IllegalArgumentException if any parameter but cb is 
    *    <code>null</code>.
    * @throws SQLException for any failed sql operation.
    * @throws PSObjectException if cloning is not allowed for the supplied 
    *    object type.
    * @throws IOException for any IO operation that failed.
    */
   public PSLocator clone(PSLocator source, Iterator relationships, 
      PSExecutionData data, PSCommandHandler ch, 
      IPSRelationshipHandlerCallback cb)
      throws SQLException, PSObjectException, IOException;
}
