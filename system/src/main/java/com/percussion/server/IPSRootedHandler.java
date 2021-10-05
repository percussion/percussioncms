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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.server;

import java.util.Iterator;

/**
 * An interface to be implemented by all handlers which have to go into
 * the list of rooted handlers.  A rooted handler is one that may handle an
 * HTTP request made against the Rhythmyx server, and is identified as the
 * appropriate handler for those requests based on the request root.  The
 * request root is the portion of the URI immediately following the Rhytmyx
 * server root.
 * <p>
 * Thus if a Rhythmyx server is handling any HTTP requests made to
 * "http://myServer:9992/Rhythmyx/...",  then in the request
 * "http://myserver:9992/Rhythmyx/foo/bar.html", "foo" is the request root.
 * Request roots may not contain multiple levels, so in the request
 * "http://myserver:9992/Rhythmyx/foo/images/bar.gif", "foo" is the request
 * root.
 */
public interface IPSRootedHandler extends IPSRequestHandler
{
   /**
    * Get the name of the rooted handler. Used by the server to identify the
    * handler during intilization and when reporting information about all
    * rooted handlers. All rooted handlers will be served by rhythmyx at
    * runtime.
    *
    * @return the handler name, should be unique across all rooted
    *    handlers, never <code>null</code> or empty. If <code>null</code>
    *    or empty the server will ignore this handler. If not unique, the
    *    results will be unpredictable as to which handler will receive the
    *    request for processing.
    */
   public String getName();

   /**
    * Get all request roots of the rooted handler.  Called by the server when
    * it is initializing the handler.
    *
    * @return an iterator over one or more request roots as Strings. The
    *    iterator must contain at least one entry, and should not contain
    *    duplicates. Never <code>null</code> or empty. If <code>null</code> or
    *    empty the server will ignore this handler.
    */
   public Iterator getRequestRoots();
}
