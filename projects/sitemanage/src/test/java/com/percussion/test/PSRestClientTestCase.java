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

package com.percussion.test;

import com.percussion.share.test.PSRestTestCase;
import com.percussion.util.IPSHtmlParameters;

import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
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
