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
package com.percussion.security.dao.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.error.PSExceptionUtils;
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
    private static final Logger logger = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);

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
                //skip the ssh config file
                if(!file.getName().equalsIgnoreCase("config")) {
                    keyNames.add(file.getName());
                }
            }
        }
        catch (FileNotFoundException e)
        {
            logger.warn("rxconfig/ssh-keys folder is missing. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
        }
        return keyNames;
    }

}
