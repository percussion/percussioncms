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
import com.percussion.search.PSSearchIndexEventQueue;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the state of the import process monitor
 * 
 * @author JaySeletz
 *
 */
public class PSImportProcessMonitor
{
    private static final String CATALOG_PAGES_MSG = " cataloged pages waiting for import";
    private static final String CATALOG_PAGE_MSG = "1 cataloged page waiting for import";
    private static final String NO_CATALOG_PAGE_MSG = "No cataloged pages waiting for import";
    
    private static IPSMonitor monitor = null;
    private static int curCount = 0;
    private static AtomicBoolean indexPause = new AtomicBoolean(false);
    public PSImportProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("Import", "Import");
        setCatalogCount(0);
    }
    
    public static void setCatalogCount(int count)
    {
        if (monitor == null) {
            return;
        }
        
        curCount = count;
        String msg = NO_CATALOG_PAGE_MSG;
        if (count > 0) {
            msg = count == 1 ? CATALOG_PAGE_MSG : count + CATALOG_PAGES_MSG;
        }
        
        monitor.setMessage(msg);
    }
    
    public static int getCatalogCount()
    {
        return curCount;
    }
    
}
