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
package com.percussion.delivery.utils.paging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a generic implementation of a Ranged Page object.
 * 
 * Intended for use in all Delivery Services that retrieve data 
 * from the sever for processing in the client user interface.
 * 
 * In general all find operations must implement paging to ensure
 * the viability/scalability and performance of both the client and server. 
 * 
 * For corner case datasets where Ranged Paging will not work, an alternative 
 * paging provider should be created. 
 * 
 * @author natechadwick
 *
 */
public class PSDefaultRangedPage implements IPSRangedPage
{
    /***
     * Default used when page size is not set. 
     * Target is roughly 3 UX screens of data. 
     */
    public static final int DEFAULT_PAGE_SIZE=75;
    
    /***
     * The direction of the paging operation. 
     */
    private PSRangedPageDirection direction;
    
    /***
     * The map of sort fields with sort directions
     */
    private ConcurrentHashMap<String,PSRangedPageSortDirection> sortFields = new ConcurrentHashMap<>();
    
    private ConcurrentHashMap<String,Object> pageFields = new ConcurrentHashMap<>();

    private int pageSize = DEFAULT_PAGE_SIZE;
    
    private int pageCount;
    private int currentPage;
    
    
    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#getSortFields()
     */
    @Override
    public Map<String, PSRangedPageSortDirection> getSortFields()
    {
        return this.sortFields;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#setSortFields(java.util.List)
     */
    @Override
    public void setSortFields(Map<String, PSRangedPageSortDirection> fields)
    {
           if(fields!=null){
               this.sortFields = (ConcurrentHashMap<String, PSRangedPageSortDirection>) fields;
           }               
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#getPageFields()
     */
    @Override
    public Map<String, Object> getPageFields()
    {
        return this.pageFields;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#setPageFields(java.util.Map)
     */
    @Override
    public void setPageFields(Map<String, Object> fields)
    {
        if(fields==null)
            throw new IllegalArgumentException("Field list may not be null");
        else
            this.pageFields = (ConcurrentHashMap<String, Object>) fields;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#getPageSize()
     */
    @Override
    public int getPageSize()
    {
        if(this.pageSize<=0)
            return DEFAULT_PAGE_SIZE;
        else
            return this.pageSize;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#setPageSize(long)
     */
    @Override
    public void setPageSize(int size)
    {
        if(size <= 0)
            this.pageSize = DEFAULT_PAGE_SIZE;
        else
            this.pageSize = size; 
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#getDirection()
     */
    @Override
    public PSRangedPageDirection getDirection()
    {
        if(this.direction==null){
            this.direction = PSRangedPageDirection.FORWARD;
        }
        
        return this.direction;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.IPSRangedPage#setDirection(com.percussion.delivery.utils.IPSRangedPage.IPSRangedPageDirection)
     */
    @Override
    public void setDirection(PSRangedPageDirection dir)
    {
        this.direction = dir;
    }
    
    public PSDefaultRangedPage(){}
    
    public PSDefaultRangedPage(IPSRangedPage page){
        this.direction = page.getDirection();
        this.pageFields = (ConcurrentHashMap<String, Object>) page.getPageFields();
        this.pageSize = page.getPageSize();
        this.sortFields = (ConcurrentHashMap<String, PSRangedPageSortDirection>) page.getSortFields();
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.paging.IPSRangedPage#setPageCount(int)
     */
    @Override
    public void setPageCount(int numPages)
    {
        pageCount = numPages;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.paging.IPSRangedPage#getPageCount()
     */
    @Override
    public int getPageCount()
    {
        return pageCount;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.paging.IPSRangedPage#setCurrentPage(int)
     */
    @Override
    public void setCurrentPage(int pageNum)
    {
        currentPage = pageNum;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.utils.paging.IPSRangedPage#getCurrentPage()
     */
    @Override
    public int getCurrentPage()
    {
        return currentPage;
    }

}
