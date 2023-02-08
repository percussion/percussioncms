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
package com.percussion.sitemanage.importer;

import com.percussion.sitemanage.importer.dao.IPSImportLogDao;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Saves log in separate thread, waits for {@link IPSSiteImportLogger#waitForThreads(long)} to return
 * to ensure all threads have had a chance to write to the log
 * 
 * @author JaySeletz
 *
 */
public class PSDeferredLogWriter implements Runnable
{
    private static final Logger log = LogManager.getLogger(PSDeferredLogWriter.class);
            
    private String siteId;
    private String desc;
    private IPSSiteImportLogger logger;
    private String objectId;
    private IPSImportLogDao logDao;

    
    /**
     * Construct a log writer.  Call {@link #saveWhenReady()} to start the deferred log writer thread.
     * 
     * @param siteId
     * @param desc
     * @param logger
     * @param objectId
     * @param logDao
     */
    public PSDeferredLogWriter(String siteId, String desc, IPSSiteImportLogger logger, String objectId,
            IPSImportLogDao logDao)
    {
        super();
        this.siteId = siteId;
        this.desc = desc;
        this.logger = logger;
        this.objectId = objectId;
        this.logDao = logDao;
    }
    
    /**
     * Start a thread and wait for the logger to be ready, then save it
     */
    public void saveWhenReady()
    {
        Executors.newSingleThreadExecutor().execute(this);
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        try
        {
            logger.waitForThreads(60);
            PSSiteImporter.saveImportLog(objectId, logger, logDao, siteId, desc);
        }
        catch (Exception e)
        {
            log.error("Failed to save import log for ID " + objectId + " and type " + logger.getType().name() + ": " + e.getLocalizedMessage(), e);
        }  
    }
}
