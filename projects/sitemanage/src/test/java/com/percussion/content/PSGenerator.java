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
package com.percussion.content;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PSGenerator<CLIENT_TYPE>
{
    private CLIENT_TYPE restClient;
    protected static final Logger log = LogManager.getLogger(PSGenerator.class);

    /**
     * See this {@link #PSGenerator(Class, String, String, String) ctor} for param
     * details. Defaults the uid/pw to Admin/demo.
     */
    public PSGenerator(Class<CLIENT_TYPE> cl, String baseUrl)
    {
        this(cl, baseUrl, "Admin", "demo");
    }
    
    /**
     * 
     * @param cl The type of generator you want.
     * @param baseUrl The url of the CM1 server, e.g. http://localhost:9992.
     * @param uid The user name to use for login, typically Admin.
     * @param pw The password for the supplied user.
     */
    public PSGenerator(Class<CLIENT_TYPE> cl, String baseUrl, String uid, String pw)
    {
        Constructor<CLIENT_TYPE> ctor;
        try
        {
            ctor = cl.getConstructor(String.class);
            restClient = ctor.newInstance(baseUrl);
            Method m = cl.getMethod("login", String.class, String.class);
            m.invoke(restClient, uid, pw);

        }
        catch (Exception e)
        {
            // this should never happen
            throw new RuntimeException(e);
        }
    }
    
    protected CLIENT_TYPE getRestClient()
    {
        return restClient;
    }
}
