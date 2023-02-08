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
import com.percussion.utils.container.PSDtsConfig;

import java.nio.file.Path;

public class DtsConnectorConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {

    private static String PROD_PATH = "Deployment";
    private static String STAGING_PATH = "Staging/Deployment";
    private static String SERVER_XML = "Server/conf/server.xml";
    private static String CATALINA_PROPERTIES = "Server/conf/perc/perc-catalina.properties";

    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path configRoot = configurationContext.getRootDir();
        Path mainDts = configRoot.resolve(PROD_PATH);
        Path stageDts = configRoot.resolve(STAGING_PATH);
        Path connectorFileRoot = mainDts.resolve("Server");

        PSDtsConfig dtsConfig = new PSDtsConfig(configRoot);
        dtsConfig.load();
        configurationContext.getConfig().setDtsConfig(dtsConfig);

    }


    @Override
    public void save(DefaultConfigurationContextImpl configurationContext) {
      //TODO
    }


}

