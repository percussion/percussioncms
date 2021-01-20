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