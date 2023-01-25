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

import com.percussion.itemmanagement.data.PSRevisionsSummary;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.test.PSObjectRestClient;

/**
 * The class used for unit test on REST layer.
 * 
 * @author peterfrontiero
 */
public class PSItemServiceRestClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/itemmanagement/item/";

    public PSItemServiceRestClient(String baseUrl)
    {
        super(baseUrl);
    }

    public PSRevisionsSummary getRevisions(String id)
    {
        return getObjectFromPath(concatPath(getPath(), "revisions", id), PSRevisionsSummary.class);
    }

    public void restoreRevision(String id)
    {
        GET(concatPath(getPath(), "restoreRevision", id));
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
