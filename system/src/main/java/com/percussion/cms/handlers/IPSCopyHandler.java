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
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSNotFoundException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.sql.SQLException;

/**
 * Each copy handler for each object type implements this interface.
 */
public interface IPSCopyHandler
{
   /**
    * Create a copy of the source object using the locator information provided
    * in the target.
    * 
    * @param source the locator of the source to be copied, 
    *    not <code>null</code>.
    * @param target the locator to use for the new target, 
    *    not <code>null</code>.
    * @param data the execution data to operate on, not <code>null</code>.
    * @param checkin <code>true</code> to checkin the created copy, 
    *    <code>false</code> to leave it checked out.
    * @throws IllegalArgumentException for any <code>null</code> parameter.
    * @throws PSAuthorizationException if the user is not authorized to create
    *    a copy.
    * @throws PSInternalRequestCallException if anything goes wrong through 
    *    internal requests.
    * @throws PSAuthenticationFailedException if the user is not authenticated
    *    to create a copy.
    * @throws PSNotFoundException if required files or resources cannot be 
    *    found.
    * @throws SQLException for any failed sql operation.
    */
   public void createCopy(PSLocator source, PSLocator target, 
      PSExecutionData data, boolean checkin)
      throws PSAuthorizationException, PSInternalRequestCallException,
         PSAuthenticationFailedException, PSNotFoundException, SQLException;
}
