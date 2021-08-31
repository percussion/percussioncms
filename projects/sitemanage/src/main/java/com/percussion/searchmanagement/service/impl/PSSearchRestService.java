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
package com.percussion.searchmanagement.service.impl;

import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.useritems.data.PSUserItem;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedItemPropertiesList;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.security.PSSecurityUtility;
import com.percussion.webservices.PSWebserviceUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/search")
@Component("searchRestService")
@Tag(name = "/search")
public class PSSearchRestService
{
    private static final String SEARCH_TYPE_MY_PAGES = "MyPages";
    private final IPSSearchService searchService;
    private final IPSItemService itemService;
    private static final Logger log = LogManager.getLogger(PSSearchRestService.class);
    private final IPSSystemService systemService;

    @Autowired
    public PSSearchRestService(IPSSearchService finderSearchService, IPSItemService itemService, IPSSystemService systemService)
    {
        this.searchService = finderSearchService;
        this.itemService = itemService;
        this.systemService = systemService;
    }

    /***
     * Sanitize any input for invalid characters / parameters
     * @param criteria
     */
    protected void sanitizeCriteria(PSSearchCriteria criteria){

        if(criteria!= null) {
            String q = criteria.getQuery();

            if (q != null) {
                q = PSSecurityUtility.sanitizeStringForHTML(q);
                q = QueryParserUtil.escape(q);

                criteria.setQuery(q);
            }


            criteria.setSortColumn(
                    PSSecurityUtility.removeInvalidSQLObjectNameCharacters(criteria.getSortColumn()));

            criteria.setSearchType(
                    PSSecurityUtility.removeInvalidSQLObjectNameCharacters(criteria.getSearchType()));

            Map<String,String> fields = criteria.getSearchFields();
            if(fields != null) {
                for (String key : fields.keySet()) {
                    fields.put(key,
                            PSSecurityUtility.sanitizeStringForHTML(fields.get(key)));
                }
            }
            if(criteria.getFolderPath() != null && !PSSecurityUtility.isValidCMSPathString(criteria.getFolderPath())){
                criteria.setFolderPath(null);
            }
        }
    }


    @POST
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException
    {
        try {

            criteria = validateSearchCriteria(criteria);

            sanitizeCriteria(criteria);

            PSPagedItemList itemList = new PSPagedItemList();


            //Don't run a blind search for all items - require some criteria
            if (!criteria.isEmpty()) {

                if (SEARCH_TYPE_MY_PAGES.equalsIgnoreCase(criteria.getSearchType())) {
                    List<PSUserItem> userItems = itemService.getUserItems(PSWebserviceUtils.getUserName());
                    List<Integer> contentIds = new ArrayList<>();
                    for (PSUserItem userItem : userItems) {
                        contentIds.add(userItem.getItemId());
                    }
                    itemList = searchService.search(criteria, contentIds);
                } else {
                    itemList = searchService.search(criteria);
                }
            }
            return itemList;
        } catch (PSNotFoundException | IPSDataService.DataServiceLoadException | PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    @POST
    @Path("/get/extendedresults")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemPropertiesList extendedSearch(PSSearchCriteria criteria) throws PSSearchServiceException
    {
        sanitizeCriteria(criteria);

        PSPagedItemPropertiesList itemList;
        itemList = searchService.getExtendedSearchResults(criteria);
        return itemList;
    }

    private PSSearchCriteria validateSearchCriteria(PSSearchCriteria criteria) {
        Map<String,String> fields = criteria.getSearchFields();
        if(fields != null){
            SecureStringUtils.DatabaseType type=null;

            if(systemService.isMySQL())
                type = SecureStringUtils.DatabaseType.MYSQL;
            else if(systemService.isOracle())
                type = SecureStringUtils.DatabaseType.ORACLE;
            else if(systemService.isDB2())
                type = SecureStringUtils.DatabaseType.DB2;
            else if(systemService.isMsSQL())
                type = SecureStringUtils.DatabaseType.MSSQL;
            else if(systemService.isDerby()){
                type = SecureStringUtils.DatabaseType.DERBY;
            }

            PSFieldSet systemFieldSet =
                    PSServer.getContentEditorSystemDef().getFieldSet();

            for(Map.Entry<String,String> field : fields.entrySet()){

                PSField f = systemFieldSet.findFieldByName(field.getKey(), false);
                if(f!= null) {
                    if (f.getDataType().equalsIgnoreCase(PSField.DT_INTEGER) || f.getDataType().equalsIgnoreCase(PSField.DT_FLOAT)) {
                        if (!StringUtils.isNumeric(field.getValue())) {
                            throw new IllegalArgumentException(field.getKey() + " must have a numeric value for search");
                        }
                    } else if (f.getDataType().equalsIgnoreCase(PSField.DT_BOOLEAN)) {
                        Boolean b = BooleanUtils.toBoolean(field.getValue());
                        if (b == null) {
                            throw new IllegalArgumentException(field.getKey() + " requires a boolean value.");
                        }

                    } else if (f.getDataType().equalsIgnoreCase(PSField.DT_DATE)) {
                        if (!SecureStringUtils.isValidDate(field.getValue())) {
                            throw new IllegalArgumentException(field.getKey() + " must be a valid date.");
                        }
                    } else if (f.getDataType().equalsIgnoreCase(PSField.DT_TIME)) {
                        if (!SecureStringUtils.isValidTime((field.getValue()))) {
                            throw new IllegalArgumentException(field.getKey() + " must be a valid time.");
                        }
                    } else if (f.getDataType().equalsIgnoreCase(PSField.DT_BINARY) || f.getDataType().equalsIgnoreCase(PSField.DT_IMAGE)) {
                        throw new IllegalArgumentException("Can't use Binary fields in Search criteria.");
                    } else {
                        //Unsure on data type so just make sure there is no SQL injection possible DT_TEXT is covered here.
                        field.setValue(SecureStringUtils.sanitizeStringForSQLStatement(field.getValue(), type));
                    }
                }else{
                    field.setValue(SecureStringUtils.sanitizeStringForSQLStatement(field.getValue(), type));
                }
            }
            //Update the criteria with any sanitized inputs
            criteria.setSearchFields(fields);
        }
        return criteria;
    }
    
}
