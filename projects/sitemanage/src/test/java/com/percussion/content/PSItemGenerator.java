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

package com.percussion.content;

import java.util.HashMap;
import java.util.Map;

import com.percussion.itemmanagement.web.service.PSItemWorkflowServiceRestClient;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;

/**
 * Code common to pages and assets.
 * @author PaulHoward
 *
 * @param <CLIENT_TYPE> The base rest client used by the derived class.
 */
public abstract class PSItemGenerator<CLIENT_TYPE> extends PSGenerator<CLIENT_TYPE> {

    protected PSItemWorkflowServiceRestClient workflowClient;
    protected PSPathServiceRestClient pathClient;
    
    /**
     * Key is the target state name. Value is a String[] of transition triggers
     * that are required to reach the target state from the draft state. May be
     * empty for a target state of draft. All keys are lower-cased.
     */
    protected static Map<String, String[]> STATE_TRANSITION_MAP = new HashMap<String, String[]>();

    /**
     * See {@link PSGenerator#PSGenerator(Class, String, String, String) base
     * class} for param details.
     */
    public PSItemGenerator(Class<CLIENT_TYPE> cl, String baseUrl, String uid, String pw) {
        super(cl, baseUrl, uid, pw);
        workflowClient = new PSItemWorkflowServiceRestClient(baseUrl);
        workflowClient.login(uid, pw);
        pathClient = new PSPathServiceRestClient(baseUrl);
        pathClient.login(uid, pw);
    }

    /**
     * 
     * @param sourceAssetName
     * @return
     */
    protected String getAssetGuidFromPath(String sourceAssetName) {
        PSPathItem assetSum = pathClient.find(sourceAssetName);
        return assetSum.getId();
    }

    /**
     * Transitions a page from the Draft state to the state specified.
     * 
     * @param itemGuid
     *            Assumed not <code>null</code> and a valid item guid.
     * @param targetStateName
     *            If <code>null</code>, returns immediately, otherwise, the
     *            name is looked up in a state/transition table
     *            (case-insensitive) to determine the appropriate transitions.
     *            If it is not a known state, no action is taken and a warning
     *            is logged.
     */
    protected void transitionToState(String itemGuid, String targetStateName) {
        if (targetStateName == null)
            return;
        String[] triggers = STATE_TRANSITION_MAP.get(targetStateName.toLowerCase());
        if (triggers == null)
        {
            log.warn("Unknown workflow state '" + targetStateName + "' - leaving in draft state.");
            return;
        }
        for ( String trigger : triggers)
        {
            workflowClient.transition(itemGuid, trigger);
        }
        if (!targetStateName.equalsIgnoreCase("draft"))
            log.info("Page transitioned from Draft to '" + targetStateName + "'");
    }

    static 
    {
        /*
         * key is the name of the state, lower-cased, value is a list of
         * transition triggers. The code will execute a transition for each
         * trigger supplied in the order supplied. The triggers are to
         * transition from the Draft state to the state specified in the key.
         * transition triggers are case-sensitive
         */
        STATE_TRANSITION_MAP.put("draft", new String[] {});
        STATE_TRANSITION_MAP.put("review", new String[] {"Submit"});
        STATE_TRANSITION_MAP.put("pending", new String[] {"Submit", "Approve"});
        STATE_TRANSITION_MAP.put("live", new String[] {"Submit", "Approve", "forcetolive"});
        STATE_TRANSITION_MAP.put("quick edit", new String[] {"Submit", "Approve", "forcetolive", "Quick Edit"});
        STATE_TRANSITION_MAP.put("archive", new String[] {"Submit", "Approve", "forcetolive", "Quick Edit", "Archive"});
    }
}
