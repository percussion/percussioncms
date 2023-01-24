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
package com.percussion.itemmanagement.service.impl;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Will lock local content when the page is in 
 * {@value #LOCK_STATE} state.
 * @author adamgent
 *
 */
public class PSLockLocalContentWorkflowAction extends PSAbstractWorkflowExtension implements IPSWorkflowAction
{

    private static final Logger log = LogManager.getLogger(PSLockLocalContentWorkflowAction.class);
    /**
     * The workflow state that page must be in for local content to be locked.
     */
    protected static final String LOCK_STATE = "Pending";

    @Override
    public void performAction(IPSWorkFlowContext wfContext, @SuppressWarnings("unused") IPSRequestContext request) 
    {
        log.debug("Started workflowing local assets");
        
        String currentUser = getUser();
        setSecurity();
        
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(STATE_PARAMETER, LOCK_STATE);
            WorkflowItemWorker worker = getWorker(params);
            worker.processItem(wfContext);

            log.debug("Finished workflowing assets");
        } catch (IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException e) {
            log.error("Error workflowing local assets Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        } finally
        {
            setSecurity(currentUser);
        }
    }

}

