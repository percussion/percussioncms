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
package com.percussion.share.service;

import java.util.List;

import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;

/**
 * An extremely low level wrapper to 
 * CM System for relatively fast item retrieval.
 * <p>
 * Be aware: Although this is public API it should probably not be used externally
 * and maybe removed in the future.
 * 
 * @author adamgent
 *
 * @param <S> item summary type.
 */
public interface IPSItemSummaryService<S extends IPSItemSummary> extends IPSCatalogService<S, String>
{
    /**
     * Returns the id for the given path.
     * The path could be to an asset, page, or folder.
     * <strong>
     * NOTICE that the return value maybe <code>null</code>.
     * </strong>
     * Higher level layers and API should deal with the null return value.
     * @param path never <code>null</code> or empty.
     * @return maybe <code>null</code> if there is not item at the given path.
     */
    public String pathToId(String path) throws IPSDataService.DataServiceNotFoundException;
    /**
     * Returns the id for the given path.
     * The path could be to an asset, page, or folder.
     * <strong>
     * NOTICE that the return value maybe <code>null</code>.
     * </strong>
     * Higher level layers and API should deal with the null return value.
     * @param path never <code>null</code> or empty.
     * @return maybe <code>null</code> if there is not item at the given path.
     */
    public String pathToId(String path, String relationshipTypeName) throws IPSDataService.DataServiceNotFoundException;
    
    /**
     * Returns the items that are children to the given id.
     * The id should probably be an item that is a folder.
     * @param id never <code>null</code> or empty.
     * @return never <code>null</code>, maybe empty.
     * @throws DataServiceLoadException if the item is not valid to have children or does not exist.
     */
    public List<S> findFolderChildren(String id) throws DataServiceLoadException;
}
