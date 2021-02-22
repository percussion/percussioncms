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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.dao.IPSResourceDefinitionGroupDao;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService.PSResourceDefinitionNotFoundException;
import com.percussion.share.dao.PSFileDataRepository;
import com.percussion.share.dao.PSXmlFileDataRepository;
import com.percussion.share.service.exception.PSDataServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang.Validate.notEmpty;

/**
 * 
 * Low level implementation of retrieving resource definitions.
 * 
 * @author adamgent
 *
 */
@Component("resourceDefinitionGroupDao")
public class PSResourceDefinitionGroupDao 
        extends PSXmlFileDataRepository<PSResourceDefinitionData, PSResourceDefinitionGroup> 
        implements IPSResourceDefinitionGroupDao
{

    public PSResourceDefinitionGroupDao()
    {
        super(PSResourceDefinitionGroup.class);
    }

    @Override
    protected PSResourceDefinitionData update(Set<PSFileDataRepository.PSFileEntry> files)
    {
        PSResourceDefinitionData data = new PSResourceDefinitionData();
        
        for (PSFileDataRepository.PSFileEntry fe : files) {
            try
            {
                PSResourceDefinitionGroup group = fileToObject(fe);
                group.setId(fe.getId());
                data.add(group);
            }
            catch (Exception e)
            {
                log.error("Failed to parse resource definition: " + fe.getFileName(), e);
            }
        }
        
        return data;
    }



    public PSResourceDefinitionGroup find(String id) throws PSDataServiceException
    {
        PSResourceDefinitionData data =  getData();
        return data.getResourceDefinitionGroups().get(id);
    }

    public List<PSResourceDefinitionGroup> findAll() throws PSDataServiceException
    {
        return new ArrayList<>(getData().getResourceDefinitionGroups().values());
    }
    
    @Override
    public List<PSResourceDefinition> findAllResources() throws PSDataServiceException {
        return new ArrayList<>(getData().getResourceDefinitions().values());
    }

    public PSResourceDefinition findResource(String uniqueId) throws PSDataServiceException {
        PSResourceDefinitionUniqueId uid = new PSResourceDefinitionUniqueId(uniqueId);
        return getData().getResourceDefinitions().get(uid);
    }

    public PSAssetResource findAssetResourceForType(String contentType) throws PSDataServiceException {
        notEmpty(contentType, "contentType");
        return getData().getPrimaryAssetResources().get(contentType);
    }
    
    @Override
    public List<PSAssetResource> findAssetResourcesForType(String contentType) throws PSDataServiceException {
        notEmpty(contentType, "contentType");
        Set<PSAssetResource> rvalue = getData().getContentTypeAssetResources().get(contentType);
        if(rvalue == null) return emptyList();
        return new ArrayList<>(rvalue);
    }

    @Override
    public List<PSAssetResource> findAssetResourcesForLegacyTemplate(String template) throws PSDataServiceException {
        notEmpty(template, "template");
        Set<PSAssetResource> rvalue = getData().getLegacyTemplateAssetResources().get(template);
        if(rvalue == null) return emptyList();
        return new ArrayList<>(rvalue);
    }

    public void delete(@SuppressWarnings("unused") String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
    {
        throw new UnsupportedOperationException("delete is not yet supported");
    }

    public PSResourceDefinitionGroup save(@SuppressWarnings("unused") PSResourceDefinitionGroup object)
            throws com.percussion.share.dao.IPSGenericDao.SaveException
    {
        throw new UnsupportedOperationException("save is not yet supported");
    }

    @Value("${rxdeploydir}/rxconfig/Resources")
    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        log.info("Setting repository directory to "+widgetsRepositoryDirectory);
        super.setRepositoryDirectory(widgetsRepositoryDirectory);
    }

}
