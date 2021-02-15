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

package com.percussion.category.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.category.dao.IPSCategoryDao;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryLockInfo;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.marshaller.PSCategoryMarshaller;
import com.percussion.category.marshaller.PSCategoryUnMarshaller;
import com.percussion.category.service.IPSCategoryService;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.user.service.IPSUserService;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@Path("/category")
@Component("categoryService")
@Lazy
public class PSCategoryService implements IPSCategoryService {

    private static final Logger log = LogManager.getLogger(PSCategoryService.class);

    private IPSUserService userService;
    private IPSDeliveryInfoService deliveryService;
    private IPSSiteDataService siteDataService;

    @Autowired
    private IPSCategoryDao categoryDao;

    @Autowired
    private IPSGuidManager guidMgr;

    public PSCategoryService() {
        // empty for jax-rs
    }

    @Autowired
    public PSCategoryService(IPSUserService userService, IPSDeliveryInfoService deliveryService, IPSSiteDataService siteDataService) {
        this.userService = userService;
        this.deliveryService = deliveryService;
        this.siteDataService = siteDataService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Path("/all")
    public String getCategoryList() throws PSDataServiceException {
        return getCategoryList(null).toJSON();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    @Path("/all/{sitename}")
    public String getCategoryListWithStrng(@PathParam("sitename") String sitename) throws PSDataServiceException {
        if (sitename != null && sitename.equals("undefined"))
            sitename = null;
        PSCategory category = getCategoryTreeForSite(sitename, null, false, true);
        return category.toJSON();
    }

    public PSCategory getCategoryList(String sitename) throws PSDataServiceException {
        return getCategoryTreeForSite(sitename, null, false, true);
    }

    public PSCategory getCategoryTreeForSite(String sitename, String rootPath, boolean includeDeleted, boolean includeNotSelectable) throws PSDataServiceException {
        if (StringUtils.isBlank(sitename))
            sitename = null;
        if (StringUtils.isBlank(rootPath))
            rootPath = null;

        PSCategoryUnMarshaller unMarshaller = new PSCategoryUnMarshaller();
        PSCategory category = unMarshaller.unMarshal();

        PSCategoryNode node = findCategoryNode(category, sitename, rootPath, includeDeleted,
                includeNotSelectable);

        // extract nodes from dummy root or other found node.
        if (node == null) {
            category.setTopLevelNodes(new ArrayList<>());
            return category;
        }

        // Create new guids for manually created category items.
        boolean saveCategories = createIds(node);

        List<PSCategoryNode> filteredNodes = node.getChildNodes();
        if (filteredNodes == null) {
            filteredNodes = new ArrayList<>();
        }
        if(node.getChildNodes() != null && !node.getChildNodes() .isEmpty() ) {
            category.setTopLevelNodes(node.getChildNodes());
        }

        if (saveCategories)
            updateCategories(category, sitename);

        return category;
    }

    private boolean createIds(PSCategoryNode node) {
        boolean createdIds = false;
        if (StringUtils.isEmpty(node.getId())) {
            String newId = PSCategoryServiceUtil.createGuid();
            log.info("Created new id for category " + node.getTitle() + " " + newId);
            node.setId(newId);
            createdIds = true;
        }

        for (PSCategoryNode sub : node.getChildNodes())
            createdIds |= createIds(sub);
        return createdIds;
    }

    @Override
    public PSCategoryNode findCategoryNode(String siteName, String rootPath, boolean includeDeleted,
                                           boolean includeNotSelectable) {

        PSCategoryUnMarshaller unMarshaller = new PSCategoryUnMarshaller();
        PSCategory category = unMarshaller.unMarshal();

        log.debug("Finding categoryNode with rootPath {} and site {}", rootPath , siteName);
        return findCategoryNode(category, siteName, rootPath, includeDeleted,
                includeNotSelectable);

    }

    private PSCategoryNode findCategoryNode(PSCategory category, String sitename, String rootPath, boolean includeDeleted,
                                            boolean includeNotSelectable) {
        LinkedList<String> findPath = new LinkedList<>();
        boolean relativePath = false;
        if (rootPath != null) {
            relativePath = (!rootPath.startsWith("/"));
            findPath = new LinkedList<>(Arrays.asList(StringUtils.split(rootPath, "/")));
        }


        if (!findPath.isEmpty()) {
            String checkElement = findPath.peek();
            while (!findPath.isEmpty() && (checkElement.equals("Categories") || StringUtils.isEmpty(checkElement))) {
                findPath.removeFirst();
                checkElement = findPath.peek();
            }


        }

        log.debug("Cleaned up parent seach path = {}", findPath);

        PSCategoryNode dummyRoot = new PSCategoryNode();
        dummyRoot.setId(PSCategoryServiceUtil.DUMMYROOT);
        dummyRoot.setChildNodes(category.getTopLevelNodes());
        dummyRoot.setDeleted(false);
        dummyRoot.setSelectable(true);
        dummyRoot.setTitle(PSCategoryServiceUtil.DUMMYROOT);

        return PSCategoryServiceUtil.filterForSite(dummyRoot, sitename, findPath, getActiveSiteNames(), relativePath, includeDeleted, includeNotSelectable);
    }

    private List<String> getActiveSiteNames() {
        List<PSSiteSummary> siteSummaries = siteDataService.findAll();
        List<String> currentSites = new ArrayList<>();
        for (PSSiteSummary site : siteSummaries) {
            currentSites.add(site.getName());
        }
        return currentSites;
    }

    @POST
    @Path("/update/{sitename}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public String updateCategoriesWithString( @RequestParam("categorystring") String categoryString, @PathParam("sitename") String sitename) throws PSDataServiceException {
        PSCategory categoryWithJson = null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            categoryWithJson = mapper.readValue(categoryString, PSCategory.class);
        }catch (JsonProcessingException ex){
            log.error("Error while parsing json to {} Error: {}",
                    categoryString, ex.getMessage());
            log.debug(ex.getMessage(),ex);
        }

        return updateCategories(categoryWithJson, sitename).toJSON();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public String updateCategoriesWithString( @RequestParam("categorystring") String categoryString) throws PSDataServiceException {
        PSCategory categoryWithJson = null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            categoryWithJson = mapper.readValue(categoryString, PSCategory.class);
        }catch (JsonProcessingException ex){
            log.error("Error while parsing json to {} Error: {}",categoryString, ex.getMessage());
            log.debug(ex.getMessage(),ex);
        }

        return updateCategories(categoryWithJson ,null).toJSON();
    }

    public PSCategory updateCategories(PSCategory category) throws PSDataServiceException {
        return updateCategories(category, null);
    }

    public PSCategory updateCategories(PSCategory category,  String sitename) throws PSSpringValidationException {

        PSCategory updatedCategory = null;
        doValidation(category);

        PSParameterValidationUtils.rejectIfNull("update", "category", category);

        PSCategoryUnMarshaller unMarshaller = new PSCategoryUnMarshaller();
        PSCategory oldCategory = unMarshaller.unMarshal();

        if (oldCategory != null) {

            PSCategoryServiceUtil.preserveDeletedNodes(category.getTopLevelNodes(), oldCategory.getTopLevelNodes(), sitename, getActiveSiteNames());

        }

        PSCategoryMarshaller marshaller = new PSCategoryMarshaller();

        marshaller.setCategory(category);

        try {
            marshaller.marshal();
        } catch (OverlappingFileLockException e) {
            log.error("Category XML is locked by another user ! - PSCategoryService.updateCategories()", e);
        }

        updatedCategory = unMarshaller.unMarshal();

        if (updatedCategory == null) {
            log.error("The updated categories are null ! - PSCategoryService.updateCategories()"
                    , new PSDataServiceException("Updated Categories are null"));
        } else if (updatedCategory.getTopLevelNodes() != null && !updatedCategory.getTopLevelNodes().isEmpty()) {
            Set<String> nodesToRemove = new HashSet<>();
            nodesToRemove = PSCategoryServiceUtil.removeDeletedNodes(updatedCategory.getTopLevelNodes(), nodesToRemove);
            if (!nodesToRemove.isEmpty()) {
                List<Integer> pageIds = categoryDao.getPageIdsFromCategoryIds(nodesToRemove);
                List<IPSGuid> guids = getGuidsFromPageIds(pageIds);
                 categoryDao.delete(nodesToRemove, guids);
            }
        }
        return updatedCategory;
    }

    @GET
    @Path("/lockinfo")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public String getLockInfo() {

        JSONObject jsonObject = null;

        if (PSCategoryLockInfo.isFileLocked())
            jsonObject = PSCategoryLockInfo.getLockInfo();

        if (jsonObject != null) {
            try {
                //make sure the object is valid
                jsonObject.get("userName");
                return jsonObject.toString();
            } catch (JSONException e) {
                log.error("JSON Exception occurred while reading from the json object - PSCategoryService.getLockInfo()",
                        new PSDataServiceException("Could not read lock information file"));
            }
        } else {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("userName", "");
                jsonObject.put("sessionId", "");
                jsonObject.put("sitename", "");
            } catch (JSONException e) {
                log.error("JSON Exception occurred while creating empty json object - PSCategoryService.getLockInfo()",
                        new WebApplicationException("No lock on category tab. Could not create an empty json to return from api."));
            }

        }
        return jsonObject.toString();
    }

