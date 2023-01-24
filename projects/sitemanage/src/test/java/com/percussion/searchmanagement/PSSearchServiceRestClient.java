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

package com.percussion.searchmanagement;

import com.percussion.share.data.PSPagedItemList;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.share.test.PSDataServiceRestClient;

public class PSSearchServiceRestClient extends PSDataServiceRestClient<PSPagedItemList>
{
    public PSSearchServiceRestClient(String url)
    {
        super(PSPagedItemList.class, url, "/Rhythmyx/services/searchmanagement/search");
    }

    public PSPagedItemList search(PSSearchCriteria criteria)
    {
        return postObjectToPath(concatPath(getPath(), "/get"), criteria, PSPagedItemList.class);
    }
}
