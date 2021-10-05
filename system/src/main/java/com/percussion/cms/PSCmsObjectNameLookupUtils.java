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

package com.percussion.cms;

import com.percussion.server.IPSRequestContext;

import java.util.HashMap;
import java.util.Map;

public class PSCmsObjectNameLookupUtils {

    /**
     * Constant for key used to store the cache in the request.
     */
    private static final String CACHE_KEY = "sys_cmsObjectNameLookupCache";

    /**
     * Initializes the cache in the current request. If this extension is invoked
     * more than once per overall request, but via more than one internal
     * requests, since the cache is initialized in a clone of the request it's
     * lifetime is then tied to each internal request and not the overall
     * request. In this case this method should be called with the top level
     * request object before the internal requests are made so that each cloned
     * request used by the internal request has the same instance of the cache
     * in it's private objects.
     *
     * @param request The current request, may not be <code>null</code>.
     *
     * @return The map used as the cache, never <code>null</code>.
     */
    public static Map initLookupCache(IPSRequestContext request)
    {
        if (request == null)
            throw new IllegalArgumentException("request may not be null");

        Map cache = (Map)request.getPrivateObject(CACHE_KEY);
        if (cache == null)
        {
            cache = new HashMap();
            request.setPrivateObject(CACHE_KEY, cache);
        }

        return cache;
    }
}
