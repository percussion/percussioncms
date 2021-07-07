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
