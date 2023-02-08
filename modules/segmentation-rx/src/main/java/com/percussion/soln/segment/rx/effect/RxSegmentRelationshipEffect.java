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

package com.percussion.soln.segment.rx.effect;

import java.io.File;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.soln.segment.data.ISegmentDataService;
import com.percussion.soln.segment.rx.SegmentServiceLocator;
@PSHandlesEffectContext(required={PSEffectContext.PRE_CONSTRUCTION,
		PSEffectContext.PRE_DESTRUCTION,
		PSEffectContext.PRE_UPDATE})
public class RxSegmentRelationshipEffect implements IPSEffect {

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(RxSegmentRelationshipEffect.class);
    
    private static final boolean trace = false;
    
    private ISegmentDataService segmentDataService;
    private IContentTypeHelper contentTypeHelper;
    
    public void attempt(Object[] params, IPSRequestContext requestContext,
            IPSExecutionContext executionContext, PSEffectResult effectResult)
            throws PSExtensionProcessingException, PSParameterMismatchException {
        init();
        logRequestContext(requestContext);
        logExecutionContext(executionContext);
        if (executionContext.getCurrentRelationship() != null &&
                (executionContext.isPreConstruction() 
                || executionContext.isPreDestruction()
                || executionContext.isPreUpdate())) {
            PSRelationship rel = executionContext.getCurrentRelationship();
            if (PSRelationshipConfig.CATEGORY_FOLDER.equals(rel.getConfig().getCategory())) {
                log.debug("The Relationship is a folder relationship");
                PSLocator dependentLocator = rel.getDependent();
                int id = dependentLocator.getId();
                if (isNeedingReset(id)) {
                    log.debug("Resetting segment tree");
                    segmentDataService.resetSegmentTree(true, null);
                }
            }
        }
        effectResult.setSuccess();
    }
    
    private boolean isNeedingReset(int id) {
        try {
            String name = contentTypeHelper.retrieveContentTypeNameForItem(id);
            log.debug("Content type name is " + name + " for item: " + id);
            if (name == null) return false;
            if (name.equals("Folder")) return true;
            if (name.equals(segmentDataService.getSegmentContentType())) return true;
            return false;
        } catch (RepositoryException e) {
            log.error(e);
            return false;
        }
    }
    
    private void logRequestContext(IPSRequestContext requestContext) {
        if (trace && log.isTraceEnabled()) {
            log.trace("Request Context parameters: " + requestContext.getParameters());
        }
    }
    
    private void logExecutionContext(IPSExecutionContext executionContext) {
        if (trace && log.isTraceEnabled()) {
            log.trace("Current Relationship: " + executionContext.getCurrentRelationship());
            log.trace("Originating Relationship: " + executionContext.getOriginatingRelationship());
            log.trace("Processed Relationships: " + executionContext.getProcessedRelationships());
        }
    }

    public void recover(Object[] params, IPSRequestContext requestContext,
            IPSExecutionContext executionContext, PSExtensionProcessingException processingException,
            PSEffectResult effectResult) throws PSExtensionProcessingException {
        init();
        log.debug("Recover");
    }

    public void test(Object[] arg0, IPSRequestContext arg1,
            IPSExecutionContext arg2, PSEffectResult arg3)
            throws PSExtensionProcessingException, PSParameterMismatchException {
        init();
    }

    private void init() {
        if (segmentDataService == null) {
            log.debug("Using Service Locator to wire segment service");
            segmentDataService = SegmentServiceLocator.getSegmentDataService();
        }
        if (contentTypeHelper == null)
            contentTypeHelper = new RxContentTypeHelper();
    }
    
    public void init(IPSExtensionDef extensionDef, File file)
            throws PSExtensionException {

    }
    

    public void setSegmentDataService(ISegmentDataService segmentService) {
        this.segmentDataService = segmentService;
    }

    public IContentTypeHelper getContentTypeHelper() {
        return contentTypeHelper;
    }

    public void setContentTypeHelper(IContentTypeHelper contentTypeHelper) {
        this.contentTypeHelper = contentTypeHelper;
    }



}
