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

package com.percussion.design.catalog.function.server;

import com.percussion.design.catalog.PSCatalogRequestHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class processes database functions related catalog requests for the
 * Rhythmyx server.
 *
 * @see com.percussion.server.IPSRequestHandler
 */
public class PSFunctionCatalogHandler extends PSCatalogRequestHandler
{
   /**
    * Construct the database functions catalog handler.
    *
    * @throws IllegalArgumentException if <code>dbFuncMgr</code> is
    * <code>null</code>
    */
   public PSFunctionCatalogHandler()
   {
      m_catalogHandlers = new ConcurrentHashMap();
      addHandler(new PSDatabaseFunctionCatalogHandler());
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }
}

