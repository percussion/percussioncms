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

import com.percussion.utils.container.adapters.JettyDatasourceConfigurationAdapter;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationContextTest {

    @Test
    public void addConfigurationAdapters() {
    }

    @Test
    public void getRootDir() {
    }

    @Test
    public void getEncKey() {
    }

    @Test
    public void load() {
    }

    @Test
    public void save() {
    }

    @Test
    public void load1() throws IOException {

        String resourcePath = "/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties";
        String dsxml = "/com/percussion/utils/container/jetty/base/etc/perc-ds.xml";

        InputStream is = getClass().getResourceAsStream(resourcePath);
        InputStream dsxmlIs = getClass().getResourceAsStream(dsxml);

        Path root = Files.createTempDirectory("test");
        root.toFile().deleteOnExit();

        Files.createDirectories(root.resolve("jetty/base/etc"));

        Files.copy(is, root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(dsxmlIs, root.resolve("jetty/base/etc/perc-ds.xml"));

        DefaultConfigurationContextImpl ctx = new DefaultConfigurationContextImpl(root,"encKey");

        ctx.addConfigurationAdapter(new JettyDatasourceConfigurationAdapter());

        ctx.load();


        BaseContainerUtils containerUtils = ctx.getConfig();

        Assert.assertEquals("jdbc/RhythmyxData",containerUtils.getDatasources().get(0).getName());

    }

    @Test
    public void save1() {
    }
}
