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
package com.percussion.sitemanage.importer.dao;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSImportLogDaoTest extends ServletTestCase
{

    private static final Logger log = LogManager.getLogger(PSImportLogDaoTest.class);
    
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    @Test
    public void testDao()
    {
        assertNotNull(dao);
        
        String testSiteId1 = "1";
        String testSiteId2 = "2";
        String testTemplateId = "1";
        List<PSImportLogEntry> allentries = new ArrayList<PSImportLogEntry>();
        
        try
        {
            String typeSite = "site";
            String typeTemplate = "template";
            List<PSImportLogEntry> entries = dao.findAll(testSiteId1, typeSite);
            allentries.addAll(entries);
            assertEquals(0, entries.size());
            
            
            String logData = "log data line 1\n log data line 2\n log data line 3";
            Date now = new Date();
            PSImportLogEntry entry1 = new PSImportLogEntry(testSiteId1, typeSite, now, logData);
            dao.save(entry1);
            allentries.add(entry1);
            
            entries = dao.findAll(testSiteId1, typeSite);
            assertEquals(1, entries.size());
            assertEquals(entry1.getObjectId(), entries.get(0).getObjectId());
            assertEquals(entry1.getType(), entries.get(0).getType());
            assertEquals(entry1.getLogData(), entries.get(0).getLogData());
            
            dao.delete(entry1);
            entries = dao.findAll(testSiteId1, typeSite);
            assertEquals(0, entries.size());
            allentries.clear();
            
            PSImportLogEntry entry2 = new PSImportLogEntry(testSiteId2, typeSite, now, logData);
            dao.save(entry2);
            allentries.add(entry2);
            
            PSImportLogEntry entry3 = new PSImportLogEntry(testTemplateId, typeTemplate, now, logData);
            dao.save(entry3);
            allentries.add(entry3);
            
            entries = dao.findAll(testSiteId1, typeSite);
            assertEquals(0, entries.size());
            dao.save(entry1);
            allentries.add(entry1);
            
            entries = dao.findAll(testSiteId1, typeSite);
            assertEquals(1, entries.size());
            assertEquals(entry1.getObjectId(), entries.get(0).getObjectId());
            assertEquals(entry1.getType(), entries.get(0).getType());
            assertEquals(entry1.getLogData(), entries.get(0).getLogData());
            assertFalse(entry2.getObjectId() == entries.get(0).getObjectId());
            
            entries = dao.findAll(testSiteId2, typeSite);
            assertEquals(1, entries.size());
            assertEquals(entry2.getObjectId(), entries.get(0).getObjectId());
            assertEquals(entry2.getType(), entries.get(0).getType());
            assertEquals(entry2.getLogData(), entries.get(0).getLogData());
            assertFalse(entry3.getObjectId() == entries.get(0).getObjectId());
            
            entries = dao.findAll(testTemplateId, typeTemplate);
            assertEquals(1, entries.size());
            assertEquals(entry3.getObjectId(), entries.get(0).getObjectId());
            assertEquals(entry3.getType(), entries.get(0).getType());
            assertEquals(entry3.getLogData(), entries.get(0).getLogData());
            assertFalse(entry2.getObjectId() == entries.get(0).getObjectId());            
            
            List<String> objectIds = new ArrayList<String>();
            objectIds.add(testSiteId1);
            objectIds.add(testSiteId2);
            List<Long> entryIds = dao.findLogIdsForObjects(objectIds, typeSite);
            assertNotNull(entryIds);
            assertEquals(objectIds.size(), entryIds.size());
            assertEquals(entry1.getLogEntryId(), entryIds.get(0).longValue());
            assertEquals(entry2.getLogEntryId(), entryIds.get(1).longValue());
            
            PSImportLogEntry found = dao.findLogEntryById(entry1.getLogEntryId());
            assertNotNull(found);
            assertEquals(entry1.getLogEntryId(), found.getLogEntryId());
        } catch (IPSGenericDao.SaveException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        } finally
        {
            // clean up
            if (allentries != null && !allentries.isEmpty())
            {
                for (PSImportLogEntry entry : allentries)
                {
                    try
                    {
                        dao.delete(entry);
                    }
                    catch (Exception e)
                    {
                        //noop
                    }
                }
            }
        }
    }
    
    private IPSImportLogDao dao;

    public void setDao(IPSImportLogDao dao)
    {
        this.dao = dao;
    }

}
