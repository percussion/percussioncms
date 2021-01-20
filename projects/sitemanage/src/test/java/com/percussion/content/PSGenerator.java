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
package com.percussion.content;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PSGenerator<CLIENT_TYPE>
{
    private CLIENT_TYPE restClient;
    protected Log log = null;

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
            
            //FB: WL_USING_GETCLASS_RATHER_THAN_CLASS_LITERAL NC 1-16-16
            synchronized (PSGenerator.class)
            {
                if (log == null)
                    log = LogFactory.getLog(getClass());
            }
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
