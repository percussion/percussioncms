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
