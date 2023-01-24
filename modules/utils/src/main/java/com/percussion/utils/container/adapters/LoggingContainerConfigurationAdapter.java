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
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;

import java.nio.file.Path;

public class LoggingContainerConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {

    private boolean enabled = true;


    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        BaseContainerUtils containerUtils = configurationContext.getConfig();
        System.out.println("Logging load configuration from "+rxDir.toString()+" container utils ="+containerUtils.toString());
        containerUtils.setEnabled(enabled);

    }

    @Override
    public void save(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        BaseContainerUtils containerUtils = configurationContext.getConfig();

        System.out.println("Logging save configuration from "+rxDir.toString()+" container utils ="+containerUtils.toString());
        enabled = containerUtils.isEnabled();
    }

}

