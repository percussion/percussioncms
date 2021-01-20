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

package com.percussion.design.catalog.exit.server;

import com.percussion.extension.IPSExtensionManager;

import java.util.Hashtable;

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
      m_catalogHandlers = new Hashtable();

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

