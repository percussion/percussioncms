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
package com.percussion.membership.services;

import com.percussion.delivery.utils.PSVersionHelper;
import com.percussion.membership.services.impl.PSMembershipService;
import org.glassfish.jersey.server.ResourceConfig;
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


/**
 * @author natechadwick
 *
 */

public  class PSBaseMembershipRestServiceTest extends JerseyTest
{

    public PSBaseMembershipRestServiceTest() {

    }

    /***
     * Takes the context file as an arg and spins up grizzly to
     * test rest methods.
     *
     */
    @Override
    protected Application configure() {
        return new ResourceConfig(PSMembershipService.class);
    }



    @Test
    @Ignore
	public void testGetRestVersion(){


        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("/membership/version");
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
