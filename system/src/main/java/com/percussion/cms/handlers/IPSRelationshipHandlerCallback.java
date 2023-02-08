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
import com.percussion.error.PSRelationshipException;

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
