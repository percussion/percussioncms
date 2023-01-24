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
package com.percussion.searchmanagement.service;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedItemPropertiesList;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;

import java.util.List;

public interface IPSSearchService
{
    /**
     * Retrieve the items found after performing a full text search for the
     * given text.
     * 
     * @param text , text must be specified. never <code>null</code>, or empty
     * @return list of search result objects, never <code>null</code>, may be
     *         empty.
     * @throws <code>PSFinderServiceException</code> if any error occurs.
     */
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException;
    
    /**
     * Creates the search result objects for the supplied content ids and returns them.
     * @param criteria uses for search result columns
     * @param contentIdList must not be <code>null</code>
     * @return list of search result objects, never <code>null</code>, may be
     *         empty.
     * @throws PSSearchServiceException
     */
    public PSPagedItemList search(PSSearchCriteria criteria, List<Integer> contentIdList) throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException;
    
    /**
     * Retrieve the items found after performing a full text search for the
     * given text.
     * @param criteria search sriteria
     * @return list of search result objects, never <code>null</code>, may be
     *         empty. The item list will have the list of {@link PSItemProperties}
     * @throws <code>PSFinderServiceException</code> if any error occurs.
     */
    public PSPagedItemPropertiesList getExtendedSearchResults(PSSearchCriteria criteria) throws PSSearchServiceException;

    public List<Integer> getContentIdsForFetchingByStatus(PSSearchCriteria criteria);

    public PSPagedItemList searchByStatus(PSSearchCriteria criteria, List<Integer> contentIdList) throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException;

    public PSSearchCriteria validateSearchCriteria(PSSearchCriteria criteria);
    

}
