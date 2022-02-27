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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

