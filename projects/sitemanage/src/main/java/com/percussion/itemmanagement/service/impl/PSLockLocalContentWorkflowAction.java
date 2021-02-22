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
package com.percussion.itemmanagement.service.impl;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
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
            log.error("Error workflowing local assets Error: {}", e.getMessage());
            log.debug(e.getMessage(),e);
        } finally
        {
            setSecurity(currentUser);
        }
    }

}

