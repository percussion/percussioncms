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
package com.percussion.share.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Startup process to touch configured files below the web_resources directory
 * 
 * @author JaySeletz
 */
@Category(IntegrationTest.class)
public class PSTouchWebResourcesTest
{

    @Test
    public void test() throws Exception
    {
        
        File tempDir = new File(".");
        File rootDir = new File(tempDir, "test_cm1_startup");
        if (rootDir.exists())
            FileUtils.cleanDirectory(rootDir);
        rootDir.mkdir();
        
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < 2; i++)
        {
            File testDir = new File(rootDir, "sub" + i);
            for (int j = 0; j < 2; j++)
            {
                File testFile = new File(testDir, "test" + j + ".txt");
                FileUtils.touch(testFile);
                testFile.deleteOnExit();
                files.add(testFile);
            }
        }
        
        Date date = new Date();
        for (File file : files)
        {
            assertFalse(FileUtils.isFileNewer(file, date));
        }
        
        PSTouchFiles touchFiles = new PSTouchFiles();
        touchFiles.setRootDir(rootDir.getPath());
        touchFiles.setDirNames("sub0,sub1");
        
        Properties props = new Properties();
        String propName = PSTouchFiles.getPropName();
        props.setProperty(propName, "true");
        
        touchFiles.doStartupWork(props);
        assertEquals("false", props.getProperty(propName));
        Collection<File> touchedFiles = FileUtils.listFiles(rootDir, null, true);
        for (File file : touchedFiles)
        {
            assertTrue(FileUtils.isFileNewer(file, date));
        }
    }

}
