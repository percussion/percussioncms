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

package com.percussion.soln.p13n.delivery;

import com.percussion.soln.p13n.delivery.data.IDeliveryDataService.DeliveryDataException;

/**
 * 
 * A factory pattern for creating snippet filter contexts
 * from a request.
 * 
 * @author adamgent
 *
 */
public interface IDeliverySnippetFilterContextFactory {

    /**
     * Create a snippet filter context.
     * @param request never <code>null</code>.
     * @return never <code>null</code>.
     * @throws DeliveryException
     * @throws DeliveryDataException
     * @throws DeliveryContextException if the context cannot be created.
     */
    public IDeliverySnippetFilterContext createContext(DeliveryRequest request) 
        throws DeliveryException, DeliveryDataException, DeliveryContextException;
    
    /**
     * 
     * Indicates a failure to create a snippet filter context.
     * 
     * @author adamgent
     *
     */
    public static class DeliveryContextException extends DeliveryException {

        private static final long serialVersionUID = 1L;

        public DeliveryContextException(String message) {
            super(message);
        }

        public DeliveryContextException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeliveryContextException(Throwable cause) {
            super(cause);
        }

    }
    
}
