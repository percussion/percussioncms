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

package com.percussion.design.catalog.exit.server;

import com.percussion.extension.IPSExtensionManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The PSExitCatalogHandler class processes extension
 * related catalog requests for the Rhythmyx server.
 *
 * @see       com.percussion.server.IPSRequestHandler
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExitCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
{
   /**
    * Construct the data related catalog handler.
    *
    * @param mgr An initialized extension manager used to perform cataloging.
    * Must not be <code>null</code>;
    *
    * @throws IllegalArgumentException if mgr is <code>null</code>.
    */
   public PSExitCatalogHandler( IPSExtensionManager mgr )
   {
      super();

      if ( null == mgr )
         throw new IllegalArgumentException( "extension mgr can't be null" );

      /* initialize m_catalogHandlers to contain all supported catalog
       * handlers
       *
       * some day, convert this to use JDK 1.2 package info instead of
       * hardcoded classes
       */
      m_catalogHandlers = new ConcurrentHashMap();

      // These two handlers are in com.percussion.design.catalog.exit.server !!!
      addHandler(new PSExtensionHandlerCatalogHandler( mgr ));
      addHandler(new PSExtensionCatalogHandler( mgr ));
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }
}

