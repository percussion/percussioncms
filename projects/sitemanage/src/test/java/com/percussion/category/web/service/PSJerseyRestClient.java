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
