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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.importer.dao;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
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
