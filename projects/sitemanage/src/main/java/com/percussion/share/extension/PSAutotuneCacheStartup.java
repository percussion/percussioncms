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

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.cache.PSAutotuneCache;
import com.percussion.server.cache.PSAutotuneCacheLocator;

/**
 * Startup process to auto tune the ehcache.xml.<br/><br/>
 * {@link com.percussion.server.cache.PSAutotuneCache}
 * 
 * @author chriswright
 *
 */
public class PSAutotuneCacheStartup implements IPSStartupProcess {
    
    private static final Logger log = LogManager
            .getLogger(PSAutotuneCacheStartup.class.getName());

    @Override
    public void doStartupWork(Properties startupProps) throws Exception {

        if (!"true".equalsIgnoreCase(startupProps.getProperty(getPropName()))) {
            log.info(getPropName()
                    + " is set to false or missing from startup properties file. Nothing to run.");
            return;
        }

        try {
            PSAutotuneCache cache = PSAutotuneCacheLocator.getAutotuneCache();
            cache.updateEhcache();
        }
        catch (Exception e) {
            log.error("Error updating ehcache.xml file.", e);
        }

        log.info(getPropName() + " has completed.");
    }

    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr) {
        mgr.addStartupProcess(this);
    }

    static String getPropName() {
        return PSAutotuneCacheStartup.class.getSimpleName();
    }

}
