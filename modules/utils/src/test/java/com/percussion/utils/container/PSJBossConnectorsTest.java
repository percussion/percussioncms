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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PSJBossConnectorsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    File root;


    @Before
    public void setup() throws IOException {
         root = tempFolder.newFolder();
         if(! Files.exists(root.toPath().resolve("AppServer/server/rx/deploy/jboss-web.deployer"))) {
             Path p = Files.createDirectories(root.toPath().resolve("AppServer/server/rx/deploy/jboss-web.deployer"));

             Files.copy(
                     getClass().getResourceAsStream("/com/percussion/utils/container/AppServer/server/rx/deploy/jboss-web.deployer/server.xml"),
                     p.resolve("server.xml"));
         }
    }

    @Test
    public void load() throws IOException {


        PSJBossConnectors c = new PSJBossConnectors(root);
        c.load();
        System.out.println(c);

    }

    @Test
    @Ignore("TODO: This test is failing. Fix it please!")
    public void save() throws IOException {


        PSJBossConnectors c = new PSJBossConnectors(root);
        c.load();
        c.save();
    }

}
