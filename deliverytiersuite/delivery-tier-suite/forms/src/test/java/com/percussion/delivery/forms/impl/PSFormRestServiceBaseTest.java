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
package com.percussion.delivery.forms.impl;

import com.percussion.delivery.utils.PSVersionHelper;
import com.percussion.delivery.utils.spring.PSConfigurableApplicationContext;
import junit.framework.Assert;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author natechadwick
 *
 */
public class PSFormRestServiceBaseTest extends JerseyTest {


	public PSFormRestServiceBaseTest(){

	}

	/***
	 * Takes the context file as an arg and spins up grizzly to
	 * test rest methods.
	 */
	@Override
	protected Application configure() {
		ResourceConfig resourceConfig =  new ResourceConfig(PSFormRestService.class);
		resourceConfig.property("contextConfig", PSConfigurableApplicationContext.class);
		return resourceConfig;

	}

	@Override
	protected DeploymentContext configureDeployment(){
		return ServletDeploymentContext
				.forPackages("com.percussion.delivery.forms.impl")
				.servletClass(HttpServlet.class)
				.contextPath("perc-form-processor")
				.addListener(ContextLoaderListener.class)
				.addListener(RequestContextListener.class)
				.addFilter(org.springframework.web.filter.DelegatingFilterProxy.class, "tenantAuthorizationFilter")
				.build();
	}

	@Test
	@Ignore
	public void testGetRestVersion(){
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("/forms/version");
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

	@Test
	@Ignore ("Integration tests are failing")
	public void testCSRF(){
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target("/forms/csrf");
		Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();
		System.out.println(response.getEntity());
	}

}
