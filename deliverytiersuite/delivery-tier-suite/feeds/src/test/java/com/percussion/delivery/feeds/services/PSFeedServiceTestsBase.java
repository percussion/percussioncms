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
package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.PSFeedsApplication;
import com.percussion.delivery.utils.PSVersionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * The base class for Feeds Rest Service Tests
 * 
 * @author natechadwick
 *
 */
public abstract class PSFeedServiceTestsBase extends JerseyTest {


    private String _appContext;

    private static final Logger log = LogManager.getLogger(PSFeedServiceTestsBase.class);

    static
    {
        System.setProperty("jersey.config.test.container.port", "9980");
    }

    /***
     * Takes the context file as an arg and spins up grizzly to
     * test rest methods.
     *
     * @param appContext
     */
//    @Override
//    protected Application configure() {
//        ResourceConfig resourceConfig =  new ResourceConfig(PSFeedService.class);
//        resourceConfig.register(SpringServlet.class);
//        resourceConfig.register(ContextLoaderListener.class);
//        resourceConfig.register(RequestContextListener.class);
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-beans.xml");
//      //  rc.property("contextConfig", ctx);
//
////        resourceConfig.getConfiguration().
////        resourceConfig.property("contextConfig", PSConfigurableApplicationContext.class);
//        return resourceConfig;
//    }
//
//    @Override
//    protected DeploymentContext configureDeployment() {
//        return ServletDeploymentContext
//                .forServlet(new ServletContainer())
//                .addListener(ContextLoaderListener.class)
//                .contextParam(ContextLoader.CONTEXT_CLASS_PARAM, AnnotationConfigWebApplicationContext.class.getName())
//                .addFilter(DelegatingFilterProxy.class, "springSecurityFilterChain")
//                .contextPath("classpath:test-beans.xml")
//                .build();
//    }


    @Override
    protected URI getBaseUri() {
        return URI.create("http://localhost:9980");
    }

    @Override
    protected Application configure() {
        PSFeedsApplication application = new PSFeedsApplication();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put("contextConfigLocation", "classpath:test-beans.xml");

        application.setProperties(properties);
        application.register(this);
        return application;
    }



    public String get_appContext() {
        return _appContext;
    }

    public void set_appContext(String _appContext) {
        this._appContext = _appContext;
    }


    /***
     * Override the port.
     */
   // @Override
    protected int getPort(int port){
        return 10178;
    }

    @Test
    @Ignore("Crazy class version error")
	public void testGetRestVersion(){

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("/rss/version");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        Assert.assertNotNull(response);
        Assert.assertEquals(200,response.getStatus());
        Assert.assertEquals(testGetVersion(), response.getEntity());

	}


	private String testGetVersion(){
		String version = PSVersionHelper.getVersion(this.getClass());
		Assert.assertNotNull(version);
		log.info(version);
		return version;
	}
  
}
