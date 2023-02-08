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

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.share.dao.IPSGenericDao.LoadException;
import com.percussion.share.dao.IPSGenericDao.SaveException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfNull;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.Validate.notNull;

public abstract class PSAbstractDataService <FULL, SUM, PK extends Serializable> implements IPSDataService<FULL, SUM, PK>
{

    protected IPSGenericDao<FULL, PK>  dao;
    
    
    /**
     * @param dao never <code>null</code>.
     */
    public PSAbstractDataService(IPSGenericDao<FULL, PK> dao)
    {
        super();
        notNull(dao);
        this.dao = dao;
    }

    public PSValidationErrors validate(FULL obj) throws PSValidationException {
        return PSBeanValidationUtils.getValidationErrorsOrFailIfInvalid(obj);
    }
    
    public void delete(PK id) throws PSDataServiceException
    {
        validateIdParameter("delete", id);
        try {
            getDao().delete(id);
        } catch (DeleteException e) {
            String error = format("Error deleting object: {}", id);
            log.error("Error: {}",  e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new DataServiceDeleteException(error,e);
        }
    }

    public FULL load(PK id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        validateIdParameter("load", id);
        
        try {
            FULL item = getDao().find(id);
            if (item == null) 
                throw new DataServiceNotFoundException("Item not found:" + id.toString());
            return item;
        } catch (PSDataServiceException e) {
            throw new DataServiceLoadException(e);
        }
    }

    public FULL save(FULL object) throws PSDataServiceException {
        try {
            validate(object);
            return getDao().save(object);
        } catch (SaveException | LoadException | DeleteException e) {
            String error = format("Error saving object: {}", object);
            throw new DataServiceSaveException(error,e);
        }
    }
    
    protected final IPSGenericDao<FULL, PK> getDao() {
        return dao;
    }
    
    protected void validateIdParameter(String action, PK id) throws PSValidationException {

        rejectIfNull(action, "id", id);
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSAbstractDataService.class);
}
