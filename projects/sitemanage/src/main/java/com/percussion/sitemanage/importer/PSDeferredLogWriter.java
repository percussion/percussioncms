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
package com.percussion.sitemanage.importer;

import com.percussion.sitemanage.importer.dao.IPSImportLogDao;

import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Saves log in separate thread, waits for {@link IPSSiteImportLogger#waitForThreads(long)} to return
 * to ensure all threads have had a chance to write to the log
 * 
 * @author JaySeletz
 *
 */
public class PSDeferredLogWriter implements Runnable
{
    private static final Log log = LogFactory.getLog(PSDeferredLogWriter.class);
            
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
