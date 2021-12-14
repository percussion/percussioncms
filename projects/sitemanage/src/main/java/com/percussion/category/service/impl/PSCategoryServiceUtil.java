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

package com.percussion.category.service.impl;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.marshaller.PSCategoryMarshaller;
import com.percussion.category.marshaller.PSCategoryUnMarshaller;
import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryClientException;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;

public class PSCategoryServiceUtil {
    public static final String DUMMYROOT = "dummyroot";
    private static final Logger log = LogManager.getLogger(PSCategoryServiceUtil.class);
    private static final String CATEGORIES_UPDATE = "perc-metadata-services/metadata/categories/update/";

    public static void preserveDeletedNodes(List<PSCategoryNode> newCategories, List<PSCategoryNode> oldCategories, String site, List<String> parentSites) {

        // Preserve category order between sites when only nodes for one site are returned to server.
        // Check for duplicate names,  merge.

        //Map id to path
        HashMap<String, PSCategoryNode> titleMap = new HashMap<>();
        HashMap<String, PSCategoryNode> idMap = new HashMap<>();

        for (PSCategoryNode category : oldCategories) {
            titleMap.put(category.getTitle(), category);
            idMap.put(category.getId(), category);
        }

        HashSet<String> newCategoryIds = new HashSet<>();

        checkAndMapIds(newCategories, titleMap, idMap, newCategoryIds);

        HashSet<String> processedIds = new HashSet<>();
        HashSet<String> processedTitles = new HashSet<>();

        Iterator<PSCategoryNode> oldCatIt = oldCategories.iterator();
        Iterator<PSCategoryNode> newCatIt = newCategories.iterator();

        ArrayList<PSCategoryNode> fullCategories = new ArrayList<>();

        PSCategoryNode nextOld = getNext(oldCatIt);
        PSCategoryNode nextNew = getNext(newCatIt);
        HashMap<String, PSCategoryNode> oldIdMap = new HashMap<>();

        while (nextOld != null || nextNew != null) {
            // Skip if category has no id (we have already added these ids to the new Items), if id has already been processed, or we
            // are skipping remaining old items that no longer exist
            if (nextOld != null && (nextOld.getId() == null || processedIds.contains(nextOld.getId()))) {
                log.debug("Skipping processed nextOld {}" , nextOld.getTitle());
                nextOld = getNext(oldCatIt);
                continue;
            }
            if (nextNew != null && processedIds.contains(nextNew.getId())) {
                log.debug("Skipping processed nextNew {}" , nextNew.getTitle());
                nextNew = getNext(newCatIt);
                continue;
            }

            PSCategoryNode processedItem = null;

            if (nextOld != null && (nextOld.getId() == null || !newCategoryIds.contains(nextOld.getId()))) {
                List<String> siteList = getAllowedSitesAsList(parentSites, nextOld);

                // handle categories not sent back.  If old category was valid for site it is
                // deleted otherwise we sequentially add
                if (!nextOld.isDeleted() && (site == null || site.equals("undefined")) || siteList.contains(site)) {
                    // skip and move to next
                    log.debug("Removing node that has been deleted or removed {}" , nextOld.getId());
                    nextOld.setDeleted(true);

                } else {
                    log.debug("Merging back categories from other sites and deleted {}" , nextOld.getId());
                }
                // merge in hidden node from other site
                processedItem = nextOld;
                fullCategories.add(nextOld);
                nextOld = getNext(oldCatIt);
            } else if (nextNew != null) {
                log.debug("adding sent category: {}" , nextNew.getId());
                fullCategories.add(nextNew);
                processedItem = nextNew;
                if (nextOld != null && nextOld.getId().equalsIgnoreCase(nextNew.getId()))
                    nextOld = getNext(oldCatIt);
                nextNew = getNext(newCatIt);
            }

            // Check for duplicates
            if (processedItem != null) {
                if (processedIds.contains(processedItem.getId()))
                    throw new IllegalArgumentException("Trying to add a duplicate id " + processedItem.getId());

                processedIds.add(processedItem.getId());
                if (!processedItem.isDeleted()) {
                    if (processedTitles.contains(processedItem.getTitle()))
                        throw new IllegalArgumentException("Trying to add a duplicate title " + processedItem.getTitle());
                    processedTitles.add(processedItem.getTitle());
                }
            }
        }

        //  process child items that map
        for (PSCategoryNode category : newCategories) {
            PSCategoryNode oldCategory = oldIdMap.get(category.getId());
            if (oldCategory != null) {
                List<String> siteList = getAllowedSitesAsList(parentSites, oldCategory);
                preserveDeletedNodes(category.getChildNodes(), oldCategory.getChildNodes(), site, siteList);
            }

        }

        // replace with new set of categories
        newCategories.clear();
        newCategories.addAll(fullCategories);

    }

