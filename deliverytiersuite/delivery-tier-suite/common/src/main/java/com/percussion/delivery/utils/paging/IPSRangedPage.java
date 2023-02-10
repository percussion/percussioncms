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
