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

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.dao.IPSResourceDefinitionGroupDao;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
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
                if(group!=null) {
                    group.setId(fe.getId());
                    data.add(group);
                }else{
                    log.debug("Null group detected.");
                }
            }
            catch (Exception e)
            {
                log.error("Failed to parse resource definition: {} Error: {}" ,
                        fe.getFileName(),
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
        if(rvalue == null) {return emptyList();}
        return new ArrayList<>(rvalue);
    }

    @Override
    public List<PSAssetResource> findAssetResourcesForLegacyTemplate(String template) throws PSDataServiceException {
        notEmpty(template, "template");
        Set<PSAssetResource> rvalue = getData().getLegacyTemplateAssetResources().get(template);
        if(rvalue == null){ return emptyList();}
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
