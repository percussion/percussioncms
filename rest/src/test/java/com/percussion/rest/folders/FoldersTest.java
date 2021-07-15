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

package com.percussion.rest.folders;

import com.percussion.rest.MainTest;
import com.percussion.rest.MoveFolderItem;
import com.percussion.rest.Status;
import com.percussion.rest.errors.RestError;
import com.percussion.rest.errors.RestErrorCode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertEquals;

@Category(IntegrationTest.class)
public class FoldersTest extends MainTest
{

	public void testFolder()
    {
    
        String responseMsg = target("folders/by-path/sitea/path1/pathsub/pathsub2").request().get(String.class);
          
        assertEquals(
                "{\"id\":\"aaaa-aaaa-aaa\",\"name\":\"pathsub2\",\"siteName\":\"sitea\",\"path\":\"path1/pathsub\",\"workflow\":\"default\",\"accessLevel\":\"READ\",\"editUsers\":[\"User1\",\"User2\"],\"sectionInfo\":{\"displayTitle\":\"Section Display Title\",\"targetWindow\":\"top\",\"navClass\":\"navclass1\",\"templateName\":\"Template 1\",\"landingPage\":{\"name\":\"file1.html\",\"href\":\"http://test.com/index.html\"}},\"pages\":[{\"name\":\"file1.html\",\"href\":\"http://test.com/index.html\"},{\"name\":\"file1.html\",\"href\":\"http://test.com/file1.html\"},{\"name\":\"file2.html\",\"href\":\"http://test.com/file2.html\"}],\"subfolders\":[{\"name\":\"sub1\",\"href\":\"http://test.com/file1.html\"},{\"name\":\"sub2\",\"href\":\"http://test.com/file2.html\"}],\"subsections\":[{\"name\":\"subsection1\",\"href\":\"http://test.com/file1.html\",\"type\":\"external\"},{\"name\":\"subsection2\",\"href\":\"http://test.com/file2.html\",\"type\":\"internal\"},{\"name\":\"subsection2\",\"href\":\"http://test.com/file2.html\",\"type\":\"subfolder\"}]}",
                responseMsg);
      
    }
    
    public void testFolderById()
    {
   
        Response response = target("folders/1234").request().get();
        Folder folder = response.readEntity(Folder.class);
        assertEquals("1234", folder.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
 
    }

    public void testFolderByIdNotFound()
    {
   
        Response response = target("folders/invalidId").request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.FOLDER_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    }
    
    

    public void testFolderRoundTrip()
    {
    
        String responseMsg = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        Response putResponseMsg = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(responseMsg, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(putResponseMsg, responseMsg);
    }

    public void testFolderWrongPath()
    {

        Response responseMsg = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        Response response = target("folders/by-path/sitea/path1/pathsub%20/pathsub3/pathsub4")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(responseMsg.getEntity(), MediaType.APPLICATION_JSON_TYPE));
                

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    public void testFolderWrongName()
    {
  
        String responseMsg = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        Response response = target("folders/by-path/sitea/pathsub%20/pathsub2/pathsubx")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(responseMsg, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    public void testFolderWrongSite()
    {
    	
        String responseMsg = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        Response response = target("folders/by-path/siteb/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(responseMsg, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }
    
    public void testFolderNameNotFound()
    {
    	
        Response response = target("folders/by-path/sitea/path1/pathsub%20/pathsub2/testNotFound")
                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.FOLDER_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    public void testSiteNotFound()
    {
    	
        Response response = target("folders/by-path/testNotFound/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.SITE_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    public void testPathNotFound()
    {
    	
        Response response = target("folders/by-path/siteb/path1/pathsub%20/testNotFound/pathsub3")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.FOLDER_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    public void testDeleteFolder()
    {
    	
        Response response = target("folders/by-path/siteb/path1/pathsub%20/pathsub2/pathsub3")
                .request(MediaType.APPLICATION_JSON_TYPE).delete(Response.class);
        Status status = response.readEntity(Status.class);
        assertEquals("Deleted",status.getMessage());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
    
    public void testDeleteFolderNotFound()
    {
    	
        Response response = target("folders/by-path/siteb/path1/pathsub%20/pathsub2/testNotFound")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .delete(Response.class);
        RestError ex = response.readEntity(RestError.class);
        assertEquals(ex.getErrorCode(), RestErrorCode.FOLDER_NOT_FOUND.getNumVal());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void testMoveFolderItem(){
    	
    	MoveFolderItem request = new MoveFolderItem("/site/folder/item","/site/newfolder/newsub");
    	
    	
    	
        Response response =	target("folders/move/item")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        
        Status s = response.readEntity(Status.class);
        
        assertEquals(s.getMessage(), "Moved OK");
    	
    }
    
    @Test
    public void testMoveFolder(){
MoveFolderItem request = new MoveFolderItem("/site/folder","/site/newfolder/newsub");
    	
    	
    	
        Response response =	target("folders/move/folder")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        
       Status status = response.readEntity(Status.class);
   
       assertEquals(status.getMessage(),"Moved OK");
    }
    
    @Test
    public void testRenameFolder(){
    	
    	
    	
        Response response =	target("folders/rename/site/folder/subfolder/newsubfoldername")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
        
        Folder f = response.readEntity(Folder.class);
        
        assertEquals(f.getPath(), "folder");
        assertEquals(f.getSiteName(), "site");
        assertEquals(f.getName(), "newsubfoldername"); 
    	
    }
    
}
