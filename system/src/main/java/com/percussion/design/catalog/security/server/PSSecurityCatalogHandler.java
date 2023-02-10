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

package com.percussion.design.catalog.security.server;

import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSSecurityCatalogHandler class processes security related catalog
 * requests for the E2 server. This class interfaces with the security
 * providers directly to perform its cataloging.
 * 
 * @see       com.percussion.server.IPSRequestHandler
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSecurityCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
{
   /**
    * Construct the data related catalog handler.
    */
   public PSSecurityCatalogHandler()
   {
      super();
      
      /* initialize m_catalogHandlers to contain all supported catalog
       * handlers
       *
       * some day, convert this to use JDK 1.2 package info instead of
       * hardcoded classes
       */
      m_catalogHandlers = new ConcurrentHashMap();
      addHandler(new PSCatalogerCatalogHandler());
      addHandler(new PSProviderCatalogHandler());
      addHandler(new PSServerCatalogHandler());
      addHandler(new PSObjectTypesCatalogHandler());
      addHandler(new PSObjectCatalogHandler());
      addHandler(new PSAttributesCatalogHandler());
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

