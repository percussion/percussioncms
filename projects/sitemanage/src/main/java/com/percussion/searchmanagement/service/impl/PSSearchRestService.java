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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.searchmanagement.service.impl;

import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.services.useritems.data.PSUserItem;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedItemPropertiesList;
import com.percussion.webservices.PSWebserviceUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/search")
@Component("searchRestService")
@Api(value = "/search")
@SwaggerDefinition(
        info = @Info(
                description = "Search Rest Service",
                version="1.0.0",
                title = "Search Rest Service",
                contact = @Contact(
                        name = "Percussion Software",
                        email = "support@percussion.com"
                )
        )
)
public class PSSearchRestService
{
    private static final String SEARCH_TYPE_MY_PAGES = "MyPages";
    private IPSSearchService searchService;
    private IPSItemService itemService;

    @Autowired
    public PSSearchRestService(IPSSearchService finderSearchService, IPSItemService itemService)
    {
        this.searchService = finderSearchService;
        this.itemService = itemService;
    }

    @POST
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException
    {
        PSPagedItemList itemList = new PSPagedItemList();


        //Don't run a blind search for all items - require some criteria
        if(!criteria.isEmpty() ){

            if (SEARCH_TYPE_MY_PAGES.equalsIgnoreCase(criteria.getSearchType())) {
                List<PSUserItem> userItems = itemService.getUserItems(PSWebserviceUtils.getUserName());
                List<Integer> contentIds = new ArrayList<>();
                for (PSUserItem userItem : userItems) {
                    contentIds.add(new Integer(userItem.getItemId()));
                }
                itemList = searchService.search(criteria, contentIds);
            } else {
                itemList = searchService.search(criteria);
            }
        }
        return itemList;
    }
    
    @POST
    @Path("/get/extendedresults")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemPropertiesList extendedSearch(PSSearchCriteria criteria) throws PSSearchServiceException
    {
        PSPagedItemPropertiesList itemList = new PSPagedItemPropertiesList();
        itemList = searchService.getExtendedSearchResults(criteria);
        return itemList;
    }
    
}
