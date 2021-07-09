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
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
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
