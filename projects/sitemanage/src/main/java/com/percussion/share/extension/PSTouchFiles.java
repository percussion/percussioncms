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
