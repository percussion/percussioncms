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
import com.percussion.utils.container.PSJBossConnectors;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class JBossConnectorConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {


    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        if (!Files.exists(rxDir.resolve("AppServer")))
            return;

        BaseContainerUtils containerUtils = configurationContext.getConfig();
        System.out.println("Loading installation properties ");

        PSJBossConnectors connectors = new PSJBossConnectors(rxDir.toFile());
        connectors.load();
        containerUtils.getConnectorInfo().setConnectors(connectors.getConnectors());

    }

    @Override
    public void save(DefaultConfigurationContextImpl configurationContext) {
        /*
        5.4 No longer save to jboss
        Path rxDir = configurationContext.getRootDir();
        System.out.println("Loading installation properties ");

        PSJBossConnectors connectors = new PSJBossConnectors(rxDir.toFile(),configurationContext.getConfig().getConnectorInfo());
        connectors.save();

         */
    }


}

