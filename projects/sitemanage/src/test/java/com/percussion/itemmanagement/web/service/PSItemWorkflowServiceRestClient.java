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
