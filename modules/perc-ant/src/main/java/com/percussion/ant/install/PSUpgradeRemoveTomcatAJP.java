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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.adapters.DtsConnectorConfigurationAdapter;
import org.apache.tools.ant.BuildException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PSUpgradeRemoveTomcatAJP extends PSAction {

    /**
     * This will handle initialization of the install logger, loading of
     * PreviousVersion.properties for upgrades, and setting of the entity
     * resolver's resolution home used to find DTD's.  It also determines if
     * all files should be refreshed by date.
     */
    @Override
    public void execute() throws BuildException {

        Path root = Paths.get(getRootDir());
        DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();
        DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(root, "ENC_KEY");
        adapter.load(config);

        //Delete Production AJP Connectors if present
        if(config.getConfig().getDtsConfig().isLocalDTSEnabled()) {
            PSLogger.logInfo("Removing AJP connectors from Production DTS if present...");
            List<IPSConnector> prodConns = config.getConfig().getDtsConfig().getDtsConnectorInfo().getConnectors();
            prodConns.removeIf(IPSConnector::isAJP);
            config.getConfig().getDtsConfig().getDtsConnectorInfo().setConnectors(prodConns);
            config.save();
            config.load();
        }

        //Delete staging AJP Connectors if present
        if(config.getConfig().getDtsConfig().isLocalStagingDTSEnabled()) {
        PSLogger.logInfo("Removing AJP connectors from Staging DTS if present...");
            List<IPSConnector> stageConns = config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().getConnectors();
            stageConns.removeIf(IPSConnector::isAJP);
            config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().setConnectors(stageConns);
            config.save();
            config.load();
        }

        super.execute();
    }
}
