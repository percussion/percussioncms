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

package com.percussion.utils.container.adapters;

import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConfigurationAdapter;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.PSAbstractConnector;
import com.percussion.utils.container.PSJettyConnectors;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;

import java.nio.file.Path;
import java.util.List;

public class JettyInstallationPropertiesConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {



    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        BaseContainerUtils containerUtils = configurationContext.getConfig();
        System.out.println("Loading installation properties ");

        PSJettyConnectors connectors = new PSJettyConnectors(rxDir);
        connectors.load();
        List<IPSConnector> conItems = connectors.getConnectors();
        if (conItems.size()==0)
        {
            List<IPSConnector> existingConnectors = containerUtils.getConnectorInfo().getConnectors();
            if (existingConnectors.size()==0)
            {
                // add default port
                conItems.add(PSAbstractConnector.getBuilder().setPort(9992).build());
            }
        } else
            containerUtils.getConnectorInfo().setConnectors(conItems);

    }

    @Override
    public void save(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        BaseContainerUtils containerUtils = configurationContext.getConfig();
        System.out.println("Loading installation properties ");

        PSJettyConnectors connectors = new PSJettyConnectors(rxDir,configurationContext.getConfig().getConnectorInfo());
        connectors.save();
    }


}

