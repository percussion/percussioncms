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

package com.percussion.rest.pages;

import com.percussion.rest.MainTest;
import com.percussion.rest.errors.RestError;
import com.percussion.rest.errors.RestErrorCode;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


@Category(IntegrationTest.class)
public class PagesTest extends MainTest
{
   
    @Test
    public void testPageById()
    {
    	
    	
        Response response = target("pages/1234")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        Page page = response.readEntity(Page.class);
        assertEquals("1234", page.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
 
    }

    @Test
    public void testPageByIdNotFound()
    {
    	
        Response response = target("pages/invalidId")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.PAGE_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    }
    
    @Test
    public void testPageRoundTrip()
    {
    	
        String responseMsg = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3/page1.html")
                .request(MediaType.APPLICATION_JSON)
                .get(
                String.class);
        String putResponseMsg = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3/page1.html")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(responseMsg, MediaType.APPLICATION_JSON_TYPE),String.class);
        assertEquals(putResponseMsg, responseMsg);
    }

    @Test
    public void testPageWrongPath()
    {
    	
        String responseMsg = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/page1.html")
                .request().get(String.class);
        Response response = target("pages/by-path/sitea/path1/pathsub%20/pathsub3/page1.html")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(responseMsg));
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.LOCATION_MISMATCH.getNumVal());
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testPageWrongName()
    {
    	
        String responseMsg = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/page1.html")
                .request().get(String.class);
        Response response = target("pages/by-path/sitea/pathsub%20/pathsub2/page2.html")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(responseMsg));
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.LOCATION_MISMATCH.getNumVal());
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testPageWrongSite()
    {
    	
        String responseMsg = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3/page1.html")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        Response response = target("pages/by-path/siteb/pathsub%20/pathsub2/page1.html")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(responseMsg));
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.LOCATION_MISMATCH.getNumVal());
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testPageNameNotFound()
    {
    	
        Response response = target("pages/by-path/sitea/path1/pathsub%20/pathsub2/testNotFound")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.PAGE_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void testSiteNotFound()
    {
    	
        Response response = target("pages/by-path/testNotFound/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.SITE_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void testPathNotFound()
    {
    	
        Response response = target("pages/by-path/siteb/path1/pathsub%20/testNotFound/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.FOLDER_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    @Ignore
    public void testPage()
    {
    	
    	
        String responseMsg2 = target("pages/by-path/sitea/path1/pathsub/pathsub2/page1.html")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        System.out.println(responseMsg2);
        assertEquals(
                "{\"id\":\"id\",\"name\":\"page1.html\",\"siteName\":\"sitea\",\"folderPath\":\"path1/pathsub/pathsub2\",\"displayName\":\"Display Name\",\"templateName\":\"Template1\",\"summary\":\"Summary\",\"overridePostDate\":\"2010-10-24T04:00:00.000+0000\",\"workflow\":{\"name\":null,\"state\":\"Approval\",\"checkedOut\":true,\"checkedOutUser\":\"Admin\"},\"seo\":{\"browserTitle\":\"Browser Title\",\"metaDescription\":\"Meta Description\",\"hideSearch\":false,\"tags\":[\"Tag1\",\"Tag2\"],\"categories\":[\"Category1\",\"Category2\"]},\"calendar\":{\"startDate\":\"2010-10-24T04:00:00.000+0000\",\"endDate\":\"2010-10-24T04:00:00.000+0000\",\"calendars\":[\"Caldendar1\",\"Calendar2\"]},\"code\":{\"head\":\"HeadCode\",\"afterStart\":\"AfterStartCode\",\"beforeClose\":\"BeforeCloseCode\"},\"body\":[{\"name\":\"region1\",\"type\":\"richtext\",\"editable\":false,\"widgets\":[{\"id\":\"1234\",\"name\":\"widget1\",\"type\":\"widgetType\",\"scope\":\"local\",\"editable\":true,\"asset\":{\"fields\":{\"Field2\":\"<a href=\\\"test\\\">test<\\\\a>\",\"Field1\":\"<a href=\\\"test\\\">test<\\\\a>\"}}}]}]}",
                responseMsg2);
       /*
        assertEquals(
                responseMsg2);
                */
    }
    
    @Test
    public void testRenamePage(){
    		
    		Page p = target("pages/rename/sitea/path1/pathsub/pathsub2/page1.html/newname.html")
    		.request(MediaType.APPLICATION_JSON)
    		.accept(MediaType.APPLICATION_JSON)
    		.post(Entity.json("{}"),Page.class);
    		assertTrue("New Name Should Match", p.getName().equals("newname.html"));
    }
    
    @Test
    public void testNeverNull(){
    	Page p = new Page();
    	
    	p.setRecentUsers(null);
    	p.setBookmarkedUsers(null);
    	
    	assertTrue("Should Never be Null", p.getBookmarkedUsers()!=null);
    	assertTrue("Should Never be Null", p.getRecentUsers()!=null);
      
    }
}
