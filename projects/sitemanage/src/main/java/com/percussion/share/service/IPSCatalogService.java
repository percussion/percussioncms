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

import java.io.Serializable;
import java.util.List;

import com.percussion.dashboardmanagement.service.IPSGadgetService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

/**
 * Represents a READ portion of a data service.
 * @author adamgent
 *
 * @param <T> object type. 
 * @param <PK> object key.
 */
public interface IPSCatalogService <T, PK extends Serializable>
{
    
    /**
     * Generic method used to get all objects of a particular type. This
     * is the same as lookup up all rows in a table.
     * @return List of populated objects
     * @throws DataServiceLoadException 
     * @throws DataServiceNotFoundException 
     */
    List<T> findAll() throws PSDataServiceException, IPSGadgetService.PSGadgetNotFoundException, IPSGadgetService.PSGadgetServiceException;

    /**
     * Generic method to get an object based on class and identifier. An
     * DataServiceNotFoundException Runtime Exception is thrown if
     * nothing is found.
     *
     * @param id the identifier (primary key) of the object to get
     * @return a populated object
     * @throws DataServiceLoadException TODO
     */
    T find(PK id) throws PSDataServiceException, IPSGadgetService.PSGadgetNotFoundException, IPSGadgetService.PSGadgetServiceException;
    
}
