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
package com.percussion.searchmanagement.data;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "SearchCriteria")
@XmlAccessorType(XmlAccessType.FIELD)
public class PSSearchCriteria
{
    private String query;
    
    private String searchType;
    
    private Integer startIndex;
    
    private Integer maxResults;
    
    private String sortColumn;
    
    private String sortOrder;

    private Integer formatId;


    private Map<String, String> searchFields;
    
    private String folderPath;
    
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    /**
     * Get the fields to search on, key is name and value is the value to search on.  
     * 
     * @return The fields, may be <code>null</code> or empty.
     */
    public Map<String, String> getSearchFields()
    {
        return searchFields;
    }

    public void setSearchFields(Map<String, String> searchFields)
    {
        this.searchFields = searchFields;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public Integer getStartIndex()
    {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex)
    {
        this.startIndex = startIndex;
    }

    public Integer getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults)
    {
        this.maxResults = maxResults;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }

    public String getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder)
    {
        if(StringUtils.isEmpty(sortOrder)){
            sortOrder = "asc";
        }else{
            if(sortOrder.trim().equalsIgnoreCase("asc") ||
                    sortOrder.trim().equalsIgnoreCase("desc")){
                sortOrder = sortOrder.trim().toLowerCase();
            }else{
                sortOrder = "asc";
            }
        }

        this.sortOrder = sortOrder;
    }

    public Integer getFormatId()
    {
        return formatId;
    }

    public void setFormatId(Integer formatId)
    {
        this.formatId = formatId;
    }

    public String getSearchType()
    {
        return searchType;
    }

    public void setSearchType(String searchType)
    {
        this.searchType = searchType;
    }

    /***
     * If this criteria object has no criteria set, returns true;
     * @return
     */
    public boolean isEmpty(){
        boolean ret = true;

        if(StringUtils.isNotEmpty(this.query)) {
            ret = false;
        }

        if(StringUtils.isNotEmpty(this.folderPath) && !folderPath.equalsIgnoreCase("//Sites/") ) {
            ret = false;
        }

        if(StringUtils.isNotEmpty(this.searchType)) {
            ret = false;
        }

        return ret;
    }

}
