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

import com.percussion.security.PSEncryptor;
import com.percussion.utils.container.adapters.JBossConnectorConfigurationAdapter;
import com.percussion.utils.container.adapters.JBossDatasourceConfigurationAdapter;
import com.percussion.utils.container.adapters.JettyDatasourceConfigurationAdapter;
import com.percussion.utils.container.adapters.JettyInstallationPropertiesConfigurationAdapter;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class PSContainerUtilsFactory {

    private static final ConcurrentHashMap<String, DefaultConfigurationContextImpl> factoryInstances = new ConcurrentHashMap<>();

    private static final List<IPSConfigurationAdapter<DefaultConfigurationContextImpl>> adapterList = new CopyOnWriteArrayList<>();
    /**
     * Logger
     */
    private static Log ms_log = LogFactory.getLog(PSContainerUtilsFactory.class);

    static {


        adapterList.add(new JBossDatasourceConfigurationAdapter());
        adapterList.add(new JBossConnectorConfigurationAdapter());

        adapterList.add(new JettyDatasourceConfigurationAdapter());
        adapterList.add(new JettyInstallationPropertiesConfigurationAdapter());
        //adapterList.add(new LoggingContainerConfigurationAdapter());
    }

    public static DefaultConfigurationContextImpl getConfigurationContextInstance(Path path) {
        DefaultConfigurationContextImpl value = factoryInstances.computeIfAbsent(path.normalize().toAbsolutePath().toString(), k-> addNew(k, PSLegacyEncrypter.getInstance(PathUtils.getRxPath().toAbsolutePath().toString().concat(PSEncryptor.SECURE_DIR)).getPartOneKey()));
        return value;
    }

    public static DefaultConfigurationContextImpl getConfigurationContextInstance() {
        return getConfigurationContextInstance(PathUtils.getRxDir(null).toPath());
    }

    public static BaseContainerUtils getInstance() {
        return getConfigurationContextInstance().getConfig();
    }

    public static BaseContainerUtils getInstance(Path root) {
        return getConfigurationContextInstance(root).getConfig();
    }

    public void save(Path path)
    {
        getConfigurationContextInstance(path).save();
    }

    public void load(Path path)
    {
        getConfigurationContextInstance(path).load();
    }

    public void save()
    {
       getConfigurationContextInstance().save();
    }

    public void load()
    {
        getConfigurationContextInstance().load();
    }


    private static DefaultConfigurationContextImpl addNew(String path, String key) {
        ms_log.info("Creating new Configuration context for path "+path);
        DefaultConfigurationContextImpl newContext = new DefaultConfigurationContextImpl(Paths.get(path), key, BaseContainerUtils::new);
        getAdapterList().forEach(newContext::addConfigurationAdapter);
        newContext.load();
        return newContext;
    }

    public static List<IPSConfigurationAdapter<DefaultConfigurationContextImpl>> getAdapterList() {
        return adapterList;
    }

    public static void setAdapterList(List<IPSConfigurationAdapter<DefaultConfigurationContextImpl>> adapterList) {
        PSContainerUtilsFactory.adapterList.clear();
        PSContainerUtilsFactory.adapterList.addAll(adapterList);
    }

}
