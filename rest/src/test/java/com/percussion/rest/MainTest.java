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

package com.percussion.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.percussion.rest.errors.RestExceptionMapper;
import com.percussion.utils.testing.PSTestNetUtils;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class MainTest extends AbstractJUnit4SpringContextTests {

    public final static String ENDPOINT_HOST = "http://127.0.0.1";
    public final static String ENDPOINT_PATH = "/rest";

    public WebTarget target(String address)
    {
        
        ClientBuilder builder = ClientBuilder.newBuilder();

        String endpoint = MainTest.ENDPOINT_HOST + ":" + ContextConfiguration.port + ENDPOINT_PATH;
        WebTarget target = builder.build()
                 .register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class)
                .target(endpoint).path(address);
    

        return target;  
    }

    @BeforeClass
    public static void initialize() throws Exception {

    }


    @Test
    public void testContextLoaded(){
        assertTrue(true);
    }

    @Configuration
    @ImportResource({"classpath:META-INF/cxf/cxf.xml"})
    @ComponentScan(basePackages = {"com.percussion.rest"})
    public static class ContextConfiguration {

        public static int port;

        @Autowired
        private ApplicationContext ctx;

        @Autowired
        private JacksonJsonProvider jacksonProvider;

        @Autowired
        private JacksonContextResolver contextResolver;

        @Bean
        public JacksonJsonProvider getJacksonJsonProvider()
        {
            return new JacksonJsonProvider();
        }

        @Bean
        public JacksonContextResolver getContextResolver()
        {
            return new JacksonContextResolver();
        }

        @Bean
        public Server getServer() {

            LinkedList<ResourceProvider> resourceProviders = new LinkedList<>();
            for (String beanName : ctx.getBeanDefinitionNames()) {
                if (ctx.findAnnotationOnBean(beanName, Path.class) != null) {
                    SpringResourceFactory factory = new SpringResourceFactory(beanName);
                    factory.setApplicationContext(ctx);
                    resourceProviders.add(factory);
                }
            }
            Map<Object, Object> extensionMap = new HashMap<>();
            extensionMap.put("json","application/json");
            extensionMap.put("xml", "application/xml");

            final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
            port = PSTestNetUtils.findFreePort();
            RestExceptionMapper exceptionMapper = new RestExceptionMapper();
            String endpoint = MainTest.ENDPOINT_HOST + ":" + port + ENDPOINT_PATH;
            factory.setExtensionMappings(extensionMap);
            factory.setBus(ctx.getBean(SpringBus.class));
            factory.setProviders(Arrays.asList(exceptionMapper,jacksonProvider,contextResolver));
            factory.setResourceProviders(resourceProviders);
            factory.setAddress(endpoint);
            return factory.create();
        }

    }
}
