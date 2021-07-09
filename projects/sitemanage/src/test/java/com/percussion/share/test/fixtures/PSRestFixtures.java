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
