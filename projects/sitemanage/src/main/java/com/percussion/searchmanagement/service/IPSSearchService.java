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
