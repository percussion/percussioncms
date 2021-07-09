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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
