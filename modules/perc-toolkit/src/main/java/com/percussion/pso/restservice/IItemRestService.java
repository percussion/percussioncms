/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
 package com.percussion.pso.restservice;

import com.percussion.pso.restservice.exception.ItemRestException;
import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.model.Items;
import com.percussion.pso.restservice.model.results.PagedResult;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 */
@Path("/Content/")
public interface IItemRestService {
	/**
	 * Method updateItem.
	 * @param item Item
	 * @return Item
	 */
	
	
	public Item updateItem(Item item);
	
	@POST
	@Path("/")
	@Consumes("text/xml")
	public Item updateItem(Item item, @QueryParam("updateOnly") @DefaultValue("false") boolean updateOnly  );
	
	/**
	 * Method getItem.
	 * @param id int
	 * @return Item
	 */
	@GET
	@Path("{id}")
	public Item getItem(@PathParam("id") int id);
	
	/**
	 * Method getItemRev.
	 * @param id int
	 * @param rev int
	 * @return Item
	 */
	@GET
	@Path("{id}/{rev}")
	public Item getItemRev(@PathParam("id") int id,@PathParam("rev") int rev);
	
	/**
	 * Method updateItem.
	 * @param id int
	 * @param item Item
	 * @return Item
	 */
	@POST
	@Path("{id}")
	@Consumes("text/xml")
	public Item updateItem(int id, Item item);
	
	/**
	 * Method purgeItem.
	 * @param id int
	 * @return Item
	 */
	@DELETE
	@Path("{id}")
	public Item purgeItem(@PathParam("id")int id);
	
	@DELETE
	@Path("/PurgeFolder/{target:.*}")
	public Response PurgeAllFolderContent(@PathParam("target")String target);
		
	
	/**
	 * Method updateItem.
	 * @param templateName String
	 * @param body String
	 * @param debug boolean
	 * @return Items
	 */
	@POST
	@Path("import/{template}")
	public Items updateItem(@PathParam("template") String templateName, String body, @QueryParam("debug") boolean debug, @QueryParam("param") String param);
	
	/**
	 * Method getFolders.
	 * @param n Integer
	 * @return PagedResult
	 */
	@GET
	@Path("/Folders/")
	public PagedResult getFolders(@QueryParam("n") Integer n);
	
	/**
	 * Method getItems.
	 * @param n Integer
	 * @return PagedResult
	 */
	@GET
	@Path("/AllContent/")
	public PagedResult getItems(@QueryParam("n") Integer n);
	/**
	 * Method getItems.
	 * @param path String
	 * @param n Integer
	 * @return PagedResult
	 */
	@GET
	// On upgrade use @Path("/Sites/{search:.*}") remove limited
	//@Path(value="/Sites/{search}", limited=false)
	@Path("/Sites/{search:.*}")
	public PagedResult getItems(@PathParam("search") String path, @QueryParam("n") Integer n);
	
	@Path("/Type/{typename}")
	public PagedResult getTypeItems(@PathParam("typename") String type,@QueryParam("n") Integer n);

	
	@GET
	@Path("{id}/{rev}/field/{fieldname}")
	@Produces("*/*")
	public Response getFile(@PathParam("id") int id,
			@PathParam("rev") int revision, @PathParam("fieldname") String field);
	
	@GET
	@Path("{id}/field/{fieldname}")
	@Produces("*/*")
	public Response getFile(@PathParam("id") int id, @PathParam("fieldname") String field);
		
	@GET
	@Path("/importfeeds/")
	public Response updateItems(@QueryParam("debug")boolean debug, @QueryParam("content_type")String content_type);
		
	/***
	 * Given a content id for a Feed Definition will import the specified feed.
	 *
	 * @param  debug
	 * @param contentId
	 * @param folderId
	 * @return
	 */
	@GET
	@Path("/importfeed/")
	public Response updateItem(@QueryParam("debug")boolean debug, @QueryParam("sys_contentid")int contentId, @QueryParam("sys_folderid")int folderId );

	/***
	 * Finds an item by a key field.
	 * @return null or the Item.
	 * @throws ItemRestException 
	 */
	@GET
	@Path("/find/v/{value}/k/{keyfield}/p/{contextRoot}/")
	public Item findByKeyField(@PathParam("value") String value, @PathParam("keyfield") String keyfield, @PathParam("contextRoot") String contextRoot);
}
