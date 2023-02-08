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
package com.percussion.monitor.process;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;

import java.text.MessageFormat;

/**
 * Monitor the progress of a site copy
 * 
 * @author JaySeletz
 *
 */
public class PSSiteCopyProcessMonitor
{

    static final String MONITOR_DESIGNATOR = "SiteCopy";
    private static IPSMonitor monitor = null;
    private static final String COPY_MSG = "Copying to {0}. {1} (Step {2} of 6)";
    static final String IDLE_MSG = "No site copy in progress";
    
    private static String siteName = null;
    
    
    public PSSiteCopyProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor(MONITOR_DESIGNATOR, "Site Copy");
        updateStatusMessage(null, null);
    }

    private static void updateStatusMessage(String stepName, String stepNum)
    {
        if (monitor == null) {
            return;
        }
        
        if (siteName == null) {
            monitor.setMessage(IDLE_MSG);
        }
        else
        {
            monitor.setMessage(MessageFormat.format(COPY_MSG, siteName, stepName, stepNum));
        }
        
    }

    
    public static void startSiteCopy(String name)
    {
        siteName = name;
        updateStatusMessage(null, null);
    }
    
    public static void copyingAssetsFolder()
    {
        updateStatusMessage("Copying assets folder", "1");
    }
    
    public static void copyingSiteContent()
    {
        updateStatusMessage("Copying pages", "2");
    }
    
    public static void copyingTemplates()
    {
        updateStatusMessage("Copying templates", "3");
    }
    
    public static void updatingAssets()
    {
        updateStatusMessage("Updating assets", "4");
    }
    
    public static void updatingPageTemplateIds()
    {
        updateStatusMessage("Updating template ids", "5");
    }
    
    public static void copyingSiteConfiguration()
    {
        updateStatusMessage("Copying site configuration", "6");
    }
    
    public static void rollbackCopyOnError()
    {
        updateStatusMessage("Rollback copy on Error", "6");
    }
    
    public static void siteCopyCompleted()
    {
        siteName = null;
        updateStatusMessage(null, null);
    }
}
