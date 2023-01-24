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

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.rxfix.PSFixResult;
import com.percussion.rxfix.PSRxFix;
import com.percussion.rxfix.PSRxFix.Entry;
import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSCacheProxy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PSRxFixStartupProcess implements IPSStartupProcess
{
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);

    IPSStartupProcessManager startupProcessManager;

    @Override
    public void doStartupWork(Properties startupProps) throws Exception
    {
        String propName = getPropName();
        String propValue = startupProps.getProperty(propName);
        if (StringUtils.isEmpty(propValue))
        {
            log.info("Nothing to process");
            return;
        }
        List<String> fixes = Arrays.asList(propValue.split(",\\s*"));

        PSRxFix fixer = getFixer(fixes);

        fixer.doFix(false,startupProcessManager);


        List<PSRxFix.Entry> entries = fixer.getEntries();

        // Print out results
        for (PSRxFix.Entry e : entries)
        {
            log.info("Running RxFix Fix: {}" ,e.getFixname());
            List<PSFixResult> result = e.getResults();
            if (result != null)
            {
                for (PSFixResult r : result)
                {
                    log.info(r);
                }
            }
        }
        
        try {
            if (PSCacheManager.isAvailable()) {
                PSCacheManager cacheManager = PSCacheManager.getInstance();
                cacheManager.flush();
                PSCacheProxy.flushFolderCache();
            }
        }
        catch (Exception e) {
            log.error("Error flushing folder cache. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
        }

        log.info("Finished running data updates.");
    }

    private PSRxFix getFixer(List<String> fixes) throws Exception
    {
        PSRxFix fixer = new PSRxFix();

        // TODO: only test running fixes that are used by the installer since
        // others fail, and we aren't going to take the time to fix them now.
        Iterator<Entry> iter = fixer.getEntries().iterator();
        while (iter.hasNext())
        {
            Entry entry = iter.next();
            if (fixes.contains(entry.getFix().getSimpleName()))
            {
                // keep these
                continue;
            }

            // remove others
            iter.remove();
        }
        return fixer;
    }

    static String getPropName()
    {
        return "RXFIX";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.server.IPSStartupProcess#setStartupProcessManager(com.
     * percussion.server.IPSStartupProcessManager)
     */
    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr)
    {
        if(mgr != null ){
            startupProcessManager = mgr;
            mgr.addStartupProcess(this);
        }

    }

}
