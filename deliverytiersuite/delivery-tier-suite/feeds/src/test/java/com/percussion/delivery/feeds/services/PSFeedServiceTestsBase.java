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
package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.PSFeedsApplication;
import com.percussion.delivery.utils.PSVersionHelper;
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
		System.out.print(version);
		return version;
	}
  
}
