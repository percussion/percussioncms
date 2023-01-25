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
