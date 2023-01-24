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

import java.util.Properties;

import com.percussion.error.PSExceptionUtils;
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
    public void doStartupWork(Properties startupProps) {

        if (!"true".equalsIgnoreCase(startupProps.getProperty(getPropName()))) {
            log.info("{} is set to false or missing from startup properties file. Nothing to run.",getPropName());
            return;
        }

        try {
            PSAutotuneCache cache = PSAutotuneCacheLocator.getAutotuneCache();
            cache.updateEhcache();
        }
        catch (Exception e) {
            log.error("Error updating ehcache.xml file. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
        }

        log.info("{} has completed.",getPropName() );
    }

    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr) {
        mgr.addStartupProcess(this);
    }

    static String getPropName() {
        return PSAutotuneCacheStartup.class.getSimpleName();
    }

}
