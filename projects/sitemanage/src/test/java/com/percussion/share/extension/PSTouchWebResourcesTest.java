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
