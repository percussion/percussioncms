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
