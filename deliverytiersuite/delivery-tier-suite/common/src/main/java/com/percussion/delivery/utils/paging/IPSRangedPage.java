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
package com.percussion.delivery.utils.paging;

import java.util.Map;

/**
 * Defines a generic Interface for a ranged page option for 
 * lookups to backend data stores.  
 * 
 * @author natechadwick
 *
 */
public interface IPSRangedPage
{  
    /***
     * Returns a Map of the backend fields that are being sorted on, paired with the sort direction.
     * 
     * @return A Map of backend fields being sorted on, paired with their Sort direction, may be empty, never null.
     */
    public Map<String, PSRangedPageSortDirection> getSortFields();
    
    /***
     * Sets the Map of backend fields to be sorted by, indicating sort direction per field.
     * @param fields List of fields, may be empty, never null.
     */
    public void setSortFields(Map<String, PSRangedPageSortDirection> fields);
    
    /***
     * Gets a map of the current paging filters and values.
     * 
     * @return A Map of field/value pairs.
     */
    public Map<String, Object> getPageFields();
    
    /***
     * Determines the fields that are being used to apply the range filter and the
     * last value used to fetch the backend page.  
     * 
     * The return value will be a map containing each field, paired with the value
     * for that field that was at the end of the last page if the direction is forward,
     * and at the beginning of the last page if the direction is backward.
     * 
     * Field values may be null when indicating the beginning or end of a dataset.
     * 
     * @param fields A Map of field value pairs.  
     */
    public void setPageFields(Map<String, Object> fields);
    
    /***
     * Returns the current pageSize.  
     * @return  Should return a fixed size, or a default size, never 0.
     */
    public int getPageSize();
    
    /***
     * Sets the current page size.  
     * 
     * @param size  If set to 0 or a negative number, should enforce a default page size. 
     */
    public void setPageSize(int size);
    
    /***
     * The current paging directing.  
     * @return  Forward or Backward.
     */
    public PSRangedPageDirection getDirection();
    
    /***
     * Sets the current paging direction.
     * 
     * @param dir  Forward or Backward.
     */
    public void setDirection(PSRangedPageDirection dir); 
    
    /***
     * Specifies the total number of pages.
     * 
     * @param numPages
     */
    public void setPageCount(int numPages);
    
    /***
     * Returns the total number of pages. 
     * @return
     */
    public int getPageCount();
    
    /***
     * Specifies the current page.
     * @param pageNum
     */
    public void setCurrentPage(int pageNum);
    
    /***
     * Returns the current page.
     * @return
     */
    public int getCurrentPage();
  
   
}
