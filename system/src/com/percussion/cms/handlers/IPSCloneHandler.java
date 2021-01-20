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