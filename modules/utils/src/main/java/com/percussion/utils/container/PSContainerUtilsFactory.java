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

package com.percussion.utils.container;

import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.container.adapters.JBossConnectorConfigurationAdapter;
import com.percussion.utils.container.adapters.JBossDatasourceConfigurationAdapter;
import com.percussion.utils.container.adapters.JettyDatasourceConfigurationAdapter;
import com.percussion.utils.container.adapters.JettyInstallationPropertiesConfigurationAdapter;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.io.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger ms_log = LogManager.getLogger(PSContainerUtilsFactory.class);

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
