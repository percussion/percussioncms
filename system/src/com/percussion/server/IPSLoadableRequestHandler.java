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

