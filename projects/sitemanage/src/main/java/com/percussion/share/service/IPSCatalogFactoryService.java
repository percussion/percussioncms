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
package com.percussion.share.service;

import java.io.Serializable;
import java.util.List;

import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;

public interface IPSCatalogFactoryService <T, PK extends Serializable>
{
    
    public static interface IPSCatalogItemFactory<F, PK> {
        public F create(PK id) throws DataServiceLoadException;
    }
    
    /**
     * Generic method used to get all objects of a particular type. This
     * is the same as lookup up all rows in a table.
     * @param factory 
     * @param <F> 
     * @return List of populated objects
     * @throws DataServiceLoadException 
     * @throws DataServiceNotFoundException 
     */
    <F extends T> List<F> findAll(IPSCatalogItemFactory<F, PK> factory) throws DataServiceLoadException, DataServiceNotFoundException;

    /**
     * Generic method to get an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if
     * nothing is found.
     * @param factory 
     * @param <F> 
     *
     * @param id the identifier (primary key) of the object to get
     * @return a populated object
     * @throws DataServiceLoadException TODO
     */
    <F extends T> F find(IPSCatalogItemFactory<F, PK> factory, PK id) throws DataServiceLoadException;

    /**
     * Generic method to get an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if
     * nothing is found.
     * @param factory
     * @param <F>
     *
     * @param id the identifier (primary key) of the object to get
     * @return a populated object
     * @throws DataServiceLoadException TODO
     */
    <F extends T> F find(IPSCatalogItemFactory<F, PK> factory, PK id, String relationshipTypeName) throws DataServiceLoadException;
    
}
