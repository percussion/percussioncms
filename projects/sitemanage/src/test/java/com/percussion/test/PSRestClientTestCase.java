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

package com.percussion.test;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Properties;

public class PSRestClientTestCase
{

    private static final Logger log = LogManager.getLogger(PSRestClientTestCase.class);

    protected static final Client c;
    //private static final DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();

    protected static String baseUrl = null;
    
    
  
    
    static  {
       // cc.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);       
        //c = ApacheHttpClient.create(cc);
        c = ClientBuilder.newClient();
    }
    
    protected WebTarget r;
    
    protected void setUp() throws Exception
    {
        try {
            if (baseUrl == null) {
                Properties cactusProps = new Properties();
                InputStream stream = PSRestTestCase.class.getResourceAsStream("/cactus.properties");
                if (stream == null) throw new RuntimeException("Cannot find cactus.properties");
                cactusProps.load(stream);
                baseUrl = cactusProps.getProperty("cactus.contextURL");
            }
            } catch (Exception e)
            {
                log.error(PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }
       
        
        r = c.target(baseUrl);
       
     
    
    }

    protected Builder getBuilder(WebTarget wr, Client client, String userName)
    {
        
        final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(userName,"demo");
        client.register(authFilter);    
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    
    protected Builder getBuilder(WebTarget wr, String userName)
    {
        
        final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(userName,"demo");
        c.register(authFilter);    
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    
    protected Builder getBuilder(WebTarget wr)
    {
        final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter("Admin","demo");
        c.register(authFilter);
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    protected static Builder getBuilder(WebTarget wr, Client client)
    {
        final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter("Admin","demo");
        client.register(authFilter);
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
}
