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

import com.percussion.utils.container.config.model.impl.BaseContainerUtils;

import java.nio.file.Path;
import java.util.function.Supplier;

public class DefaultConfigurationContextImpl extends ConfigurationContextAbstract<DefaultConfigurationContextImpl, BaseContainerUtils> implements ConfigurationCtx {

    private Path rootDir;
    private String encKey;

    public DefaultConfigurationContextImpl(Path path, String key) {
      this(path,key,BaseContainerUtils::new);
    }

    public DefaultConfigurationContextImpl(Path path, String key, Supplier<BaseContainerUtils> ctor) {
        super(ctor);
        this.rootDir=path;
        this.encKey=key;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public String getEncKey() {
        return encKey;
    }



}