    private static void checkAndMapIds(List<PSCategoryNode> newCategories, HashMap<String, PSCategoryNode> titleMap,
                                       HashMap<String, PSCategoryNode> idMap, HashSet<String> newCategoryIds) {
        Iterator<PSCategoryNode> newCatIt1 = newCategories.iterator();

        while (newCatIt1.hasNext()) {
            PSCategoryNode newCategory = newCatIt1.next();
            String id = newCategory.getId();
            String title = newCategory.getTitle();

            // Empty category ids should have been added before this point in PSCategoryService.getCategoryTreeForSite
            if (StringUtils.isEmpty(id))
                throw new RuntimeException("Category node does not have an id");
            PSCategoryNode existing = titleMap.get(title);
            if (!idMap.containsKey(id) && existing != null && existing.getId() != null) {
                // if there is an existing deleted item we adopt it.
                id = existing.getId();
                newCategory.setCreatedBy(existing.getCreatedBy());
                newCategory.setCreationDate(existing.getCreationDate());
                newCategory.setId(existing.getId());
                newCategory.setChildNodes(existing.getChildNodes());
                newCategory.setId(id);
            }
            if (newCategoryIds.contains(id)) {
                log.warn("Duplicate id {} passed in category update skipping", id);
                newCatIt1.remove();
            } else if (title.equals("Add Top Level Categories")) {
                newCategoryIds.remove(id);
                newCatIt1.remove();
            } else
                newCategoryIds.add(id);

        }
    }

    private static PSCategoryNode getNext(Iterator<PSCategoryNode> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }


    public static String createGuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static Set<String> removeDeletedNodes(List<PSCategoryNode> childNodes, Set<String> nodesToRemove) {

        log.debug("Total nodes for removal : {}" , childNodes.size());

        Set<String> removedNodes = nodesToRemove;
        List<PSCategoryNode> tempList = new ArrayList<>();
        tempList.addAll(childNodes);

        for (PSCategoryNode node : tempList) {

            if (node.isDeleted()) {
                nodesToRemove.add(node.getId());
                childNodes.remove(node);
            } else if (node.getChildNodes() != null && !node.getChildNodes().isEmpty()) {
                removeDeletedNodes(node.getChildNodes(), nodesToRemove);
            }

        }
        return nodesToRemove;
    }


    public static String prepareCategoryJson(PSCategory category) {
        String categoryJson = null;

        categoryJson = PSCategoryMarshaller.marshalToJson(category);

        log.debug("Prepared Category Json is : {}" , categoryJson);

        return categoryJson;
    }

    public static void publishToDTS(String category, String sitename, String deliveryServer, IPSDeliveryInfoService deliveryService) throws PSValidationException {

        PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_INDEXER, deliveryServer.toUpperCase());

        if (server == null)
            throw new PSDeliveryClientException("The " + deliveryServer + " Server is not configured. Cannot perform the category publish. Please select the correct option.");

        log.debug("Server to publish the categories is {}" , server.getServerType());

        PSDeliveryClient deliveryClient = new PSDeliveryClient();

        // Get a json array out from the category object for all the categories that were changed for the title but were not published.
        String categories = getCategoriesForPublish(category);

