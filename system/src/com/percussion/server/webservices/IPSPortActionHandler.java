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

package com.percussion.server.webservices;

import com.percussion.error.PSException;
import com.percussion.server.PSRequest;

import org.w3c.dom.Document;

public interface IPSPortActionHandler
{
   /**
    * This is the implementation of the interface for executing all services 
    * within the specified port.
    *
    * @param port The name of the port to be handling this action, only used if
    *    there is an error, must not be <code>null</code> or empty.
    * @param action The name of the action to be exectuded, must not be <code>
    *    null</code> or empty.
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   public abstract void processAction(
      String port,
      String action,
      PSRequest request,
      Document parent)
      throws PSException;
}