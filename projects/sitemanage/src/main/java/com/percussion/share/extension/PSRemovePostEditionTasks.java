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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.PSServer;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * Startup process to remove the following post-edition tasks from publish on
 * demand and unpublish on demand jobs:
 * 
 * <li>perc_PushFeedDescriptorTask</li> <li>sys_flushPublicationCache</li>
 * 
 * <br/>
 * These tasks aren't necessary for on demand jobs and have been causing these
 * jobs to take quite a long time.<br />
 * <br/>
 * 
 * @author chriswright
 *
 */
public class PSRemovePostEditionTasks implements IPSStartupProcess {

    private static final Logger log = LogManager
            .getLogger(PSRemovePostEditionTasks.class.getName());

    private static final String BASE_DIR = PSServer.getBaseConfigDir();

    private static final String WORK_COMPLETED_FILE_LOCATION = BASE_DIR
            + "/Server/";

    private static final String WORK_COMPLETED_FILE = WORK_COMPLETED_FILE_LOCATION
            + "PSRemovePostEditionTasks.txt";

    private static final String MESSAGE = "Delete this file to run the PSRemovePostEditionTasks job again."
            + "\nThe job removes the following tasks from unpublish now and publish now jobs:"
            + "\n\nJava/global/percussion/task/perc_PushFeedDescriptorTask"
            + "\nJava/global/percussion/task/sys_flushPublicationCach"
            + "\n\nContact Percussion Technical Support if unsure.";

    @Override
    public void doStartupWork(Properties startupProps) throws Exception {

        if (!"true".equalsIgnoreCase(startupProps.getProperty(getPropName()))) {
            log.info(getPropName()
                    + " is set to false or missing from startup properties file. Nothing to run.");
            return;
        }

        if (hasWorkBeenCompleted()) {
            log.info(getPropName()
                    + " has already been completed.  Nothing to run.");
            return;
        }

        // surrounding with try/catch so that server resumes initialization if
        // an error is encountered. not the end of the world if post-edition
        // tasks aren't removed. server not starting up would be worse.
        try {
            IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
            IPSPublisherService psvc = PSPublisherServiceLocator
                    .getPublisherService();
            // for all sites
            for (IPSGuid guid : smgr.getAllSiteIdNames().keySet()) {
                IPSSite site = smgr.loadSite(guid);
                List<IPSEdition> editions = psvc.findAllEditionsBySite(site
                        .getGUID());
                // for all editions in the site
                for (IPSEdition edition : editions) {
                    if (edition.getDisplayTitle().contains("PUBLISH_NOW")) {
                        List<IPSEditionTaskDef> tasks = psvc
                                .loadEditionTasks(edition.getGUID());
                        // for all edition tasks in the edition
                        for (IPSEditionTaskDef task : tasks) {
                            if ("Java/global/percussion/task/perc_PushFeedDescriptorTask"
                                    .equals(task.getExtensionName())) {
                                log.info("Deleting task Java/global/percussion/task/perc_PushFeedDescriptorTask from edition: "
                                        + edition.getDisplayTitle());
                                psvc.deleteEditionTask(task);
                            } else if ("Java/global/percussion/task/sys_flushPublicationCache"
                                    .equals(task.getExtensionName())) {
                                log.info("Deleting task Java/global/percussion/task/sys_flushPublicationCache from edition: "
                                        + edition.getDisplayTitle());
                                psvc.deleteEditionTask(task);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error removing post-edition tasks within "
                    + getPropName(), e);
        }

        markAsCompleted();
        log.info(getPropName() + " has completed.  File location: "
                + WORK_COMPLETED_FILE);
    }

    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr) {
        mgr.addStartupProcess(this);
    }

    /**
     * Creates an empty file in rxconfig/Server to denote that this process has
     * been run and does not need to run again. File can be deleted to re-run
     * this process.
     */
    public void markAsCompleted() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        File workCompleted = null;

        try {
            workCompleted = new File(WORK_COMPLETED_FILE);
            if (!workCompleted.exists() || !workCompleted.isFile()) {
                workCompleted.createNewFile();
                fw = new FileWriter(workCompleted);
                bw = new BufferedWriter(fw);
                bw.write(MESSAGE);
            }
        } catch (NullPointerException npe) {
            log.error("Error opening " + WORK_COMPLETED_FILE, npe);
        } catch (IOException e) {
            log.error(
                    "Unable to create new file to indicate the completion of: "
                            + getPropName(), e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Determines if the work required for this startup task has already been
     * completed.
     * 
     * @return <code>true</code> if the work has been completed.
     */
    public boolean hasWorkBeenCompleted() {
        File workCompleted = null;
        try {
            workCompleted = new File(WORK_COMPLETED_FILE);
            if (workCompleted.isFile())
                return true;
        } catch (Exception e) {
            log.error("Error opening " + WORK_COMPLETED_FILE, e);
        }
        return false;
    }

    static String getPropName() {
        return PSRemovePostEditionTasks.class.getSimpleName();
    }

}
