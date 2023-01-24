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

package com.percussion.soln.p13n.delivery.ds;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliveryService;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;
import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.data.ISegmentDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

public class DSDataSetup implements InitializingBean, DisposableBean {
    
    IVisitorProfileDataService visitorProfileDataService;
    IDeliveryDataService deliveryDataService;
    IDeliveryService deliveryService;
    ISegmentService segmentService;
    ISegmentDataService segmentDataService;
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DSDataSetup.class);
    
    
    public void afterPropertiesSet() throws Exception {
        setupData();
    }
    
    public void destroy() {
        /*
         * Should delete the data here.
         */
    }
    
    public void setupData() {
        log.info("Loading Test Data");
        String message = "Should have been setup by spring";
        assertNotNull(message, visitorProfileDataService);
        assertNotNull(message, deliveryDataService);
        assertNotNull(message, deliveryService);
        assertNotNull(message, segmentService);
        assertNotNull(message, segmentDataService);
        
        segmentDataService.resetSegmentTree(true, null);
        //createSegment("1", "//Folders");
        createSegment("2", "//Folders/Segments/Regions", false);
        createSegment("3", "//Folders/Segments/Regions/Western Hemisphere", false);
        createSegment("4", "//Folders/Segments/Regions/Eastern Hemisphere", false);
        createSegment("5", "//Folders/Segments/Regions/Western Hemisphere/North America", true, "NA");
        createSegment("6", "//Folders/Segments/Regions/Western Hemisphere/North America/United States", 
                true, "usa", "america");
        createSegment("7", "//Folders/Segments/Regions/Western Hemisphere/North America/Canada", true);
        createSegment("8", "//Folders/Segments/Regions/Eastern Hemisphere/South America", true);
        createSegment("9", "//Folders/Segments/Demographic", true);
        createSegment("10", "//Folders/Segments/Interest Group", true);
        
        createProfile(1, "userid1", "6", 5, false);
        createProfile(2, "userid2", "7", 4, true);
        
        createRuleItem(10, Collections.singletonList("local_rule"),
                createSnipItem(1, asList("2","3")),
                createSnipItem(2, Collections.singletonList("3")));
        
        createRuleItem(20, Collections.singletonList("spring_rule"),
                createSnipItem(4, asList("4","5")),
                createSnipItem(5, Collections.singletonList("4")));
        
        createRuleItem(30, Collections.singletonList("p13nRule_simpleMatch"),
                createSnipItem(6, asList("4","5")),
                createSnipItem(7, Collections.singletonList("8")));
        
        deliveryService.registerSnippetFilter("local_rule",
                (context, items) -> {
                    for(IDeliveryResponseSnippetItem item : items) {
                        item.setStyle("local");
                    }
                    return items;
                });
        
        
    }
    
    public DeliverySnippetItem createSnipItem(int contentId, 
            Collection<String> segmentIds) {
        DeliverySnippetItem snipItemData = new DeliverySnippetItem();
        snipItemData.setContentId(contentId);
        HashSet<String> segIds = new HashSet<>(segmentIds);
        snipItemData.setSegmentIds(segIds);
        String rendering = "<div class=\"snip\">" +
                "<span class=\"contentId\">" +
                contentId +
                "</span>" +
                "<span class=\"segmentIds\">" +
                segmentIds +
                "</span>" + "</div>";
        snipItemData.setRendering(rendering);
        return snipItemData;
    }
    public DeliveryListItem createRuleItem(int contentId, 
            List<String> ruleIds,
            DeliverySnippetItem ... snippets) {
        DeliveryListItem ruleItemData = new DeliveryListItem();
        ruleItemData.setContentId(contentId);
        ruleItemData.setSnippetFilterIds(ruleIds);
        ruleItemData.setSnippets(asList(snippets));
        deliveryDataService.saveListItems(Collections.singletonList(ruleItemData));
        return ruleItemData;
    }
    
    public VisitorProfile createProfile(
            long id, 
            String userid, 
            String segment, 
            int weight, 
            boolean lockProfile) {
        VisitorProfile profile = new VisitorProfile(id, userid, userid);
        profile.getSegmentWeights().put(segment, weight);
        profile.setLockProfile(lockProfile);
        visitorProfileDataService.save(profile);
        return profile;
    }
    
    public Segment createSegment(String id, String path, boolean selectable, String ... aliases) {
        Segment segmentData = new Segment();
        segmentData.setId(id);
        segmentData.setFolderPath(path);
        segmentData.setSelectable(selectable);
        if (aliases != null)
            segmentData.setAliases(new HashSet<>(asList(aliases)));
        segmentDataService.updateSegmentTree(new Segments(Collections.singletonList(segmentData)));
        return segmentService.retrieveSegments(Collections.singletonList(id)).getList().get(0);

    }
    
    public Segment createSegment(String id, String path, boolean selectable) {
        String [] aliases = null;
        return createSegment(id, path, selectable, aliases);
    }
    
    public IDeliveryDataService getDeliveryDataService() {
        return deliveryDataService;
    }
    public void setDeliveryDataService(IDeliveryDataService deliveryDataService) {
        this.deliveryDataService = deliveryDataService;
    }
    public ISegmentService getSegmentService() {
        return segmentService;
    }
    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }
    public IDeliveryService getDeliveryService() {
        return deliveryService;
    }
    public void setDeliveryService(IDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    public ISegmentDataService getSegmentDataService() {
        return segmentDataService;
    }

    public void setSegmentDataService(ISegmentDataService segmentDataService) {
        this.segmentDataService = segmentDataService;
    }

    public IVisitorProfileDataService getVisitorProfileDataService() {
        return visitorProfileDataService;
    }

    public void setVisitorProfileDataService(
            IVisitorProfileDataService visitorProfileDataService) {
        this.visitorProfileDataService = visitorProfileDataService;
    }


}
