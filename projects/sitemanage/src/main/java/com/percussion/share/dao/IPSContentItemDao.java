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
package com.percussion.share.dao;

import java.io.Serializable;
import java.util.Collection;

import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.data.IPSItemSummary;

import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import org.springframework.validation.Errors;

public interface IPSContentItemDao extends IPSGenericDao<PSContentItem, String>, IPSRelationshipCataloger
{

    public void validateDelete(String id, Errors errors);
    public PSContentItem findItemByPath(String name, String folderPath) throws PSDataServiceException;
    public PSContentItem findItemByPath(String fullPath) throws PSDataServiceException;
    public IPSItemSummary addItemToPath(IPSItemSummary item, String folderPath) throws PSDataServiceException;

    /**
     * Gets the content item from its identifier, similar with 
     * {@link #find(String)}, except caller can specify the returned object
     * includes all fields or only the summary properties of the object.
     *  
     * @param id the identifier (primary key) of the object to get
     * @param isSummary <code>true</code> if load summary properties of the 
     * items, which does not include Clob or Blob type fields; otherwise load 
     * all properties of the items.
     * 
     * @return item. It may be <code>null</code> if cannot find the specified item.
     * 
     * @throws LoadException if error occurs during the find operation.
     */
    PSContentItem find(String id, boolean isSummary) throws PSDataServiceException;
    
    /**
     * Turns revision control on for the item with the given id.
     * @param id Id of the item.
     */
    public void revisionControlOn(String id) throws LoadException;

    /**
     * @param item may not be <code>null</code>.
     * @param folderPath may not be <code>null</code> or empty.
     */
    public void removeItemFromPath(IPSItemSummary item, String folderPath) throws PSDataServiceException;
    
    /**
     * Gets all item IDs for a specified Content Type.
     * 
     * @param name the name of the Content Type, not blank.
     * 
     * @return a list of item IDs with the specified Content Type name, 
     * not <code>null</code>, but may empty.
     */
    public Collection<Integer> findAllItemIdsByType(String name) throws PSDataServiceException;
}
