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

import com.percussion.conn.PSServerException;

import java.io.InputStream;
import java.util.Collection;

/**
 * The IPSLoadableRequestHandler interface defines the mechanism by which a
 * loadable request handler is initialized.
 */
public interface IPSLoadableRequestHandler extends IPSRootedHandler
{
   /**
    * Initializes the request handler. Called by the server when  the handler is
    * loaded, before using the handler to process a request.  The handler should
    * perform any one time processing it needs to do before processing requests
    * in this methos.  Handlers are defined in the {@link
    * PSRequestHandlerConfiguration}.
    *
    * @param requestRoots The list of request names which this handler wants to
    * process as Strings, not <code>null</code> or empty.  See description of
    * <code>RequestRoots</code> element in DTD for Request Handler Configuration
    * XML found in {@link PSRequestHandlerConfiguration} class description, and
    * {@link IPSRootedHandler} for more information.  This Collection of request
    * roots should be used to implement the {@link
    * IPSRootedHandler#getRequestRoots()} method.
    * 
    * @param cfgFileIn An input stream to its config file if one is defined in
    * the Request Handler Configuration.  May be <code>null</code> if no config
    * file is required.  Handler is responsible for closing the stream when
    * finished with it.  See the <code>configFile</code> attribute of the
    * <code>RequestHandlerDef</code> element defined in the {@link
    * PSRequestHandlerConfiguration} class description for more information.
    *
    * @throws PSServerException if the handler fails to initialize.
    */
   public void init(Collection requestRoots, InputStream cfgFileIn) 
      throws PSServerException;
}

