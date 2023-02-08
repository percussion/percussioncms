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
