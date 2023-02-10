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

import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.PSServer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to touch files under web_resources that may have been "back-dated" by the 
 * installer to the date the kit was built.
 * 
 * @author JaySeletz
 */
public class PSTouchFiles implements IPSStartupProcess
{
    private static final Logger log = LogManager.getLogger(PSTouchFiles.class);
            
    private String dirNames = "";
    private String rootDir = "";
    
    @Override
    public void doStartupWork(Properties startupProps) throws Exception
    {
        String propName = getPropName();
        if (!"true".equalsIgnoreCase(startupProps.getProperty(propName)))
        {
            log.info("Nothing to process");
            return;
        }
        
        if (!StringUtils.isEmpty(dirNames))
        {
            File webResourcesDir = new File(PSServer.getRxDir(), rootDir); 
            String[] touchDirNameList = dirNames.split(",");
            for (String dirName : touchDirNameList)
            {
                File baseDir = new File(webResourcesDir, dirName);
                touchFiles(baseDir);
            } 
        }
        else
        {
            log.info("No directories configured for touch");
        }
         
        startupProps.setProperty(propName, "false");
        log.info("Finished touching files");
    }

    static String getPropName()
    {
        return PSTouchFiles.class.getSimpleName();
    }

    private void touchFiles(File baseDir)
    {
        if (!baseDir.exists())
        {
            log.error("Failed to locate directory for touch: " + baseDir.getAbsolutePath());
            return;
        }
        
        log.info("Touching files in " + baseDir.getPath());
        
        Collection<File> files = FileUtils.listFiles(baseDir, null, true);
        for (File file : files)
        {
            try
            {
                FileUtils.touch(file);
            }
            catch (IOException e)
            {
                log.error("Failed to touch file: " + file.getAbsolutePath());
            }
        }
    }

    public void setDirNames(String dirNames)
    {
        this.dirNames = dirNames;
    }

    public void setRootDir(String rootDir)
    {
        this.rootDir = rootDir;
    }

    /* (non-Javadoc)
     * @see com.percussion.server.IPSStartupProcess#setStartupProcessManager(com.percussion.server.IPSStartupProcessManager)
     */
    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr)
    {
        mgr.addStartupProcess(this);
    }
    
}