    @POST
    @Path("/locktab/{date}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public void lockCategoryTab(@PathParam("date") String date){

        JSONObject jsonObject = null;

        if (PSCategoryLockInfo.isFileLocked())
            jsonObject = PSCategoryLockInfo.getLockInfo();

        if (jsonObject != null) {
            try {
                if (!( jsonObject.get("userName")).equals(userService.getCurrentUser().getName())) {
                    PSCategoryLockInfo.removeLockInfo();
                    PSCategoryLockInfo.writeLockInfoToFile(userService, date);
                } else if (!( jsonObject.get("creationDate")).equals(date)) {
                    PSCategoryLockInfo.removeLockInfo();
                    PSCategoryLockInfo.writeLockInfoToFile(userService, date);
                }

            } catch (JSONException | PSDataServiceException e) {
                log.error("JSON Exception occurred while reading from the json object - PSCategoryService.overrideCatTabLock()",
                        new WebApplicationException("Could not read lock information file"));
            }
        } else {
            PSCategoryLockInfo.writeLockInfoToFile(userService, date);
        }
    }

    @POST
    @Path("/removelocktab")
    public void removeCategoryTabLock() {
        PSCategoryLockInfo.removeLockInfo();
    }

    @POST
    @Path("/updateindts/{sitename}/{deliveryserver}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
    public void updateCategoryInDTS(@PathParam("sitename") String sitename, @PathParam("deliveryserver") String deliveryserver) {

        try {
            String category = PSCategoryServiceUtil.prepareCategoryJson(getCategoryList(sitename));

            if (deliveryserver.equalsIgnoreCase("Both")) {
                PSCategoryServiceUtil.publishToDTS(category, sitename, "Production", deliveryService);
                PSCategoryServiceUtil.publishToDTS(category, sitename, "Staging", deliveryService);
            } else
                PSCategoryServiceUtil.publishToDTS(category, sitename, deliveryserver, deliveryService);
        } catch (PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Validates the specified category. It validates the role object according to
     * its annotation and invokes
     * {@link PSCategoryValidator validate(Object)}
     * for additional validation.
     *
     * @param category the category in question, not <code>null</code>.
     * @throws PSBeanValidationException if failed to validate the specified
     *                                   role.
     */
    protected void doValidation(PSCategory category) throws PSSpringValidationException {
        PSCategoryValidator validator = new PSCategoryValidator();

        validator.validate(category).throwIfInvalid();
    }

    /**
     * This is used to validate a {@link PSCategory} object before updating an
     * existing category or create a new one.
     */
    protected class PSCategoryValidator extends PSAbstractBeanValidator<PSCategory> {

        PSCategoryValidator() {}

        @Override
        protected void doValidation(PSCategory category, PSBeanValidationException e) {
            throw new NotImplementedException();
        }
    }

    /**
     *
     * @param pageIds
     * @return
     */
    private List<IPSGuid> getGuidsFromPageIds(List<Integer> pageIds) {
        List<IPSGuid> guids = new ArrayList<>();
        for (Integer id : pageIds) {
            try {
                IPSGuid guid = new PSLegacyGuid(id, -1);
                guids.add(guid);
            } catch (Exception e) {
                log.error("Error creating guid from id: {}", id);
            }
        }
        return guids;
    }

}
