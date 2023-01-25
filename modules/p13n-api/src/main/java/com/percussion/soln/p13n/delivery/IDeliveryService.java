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

import java.util.List;

/**
 * Delivery Service is responsible for delivering dynamic content to the visitor.
 * 
 * @author adamgent
 *
 */
public interface IDeliveryService {
    
    /**
     * Indicates if the request was successfully processed.
     * The {@link DeliveryResponse response} contains the string version
     * of this enum ({@link #name()})
     * This is due to some serialization issues with enum types in some Java libraries.
     * @author adamgent
     */
    public enum ResponseStatus {
        OK, ERROR, WARN;
    }
    
    /**
     * Delivers dynamic content by processing the given delivery request.
     * 
     * @param request the delivery request.
     * @return the delivery response, never <code>null</code.
     * @throws DeliveryServiceFatalException should only throw a fatal exception.
     *  All other exceptions will be in the response.
     * @see DeliveryResponse
     */
    public DeliveryResponse deliver(DeliveryRequest request) throws DeliveryServiceFatalException;
    
    public List<IDeliverySnippetFilter> findSnippetFilters(List<String> name) throws DeliveryException;
    
    /**
     * Registers a snippet filter with the delivery service.
     * This is for plugin registration.
     * @param name never <code>null</code>, empty, or blank.
     * @param snippetFilter never <code>null</code>.
     * @throws DeliveryException unable to register snippet filters.
     */
    public void registerSnippetFilter(String name, IDeliverySnippetFilter snippetFilter) throws DeliveryException;
    
    /**
     * This should be the only exception thrown by the delivery service when delivering if there is a failure.
     * @author adamgent
     *
     */
    public static class DeliveryServiceFatalException extends DeliveryException {

        private static final long serialVersionUID = 1L;

        public DeliveryServiceFatalException(String message) {
            super(message);
        }

        public DeliveryServiceFatalException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeliveryServiceFatalException(Throwable cause) {
            super(cause);
        }

    }
    
}
