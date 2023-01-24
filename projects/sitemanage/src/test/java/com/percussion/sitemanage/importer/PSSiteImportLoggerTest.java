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
