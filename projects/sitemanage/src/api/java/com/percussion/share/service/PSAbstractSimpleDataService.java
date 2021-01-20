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

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.IPSGenericDao.LoadException;


public abstract class PSAbstractSimpleDataService<T, PK extends Serializable> extends PSAbstractDataService<T, T, PK> implements IPSDataService<T, T, PK> {
    

	public PSAbstractSimpleDataService(IPSGenericDao<T, PK> dao) {
		super(dao);
	}


    public T find(PK id) throws DataServiceLoadException, DataServiceNotFoundException {
        validateIdParameter("find", id);
        return load(id);
    }
    


    public List<T> findAll() {
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
    protected final Log log = LogFactory.getLog(getClass());
}
