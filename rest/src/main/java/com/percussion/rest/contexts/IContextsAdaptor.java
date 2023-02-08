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

package com.percussion.rest.contexts;

import com.percussion.rest.errors.BackendException;

import java.net.URI;
import java.util.List;

/***
 * Defines the adaptor interface for publishing Contexts
 */
public interface IContextsAdaptor {

    /***
     * Delete a publishing Context by id
     * @param baseURI referring url
     * @param id A string guid id
     */
    public void deleteContext(URI baseURI, String id) throws BackendException;

    /***
     * Get a publishing context by it's ID
     * @param baseUri referring uri
     * @param id A string guid id
     * @return The publishing Conext
     */
    public Context getContextById(URI baseUri, String id) throws BackendException;

    /***
     * List all publishing contexts configured on the system
     * @param baseURI
     * @return a list of publishing contexts
     */
    public List<Context> listContexts(URI baseURI) throws BackendException;

    /***
     * Create or update a publishing context
     * @param baseURI referring url
     * @param context a fully initialized Context
     * @return The updated context
     */
    public Context createOrUpdateContext(URI baseURI, Context context) throws BackendException;
}
