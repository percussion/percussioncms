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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PSAbstractConnectorsTest {
    Map<String,String> loadMap;

    @Test
    @Before
    public void loadProperties() {
        File root = new File(getClass().getClassLoader().getResource("com/percussion/utils/container/sample.properties").getFile());
        Path rootPath = Paths.get(root.toURI());

        PSAbstractConnectors loadProp=new PSAbstractConnectors();
        loadMap = loadProp.loadProperties(rootPath);
    }

    @Test
    @After
    public void saveProperties() {
        Path root = Paths.get(new File(getClass().getClassLoader().getResource("com/percussion/utils/container/sample1.properties").getFile()).toURI());
        PSAbstractConnectors saveProp=new PSAbstractConnectors();

        saveProp.saveProperties(loadMap,root);


    }


}
