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
package com.percussion.share.service;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.share.dao.IPSGenericDao.LoadException;
import com.percussion.share.dao.IPSGenericDao.SaveException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
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
            String error = format("Error deleting object: {0}", id);
            log.error(error,e);
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
            String error = format("Error saving object: {0}", object);
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
