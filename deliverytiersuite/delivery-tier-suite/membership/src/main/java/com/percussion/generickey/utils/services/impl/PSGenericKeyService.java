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
