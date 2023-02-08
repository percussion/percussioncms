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

package com.percussion.delivery.metadata.data;

import java.util.List;


/**
 * Represents count for total entries and list of PSDbMetadataEntry objects for requested
 * page number and page size
 * 
 * @author radharanisonnathi
 * 
 */
public class PSSearchResults
{
    private Integer totalEntries;
    private List<PSMetadataRestEntry> resultEntries;

    public PSSearchResults(){}
    /**
     * @return the results
     */
    public List<PSMetadataRestEntry> getResults()
    {
        return resultEntries;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<PSMetadataRestEntry> resultEntries)
    {
        this.resultEntries = resultEntries;
    }
    
    /**
     * @return total entries after the search
     */
    public Integer getTotalEntries()
    {
        return totalEntries;
    }

    /**
     * @param total entries to set
     */
    public void setTotalEntries(Integer totalEntries)
    {
        this.totalEntries = totalEntries;
    }
 
}
