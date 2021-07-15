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

package com.percussion.delivery.utils.spring;

import org.apache.logging.log4j.LogManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.servlet.Servlet;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import org.apache.logging.log4j.Logger;

public class SpringAwareGrizzlyTestContainerFactory implements TestContainerFactory {

    private Object springTarget;

    public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
        return new SpringAwareGrizzlyWebTestContainer(baseUri, deploymentContext, springTarget);
    }


    private static class SpringAwareGrizzlyWebTestContainer implements TestContainer {

        private static final Logger log = LogManager.getLogger(SpringAwareGrizzlyWebTestContainer.class.getName());
        private URI baseUri;
        private HttpServer webServer;
        private Object springTarget;
        private Servlet servletInstance;

        private SpringAwareGrizzlyWebTestContainer(URI baseUri, DeploymentContext context, Object springTarget) {
            this.springTarget = springTarget;
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(context.getContextPath()).path(context.getContextPath())
                    .build();

            log.info("Creating Grizzly Web Container configured at the base URI " + this.baseUri);
            this.webServer = GrizzlyHttpServerFactory.createHttpServer(this.baseUri, context.getResourceConfig(), false);
        }

        public Client getClient() {

            Client client = ClientBuilder.newClient();
            return client;
        }

        @Override
        public ClientConfig getClientConfig() {
            return null;
        }

        public URI getBaseUri() {
            return baseUri;
        }

        public void start() {
            log.info("Starting the Grizzly Web Container...");

            try {
                webServer.start();
               // autoWireSpringTarget();
            } catch (IOException ex) {
                throw new TestContainerException(ex);
            }

        }

        public void stop() {
            log.info("Stopping the Grizzly Web Container...");

            webServer.shutdown();
        }

//        private void instantiateGrizzlyWebServer(DeploymentContext deploymentContext, Object springTarget) {
//            webServer = new HttpServer.createSimpleServer("",baseUri.getPort());
//            ServletAdapter sa = new ServletAdapter();
//            sa.setProperty("load-on-startup", 1);
//            servletInstance = createrServletInstance(ad.getServletClass());
//            sa.setServletInstance(servletInstance);
//
//            populateEventListeners(sa, deploymentContext.getListeners());
//            populateFilterDescriptors(sa, deploymentContext.getFilters());
//            populateContextParams(sa, ad.getContextParams());
//            populateInitParams(sa, ad.getInitParams());
//            setContextPath(sa, ad.getContextPath());
//            setServletPath(sa, ad.getServletPath());
//
//            String[] mapping = null;
//            webServer.addGrizzlyAdapter(sa, mapping);

        }

//        private void setServletPath(ServletAdapter sa, String servletPath) {
//            if ( notEmpty(servletPath)) {
//                sa.setServletPath(servletPath);
//            }
//        }
//
//        private void setContextPath(ServletAdapter sa, String contextPath) {
//            if (notEmpty(contextPath)) {
//                sa.setContextPath(ensureLeadingSlash(contextPath));
//            }
//        }

        private boolean notEmpty(String string) {
            return string != null && string.length() > 0;
        }

//        private void populateInitParams(ServletAdapter sa, Map<String, String> initParams) {
//            for (String initParamName : initParams.keySet()) {
//                sa.addInitParameter(initParamName, initParams.get(initParamName));
//            }
//
//        }
//
//        private void populateContextParams(ServletAdapter sa, Map<String, String> contextParams) {
//            for (String contextParamName : contextParams.keySet()) {
//                sa.addContextParameter(contextParamName, contextParams.get(contextParamName));
//            }
//        }
//
//        private void populateFilterDescriptors(ServletAdapter sa, List<FilterDescriptor> filters) {
//            if (filters != null) {
//                for (WebAppDescriptor.FilterDescriptor d : filters) {
//                    sa.addFilter(instantiate(d.getFilterClass()), d.getFilterName(), d.getInitParams());
//                }
//            }
//
//        }
//
//        private void populateEventListeners(ServletAdapter sa, List<Class<? extends EventListener>> listeners) {
//            for (Class<? extends EventListener> eventListener : listeners) {
//                sa.addServletListener(eventListener.getName());
//            }
//        }

        private String ensureLeadingSlash(String string) {
            return (string.startsWith("/") ? string : "/".concat(string));
        }

//        private Servlet createrServletInstance(Class<? extends HttpServlet> servletClass) {
//            return ( servletClass == null ? new SpringServlet() : instantiate(servletClass));
//        }

        private <I> I instantiate(Class<? extends I> clazz) {
            I instance = null;
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException e) {
                throw new TestContainerException(e);
            } catch (IllegalAccessException e) {
                throw new TestContainerException(e);
            }
            return instance;
        }
//
//        private void autoWireSpringTarget() {
//            WebApplicationContext ctx = WebApplicationContextUtils
//                    .getRequiredWebApplicationContext(servletInstance.getServletConfig()
//                            .getServletContext());
//            AutowireCapableBeanFactory beanFactory = ctx
//                    .getAutowireCapableBeanFactory();
//            beanFactory.autowireBean(springTarget);
//        }

  //  }

}
