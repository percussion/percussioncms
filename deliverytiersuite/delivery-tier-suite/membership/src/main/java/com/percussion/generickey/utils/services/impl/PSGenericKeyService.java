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
package com.percussion.generickey.utils.services.impl;

import com.percussion.generickey.data.IPSGenericKey;
import com.percussion.generickey.services.IPSGenericKeyDao;
import com.percussion.generickey.services.IPSGenericKeyService;
import com.percussion.generickey.utils.data.rdbms.impl.PSGenericKey;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides services to create, validate and delete generic keys to be used
 * for other services.
 * 
 * @author Leonardo Hildt
 *
 */
public class PSGenericKeyService implements IPSGenericKeyService
{

    private IPSGenericKeyDao dao;

    @Autowired
    public PSGenericKeyService(IPSGenericKeyDao dao)
    {
        Validate.notNull(dao);
        this.dao = dao;
    }

    @Override
    public String generateKey(long duration) throws Exception
    {
        // get a calendar instance
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        currentDate = DateUtils.addMilliseconds(currentDate, (int) duration);

        // create the generic key
        //IPSGenericKey genericKey = new PSGenericKey();
        IPSGenericKey genericKey = dao.createKey();
        genericKey.setExpirationDate(currentDate);
        genericKey.setGenericKey(UUID.randomUUID().toString());
        dao.saveKey(genericKey);

        return genericKey.getGenericKey();
    }

    @Override
    public boolean isValidKey(String key) throws Exception
    {
        boolean isValidKey = false;

        // get a calendar instance
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        IPSGenericKey genericKey = dao.findByResetKey(key);
        if (genericKey != null && genericKey.getGenericKey().equalsIgnoreCase(key)
                && (genericKey.getExpirationDate().compareTo(currentDate) > 0))
        {
            isValidKey = true;
        }

        return isValidKey;
    }

    @Override
    public void deleteKey(String key) throws Exception
    {
        IPSGenericKey genericKey = dao.findByResetKey(key);

        if (genericKey == null)
        {
            // the key provided doesn't exist
            throw new Exception("Unable to locate generic key for key: " + key);
        }
        dao.deleteKey(genericKey);
    }

}
