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

package com.percussion.utils.container;

import com.percussion.utils.container.config.ContainerConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PSDtsConfig implements IPSDtsConfig, ContainerConfig {

    boolean localStagingDTSEnabled = false;
    boolean localDTSEnabled = false;

    private Path rxDir;

    private PSAbstractXmlConnectors dtsConnector;
    private PSAbstractXmlConnectors stagingDtsConnector;

    public PSDtsConfig(Path rxDir)

    {
        this.rxDir = rxDir;
        Path mainPath = rxDir.resolve(Paths.get("Deployment"));
        Path stagingPath =  rxDir.resolve(Paths.get("Staging/Deployment"));
        if (Files.exists(mainPath)) {
            localDTSEnabled=true;
            dtsConnector = new PSTomcatConnectors(rxDir, mainPath);
        }
        if (Files.exists(stagingPath)) {
            localStagingDTSEnabled=true;
            stagingDtsConnector = new PSTomcatConnectors(rxDir, stagingPath);
        }
    }

    public Path getRxDir() {
        return rxDir;
    }


    @Override
    public boolean isLocalStagingDTSEnabled() {
        return localStagingDTSEnabled;
    }

    @Override
    public boolean isLocalDTSEnabled() {
        return localDTSEnabled;
    }

    @Override
    public PSAbstractConnectors getDtsConnectorInfo() {
        return dtsConnector;
    }

    @Override
    public PSAbstractConnectors getStagingDtsConnectorInfo() {
        return stagingDtsConnector;
    }

    @Override
    public String toString() {
        return "PSDtsConfig{" +
                "localStatingDTSEnabled=" + localStagingDTSEnabled +
                ", localDTSEnabled=" + localDTSEnabled +
                ", rxDir=" + rxDir +
                ", dtsConnector=" + dtsConnector +
                ", stagingDtsConnector=" + stagingDtsConnector +
                '}';
    }

    public void load() {
        if (localDTSEnabled)
            dtsConnector.load();
        if (localStagingDTSEnabled)
            stagingDtsConnector.load();
    }

    public void save() {
        if (localDTSEnabled)
            dtsConnector.save();
        if (localStagingDTSEnabled)
            stagingDtsConnector.save();
    }
}
