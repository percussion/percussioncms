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

package com.percussion.utils.container.config.model.impl;

import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.IPSDtsConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSAbstractConnectors;
import com.percussion.utils.container.config.ContainerConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import java.util.ArrayList;
import java.util.List;

public class BaseContainerUtils implements IPSContainerUtils, ContainerConfig {


    private PSAbstractConnectors connectorInfo = new PSAbstractConnectors();

    private IPSDtsConfig dtsConfig=null;

    private List<IPSJndiDatasource> datasources = new ArrayList<>();
    private IPSDatasourceResolver datasourceResolver = new PSDatasourceResolver();
    private boolean enabled = false;
    private boolean isLoaded = false;


    @Override
    public PSAbstractConnectors getConnectorInfo() {
        return connectorInfo;
    }

    @Override
    public List<IPSJndiDatasource> getDatasources(){
        return datasources;
    }

    @Override
    public void setDatasources(List<IPSJndiDatasource> datasources)
    {
        this.datasources = datasources;
    }

    @Override
    public IPSDatasourceResolver getDatasourceResolver() {
        return this.datasourceResolver;
    }

    @Override
    public void setDatasourceResolver(IPSDatasourceResolver resolver) {
        this.datasourceResolver = resolver;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }
    @Override
    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public IPSDtsConfig getDtsConfig() {
        return dtsConfig;
    }


    public void setDtsConfig(IPSDtsConfig dtsConfig) {

        this.dtsConfig = dtsConfig;
    }

    @Override
    public String toString() {
        return "BaseContainerUtils{" +
                "connectorInfo=" + connectorInfo +
                ", datasources=" + datasources +
                ", resolver=" + datasourceResolver +
                ", enabled=" + enabled +
                ", isLoaded=" + isLoaded +
                '}';
    }
}


