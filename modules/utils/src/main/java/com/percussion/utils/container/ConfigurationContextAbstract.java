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

import com.percussion.utils.container.config.ContainerConfig;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class ConfigurationContextAbstract<T extends ConfigurationCtx, U extends ContainerConfig> implements ConfigurationAdaptorComposite<T, U> {

    private final Supplier<U> ctor;

    private U config;

    private List<IPSConfigurationAdapter<T>> adapters = new ArrayList<>();


    public ConfigurationContextAbstract(Supplier<U> ctor) {
        this.ctor = Objects.requireNonNull(ctor);
        config = ctor.get();
    }

    @Override
    public void addConfigurationAdapter(IPSConfigurationAdapter<T> adapter) {
        adapters.add(adapter);
    }

    @Override
    public void load(T ctx) {
        adapters.stream().forEach(c -> c.load(ctx));
    }

    @Override
    public void save(T ctx) {
        adapters.stream().forEach(c -> c.save(ctx));
    }

    @Override
    public U getConfig() {
        return config;
    }

    @Override
    public void load() {
        load((T) this);
    }

    @Override
    public void save() {
        save((T) this);
    }

    public void copyFrom(ConfigurationContextAbstract<T, U> from) {
        try {
            this.config = (U) BeanUtils.cloneBean(from.getConfig());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
