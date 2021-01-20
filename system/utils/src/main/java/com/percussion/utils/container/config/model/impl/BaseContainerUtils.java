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


