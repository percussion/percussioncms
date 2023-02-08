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

package com.percussion.category.web.service;

import com.percussion.category.data.PSCategory;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;

public class PSJerseyRestClient {
	
	private static final Logger log = LogManager.getLogger(PSJerseyRestClient.class);
	//private String contextUrl = "http://localhost:9992/Rhythmyx";
	private static String baseUrl;
	
	private static Client client = ClientBuilder.newClient();
	private static WebTarget WebTarget;
	
	static {
		if (baseUrl == null) {
            Properties cactusProps = new Properties();
            InputStream stream = PSRestTestCase.class.getResourceAsStream("/cactus.properties");
            if (stream == null) throw new RuntimeException("Cannot find cactus.properties");
            try {
				cactusProps.load(stream);
			} catch (IOException e) {
				log.error("IO exception occurred during getting the cactus properties ! " + e);
			}
            baseUrl = cactusProps.getProperty("cactus.contextURL");
        }
	}

	public PSCategory getData(String path) {

		
		WebTarget = client.target(path);
		PSCategory response = WebTarget.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE).get(PSCategory.class);

		System.out.println("Output from Server .... \n");
		System.out.println(response);
		
		return response;
	}
	
	public PSCategory postData(String path, PSCategory data) {
		
		WebTarget = client.target(path);
		PSCategory response = WebTarget.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE).post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE), PSCategory.class );

		System.out.println("Output from Server .... \n");
		
		System.out.println(response);
		
		return response;
	}
	
	public String concatPath(String start, String ... end) {
        isTrue(isNotBlank(start), "start cannot be blank");
        notEmpty(end, "Must have end paths.");
        String path = start;
        for (String p : end ) {
            path = removeEnd(path, "/") + "/" + removeStart(p, "/");
        }
        return baseUrl+path;
    }
	
	public void login(String userName, String password) {

		final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(userName,password);
        client.register(authFilter); 
	}
	
	public class HTTPBasicAuthFilter implements ClientRequestFilter {

	    private final String user;
	    private final String password;

	    public HTTPBasicAuthFilter(String user, String password) {
	        this.user = user;
	        this.password = password;
	    }

	    public void filter(ClientRequestContext requestContext) throws IOException {
	        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
	        final String basicAuthentication = getBasicAuthentication();
	        headers.add("Authorization", basicAuthentication);

	    }

	    private String getBasicAuthentication() {
	        String token = this.user + ":" + this.password;
	            return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
	    }
	}

	
}
