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

import com.percussion.pagemanagement.dao.IPSWidgetDao;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.server.PSServer;
import com.percussion.share.dao.PSFileDataRepository;
import com.percussion.share.dao.PSXmlFileDataRepository;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

@Component("widgetDao")
@Lazy
public class PSWidgetDao extends PSXmlFileDataRepository<PSWidgetDao.PSWidgetDefinitionData, PSWidgetDefinition> implements IPSWidgetDao
{
    public static class PSWidgetDefinitionData {
        protected Map<String, PSWidgetDefinition> widgetDefinitionsMap = new HashMap<>();

        protected void add(PSWidgetDefinition def) {
            notNull(def);
            notEmpty(def.getId());
            widgetDefinitionsMap.put(def.getId(), def);
        }
    }
    
    public PSWidgetDao()
    {
        super(PSWidgetDefinition.class);
    }

    @Override
    protected synchronized PSWidgetDefinitionData update(Set<PSFileDataRepository.PSFileEntry> files) throws IOException
    {
        notNull(files, "files");
        PSWidgetDefinitionData data = new PSWidgetDefinitionData();
        for (PSFileDataRepository.PSFileEntry fe : files) {
            try
            {
                PSWidgetDefinition wd = fileToObject(fe);
                wd.setId(fe.getId());
                data.add(wd);
            }
            catch (Exception e)
            {
                log.error("Failed to parse widget definition: " + fe.getFileName(), e);
            }
        }
        
        return data;
        
    }


    public PSWidgetDefinition find(String id) throws PSDataServiceException {
        return getData().widgetDefinitionsMap.get(id);
    }

    public List<PSWidgetDefinition> findAll() throws PSDataServiceException {
        return new ArrayList<>(getData().widgetDefinitionsMap.values());
    }


    
    public PSWidgetDefinition save(PSWidgetDefinition object)
            throws com.percussion.share.dao.IPSGenericDao.SaveException
    {
        throw new UnsupportedOperationException("save is not yet supported");
    }
    
    
    public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
    {
        throw new UnsupportedOperationException("delete is not yet supported");
    }

    @Override
    public String getBaseConfigDir()
    {
        String fullPath = getRepositoryDirectory().replace('\\', '/');
        String rxDir = PSServer.getRxDir().getPath().replace('\\', '/');
        String path = StringUtils.removeStart(fullPath, rxDir);
        path = StringUtils.removeStart(path, "/");
        return path;
    }

    @Override
    @Value("${rxdeploydir}/rxconfig/Widgets")
    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        // TODO Auto-generated method stub
        super.setRepositoryDirectory(widgetsRepositoryDirectory);
    }
    

}
