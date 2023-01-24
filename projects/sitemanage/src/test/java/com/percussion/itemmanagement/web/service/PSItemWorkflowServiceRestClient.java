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

package com.percussion.itemmanagement.web.service;

import com.percussion.itemmanagement.data.PSApprovableItems;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.data.PSItemTransitionResults;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.share.test.PSObjectRestClient;

/**
 * The class used for unit test on REST layer.
 * 
 * @author peterfrontiero
 */
public class PSItemWorkflowServiceRestClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/itemmanagement/workflow/";

    public PSItemWorkflowServiceRestClient(String baseUrl)
    {
        super(baseUrl);
    }

    public void checkIn(String id)
    {
        GET(concatPath(getPath(), "checkIn", id));
    }
    
    public PSItemUserInfo checkOut(String id)
    {
        return getObjectFromPath(concatPath(getPath(), "checkOut", id), PSItemUserInfo.class);
    }

    public PSItemUserInfo forceCheckOut(String id)
    {
        return getObjectFromPath(concatPath(getPath(), "forceCheckOut", id), PSItemUserInfo.class);
    }
    
    public PSItemStateTransition getTransitions(String id)
    {
        return getObjectFromPath(concatPath(getPath(), "getTransitions", id), PSItemStateTransition.class);
    }
    
    public PSItemTransitionResults transition(String id, String trigger)
    {
       return getObjectFromPath(concatPath(getPath(), "transition", id, trigger), PSItemTransitionResults.class);
    }
    
    public void approvePages(PSApprovableItems items)
    {
        postObjectToPath(concatPath(getPath(), "bulkapprove"), items);
    }
    
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
