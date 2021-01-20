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

import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;

import java.util.List;

import static junit.framework.Assert.*;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSSiteImportLoggerTest
{
    @Test
    public void testImportLogErrors() throws Exception
    {
        String siteId = "1234";
        String desc = "//sites/mysite/index";
        IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.TEMPLATE);
        assertNull(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc));
        
        String category = "cat";
        logger.appendLogMessage(PSLogEntryType.ERROR, category, "error1");
        assertNull(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc));
        
        
        logger.logErrors();
        assertNotNull(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc));
        String log = logger.getLog();
        assertTrue(!StringUtils.isEmpty(log));
        assertEquals(log, logger.getLog());
        
        assertTrue(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc).isEmpty());
        String errorString = "error";
        int i = 0;
        logger.appendLogMessage(PSLogEntryType.ERROR, category, errorString + i++);
        assertNotNull(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc));
        assertTrue(!logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc).isEmpty());
        String log2 = logger.getLog();
        assertTrue(!log.equals(log2));
        
        logger.appendLogMessage(PSLogEntryType.ERROR, category, errorString + i++);
        assertNotNull(logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc));
        assertTrue(!logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc).isEmpty());
        assertTrue(!log2.equals(logger.getLog()));
        assertEquals(2, logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc).size());       
        
        List<PSImportLogEntry> entries = logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc);
        for (int j = 0; j < entries.size(); j++)
        {
            PSImportLogEntry entry = entries.get(j);
            assertEquals(siteId, entry.getObjectId());
            assertEquals(PSLogObjectType.SITE_ERROR.name(), entry.getType());
            assertEquals(category, entry.getCategory());
            assertEquals(desc, entry.getDescription());
            assertEquals(errorString + j, entry.getLogData());
        }
    }
}
