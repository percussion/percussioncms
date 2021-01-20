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
