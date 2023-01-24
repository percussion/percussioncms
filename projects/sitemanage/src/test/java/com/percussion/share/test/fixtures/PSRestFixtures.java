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

package com.percussion.share.test.fixtures;

import static org.junit.Assert.assertEquals;

import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.util.IPSHtmlParameters;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXB;


public class PSRestFixtures
{
    private WebTarget r;
    private Client c;
    
    public static final String siteServiceRoot = "services/sitemanage/site";
    public static final String templateServiceRoot = "services/pagemanagement/template/";

    public static final String SITE_NAME_PREFIX = "restFixtures";
    
    public PSRestFixtures(Client c, WebTarget r)
    {
        this.c=c;
        this.r=r;
    }
    
    public void createSite()
    {
        List<PSTemplateSummary> templates = findTemplates();
        
        String defaultTemplateId = templates.get(0).getId();
        PSTemplate template = createTemplate(SITE_NAME_PREFIX+"1",defaultTemplateId);
        
      
        WebTarget wr = r.path(siteServiceRoot);
            PSSite site = new PSSite();
            site.setName(SITE_NAME_PREFIX + "--" + System.currentTimeMillis());
            site.setLabel("My test site");
            site.setHomePageTitle("homePageTitle");
            site.setNavigationTitle("navigationTitle");
            site.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
            site.setTemplateName(template.getName());
            
            StringWriter sw = new StringWriter();
            JAXB.marshal(site, sw);
            System.out.println("output="+sw.getBuffer().toString());
            
            
            Response  response = getBuilder(wr,c)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(site, MediaType.APPLICATION_JSON_TYPE));  
            
            assertEquals(Status.OK, response.getStatus());
    }
    
    public List<PSTemplateSummary> findTemplates()
    {
        WebTarget wr = r.path(templateServiceRoot).path("summary/all/readonly");
        PSTemplateSummary[] summaries = getBuilder(wr,c)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(PSTemplateSummary[].class);  
        return Arrays.asList(summaries);
    }
    
    
    public PSTemplate createTemplate(String name, String srcId)
    { 
        WebTarget wr = r.path(templateServiceRoot).path("create")
                .path(name).path(srcId);
        PSTemplate template = getBuilder(wr,c)      
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(PSTemplate.class);
        
        return template;
    }

    protected Builder getBuilder(WebTarget wr, Client client, String userName)
    {
        Builder b = wr.request(MediaType.APPLICATION_JSON_TYPE);
        b = addAuth(b, userName,"demo");
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    
    protected Builder getBuilder(WebTarget wr, String userName)
    {
        Builder b = wr.request(MediaType.APPLICATION_JSON_TYPE);
        b = addAuth(b, userName,"demo");
        return  wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    
    protected Builder getBuilder(WebTarget wr)
    {
        Builder b = wr.request(MediaType.APPLICATION_JSON_TYPE);
        b = addAuth(b, "Admin","demo");
        return b.header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    protected static Builder getBuilder(WebTarget wr, Client client)
    {
        Builder b = wr.request(MediaType.APPLICATION_JSON_TYPE);
        b = addAuth(b, "Admin","demo");
        return wr.request(MediaType.APPLICATION_JSON_TYPE).header(IPSHtmlParameters.SYS_USE_BASIC_AUTH,Boolean.TRUE);
    }
    
    private static Builder addAuth(Invocation.Builder b, String username, String password)
    {
        String usernameAndPassword = username + ":" + password;
        
        String authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString( usernameAndPassword.getBytes() );
        b.header("Authorization", authorizationHeaderValue);
        return b;
    }
    
}
