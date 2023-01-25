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

package com.percussion.soln.segment.rx.editor;

import java.util.Map;

import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.soln.relationshipbuilder.IPSRelationshipBuilder;
import com.percussion.soln.relationshipbuilder.exit.PSAbstractBuildRelationshipsExtension;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.rx.RxSegmentRelationshipBuilder;
import com.percussion.soln.segment.rx.SegmentServiceLocator;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class SegmentRelationshipBuilderExit extends
        PSAbstractBuildRelationshipsExtension {
    
    private ISegmentService segmentService;
    private IPSContentWs contentService;
    private IPSGuidManager guidManager;

    private void init() {
        if (segmentService == null) {
            segmentService = SegmentServiceLocator.getSegmentService();
        }
        
        if (guidManager == null) {
            guidManager = PSGuidManagerLocator.getGuidMgr();
        }
        
        if (contentService == null) {
            contentService = PSContentWsLocator.getContentWebservice();
        }
    }

    @Override
    public IPSRelationshipBuilder createRelationshipBuilder(
            Map<String, String> paramMap, IPSRequestContext request, Mode mode)
            throws IllegalArgumentException {
        init();
        RxSegmentRelationshipBuilder builder = new RxSegmentRelationshipBuilder();
        builder.setSegmentService(getSegmentService());
        builder.setContentService(getContentService());
        builder.setGuidManager(getGuidManager());
        //Should we Tell the relationship building framework to select all display choices?
        //This is a little nasty but for now it we won't do it till its a performance problem.
        //paramMap.put("selectAll", "true");
        return builder;
    }

    public ISegmentService getSegmentService() {
        return segmentService;
    }

    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }
    
    
    public IPSContentWs getContentService() {
        return contentService;
    }

    
    public void setContentService(IPSContentWs contentService) {
        this.contentService = contentService;
    }

    
    public IPSGuidManager getGuidManager() {
        return guidManager;
    }

    
    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

}
