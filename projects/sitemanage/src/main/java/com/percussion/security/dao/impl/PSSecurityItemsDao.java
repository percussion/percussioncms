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
package com.percussion.security.dao.impl;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.security.dao.IPSSecurityItemsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author miltonpividori
 *
 */
public class PSSecurityItemsDao implements IPSSecurityItemsDao
{
    private static final Logger logger = LogManager.getLogger(PSSecurityItemsDao.class);

    /**
     * An {@link IPSFileSystemService} implementation pointing to the root folder where
     * the SSH private keys are stored.
     */
    private IPSFileSystemService fileSystemService;
    
    public PSSecurityItemsDao(IPSFileSystemService privateKeysFileSystemService)
    {
        this.fileSystemService = privateKeysFileSystemService;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.security.dao.IPSSecurityItemsDao#getAvailablePrivateKeys()
     */
    public List<String> getAvailablePrivateKeys()
    {
        List<File> privateKeys;
        List<String> keyNames = new ArrayList<>();

        try
        {
            privateKeys = fileSystemService.getChildren("/");
            for (File file : privateKeys) {
                keyNames.add(file.getName());
            }
        }
        catch (FileNotFoundException e)
        {
            logger.debug("rxconfig/ssh-keys folder is missing.",e);
        }
        return keyNames;
    }

}
