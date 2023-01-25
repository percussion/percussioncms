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

package com.percussion.server;


/**
 * The IPSRequestHandler interface defines the mechanism by which a
 * request is sent to the appropriate processing module.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSRequestHandler {
   /**
    * Process the request using the input context information and data.
    * The results must be written to the request's output stream via the
    * request's {@link com.percussion.server.PSResponse} object 
    * (see {@link com.percussion.server.PSRequest#getResponse()}).
    * This method is called immediately after the request is parsed by the 
    * server.  This means that security has not been checked and the user's 
    * session has not been created.
    * 
    * @param   request     the request object containing all context
    *                      data associated with the request
    */
   public void processRequest(PSRequest request);

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown();
}

