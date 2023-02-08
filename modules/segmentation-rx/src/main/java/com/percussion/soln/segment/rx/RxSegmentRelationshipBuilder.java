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

package com.percussion.soln.segment.rx;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.soln.relationshipbuilder.PSAbstractRelationshipBuilder;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;

public class RxSegmentRelationshipBuilder extends PSAbstractRelationshipBuilder {
    
    private ISegmentService segmentService;
    private IPSContentWs contentService;
    private IPSGuidManager guidManager;
    
    
    public IPSContentWs getContentService() {
        return contentService;
    }

    
    public void setContentService(IPSContentWs contentService) {
        this.contentService = contentService;
    }

    @Override
    public void add(int sourceId, Collection<Integer> targetIds) {
        Collection<Integer> folderIds = retrieveFolderIdsForSegmentIds(targetIds);
        addFolderRelationships(folderIds, sourceId);
    }

    @Override
    public void delete(int sourceId, Collection<Integer> targetIds) {
        Collection<Integer> folderIds = retrieveFolderIdsForSegmentIds(targetIds);
        deleteFolderRelationships(folderIds, sourceId);
    }

    public Collection<Integer> retrieve(int sourceId) {
        ISegmentService segService = getSegmentService();
        List<Integer> ids = new ArrayList<Integer>();
        Collection<? extends Segment> segs = segService.retrieveSegmentsForItem(sourceId).getList();
        for (Segment seg : segs) {
            ids.add(Integer.parseInt(seg.getId()));
        }
        return ids;

    }

    
    protected void addFolderRelationships(Collection<Integer> folderIds, int itemId) {
        addFolderRelationships(folderIds, asList(itemId));
    
    }
    
    protected void deleteFolderRelationships(Collection<Integer> folderIds, int itemId) {
        deleteFolderRelationships(folderIds, asList(itemId));
    }
    
    
    private void addFolderRelationships(
            Collection<Integer> folderIds,
            Collection<Integer> itemIds)  {
        List<IPSGuid> folderGuids = asGuids(folderIds);
        List<IPSGuid> itemGuids = asGuids(itemIds);
        
        for (IPSGuid fg: folderGuids) {
            try {
                getContentService().addFolderChildren(fg, itemGuids);
            } catch (PSErrorException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void deleteFolderRelationships(Collection<Integer> folderIds,
            Collection<Integer> itemIds) {
        Collection<IPSGuid> folderGuids = asGuids(folderIds);
        List<IPSGuid> childGuids = asGuids(itemIds);
        for (IPSGuid fg : folderGuids) {
            try {

                getContentService().removeFolderChildren(fg, childGuids, false);

            } catch (PSErrorsException e) {
                throw new RuntimeException("Errors in deleting items: "
                        + itemIds + "from folder: " + fg, e);
            } catch (PSErrorException e) {
                throw new RuntimeException("Error in deleting items: "
                        + itemIds + "from folder: " + fg, e);
            }
        }
    }
    
    private List<IPSGuid> asGuids(Collection<Integer> ids) {
        List<IPSGuid> guids = new ArrayList<IPSGuid>(ids.size());
        for (Integer id : ids) {
            guids.add(getGuidManager().makeGuid(id, PSTypeEnum.LEGACY_CONTENT));
        }
        return guids;
    }
    
    
    private Collection<Integer> retrieveFolderIdsForSegmentIds(
            Collection<Integer> segIds) throws SegmentException {
        ISegmentService segService = getSegmentService();
        List<String> targetIdsList = new ArrayList<String>();
        List<Integer> folderIds;
        for (Integer id : segIds) {
            targetIdsList.add(id.toString());
        }
        List<? extends Segment> segments = segService
                .retrieveSegments(targetIdsList).getList();
        folderIds = new ArrayList<Integer>();
        for (Segment seg : segments) {
            folderIds.add(seg.getFolderId());
        }
        return folderIds;
    }
    
    public ISegmentService getSegmentService() {
        return segmentService;
    }

    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }


    
    public IPSGuidManager getGuidManager() {
        return guidManager;
    }


    
    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

}
