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

package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.data.PSCategoryConfiguration;
import com.percussion.share.dao.PSFileDataRepository;
import com.percussion.share.dao.PSXmlFileDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component("categoryConfigurationDao")
@Lazy
public class PSCategoryConfigurationDao extends PSXmlFileDataRepository<PSCategoryConfiguration, PSCategoryConfiguration>
{
    public PSCategoryConfigurationDao()
    {
        super(PSCategoryConfiguration.class);
    }

    @Override
    protected PSCategoryConfiguration update(Set<PSFileDataRepository.PSFileEntry> files)
            throws IOException, PSXmlFileDataRepositoryException {
        if (files.isEmpty()) {
            return new PSCategoryConfiguration();
        }
        PSFileEntry fe = files.iterator().next();
        return fileToObject(fe);
    }

    @Override
    @Value("${rxdeploydir}/rxconfig/Categories")
    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        // TODO Auto-generated method stub
        super.setRepositoryDirectory(widgetsRepositoryDirectory);
    }
    
    

}
