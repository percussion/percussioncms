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
package com.percussion.delivery.utils.paging;

import java.util.Hashtable;
import java.util.Map;

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
    private Hashtable<String,PSRangedPageSortDirection> sortFields = new Hashtable<>();
    
    private Hashtable<String,Object> pageFields = new Hashtable<>();

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
               this.sortFields = (Hashtable<String, PSRangedPageSortDirection>) fields;
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
            this.pageFields = (Hashtable<String, Object>) fields;
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
        this.pageFields = (Hashtable<String, Object>) page.getPageFields();
        this.pageSize = page.getPageSize();
        this.sortFields = (Hashtable<String, PSRangedPageSortDirection>) page.getSortFields();
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
