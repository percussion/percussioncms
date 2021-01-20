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