        if (categories != null && !categories.equals("[]"))
            deliveryClient.getJsonObject(new PSDeliveryActionOptions(server, CATEGORIES_UPDATE + sitename + "/" + deliveryServer, HttpMethodType.POST, true), categories).toString();
        else {
            PSValidationErrorsBuilder builder = validateParameters("publishToDTS");
            builder.reject("no.categories.to.publish", "There are no recently edited categories to publish.  A category should be edited before publishing.").throwIfInvalid();
        }
    }

    private static String getCategoriesForPublish(String categoryString) {

        String forPublish = null;
        PSCategory category = PSCategoryUnMarshaller.unMarshalFromString(categoryString);
        log.debug("Getting categories for publish.");

        // traverse the category object to find where the title is different than the previousCategoryName and publish date is not same as last modified date.
        List<PSCategoryNode> topCategories = category.getTopLevelNodes();

        if (topCategories != null && !topCategories.isEmpty()) {

            forPublish = findModifiedCategories(topCategories, "/" + category.getTitle(), null, false).toString();
        }

        return forPublish;
    }

    private static JSONArray findModifiedCategories(List<PSCategoryNode> categories, String oldPrefix, String newPrefix, boolean hasParentChanged) {

        JSONArray jsonArray = new JSONArray();
        boolean thisParentChanged = false;
        log.debug("Finding modified categories.");

        try {
            for (PSCategoryNode parent : categories) {
                JSONObject obj = new JSONObject();
                if (StringUtils.isNotBlank(parent.getPreviousCategoryName()) && StringUtils.isNotBlank(parent.getTitle())) {
                    thisParentChanged = true;
                    if (hasParentChanged) {
                        obj.put("previousCategoryName", oldPrefix + "/" + parent.getPreviousCategoryName());
                        obj.put("title", newPrefix + "/" + parent.getTitle());
                    } else {
                        obj.put("previousCategoryName", oldPrefix + "/" + parent.getPreviousCategoryName());
                        obj.put("title", oldPrefix + "/" + parent.getTitle());
                    }
                    jsonArray.put(obj);

                    if (StringUtils.isBlank(newPrefix))
                        newPrefix = oldPrefix;

                } else {
                    if (hasParentChanged) {

                        obj.put("previousCategoryName", oldPrefix + "/" + parent.getTitle());
                        obj.put("title", newPrefix + "/" + parent.getTitle());

                        jsonArray.put(obj);
                    }
                }

                if (parent.getChildNodes() != null && !parent.getChildNodes().isEmpty()) {
                    JSONArray temp = null;

                    if (thisParentChanged)
                        temp = findModifiedCategories(parent.getChildNodes(), oldPrefix + "/" + parent.getPreviousCategoryName(), newPrefix + "/" + parent.getTitle(), true);
                    else {
                        if (hasParentChanged)
                            temp = findModifiedCategories(parent.getChildNodes(), oldPrefix + "/" + parent.getTitle(), newPrefix + "/" + parent.getTitle(), true);
                        else
                            temp = findModifiedCategories(parent.getChildNodes(), oldPrefix + "/" + parent.getTitle(), oldPrefix + "/" + parent.getTitle(), false);
                    }

                    for (int i = 0; i < temp.length(); i++) {
                        jsonArray.put(temp.get(i));
                    }
                }
            }

        } catch (JSONException e) {
            log.error("Error occurred while creating json object for category to be published. - PSCategoryServiceUtil.getCategoriesForPublish()", e);
        }

        return jsonArray;
    }


    private static List<String> getAllowedSitesAsList(List<String> parentSites, PSCategoryNode node) {
        List<String> nodeAllowedSites = new ArrayList<>();
        if (node.getAllowedSites() != null) {
            for (String string : StringUtils.split(node.getAllowedSites(), ",")) {
                nodeAllowedSites.add(string.trim());
            }
        }
        if (nodeAllowedSites.isEmpty())
            nodeAllowedSites = parentSites;
        else if (parentSites != null) {
            nodeAllowedSites.retainAll(parentSites);
        }

        return nodeAllowedSites;
    }


    public static PSCategoryNode filterForSite(PSCategoryNode currentNode, String sitename,
                                               LinkedList<String> findPath, List<String> allowedSites, boolean relativePath, boolean includeDeleted,
                                               boolean includeNotSelectable) {

        if (!includeDeleted && currentNode.isDeleted())
            return null;

        List<String> nodeAllowedSites = getAllowedSitesAsList(allowedSites, currentNode);
        if (StringUtils.isNotEmpty(sitename) && !nodeAllowedSites.contains(sitename))
            return null;

        String pathElement = null;
        PSCategoryNode foundNode = null;
        if (findPath != null && !findPath.isEmpty()) {
            if (!StringUtils.equals(currentNode.getId(), DUMMYROOT)) {
                pathElement = findPath.peekFirst();
                boolean matches = testTitleOrId(pathElement, currentNode);

                if (matches) {
                    relativePath = false;
                    findPath.removeFirst();

                } else if (!relativePath) {
                    return null;
                }
            }

            if (!findPath.isEmpty()) {
                for (PSCategoryNode child : currentNode.getChildNodes()) {
                    PSCategoryNode testNode = filterForSite(child, sitename,
                            findPath, nodeAllowedSites, relativePath, includeDeleted,
                            includeNotSelectable);
                    if (testNode != null) {
                        foundNode = testNode;
                        break;
                    }
                }
                return foundNode;
            }
            // else found node so continue to process
        }

        // don't filter if this is a root element
        if (!StringUtils.equals(currentNode.getId(), DUMMYROOT) && pathElement == null && !includeNotSelectable && !currentNode.isSelectable())
            return null;

        List<PSCategoryNode> filteredChildList = new ArrayList<>();
        for (PSCategoryNode child : currentNode.getChildNodes()) {
            PSCategoryNode testNode = filterForSite(child, sitename,
                    null, nodeAllowedSites, relativePath, includeDeleted,
                    includeNotSelectable);
            if (testNode != null) {
                filteredChildList.add(testNode);
            }
        }
        currentNode.setChildNodes(filteredChildList);
        return currentNode;

    }

    private static boolean testTitleOrId(String checkString, PSCategoryNode node) {
        return StringUtils.equals(node.getTitle(), checkString) || StringUtils.equals(node.getId(), checkString);
    }
}
