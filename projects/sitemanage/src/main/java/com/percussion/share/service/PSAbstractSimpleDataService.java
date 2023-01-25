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

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.IPSGenericDao.LoadException;


public abstract class PSAbstractSimpleDataService<T, PK extends Serializable> extends PSAbstractDataService<T, T, PK> implements IPSDataService<T, T, PK> {
    

	public PSAbstractSimpleDataService(IPSGenericDao<T, PK> dao) {
		super(dao);
	}


    public T find(PK id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        validateIdParameter("find", id);
        return load(id);
    }
    


    public List<T> findAll() throws PSDataServiceException {
        try {
            return getDao().findAll();
        } catch (LoadException e) {
            String error = "Error loading all objects";
            log.error(error,e);
            throw new DataServiceLoadException(error,e);
        }
    }


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Logger log = LogManager.getLogger(getClass());
}
